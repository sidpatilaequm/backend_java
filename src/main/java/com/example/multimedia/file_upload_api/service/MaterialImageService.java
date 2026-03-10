package com.example.multimedia.file_upload_api.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.List;

@Service
public class MaterialImageService {
    
    private static final Logger logger = LoggerFactory.getLogger(MaterialImageService.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * Fetch image data from database using materialId and imageName
     */
    public Optional<byte[]> getImageData(String materialId, String imageName) {
        try {
            String sql = "SELECT image_data FROM material_images WHERE material_id = ? AND image_name = ?";
            
            List<byte[]> results = jdbcTemplate.query(sql, 
                new Object[]{materialId, imageName}, 
                new ImageDataRowMapper());
            
            if (!results.isEmpty()) {
                logger.info("Found image for materialId: {}, imageName: {}", materialId, imageName);
                return Optional.of(results.get(0));
            } else {
                logger.warn("No image found for materialId: {}, imageName: {}", materialId, imageName);
                return Optional.empty();
            }
            
        } catch (Exception e) {
            logger.error("Error fetching image from database: materialId={}, imageName={}, error={}", 
                materialId, imageName, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Row mapper for image data
     */
    private static class ImageDataRowMapper implements RowMapper<byte[]> {
        @Override
        public byte[] mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getBytes("image_data");
        }
    }
}
