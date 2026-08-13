package com.example.multimedia.file_upload_api.service.impl;

import com.example.multimedia.file_upload_api.dto.AsnItemRequestDto;
import com.example.multimedia.file_upload_api.dto.AsnRequestDto;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.*;
import com.example.multimedia.file_upload_api.repository.*;
import com.example.multimedia.file_upload_api.service.AsnService;
import com.example.multimedia.file_upload_api.service.FileUploadService;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AsnServiceImpl implements AsnService {

    @Autowired
    private PortalPurchaseOrderRepository portalPurchaseOrderRepository;

    @Autowired
    private AsnRepository asnRepository;

    @Autowired
    private AsnItemRepository asnItemRepository;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private ServiceControllerUtils serviceControllerUtils;

    @Autowired
    private UserDetailRepository userDetailRepository;

    private static final BigDecimal OVER_SHIPMENT_TOLERANCE_PERCENT = new BigDecimal("1.05");

    @Override
    @Transactional
    public ServiceResponse createAsn(AsnRequestDto asnRequestDto, Map<String, MultipartFile> files, Long userId) {
        ServiceResponse response = new ServiceResponse();

        // 1. Mandatory Files check
        if (!files.containsKey("taxInvoiceAttached") || files.get("taxInvoiceAttached").isEmpty()) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(response, "400", "Tax Invoice is mandatory");
        }
        if (!files.containsKey("ewayBillAttached") || files.get("ewayBillAttached").isEmpty()) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(response, "400", "E-Way Bill is mandatory");
        }
        if (!files.containsKey("packingListAttached") || files.get("packingListAttached").isEmpty()) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(response, "400", "Packing List is mandatory");
        }

        // 2. Fetch User & Lock PO
        UserDetail user = userDetailRepository.findById(userId).orElse(null);
        if (user == null) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "User not found");
        }

        Optional<PortalPurchaseOrder> poOpt = portalPurchaseOrderRepository.findByPoNumberAndVendorBpnoForUpdate(asnRequestDto.getPoId(), asnRequestDto.getVendorBpno());
        if (poOpt.isEmpty()) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(response, "404", "Purchase Order not found or unauthorized");
        }

        PortalPurchaseOrder po = poOpt.get();
        CompanyDetails vendor = po.getVendor();

        // 3. Validate items & Tolerances
        boolean needsApproval = false;
        boolean isComplete = true;

        for (AsnItemRequestDto reqItem : asnRequestDto.getItems()) {
            Optional<PortalPurchaseOrderItem> poItemOpt = po.getItems().stream()
                    .filter(i -> i.getLineNumber().equals(reqItem.getLineNumber()))
                    .findFirst();

            if (poItemOpt.isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(response, "400", "Line item not found in PO: " + reqItem.getLineNumber());
            }

            PortalPurchaseOrderItem poItem = poItemOpt.get();
            BigDecimal ordered = poItem.getQuantity();
            
            BigDecimal alreadyShipped = asnItemRepository.getTotalShippedQuantityForPoLine(po.getPoNumber(), poItem.getLineNumber());
            if (alreadyShipped == null) alreadyShipped = BigDecimal.ZERO;
            
            BigDecimal available = ordered.subtract(alreadyShipped);

            BigDecimal shippingNow = reqItem.getQuantityShipped();
            if (shippingNow == null) shippingNow = BigDecimal.ZERO;
            
            BigDecimal newShippedTotal = alreadyShipped.add(shippingNow);

            if (newShippedTotal.compareTo(ordered) > 0) {
                // Check tolerance
                BigDecimal maxAllowed = ordered.multiply(OVER_SHIPMENT_TOLERANCE_PERCENT);
                if (newShippedTotal.compareTo(maxAllowed) > 0) {
                    return serviceControllerUtils.prepareMobileResponseErrorStatus(response, "400", "Tolerance exceeded for line item: " + reqItem.getLineNumber());
                } else {
                    needsApproval = true;
                }
            }

            if (newShippedTotal.compareTo(ordered) < 0) {
                isComplete = false;
            }
        }

        // 4. Create ASN
        Asn asn = new Asn();
        asn.setPurchaseOrder(po);
        asn.setVendorBpno(vendor.getCompanyCode());
        asn.setInvoiceNumber(asnRequestDto.getShipmentDetails().getInvoiceNumber());
        asn.setIrn(asnRequestDto.getShipmentDetails().getIrn());
        asn.setEwayBill(asnRequestDto.getShipmentDetails().getEwayBill());
        asn.setEwbValidTo(asnRequestDto.getShipmentDetails().getEwbValidTo());
        asn.setVehicleNumber(asnRequestDto.getShipmentDetails().getVehicleNumber());
        asn.setTransporterCode(asnRequestDto.getShipmentDetails().getTransporterCode());
        asn.setLrNumber(asnRequestDto.getShipmentDetails().getLrNumber());
        asn.setDispatchDate(asnRequestDto.getShipmentDetails().getDispatchDate());
        asn.setExpectedDelivery(asnRequestDto.getShipmentDetails().getExpectedDelivery());
        asn.setPackaging(asnRequestDto.getShipmentDetails().getPackaging());

        if (needsApproval) {
            asn.setStatus("BUYER_APPROVAL_PENDING");
        } else {
            asn.setStatus("IN_TRANSIT");
        }

        String monthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM-yyyy"));
        
        String vendorName = vendor.getCompanyName();
        if (vendorName == null || vendorName.trim().isEmpty()) {
            vendorName = asnRequestDto.getVendorBpno();
        }

        try {
            if (files.containsKey("taxInvoiceAttached") && !files.get("taxInvoiceAttached").isEmpty()) {
                FileUpload file = fileUploadService.uploadFile(files.get("taxInvoiceAttached"), "ASN - Tax Invoice", userId, vendorName, monthYear);
                asn.setTaxInvoiceUrl(file.getFilePath());
            }
            if (files.containsKey("ewayBillAttached") && !files.get("ewayBillAttached").isEmpty()) {
                FileUpload file = fileUploadService.uploadFile(files.get("ewayBillAttached"), "ASN - E-Way Bill", userId, vendorName, monthYear);
                asn.setEwayBillUrl(file.getFilePath());
            }
            if (files.containsKey("packingListAttached") && !files.get("packingListAttached").isEmpty()) {
                FileUpload file = fileUploadService.uploadFile(files.get("packingListAttached"), "ASN - Packing List", userId, vendorName, monthYear);
                asn.setPackingListUrl(file.getFilePath());
            }
            if (files.containsKey("pdirAttached") && !files.get("pdirAttached").isEmpty()) {
                FileUpload file = fileUploadService.uploadFile(files.get("pdirAttached"), "ASN - PDIR", userId, vendorName, monthYear);
                asn.setPdirUrl(file.getFilePath());
            }
            if (files.containsKey("deviationAttached") && !files.get("deviationAttached").isEmpty()) {
                FileUpload file = fileUploadService.uploadFile(files.get("deviationAttached"), "ASN - Deviation", userId, vendorName, monthYear);
                asn.setDeviationUrl(file.getFilePath());
            }
            if (files.containsKey("othersAttached") && !files.get("othersAttached").isEmpty()) {
                FileUpload file = fileUploadService.uploadFile(files.get("othersAttached"), "ASN - Others", userId, vendorName, monthYear);
                asn.setOthersUrl(file.getFilePath());
            }
        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(response, "500", "File upload failed: " + e.getMessage());
        }

        asnRepository.save(asn);

        // 6. Save items and update PO Item shipped quantities
        for (AsnItemRequestDto reqItem : asnRequestDto.getItems()) {
            PortalPurchaseOrderItem poItem = po.getItems().stream()
                    .filter(i -> i.getLineNumber().equals(reqItem.getLineNumber()))
                    .findFirst().get();

            AsnItem asnItem = new AsnItem();
            asnItem.setAsn(asn);
            asnItem.setPurchaseOrderItem(poItem);
            asnItem.setPartNumber(reqItem.getPartNumber());
            asnItem.setQuantityShipped(reqItem.getQuantityShipped());
            asnItem.setBatchHeatNumber(reqItem.getBatchHeatNumber());

            // Handle test certificate
            try {
                String testCertKey = "testCertAttached_" + reqItem.getLineNumber();
                if (files.containsKey(testCertKey) && !files.get(testCertKey).isEmpty()) {
                    FileUpload file = fileUploadService.uploadFile(files.get(testCertKey), "ASN - Test Certificate", userId, vendorName, monthYear);
                    asnItem.setTestCertUrl(file.getFilePath());
                }
            } catch (Exception e) {
                // Ignore for now
            }

            asnItemRepository.save(asnItem);

            BigDecimal alreadyShipped = poItem.getShippedQuantity() != null ? poItem.getShippedQuantity() : BigDecimal.ZERO;
            poItem.setShippedQuantity(alreadyShipped.add(reqItem.getQuantityShipped()));
        }

        // 7. Update PO Status dynamically based on all items
        updatePurchaseOrderStatus(po.getPoNumber());

        response.addData("asnId", asn.getId());
        return serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "ASN created successfully");
    }

    @Override
    public ServiceResponse getAsnsByVendorBpno(String vendorBpno) {
        ServiceResponse response = new ServiceResponse();
        List<Asn> asns = asnRepository.findByVendorBpno(vendorBpno);
        List<com.example.multimedia.file_upload_api.dto.AsnResponseDto> dtos = asns.stream().map(this::mapToDto).collect(java.util.stream.Collectors.toList());
        response.addData("asns", dtos);
        response.setStatus("SUCCESS");
        response.setStatusMsg("Fetched ASNs successfully");
        return response;
    }

    @Override
    public ServiceResponse getAllAsns() {
        ServiceResponse response = new ServiceResponse();
        List<Asn> asns = asnRepository.findAll();
        List<com.example.multimedia.file_upload_api.dto.AsnResponseDto> dtos = asns.stream().map(this::mapToDto).collect(java.util.stream.Collectors.toList());
        response.addData("asns", dtos);
        response.setStatus("SUCCESS");
        response.setStatusMsg("Fetched all ASNs successfully");
        return response;
    }

    @Override
    public ServiceResponse getAsnsByPoNumber(String poNumber) {
        ServiceResponse response = new ServiceResponse();
        List<Asn> asns = asnRepository.findByPurchaseOrder_PoNumber(poNumber);
        List<com.example.multimedia.file_upload_api.dto.AsnHistoryResponseDto> dtos = asns.stream().map(this::mapToHistoryDto).collect(java.util.stream.Collectors.toList());
        response.addData("asns", dtos);
        response.setStatus("SUCCESS");
        response.setStatusMsg("Fetched ASN history successfully");
        return response;
    }

    private com.example.multimedia.file_upload_api.dto.AsnResponseDto mapToDto(Asn asn) {
        com.example.multimedia.file_upload_api.dto.AsnResponseDto dto = new com.example.multimedia.file_upload_api.dto.AsnResponseDto();
        dto.setId(asn.getId());
        if (asn.getPurchaseOrder() != null) {
            dto.setPoNumber(asn.getPurchaseOrder().getPoNumber());
        }
        if (asn.getVendorBpno() != null) {
            dto.setVendorBpno(asn.getVendorBpno());
        }
        dto.setInvoiceNumber(asn.getInvoiceNumber());
        dto.setIrn(asn.getIrn());
        dto.setEwayBill(asn.getEwayBill());
        dto.setEwbValidTo(asn.getEwbValidTo());
        dto.setVehicleNumber(asn.getVehicleNumber());
        dto.setTransporterCode(asn.getTransporterCode());
        dto.setLrNumber(asn.getLrNumber());
        dto.setDispatchDate(asn.getDispatchDate());
        dto.setExpectedDelivery(asn.getExpectedDelivery());
        dto.setPackaging(asn.getPackaging());
        dto.setStatus(asn.getStatus());
        dto.setTaxInvoiceUrl(asn.getTaxInvoiceUrl());
        dto.setEwayBillUrl(asn.getEwayBillUrl());
        dto.setPackingListUrl(asn.getPackingListUrl());
        dto.setPdirUrl(asn.getPdirUrl());
        dto.setDeviationUrl(asn.getDeviationUrl());
        dto.setOthersUrl(asn.getOthersUrl());
        dto.setCreatedDate(asn.getCreatedDate());
        dto.setModifiedDate(asn.getModifiedDate());
        
        // Calculate if it's partial
        if (asn.getPurchaseOrder() != null && asn.getItems() != null) {
            BigDecimal totalAsnQty = asn.getItems().stream().map(item -> item.getQuantityShipped() != null ? item.getQuantityShipped() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalPoQty = asn.getPurchaseOrder().getItems().stream().map(item -> item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setPartial(totalAsnQty.compareTo(totalPoQty) < 0);
        }

        List<com.example.multimedia.file_upload_api.dto.AsnItemResponseDto> itemDtos = asn.getItems().stream().map(item -> {
            com.example.multimedia.file_upload_api.dto.AsnItemResponseDto itemDto = new com.example.multimedia.file_upload_api.dto.AsnItemResponseDto();
            itemDto.setId(item.getId());
            if (item.getPurchaseOrderItem() != null) {
                itemDto.setLineNumber(item.getPurchaseOrderItem().getLineNumber());
            }
            itemDto.setPartNumber(item.getPartNumber());
            itemDto.setQuantityShipped(item.getQuantityShipped());
            itemDto.setBatchHeatNumber(item.getBatchHeatNumber());
            itemDto.setTestCertUrl(item.getTestCertUrl());
            return itemDto;
        }).collect(java.util.stream.Collectors.toList());

        dto.setItems(itemDtos);
        return dto;
    }

    private void updatePurchaseOrderStatus(String poNumber) {
        PortalPurchaseOrder po = portalPurchaseOrderRepository.findByPoNumber(poNumber)
                .orElseThrow(() -> new RuntimeException("PO not found: " + poNumber));
        boolean isFullyShipped = true;
        boolean hasAnyShipment = false;
        
        for (PortalPurchaseOrderItem item : po.getItems()) {
            BigDecimal received = asnItemRepository.getReceivedQuantity(poNumber, item.getLineNumber());
            if (received == null) received = BigDecimal.ZERO;
            
            BigDecimal inTransit = asnItemRepository.getInTransitQuantity(poNumber, item.getLineNumber());
            if (inTransit == null) inTransit = BigDecimal.ZERO;
            
            BigDecimal totalShipped = received.add(inTransit);
            
            if (totalShipped.compareTo(BigDecimal.ZERO) > 0) {
                hasAnyShipment = true;
            }
            if (totalShipped.compareTo(item.getQuantity()) < 0) {
                isFullyShipped = false;
            }
        }
        
        if (isFullyShipped && hasAnyShipment) {
            po.setStatus("FULLY_SHIPPED");
        } else if (hasAnyShipment) {
            po.setStatus("PARTIAL_DISPATCH");
        }
        portalPurchaseOrderRepository.save(po);
    }

    private com.example.multimedia.file_upload_api.dto.AsnHistoryResponseDto mapToHistoryDto(Asn asn) {
        com.example.multimedia.file_upload_api.dto.AsnHistoryResponseDto dto = new com.example.multimedia.file_upload_api.dto.AsnHistoryResponseDto();
        dto.setAsnId(asn.getId());
        dto.setAsnNumber("ASN-" + asn.getCreatedDate().getYear() + "-" + String.format("%05d", asn.getId()));
        dto.setDispatchDate(asn.getDispatchDate());
        dto.setStatus(asn.getStatus());
        dto.setEffect(null); // Not tracked yet
        dto.setGrnNumber(null); // Not tracked yet
        dto.setInvoiceNumber(asn.getInvoiceNumber());
        dto.setEwayBill(asn.getEwayBill());
        dto.setVehicleNumber(asn.getVehicleNumber());
        
        if ("IN_TRANSIT".equalsIgnoreCase(asn.getStatus()) && asn.getExpectedDelivery() != null) {
            dto.setEta(asn.getExpectedDelivery().toString());
        } else {
            dto.setEta(null);
        }
        dto.setRemarks(null); // Not tracked yet
        
        List<com.example.multimedia.file_upload_api.dto.AsnHistoryResponseDto.DocumentDto> docs = new java.util.ArrayList<>();
        if (asn.getTaxInvoiceUrl() != null) {
            com.example.multimedia.file_upload_api.dto.AsnHistoryResponseDto.DocumentDto doc = new com.example.multimedia.file_upload_api.dto.AsnHistoryResponseDto.DocumentDto();
            doc.setName("Tax Invoice"); doc.setUrl(asn.getTaxInvoiceUrl()); docs.add(doc);
        }
        if (asn.getEwayBillUrl() != null) {
            com.example.multimedia.file_upload_api.dto.AsnHistoryResponseDto.DocumentDto doc = new com.example.multimedia.file_upload_api.dto.AsnHistoryResponseDto.DocumentDto();
            doc.setName("E-Way Bill"); doc.setUrl(asn.getEwayBillUrl()); docs.add(doc);
        }
        if (asn.getPackingListUrl() != null) {
            com.example.multimedia.file_upload_api.dto.AsnHistoryResponseDto.DocumentDto doc = new com.example.multimedia.file_upload_api.dto.AsnHistoryResponseDto.DocumentDto();
            doc.setName("Packing List"); doc.setUrl(asn.getPackingListUrl()); docs.add(doc);
        }
        dto.setDocuments(docs);
        
        List<com.example.multimedia.file_upload_api.dto.AsnHistoryResponseDto.LineDto> lines = asn.getItems().stream().map(item -> {
            com.example.multimedia.file_upload_api.dto.AsnHistoryResponseDto.LineDto lineDto = new com.example.multimedia.file_upload_api.dto.AsnHistoryResponseDto.LineDto();
            if (item.getPurchaseOrderItem() != null) {
                lineDto.setLineNumber(item.getPurchaseOrderItem().getLineNumber());
            }
            lineDto.setPartNumber(item.getPartNumber());
            lineDto.setQuantity(item.getQuantityShipped());
            lineDto.setGrnQuantity(null); // Not tracked yet
            return lineDto;
        }).collect(java.util.stream.Collectors.toList());
        dto.setLines(lines);
        
        return dto;
    }
}
