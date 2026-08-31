package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.entity.MasterPurchaseOrder;
import com.example.multimedia.file_upload_api.entity.PortalPurchaseOrder;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.repository.UserDetailRepository;
import com.example.multimedia.file_upload_api.service.FolderItService;
import com.example.multimedia.file_upload_api.service.MasterPurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/master-purchase-orders")
public class MasterPurchaseOrderController {

    @Autowired
    private MasterPurchaseOrderService service;

    @Autowired
    private FolderItService folderItService;

    @Autowired
    private UserDetailRepository userDetailRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadExcel(@RequestParam("file") MultipartFile file) {
        try {
            Long userId = null;
            try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getName() != null) {
                    Optional<UserDetail> userOpt = userDetailRepository.findByEmail(auth.getName());
                    if (userOpt.isPresent()) {
                        userId = userOpt.get().getUserId();
                    }
                }
            } catch (Exception ignored) {}
            
            List<PortalPurchaseOrder> pos = service.saveExcelData(file.getBytes(), userId);
            return ResponseEntity.ok(Map.of("message", "Excel uploaded and data saved successfully", "pos", pos));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error uploading file: " + e.getMessage()));
        }
    }

    @PostMapping("/sync-folderit")
    public ResponseEntity<?> syncFromFolderIt(@RequestParam("uid") String uid) {
        try {
            String fileUid = uid;
            
            try {
                fileUid = folderItService.findFirstExcelFileInFolder(uid);
                if (fileUid == null) {
                    fileUid = uid;
                }
            } catch (Exception ignored) {}
            
            FolderItService.DownloadedFile downloadedFile = folderItService.downloadFileBytes(fileUid);
            
            Long userId = null;
            try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getName() != null) {
                    Optional<UserDetail> userOpt = userDetailRepository.findByEmail(auth.getName());
                    if (userOpt.isPresent()) {
                        userId = userOpt.get().getUserId();
                    }
                }
            } catch (Exception ignored) {}
            
            List<PortalPurchaseOrder> pos = service.saveExcelData(downloadedFile.bytes(), userId);
            
            if (pos.isEmpty()) {
                String fileStart = new String(downloadedFile.bytes(), java.nio.charset.StandardCharsets.UTF_8);
                
                String xmlDebug = "";
                if (fileStart.trim().startsWith("<?xml")) {
                    try {
                        javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
                        factory.setNamespaceAware(true);
                        javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
                        org.w3c.dom.Document doc = builder.parse(new java.io.ByteArrayInputStream(downloadedFile.bytes()));
                        org.w3c.dom.NodeList rows = doc.getElementsByTagNameNS("*", "Row");
                        xmlDebug += "Total Rows: " + rows.getLength() + ". ";
                        if (rows.getLength() > 1) {
                            org.w3c.dom.Element rowEl = (org.w3c.dom.Element) rows.item(1);
                            org.w3c.dom.NodeList cells = rowEl.getElementsByTagNameNS("*", "Data");
                            xmlDebug += "Row 1 (index 1) has " + cells.getLength() + " Data cells. ";
                        }
                    } catch (Exception ex) {
                        xmlDebug = "XML parse error: " + ex.getMessage();
                    }
                }

                if (fileStart.length() > 500) {
                    fileStart = fileStart.substring(0, 500);
                }
                
                return ResponseEntity.ok(Map.of(
                    "message", "File synced but NO POs created. See debug info.",
                    "pos", pos,
                    "debug_xml", xmlDebug,
                    "debug_size", downloadedFile.bytes().length,
                    "debug_content", fileStart
                ));
            }
            
            return ResponseEntity.ok(Map.of("message", "File synced and PO created successfully", "pos", pos));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error syncing file: " + e.getMessage()));
        }
    }

    @GetMapping("/user-data")
    public ResponseEntity<?> getOrdersForVendor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
        }

        String email = authentication.getName();
        Optional<UserDetail> userOpt = userDetailRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            Long userId = userOpt.get().getUserId();
            List<MasterPurchaseOrder> orders = service.getOrdersByUserId(userId);
            return ResponseEntity.ok(orders);
        }

        return ResponseEntity.status(404).body(Map.of("error", "User not found"));
    }
}
