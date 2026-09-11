package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.entity.Asn;
import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import com.example.multimedia.file_upload_api.entity.GateEntry;
import com.example.multimedia.file_upload_api.entity.PortalPurchaseOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Service
public class GoodsReceiptFolderitSyncService {

    private static final Logger logger = LoggerFactory.getLogger(GoodsReceiptFolderitSyncService.class);
    
    // Root FolderIT folder UID for "Goods receipt"
    private static final String GOODS_RECEIPT_ROOT_UID = "wwmTu0yiRa";

    private final FolderItService folderItService;
    private final GoodsReceiptExcelExportService excelExportService;
    private final com.example.multimedia.file_upload_api.repository.GateEntryRepository gateEntryRepository;

    public GoodsReceiptFolderitSyncService(FolderItService folderItService,
                                          GoodsReceiptExcelExportService excelExportService,
                                          com.example.multimedia.file_upload_api.repository.GateEntryRepository gateEntryRepository) {
        this.folderItService = folderItService;
        this.excelExportService = excelExportService;
        this.gateEntryRepository = gateEntryRepository;
    }

    /**
     * Asynchronously creates folder hierarchy and uploads Excel + document attachments into FolderIT.
     * Hierarchy: Goods receipt (wwmTu0yiRa) > MMM yyyy (e.g. Sept 2026) > {vendor_bpno} - {vendor_name}
     */
    public void syncGoodsReceiptAsync(GateEntry rawGateEntry) {
        Long gateEntryId = rawGateEntry != null ? rawGateEntry.getId() : null;
        if (gateEntryId == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                logger.info("Starting FolderIT sync for Gate Entry ID: {}", gateEntryId);
                
                GateEntry gateEntry = gateEntryRepository.findWithDetailsById(gateEntryId)
                        .orElse(rawGateEntry);

                Asn asn = gateEntry.getAsn();
                if (asn == null) {
                    logger.warn("No ASN associated with Gate Entry ID {}, skipping FolderIT sync.", gateEntryId);
                    return;
                }

                // 1. Determine Month-Year Folder Name (e.g. "Sept 2026")
                LocalDateTime entryTime = gateEntry.getInTime() != null ? gateEntry.getInTime() : LocalDateTime.now();
                String monthYearFolder = entryTime.format(DateTimeFormatter.ofPattern("MMM yyyy"));

                // 2. Determine Vendor Folder Name (e.g. "VEND-123 - VENDOR NAME")
                String bpNo = asn.getVendorBpno() != null ? asn.getVendorBpno() : "VEND-UNKNOWN";
                String vendorName = "UNKNOWN VENDOR";
                
                if (asn.getPurchaseOrder() != null && asn.getPurchaseOrder().getVendor() != null) {
                    CompanyDetails v = asn.getPurchaseOrder().getVendor();
                    if (v.getCompanyName() != null && !v.getCompanyName().trim().isEmpty()) {
                        vendorName = v.getCompanyName().trim();
                    }
                }
                String vendorFolder = bpNo + " - " + vendorName;

                // 3. Create/Get Subfolder Hierarchy in FolderIT under Goods receipt (wwmTu0yiRa)
                String monthFolderUid = folderItService.getOrCreateSubFolder(GOODS_RECEIPT_ROOT_UID, monthYearFolder);
                String targetVendorFolderUid = folderItService.getOrCreateSubFolder(monthFolderUid, vendorFolder);

                logger.info("Target FolderIT vendor folder resolved: {} (UID: {})", vendorFolder, targetVendorFolderUid);

                // 4. Generate & Upload Formatted Excel Workbook
                byte[] excelBytes = excelExportService.generateGoodsReceiptExcel(gateEntry);
                String gatePassNo = gateEntry.getGatePassNumber() != null ? gateEntry.getGatePassNumber() : ("GE-" + gateEntry.getId());
                String excelFileName = "GateEntry_" + gatePassNo + "_Summary.xlsx";
                
                folderItService.uploadBytesToFolder(excelBytes, excelFileName, 
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", targetVendorFolderUid);
                logger.info("Successfully uploaded Excel summary: {} to FolderIT", excelFileName);

                // 5. Upload Attached ASN Documents (Invoices, eWay Bills, Packing Lists, etc.)
                uploadAttachmentIfPresent(asn.getTaxInvoiceUrl(), "TaxInvoice_" + gatePassNo, targetVendorFolderUid);
                uploadAttachmentIfPresent(asn.getEwayBillUrl(), "EWayBill_" + gatePassNo, targetVendorFolderUid);
                uploadAttachmentIfPresent(asn.getPackingListUrl(), "PackingList_" + gatePassNo, targetVendorFolderUid);
                uploadAttachmentIfPresent(asn.getPdirUrl(), "PDIR_" + gatePassNo, targetVendorFolderUid);
                uploadAttachmentIfPresent(asn.getDeviationUrl(), "DeviationDoc_" + gatePassNo, targetVendorFolderUid);
                uploadAttachmentIfPresent(asn.getOthersUrl(), "OtherDoc_" + gatePassNo, targetVendorFolderUid);

                logger.info("FolderIT Goods Receipt sync completed successfully for Gate Entry Pass: {}", gatePassNo);

            } catch (Exception e) {
                logger.error("Error during FolderIT Goods Receipt sync for Gate Entry ID: {}", gateEntryId, e);
            }
        });
    }

    private void uploadAttachmentIfPresent(String fileUrl, String baseFileName, String folderUid) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return;
        }
        try {
            String extension = "";
            int lastDot = fileUrl.lastIndexOf('.');
            if (lastDot != -1 && lastDot < fileUrl.length() - 1) {
                extension = fileUrl.substring(lastDot);
                if (extension.contains("?")) {
                    extension = extension.substring(0, extension.indexOf('?'));
                }
            }
            if (extension.isEmpty() || extension.length() > 5) {
                extension = ".pdf";
            }

            String fileName = baseFileName + extension;
            URL url = new URL(fileUrl);
            try (InputStream in = url.openStream()) {
                byte[] bytes = in.readAllBytes();
                folderItService.uploadBytesToFolder(bytes, fileName, "application/octet-stream", folderUid);
                logger.info("Uploaded attached document: {} to FolderIT", fileName);
            }
        } catch (Exception e) {
            logger.warn("Failed to fetch/upload document from URL: {} to FolderIT: {}", fileUrl, e.getMessage());
        }
    }
}
