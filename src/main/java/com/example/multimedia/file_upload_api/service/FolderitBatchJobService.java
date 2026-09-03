package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.entity.FolderitSyncConfig;
import com.example.multimedia.file_upload_api.repository.FolderitSyncConfigRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class FolderitBatchJobService {

    private static final Logger logger = LoggerFactory.getLogger(FolderitBatchJobService.class);

    private final FolderItService folderItService;
    private final MasterPurchaseOrderService masterPurchaseOrderService;
    private final FolderitSyncConfigRepository configRepository;

    public FolderitBatchJobService(FolderItService folderItService,
                                   MasterPurchaseOrderService masterPurchaseOrderService,
                                   FolderitSyncConfigRepository configRepository) {
        this.folderItService = folderItService;
        this.masterPurchaseOrderService = masterPurchaseOrderService;
        this.configRepository = configRepository;
    }

    // Runs every 1 minute to check if it's time to execute the batch job
    @Scheduled(fixedDelay = 60000)
    public void scheduleTask() {
        Optional<FolderitSyncConfig> configOpt = configRepository.findById(1L);
        if (configOpt.isEmpty()) return;

        FolderitSyncConfig config = configOpt.get();
        if (!config.isEnabled()) return;

        if (config.getLastRunTime() != null) {
            LocalDateTime nextRun = config.getLastRunTime().plusMinutes(config.getIntervalMinutes());
            if (LocalDateTime.now().isBefore(nextRun)) {
                return; // Not time yet
            }
        }

        runBatchJob(config);
    }

    public synchronized void runBatchJob(FolderitSyncConfig config) {
        if (config == null) {
            config = configRepository.findById(1L).orElse(new FolderitSyncConfig());
        }

        int filesProcessed = 0;
        StringBuilder debugLog = new StringBuilder();
        try {
            logger.info("Starting Folderit PO Automation Job...");
            
            // 1. Get all folders to build a tree
            JSONArray allFolders = folderItService.getAllFolders();
            debugLog.append("Fetched ").append(allFolders.length()).append(" folders. ");
            logger.info("Total folders fetched from Folderit API: {}", allFolders.length());
            
            Map<String, JSONObject> folderMap = new HashMap<>();
            
            for (int i = 0; i < allFolders.length(); i++) {
                JSONObject f = allFolders.getJSONObject(i);
                folderMap.put(f.getString("uid"), f);
            }
            
            // 2. Find "01 Initial" folders
            for (JSONObject folder : folderMap.values()) {
                if ("01 Initial".equalsIgnoreCase(folder.optString("name", "").trim())) {
                    
                    String parentUid = folder.optString("parentUid", null);
                    logger.info("Found '01 Initial' folder! UID: {}, ParentUID: {}", folder.optString("uid"), parentUid);
                    debugLog.append("Found 01 Initial (").append(folder.optString("uid")).append("). ");
                    
                    if (parentUid == null) continue;
                    
                    // Find sibling "02 Process" folder
                    String processFolderUid = null;
                    for (JSONObject sibling : folderMap.values()) {
                        if (parentUid.equals(sibling.optString("parentUid", null)) &&
                            "02 Process".equalsIgnoreCase(sibling.optString("name", "").trim())) {
                            processFolderUid = sibling.getString("uid");
                            break;
                        }
                    }
                    
                    if (processFolderUid == null) {
                        logger.warn("Found '01 Initial' folder (uid={}) but no sibling '02 Process' folder. Skipping.", folder.getString("uid"));
                        debugLog.append("No 02 Process found for it. ");
                        continue;
                    }
                    debugLog.append("Found 02 Process (").append(processFolderUid).append("). ");
                    
                    // Process files in this "01 Initial" folder
                    JSONArray files = folderItService.getFilesInFolder(folder.getString("uid"));
                    debugLog.append("Files in 01 Initial: ").append(files.length()).append(". ");
                    logger.info("Found {} files inside '01 Initial' (UID: {})", files.length(), folder.getString("uid"));
                    
                    for (int j = 0; j < files.length(); j++) {
                        JSONObject file = files.getJSONObject(j);
                        String fileName = file.optString("name", "").toLowerCase();
                        if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx") || fileName.endsWith(".csv") || fileName.endsWith(".xml")) {
                            
                            String fileUid = file.getString("uid");
                            logger.info("Processing file: {} (uid={})", fileName, fileUid);
                            
                            try {
                                FolderItService.DownloadedFile downloadedFile = folderItService.downloadFileBytes(fileUid);
                                java.util.List<?> pos = masterPurchaseOrderService.saveExcelData(downloadedFile.bytes(), null);
                                logger.info("Parsed {} POs from {}", (pos == null ? 0 : pos.size()), fileName);
                                
                                // Move file on success
                                folderItService.moveFile(fileUid, processFolderUid);
                                logger.info("Successfully processed and moved file {} to '02 Process'", fileName);
                                debugLog.append("Processed ").append(fileName).append(" (POs:").append(pos == null ? 0 : pos.size()).append("). ");
                                filesProcessed++;
                            } catch (Exception e) {
                                logger.error("Failed to process file " + fileName, e);
                                debugLog.append("Error on ").append(fileName).append(": ").append(e.getMessage()).append(". ");
                            }
                        }
                    }
                }
            }
            
            String finalStatus = "Success. " + debugLog.toString();
            if (finalStatus.length() > 255) {
                finalStatus = finalStatus.substring(0, 252) + "...";
            }
            config.setLastRunStatus(finalStatus);
            config.setFilesProcessedLastRun(filesProcessed);
            logger.info("Folderit PO Automation Job completed. Processed {} files.", filesProcessed);
            
        } catch (Exception e) {
            logger.error("Folderit Batch Job failed", e);
            config.setLastRunStatus("Failed: " + e.getMessage());
            config.setFilesProcessedLastRun(filesProcessed);
        } finally {
            config.setLastRunTime(LocalDateTime.now());
            configRepository.save(config);
        }
    }
}
