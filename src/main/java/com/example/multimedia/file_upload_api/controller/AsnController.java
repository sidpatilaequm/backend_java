package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.AsnRequestDto;
import com.example.multimedia.file_upload_api.dto.ExcelColumnMappingDto;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.Asn;
import com.example.multimedia.file_upload_api.entity.AsnItem;
import com.example.multimedia.file_upload_api.enums.ExcelReportType;
import com.example.multimedia.file_upload_api.repository.AsnRepository;
import com.example.multimedia.file_upload_api.service.AsnService;
import com.example.multimedia.file_upload_api.service.ExcelImportMappingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.Column;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vendor/asns")
public class AsnController {

    @Autowired
    private AsnService asnService;

    @Autowired
    private AsnRepository asnRepository;

    @Autowired
    private ExcelImportMappingService excelImportMappingService;

    @Autowired
    private com.example.multimedia.file_upload_api.util.SecurityContextUtils securityContextUtils;

    @PostMapping(value = "", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ServiceResponse> createAsn(
            @RequestPart("asnData") String asnDataJson,
            HttpServletRequest request) {
        
        try {
            // Extract the user ID from headers (e.g. injected by API Gateway or Spring Security filter)
            String userIdStr = request.getHeader("X-User-Id");
            if (userIdStr == null) {
                // Try from another standard header or parameter if needed, returning error for now
                ServiceResponse response = new ServiceResponse();
                response.setStatus("ERROR");
                response.setStatusMsg("Unauthorized: X-User-Id missing");
                return ResponseEntity.status(401).body(response);
            }
            Long userId = Long.parseLong(userIdStr);

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            AsnRequestDto asnRequestDto = mapper.readValue(asnDataJson, AsnRequestDto.class);

            // Get all files from MultipartHttpServletRequest
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();

            ServiceResponse serviceResponse = asnService.createAsn(asnRequestDto, fileMap, userId);

            if ("SUCCESS".equalsIgnoreCase(serviceResponse.getStatus()) || "200".equals(serviceResponse.getStatus())) {
                return ResponseEntity.ok(serviceResponse);
            } else {
                return ResponseEntity.badRequest().body(serviceResponse);
            }
        } catch (Exception e) {
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setStatusMsg("Error creating ASN: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("")
    public ResponseEntity<ServiceResponse> getAsns(
            @RequestParam(value = "vendorBpno", required = false) String vendorBpno,
            @RequestParam(value = "company_code", required = false) String companyCode) {
        try {
            ServiceResponse serviceResponse;
            if (vendorBpno != null && !vendorBpno.trim().isEmpty()) {
                serviceResponse = asnService.getAsnsByVendorBpno(vendorBpno, companyCode);
            } else {
                Long vendorId = null;
                try {
                    if (securityContextUtils.isCurrentUserVendor()) {
                        vendorId = securityContextUtils.getCurrentVendorId();
                    }
                } catch (Exception e) {
                    // Ignore if not authenticated as vendor
                }
                
                if (vendorId != null) {
                    serviceResponse = asnService.getAsnsByVendorId(vendorId, companyCode);
                } else {
                    serviceResponse = asnService.getAllAsns(companyCode);
                }
            }

            if ("SUCCESS".equalsIgnoreCase(serviceResponse.getStatus()) || "200".equals(serviceResponse.getStatus())) {
                return ResponseEntity.ok(serviceResponse);
            } else {
                return ResponseEntity.badRequest().body(serviceResponse);
            }
        } catch (Exception e) {
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setStatusMsg("Error fetching ASNs: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/{asnNumber}")
    public ResponseEntity<ServiceResponse> getAsnById(@PathVariable String asnNumber) {
        try {
            ServiceResponse serviceResponse = asnService.getAsnById(asnNumber);
            if ("SUCCESS".equalsIgnoreCase(serviceResponse.getStatus()) || "200".equals(serviceResponse.getStatus())) {
                return ResponseEntity.ok(serviceResponse);
            } else {
                return ResponseEntity.status(404).body(serviceResponse);
            }
        } catch (Exception e) {
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setStatusMsg("Error fetching ASN details: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Downloads the visible ASNs as a real excel, using the admin-configured mapping
     * (ExcelReportType.ASN, defined in the Excel Column Mappings admin screen). Same
     * vendor-vs-admin visibility rules as getAsns() above -- this is deliberately NOT under
     * /api/admin/excel-mappings, since whoever can already see this data should be able to
     * export it, not just admins. One row per (Asn x AsnItem) pair, header fields repeated;
     * an ASN with no items still gets one row with blank item columns.
     */
    @GetMapping("/export.xlsx")
    @Transactional(readOnly = true)
    public ResponseEntity<?> exportAsns(
            @RequestParam(value = "vendorBpno", required = false) String vendorBpno,
            @RequestParam(value = "company_code", required = false) String companyCode) {
        try {
            List<Asn> asns;
            if (vendorBpno != null && !vendorBpno.trim().isEmpty()) {
                asns = asnRepository.findByVendorBpno(vendorBpno, companyCode);
            } else {
                Long vendorId = null;
                try {
                    if (securityContextUtils.isCurrentUserVendor()) {
                        vendorId = securityContextUtils.getCurrentVendorId();
                    }
                } catch (Exception e) {
                    // Ignore if not authenticated as vendor
                }
                if (vendorId != null) {
                    asns = asnRepository.findByVendorId(vendorId, companyCode);
                } else {
                    asns = asnRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
                }
            }

            List<ExcelColumnMappingDto> mapping = excelImportMappingService.get(ExcelReportType.ASN).getMapping();
            if (mapping == null || mapping.isEmpty()) {
                ServiceResponse response = new ServiceResponse();
                response.setStatus("ERROR");
                response.setStatusMsg("No export mapping configured for ASN yet -- set one up in Excel Column Mappings first.");
                return ResponseEntity.badRequest().body(response);
            }

            byte[] file = buildAsnWorkbook(asns, mapping);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"asn-export.xlsx\"")
                    .body(file);
        } catch (Exception e) {
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setStatusMsg("Error generating ASN export: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    private byte[] buildAsnWorkbook(List<Asn> asns, List<ExcelColumnMappingDto> mapping) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("ASN");
            CellStyle headerStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            headerStyle.setFont(boldFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < mapping.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(mapping.get(i).getExcelColumn());
                cell.setCellStyle(headerStyle);
            }

            int r = 1;
            for (Asn asn : asns) {
                List<AsnItem> items = asn.getItems();
                if (items == null || items.isEmpty()) {
                    r = writeAsnRow(sheet, r, mapping, asn, null);
                } else {
                    for (AsnItem item : items) {
                        r = writeAsnRow(sheet, r, mapping, asn, item);
                    }
                }
            }

            for (int i = 0; i < mapping.size(); i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private int writeAsnRow(Sheet sheet, int rowIndex, List<ExcelColumnMappingDto> mapping, Asn asn, AsnItem item) {
        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < mapping.size(); i++) {
            String dbColumn = mapping.get(i).getDbColumn();
            Object value = resolveColumnValue(dbColumn, asn, item);
            ExcelImportMappingService.writeCell(row.createCell(i), value);
        }
        return rowIndex + 1;
    }

    /** Resolves a saved mapping's dbColumn (e.g. "vendor_bpno") to a live value -- checks Asn's
     *  @Column-annotated fields first, then AsnItem's (null item means blank for item columns). */
    private Object resolveColumnValue(String dbColumn, Asn asn, AsnItem item) {
        Object value = getFieldValueByColumn(Asn.class, asn, dbColumn);
        if (value != null) return value;
        if (item != null) return getFieldValueByColumn(AsnItem.class, item, dbColumn);
        return null;
    }

    private Object getFieldValueByColumn(Class<?> entityClass, Object instance, String dbColumn) {
        if (instance == null) return null;
        for (Field field : entityClass.getDeclaredFields()) {
            Column columnAnn = field.getAnnotation(Column.class);
            String columnName = (columnAnn != null && !columnAnn.name().isEmpty()) ? columnAnn.name() : camelToSnake(field.getName());
            if (!columnName.equals(dbColumn)) continue;
            try {
                String getterName = "get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                Method getter = entityClass.getMethod(getterName);
                return getter.invoke(instance);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private String camelToSnake(String camel) {
        StringBuilder sb = new StringBuilder();
        for (char c : camel.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append('_').append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
