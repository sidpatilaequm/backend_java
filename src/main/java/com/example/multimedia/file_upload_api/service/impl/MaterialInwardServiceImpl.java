package com.example.multimedia.file_upload_api.service.impl;

import com.example.multimedia.file_upload_api.dto.materialinward.*;
import com.example.multimedia.file_upload_api.entity.*;
import com.example.multimedia.file_upload_api.repository.*;
import com.example.multimedia.file_upload_api.service.MaterialInwardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MaterialInwardServiceImpl implements MaterialInwardService {

    @Autowired
    private GateEntryRepository gateEntryRepository;

    @Autowired
    private GoodsReceiptRepository goodsReceiptRepository;

    @Autowired
    private com.example.multimedia.file_upload_api.service.GoodsReceiptFolderitSyncService goodsReceiptFolderitSyncService;

    @Autowired
    private AsnRepository asnRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private com.example.multimedia.file_upload_api.security.OrgConfigGate orgConfigGate;

    @Override
    public List<MaterialInwardQueueDto> getQueue() {
        // Fetch gate entries that are ALLOWED
        List<GateEntry> allowedEntries = gateEntryRepository.findByDecision("ALLOW");
        
        return allowedEntries.stream()
                .map(ge -> {
                    MaterialInwardQueueDto dto = new MaterialInwardQueueDto();
                    dto.setGateEntryId(ge.getId());
                    dto.setGateEntryNo(ge.getGatePassNumber());
                    dto.setVehicleNo(ge.getAsn() != null ? ge.getAsn().getVehicleNumber() : "N/A");
                    dto.setGateInTime(ge.getInTime());
                    
                    if (ge.getAsn() != null) {
                        dto.setVendorName(ge.getAsn().getPurchaseOrder() != null && ge.getAsn().getPurchaseOrder().getVendor() != null ? ge.getAsn().getPurchaseOrder().getVendor().getCompanyName() : "Unknown");
                        dto.setVendorCode(ge.getAsn().getVendorBpno());
                        dto.setPoReference(ge.getAsn().getPurchaseOrder() != null ? ge.getAsn().getPurchaseOrder().getPoNumber() : "N/A");
                        dto.setPackingSlipNo(ge.getAsn().getInvoiceNumber());
                        dto.setNoOfBoxes(ge.getAsn().getNoOfPackages());
                    }
                    
                    boolean isCompleted = goodsReceiptRepository.existsByGateEntryId(ge.getId());
                    dto.setStatus(isCompleted ? "Completed" : "Pending");
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public MaterialInwardDetailDto getDetails(Long gateEntryId) {
        GateEntry ge = gateEntryRepository.findById(gateEntryId)
                .orElseThrow(() -> new RuntimeException("GateEntry not found"));

        MaterialInwardDetailDto dto = new MaterialInwardDetailDto();
        dto.setGateEntryId(ge.getId());
        dto.setGateEntryNo(ge.getGatePassNumber());
        dto.setGateIn(ge.getInTime() != null ? ge.getInTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")) : "N/A");
        
        Asn asn = ge.getAsn();
        if (asn != null) {
            dto.setVehicleNo(asn.getVehicleNumber());
            dto.setPackingSlipNo(asn.getInvoiceNumber());
            dto.setPackingSlipDate(asn.getInvoiceDate() != null ? asn.getInvoiceDate().toString() : "N/A");
            dto.setInvoiceNo(asn.getInvoiceNumber());
            dto.setInvoiceDate(asn.getInvoiceDate() != null ? asn.getInvoiceDate().toString() : "N/A");
            
            PortalPurchaseOrder po = asn.getPurchaseOrder();
            if (po != null) {
                dto.setVendorName(po.getVendor() != null ? po.getVendor().getCompanyName() : "Unknown");
                dto.setVendorCode(asn.getVendorBpno());
                dto.setPoReference(po.getPoNumber());
                dto.setPoDate(po.getPoDate() != null ? po.getPoDate().toString() : "N/A");
                dto.setDestination("WH1 — Main Warehouse");

                // The founder's rule: only a doc type ending in "RM" (today just ZFRM) needs a
                // real warehouse + bin at putaway — everything else is location-only. Resolved
                // fresh from the PR each time rather than copied forward onto the ASN/PO.
                // Org-wide master switch overrides this: when off, every receipt is
                // location-only regardless of doc type — not a replacement for the rule above,
                // just a way to disable warehouse-based receipt org-wide when it's on.
                if (po.getPurchaseRequisition() != null) {
                    String docTypeCode = po.getPurchaseRequisition().getDocTypeCode();
                    dto.setDocTypeCode(docTypeCode);
                    dto.setRawMaterial(orgConfigGate.isGoodsReceiptWarehouseEnabled()
                            && docTypeCode != null && docTypeCode.endsWith("RM"));
                    if (docTypeCode != null) {
                        documentTypeRepository.findById(docTypeCode)
                                .ifPresent(dt -> dto.setDocTypeDescription(dt.getDescription()));
                    }
                }
            }
            
            // Map packages (boxes) and items (lines)
            List<MaterialInwardDetailDto.BoxDto> boxDtos = new ArrayList<>();
            if (asn.getPackages() != null) {
                // Group by packageNumber to avoid duplicates when multiple materials are in one package
                java.util.Map<Integer, List<AsnPackage>> groupedPackages = asn.getPackages().stream()
                        .filter(p -> p.getPackageNumber() != null)
                        .collect(Collectors.groupingBy(AsnPackage::getPackageNumber));
                
                for (java.util.Map.Entry<Integer, List<AsnPackage>> entry : groupedPackages.entrySet()) {
                    List<AsnPackage> pkgs = entry.getValue();
                    AsnPackage primaryPkg = pkgs.get(0); // Take the first one for weight/seal info

                    MaterialInwardDetailDto.BoxDto boxDto = new MaterialInwardDetailDto.BoxDto();
                    boxDto.setId("PKG-" + primaryPkg.getId());
                    boxDto.setBoxNo("BOX-" + String.format("%03d", primaryPkg.getPackageNumber()));
                    boxDto.setManifestSeal("SL-" + (88000 + primaryPkg.getId())); // Mock seal
                    
                    // Sum weights if needed, or just use the first
                    double totalWeight = 0;
                    for(AsnPackage p : pkgs) {
                        if(p.getQuantity() != null) totalWeight += p.getQuantity();
                    }
                    boxDto.setWeight(totalWeight > 0 ? totalWeight + " kg gross" : "N/A");
                    
                    // Add items for each box. Use the materialDetails to map correctly
                    List<MaterialInwardDetailDto.LineDto> lineDtos = new ArrayList<>();
                    
                    // Collect all material details for this package group
                    java.util.Set<String> packageMatCodes = new java.util.HashSet<>();
                    for(AsnPackage p : pkgs) {
                        if (p.getMaterialDetails() != null && !p.getMaterialDetails().isEmpty()) {
                            String[] details = p.getMaterialDetails().split(",");
                            for(String d : details) {
                                packageMatCodes.add(d.split(" - ")[0].trim());
                            }
                        }
                    }
                    
                    if (asn.getItems() != null) {
                        for (AsnItem item : asn.getItems()) {
                            boolean belongsToBox = false;
                            String itemPartNo = item.getPartNumber() != null ? item.getPartNumber().trim() : "";
                            if (packageMatCodes.isEmpty()) {
                                belongsToBox = true; // Fallback: add all if no mapping
                            } else {
                                if (packageMatCodes.contains(itemPartNo)) {
                                    belongsToBox = true;
                                }
                            }
                            
                            if (belongsToBox) {
                                MaterialInwardDetailDto.LineDto lineDto = new MaterialInwardDetailDto.LineDto();
                                lineDto.setId("LN-" + item.getId());
                                lineDto.setItemNo(item.getPartNumber());
                                lineDto.setDescription(item.getPurchaseOrderItem() != null ? item.getPurchaseOrderItem().getMaterialDescription() : "Item Description");
                                lineDto.setUom(item.getPurchaseOrderItem() != null ? item.getPurchaseOrderItem().getUom() : "EA");
                                lineDto.setManifestQty(item.getQuantityShipped() != null ? item.getQuantityShipped().doubleValue() : 0.0);
                                
                                // Mock batches
                                if (item.getBatchHeatNumber() != null && !item.getBatchHeatNumber().isEmpty()) {
                                    MaterialInwardDetailDto.BatchDto batchDto = new MaterialInwardDetailDto.BatchDto();
                                    batchDto.setBatchNo(item.getBatchHeatNumber());
                                    batchDto.setQty(lineDto.getManifestQty());
                                    lineDto.setBatches(List.of(batchDto));
                                }
                                
                                lineDtos.add(lineDto);
                            }
                        }
                    }
                    boxDto.setLines(lineDtos);
                    boxDtos.add(boxDto);
                }
            }
            
            dto.setBoxes(boxDtos);
        }
        
        return dto;
    }

    @Override
    public GoodsReceipt submitVerification(Long gateEntryId, MaterialInwardSubmitDto dto) {
        GateEntry ge = gateEntryRepository.findById(gateEntryId)
                .orElseThrow(() -> new RuntimeException("GateEntry not found"));
                
        GoodsReceipt gr = new GoodsReceipt();
        gr.setGateEntry(ge);
        gr.setAsn(ge.getAsn());
        gr.setDecision(dto.getDecision());
        gr.setProcessedBy(dto.getBy());
        gr.setRemarks(dto.getReason());
        gr.setInwardDetails(dto.getInwardDetailsJson());
        
        if ("ACCEPT".equalsIgnoreCase(dto.getDecision())) {
            gr.setGrnNumber("GRN-" + ge.getGatePassNumber().replace("GE-", ""));
        } else {
            gr.setRtvNumber("RTV-" + ge.getGatePassNumber().replace("GE-", ""));
        }
        
        GoodsReceipt savedGr = goodsReceiptRepository.save(gr);

        // Sync Excel summary and documents to FolderIT
        goodsReceiptFolderitSyncService.syncGoodsReceiptAsync(ge);

        return savedGr;
    }
}
