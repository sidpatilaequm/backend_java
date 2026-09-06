package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.entity.MasterPurchaseOrder;
import com.example.multimedia.file_upload_api.entity.PortalPurchaseOrder;
import com.example.multimedia.file_upload_api.entity.PortalPurchaseOrderItem;
import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import com.example.multimedia.file_upload_api.repository.MasterPurchaseOrderRepository;
import com.example.multimedia.file_upload_api.repository.PortalPurchaseOrderRepository;
import com.example.multimedia.file_upload_api.repository.CompanyDetailsRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

@Service
public class MasterPurchaseOrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(MasterPurchaseOrderService.class);
    private static final Logger log = LoggerFactory.getLogger(MasterPurchaseOrderService.class);

    private final CompanyDetailsRepository companyRepo;
    private final MasterPurchaseOrderRepository masterPoRepo;
    private final PortalPurchaseOrderRepository portalPoRepo;
    private final com.example.multimedia.file_upload_api.repository.VendorMasterRepository vendorMasterRepo;

    @Autowired
    public MasterPurchaseOrderService(CompanyDetailsRepository companyRepo, MasterPurchaseOrderRepository masterPoRepo, PortalPurchaseOrderRepository portalPoRepo, com.example.multimedia.file_upload_api.repository.VendorMasterRepository vendorMasterRepo) {
        this.companyRepo = companyRepo;
        this.masterPoRepo = masterPoRepo;
        this.portalPoRepo = portalPoRepo;
        this.vendorMasterRepo = vendorMasterRepo;
    }

    @Transactional
    public void saveExcelData(MultipartFile file, Long userId) throws Exception {
        byte[] fileBytes = file.getBytes();
        saveExcelData(fileBytes, userId);
    }

    @Transactional
    public List<PortalPurchaseOrder> saveExcelData(byte[] fileBytes, Long userId) throws Exception {
        String fileContentStr = new String(fileBytes, java.nio.charset.StandardCharsets.UTF_8);
        logger.info("File contents preview: " + fileContentStr.substring(0, Math.min(fileContentStr.length(), 500)));
        if (fileContentStr.trim().startsWith("<?xml")) {
            return parseXmlData(fileBytes, userId);
        }

        Map<String, PortalPurchaseOrder> portalPoMap = new HashMap<>();
        List<MasterPurchaseOrder> masterPos = new ArrayList<>();
        Map<String, MasterPurchaseOrderHeaderDto> headerDtoMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (InputStream is = new java.io.ByteArrayInputStream(fileBytes); Workbook workbook = new XSSFWorkbook(is)) {
            // ============================================
            // PARSE SINGLE SHEET (Headers + Items)
            // ============================================
            Sheet sheet = workbook.getSheetAt(0);
            boolean isFirstRow = true;
            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }
                
                String poNumber = getCellValueAsString(row.getCell(0)); // Purchasing Document
                if (poNumber == null || poNumber.trim().isEmpty()) continue;
                
                String itemNoStr = getCellValueAsString(row.getCell(1));
                String coCode = getCellValueAsString(row.getCell(2));
                String plant = getCellValueAsString(row.getCell(3));
                String sloc = getCellValueAsString(row.getCell(4));
                String poType = getCellValueAsString(row.getCell(5));
                String dateStr = getCellValueAsString(row.getCell(6));
                String purchOrg = getCellValueAsString(row.getCell(7));
                String purchGrp = getCellValueAsString(row.getCell(8));
                String currency = getCellValueAsString(row.getCell(9));
                String refQuot = getCellValueAsString(row.getCell(10));
                String vendorNo = getCellValueAsString(row.getCell(11));
                String material = getCellValueAsString(row.getCell(12));
                String desc = getCellValueAsString(row.getCell(13));
                String qtyStr = getCellValueAsString(row.getCell(14));
                String uom = getCellValueAsString(row.getCell(15));
                String priceStr = getCellValueAsString(row.getCell(16));
                String valueStr = getCellValueAsString(row.getCell(17));
                String currency2 = getCellValueAsString(row.getCell(18));
                String inco1 = getCellValueAsString(row.getCell(19));
                String inco2 = getCellValueAsString(row.getCell(20));

                String companyName = resolveCompanyName(coCode);
                String vendorName = resolveCompanyName(vendorNo); // Find vendor using Company Code logic

                PortalPurchaseOrder po = portalPoMap.getOrDefault(poNumber, portalPoRepo.findByPoNumber(poNumber).orElse(new PortalPurchaseOrder()));
                if (po.getPoNumber() == null) {
                    po.setPoNumber(poNumber);
                    po.setCompanyCode(coCode);
                    po.setPurchasingDocType(poType);
                    
                    try {
                        po.setPoDate(LocalDate.parse(dateStr, formatter));
                    } catch (Exception e) {
                        po.setPoDate(LocalDate.now());
                    }
                    
                    if (vendorNo != null && !vendorNo.isEmpty()) {
                        vendorMasterRepo.findByBpNo(vendorNo.trim()).ifPresentOrElse(
                            vendorMaster -> {
                                companyRepo.findById(vendorMaster.getVendorId()).ifPresent(po::setVendor);
                            },
                            () -> {
                                List<CompanyDetails> vendors = companyRepo.findByCompanyCode(vendorNo.trim());
                                if (!vendors.isEmpty()) po.setVendor(vendors.get(0));
                                else {
                                     try {
                                         Long vId = Long.parseLong(vendorNo.trim());
                                         companyRepo.findById(vId).ifPresent(po::setVendor);
                                     } catch (NumberFormatException ignored) {}
                                }
                            }
                        );
                    }
                    
                    po.setLanguageKey("EN");
                    po.setPurchasingOrganization(purchOrg);
                    po.setPurchasingGroup(purchGrp);
                    po.setCurrency(currency);
                    po.setIncoterms(inco1);
                    po.setIncotermsPart2(inco2);
                    
                    po.setGrandTotal(BigDecimal.valueOf(parseDouble(valueStr)));
                    po.setStatus("APPROVED"); // Default status
                    if (userId != null) {
                        po.setCreatedBy(String.valueOf(userId));
                    }
                    
                    if (po.getItems() == null) po.setItems(new ArrayList<>());
                    else po.getItems().clear(); 
                } else {
                    po.setGrandTotal(po.getGrandTotal().add(BigDecimal.valueOf(parseDouble(valueStr))));
                }
                
                PortalPurchaseOrderItem poItem = new PortalPurchaseOrderItem();
                poItem.setPurchaseOrder(po);
                try { poItem.setLineNumber(Integer.parseInt(itemNoStr)); } catch (Exception e) {}
                poItem.setDocumentItem(itemNoStr);
                poItem.setMaterialDescription(desc);
                poItem.setMaterialNumber(material);
                poItem.setCompanyCode(coCode);
                poItem.setPlant(plant);
                poItem.setStorageLocation(sloc);
                poItem.setQuantity(BigDecimal.valueOf(parseDouble(qtyStr)));
                poItem.setUom(uom);
                poItem.setUnitPrice(BigDecimal.valueOf(parseDouble(priceStr)));
                poItem.setPriceUnit(1);
                poItem.setNetValue(BigDecimal.valueOf(parseDouble(valueStr)));
                poItem.setTotalValue(BigDecimal.valueOf(parseDouble(valueStr)));

                poItem.setIgstPercent(BigDecimal.ZERO);
                poItem.setSgstPercent(BigDecimal.ZERO);
                poItem.setCgstPercent(BigDecimal.ZERO);
                poItem.setIgstAmount(BigDecimal.ZERO);
                poItem.setSgstAmount(BigDecimal.ZERO);
                poItem.setCgstAmount(BigDecimal.ZERO);
                poItem.setTaxAmount(BigDecimal.ZERO);
                poItem.setShippedQuantity(BigDecimal.ZERO);

                po.getItems().add(poItem);
                portalPoMap.put(poNumber, po);
                
                // Populate MasterPurchaseOrder (Raw records)
                MasterPurchaseOrder masterPo = new MasterPurchaseOrder();
                masterPo.setDocNo(poNumber);
                masterPo.setCoCode(coCode);
                masterPo.setDocType(poType);
                masterPo.setVendor(vendorNo);
                masterPo.setVendorName(vendorName); // Fetched dynamically
                masterPo.setItem(itemNoStr);
                masterPo.setMaterialNumber(material);
                masterPo.setShortText(desc);
                masterPo.setQuantity(parseDouble(qtyStr));
                masterPo.setOrderUnit(uom);
                masterPo.setDocumentItem(itemNoStr);
                masterPo.setPlant(plant);
                masterPo.setStorageLocation(sloc);
                masterPo.setNetOrderPrice(parseDouble(priceStr));
                masterPo.setPriceUnit(1);
                masterPo.setNetOrderValue(parseDouble(valueStr));
                masterPo.setGrossOrderValue(parseDouble(valueStr));
                masterPo.setUserId(0L);
                masterPo.setAdminId(0L);
                
                masterPos.add(masterPo);
            }

            // Save all Portal Purchase Orders (cascades items)
            portalPoRepo.saveAll(portalPoMap.values());
            
            // Save all Master Purchase Orders
            masterPoRepo.saveAll(masterPos);
            
            return new ArrayList<>(portalPoMap.values());
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                double numericValue = cell.getNumericCellValue();
                if (numericValue == (long) numericValue) {
                    return String.format("%d", (long) numericValue);
                } else {
                    return String.format("%s", numericValue);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    private Double parseDouble(String val) {
        if (val == null || val.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(val.replace(",", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public List<MasterPurchaseOrder> getOrdersByUserId(Long userId) {
        return masterPoRepo.findByUserId(userId);
    }
    
    // Internal DTO to hold header data for MasterPurchaseOrder building
    private static class MasterPurchaseOrderHeaderDto {
        String docNo;
        String coCode;
        String docType;
        String vendor;
        String vendorName;
    }

    private String resolveCompanyName(String companyCode) {
        if (companyCode == null || companyCode.trim().isEmpty()) return "";
        List<CompanyDetails> companies = companyRepo.findByCompanyCode(companyCode);
        if (!companies.isEmpty()) {
            return companies.get(0).getCompanyName();
        }
        return companyCode;
    }

    private List<PortalPurchaseOrder> parseXmlData(byte[] xmlBytes, Long userId) throws Exception {
        Map<String, PortalPurchaseOrder> portalPoMap = new HashMap<>();
        List<MasterPurchaseOrder> masterPos = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new java.io.ByteArrayInputStream(xmlBytes));
        
        NodeList rows = doc.getElementsByTagNameNS("*", "Row");
        boolean isFirstRow = true;
        for (int i = 0; i < rows.getLength(); i++) {
            Node rowNode = rows.item(i);
            if (rowNode.getNodeType() == Node.ELEMENT_NODE) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }
                Element rowEl = (Element) rowNode;
                NodeList cellNodes = rowEl.getElementsByTagNameNS("*", "Cell");
                
                String[] cellValues = new String[30]; // At least 21
                for(int j=0; j<cellValues.length; j++) cellValues[j] = "";
                
                int currentIdx = 1;
                for (int j = 0; j < cellNodes.getLength(); j++) {
                    Element cellEl = (Element) cellNodes.item(j);
                    if (cellEl.hasAttributeNS("urn:schemas-microsoft-com:office:spreadsheet", "Index")) {
                        try { currentIdx = Integer.parseInt(cellEl.getAttributeNS("urn:schemas-microsoft-com:office:spreadsheet", "Index")); } catch (Exception ignored) {}
                    } else if (cellEl.hasAttribute("ss:Index")) {
                        try { currentIdx = Integer.parseInt(cellEl.getAttribute("ss:Index")); } catch (Exception ignored) {}
                    }
                    
                    if (currentIdx <= cellValues.length) {
                        NodeList dataNodes = cellEl.getElementsByTagNameNS("*", "Data");
                        if (dataNodes.getLength() > 0) {
                            cellValues[currentIdx - 1] = getXmlNodeText(dataNodes.item(0));
                        }
                    }
                    currentIdx++;
                }
                
                String poNumber = cellValues[0];
                if (poNumber == null || poNumber.trim().isEmpty()) continue;
                
                String itemNoStr = cellValues[1];
                String coCode = cellValues[2];
                String plant = cellValues[3];
                String sloc = cellValues[4];
                String poType = cellValues[5];
                String dateStr = cellValues[6];
                String purchOrg = cellValues[7];
                String purchGrp = cellValues[8];
                String currency = cellValues[9];
                String refQuot = cellValues[10];
                String vendorNo = cellValues[11];
                String material = cellValues[12];
                String desc = cellValues[13];
                String qtyStr = cellValues[14];
                String uom = cellValues[15];
                String priceStr = cellValues[16];
                String valueStr = cellValues[17];
                String currency2 = cellValues[18];
                String inco1 = cellValues[19];
                String inco2 = cellValues[20];
                
                String companyName = resolveCompanyName(coCode);
                String vendorName = resolveCompanyName(vendorNo); // Using CompanyCode repository for Vendors as well since they share the same table in this system.
                
                PortalPurchaseOrder po = portalPoMap.getOrDefault(poNumber, portalPoRepo.findByPoNumber(poNumber).orElse(new PortalPurchaseOrder()));
                if (po.getPoNumber() == null) {
                    po.setPoNumber(poNumber);
                    po.setCompanyCode(coCode);
                    po.setPurchasingDocType(poType);
                    try {
                        po.setPoDate(LocalDate.parse(dateStr, formatter));
                    } catch (Exception e) {
                        po.setPoDate(LocalDate.now());
                    }
                    
                    if (vendorNo != null && !vendorNo.isEmpty()) {
                        vendorMasterRepo.findByBpNo(vendorNo.trim()).ifPresentOrElse(
                            vendorMaster -> {
                                companyRepo.findById(vendorMaster.getVendorId()).ifPresent(po::setVendor);
                            },
                            () -> {
                                List<CompanyDetails> vendors = companyRepo.findByCompanyCode(vendorNo.trim());
                                if (!vendors.isEmpty()) po.setVendor(vendors.get(0));
                            }
                        );
                    }
                    po.setLanguageKey("EN");
                    po.setPurchasingOrganization(purchOrg);
                    po.setPurchasingGroup(purchGrp);
                    po.setCurrency(currency);
                    po.setIncoterms(inco1);
                    po.setIncotermsPart2(inco2);
                    po.setGrandTotal(BigDecimal.valueOf(parseDouble(valueStr)));
                    po.setStatus("APPROVED");
                    if (userId != null) {
                        po.setCreatedBy(String.valueOf(userId));
                    }
                    if (po.getItems() == null) po.setItems(new ArrayList<>());
                    else po.getItems().clear();
                } else {
                    po.setGrandTotal(po.getGrandTotal().add(BigDecimal.valueOf(parseDouble(valueStr))));
                }
                
                PortalPurchaseOrderItem poItem = new PortalPurchaseOrderItem();
                poItem.setPurchaseOrder(po);
                try { poItem.setLineNumber(Integer.parseInt(itemNoStr)); } catch (Exception e) {}
                poItem.setDocumentItem(itemNoStr);
                poItem.setMaterialDescription(desc);
                poItem.setMaterialNumber(material);
                poItem.setCompanyCode(coCode);
                poItem.setPlant(plant);
                poItem.setStorageLocation(sloc);
                poItem.setQuantity(BigDecimal.valueOf(parseDouble(qtyStr)));
                poItem.setUom(uom);
                poItem.setUnitPrice(BigDecimal.valueOf(parseDouble(priceStr)));
                poItem.setPriceUnit(1);
                poItem.setNetValue(BigDecimal.valueOf(parseDouble(valueStr)));
                poItem.setTotalValue(BigDecimal.valueOf(parseDouble(valueStr)));
                
                poItem.setIgstPercent(BigDecimal.ZERO);
                poItem.setSgstPercent(BigDecimal.ZERO);
                poItem.setCgstPercent(BigDecimal.ZERO);
                poItem.setIgstAmount(BigDecimal.ZERO);
                poItem.setSgstAmount(BigDecimal.ZERO);
                poItem.setCgstAmount(BigDecimal.ZERO);
                poItem.setTaxAmount(BigDecimal.ZERO);
                poItem.setShippedQuantity(BigDecimal.ZERO);
                
                po.getItems().add(poItem);
                portalPoMap.put(poNumber, po);
                
                MasterPurchaseOrder masterPo = new MasterPurchaseOrder();
                masterPo.setDocNo(poNumber);
                masterPo.setCoCode(coCode);
                masterPo.setDocType(poType);
                masterPo.setVendor(vendorNo);
                masterPo.setVendorName(vendorName); // Fetched dynamically
                masterPo.setItem(itemNoStr);
                masterPo.setMaterialNumber(material);
                masterPo.setShortText(desc);
                masterPo.setQuantity(parseDouble(qtyStr));
                masterPo.setOrderUnit(uom);
                masterPo.setDocumentItem(itemNoStr);
                masterPo.setPlant(plant);
                masterPo.setStorageLocation(sloc);
                masterPo.setNetOrderPrice(parseDouble(priceStr));
                masterPo.setPriceUnit(1);
                masterPo.setNetOrderValue(parseDouble(valueStr));
                masterPo.setGrossOrderValue(parseDouble(valueStr));
                masterPo.setUserId(0L);
                masterPo.setAdminId(0L);
                masterPos.add(masterPo);
            }
        }
        
        portalPoRepo.saveAll(portalPoMap.values());
        masterPoRepo.saveAll(masterPos);
        
        return new ArrayList<>(portalPoMap.values());
    }
    
    private String getXmlNodeText(Node node) {
        if (node != null && node.getTextContent() != null) {
            return node.getTextContent().trim();
        }
        return "";
    }
}
