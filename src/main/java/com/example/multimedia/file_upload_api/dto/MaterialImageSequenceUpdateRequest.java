package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.util.List;

@Data
public class MaterialImageSequenceUpdateRequest {
    private Long materialId;
    private List<ImageSequence> imageSequences;

    @Data
    public static class ImageSequence {
        private Long imageId;
        private Integer sequenceOrder;
    }
} 