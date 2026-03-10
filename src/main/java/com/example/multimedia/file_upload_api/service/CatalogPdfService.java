package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.CatalogPdfRequest;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.CompanyCoverPhoto;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.layout.element.Link;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.properties.BorderRadius;
import com.itextpdf.kernel.colors.DeviceRgb;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class CatalogPdfService {

    private static final Logger logger = LoggerFactory.getLogger(CatalogPdfService.class);


    @Autowired
    private MaterialImageService materialImageService;
    
    @Autowired
    private CompanyCoverPhotoService companyCoverPhotoService;

    /**
     * Generate PDF catalog from frontend data
     */
    public ServiceResponse generateCatalogPdf(CatalogPdfRequest request) {
        ServiceResponse response = new ServiceResponse();
        
        try {
            // Validate request
            if (request == null || request.getProducts() == null || request.getProducts().isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("Invalid request: Products data is required");
                return response;
            }

            // Generate PDF
            byte[] pdfBytes = createCatalogPdf(request);
            
            // Generate filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "catalog_" + request.getChannelCode() + "_" + timestamp + ".pdf";
            
            response.setStatus("SUCCESS");
            response.setStatusMsg("Catalog PDF generated successfully");
            response.addData("pdfBytes", pdfBytes);
            response.addData("filename", filename);
            response.addData("fileSize", pdfBytes.length);
            response.addData("totalProducts", request.getProducts().size());
            response.addData("channelName", request.getChannelName());
            
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to generate catalog PDF: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Create the actual PDF document
     */
    private byte[] createCatalogPdf(CatalogPdfRequest request) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        
        // Page 1: Cover page with cover photo
        addCoverPage(document, request);
        
        // Page 2+: Table of Contents (dynamic pages)
        int productsStartPage = addTableOfContents(document, request.getProducts());
        
        // Products section starts after all TOC pages
        logger.info("Adding products section starting from page {}...", productsStartPage);
        addProductsSection(document, request.getProducts(), request.getProductsPerRow(), productsStartPage);
        
        document.close();
        return baos.toByteArray();
    }

    /**
     * Add cover page with company cover photo (full page)
     */
    private void addCoverPage(Document document, CatalogPdfRequest request) throws Exception {
        logger.info("Adding cover page...");
        
        // Get company cover photo
        try {
            ServiceResponse coverPhotoResponse = companyCoverPhotoService.getPrimaryCoverPhoto();
            if (coverPhotoResponse.getStatus().equals("SUCCESS") && coverPhotoResponse.getData().get("coverPhoto") != null) {
                CompanyCoverPhoto coverPhoto = (CompanyCoverPhoto) coverPhotoResponse.getData().get("coverPhoto");
                
                // Add cover photo to fill entire page using absolute positioning
                Image coverImage = new Image(ImageDataFactory.create(coverPhoto.getCoverPhotoData()));
                
                // Get page dimensions and set image to fill entire page
                float pageWidth = document.getPdfDocument().getDefaultPageSize().getWidth();
                float pageHeight = document.getPdfDocument().getDefaultPageSize().getHeight();
                
                coverImage.setWidth(pageWidth);
                coverImage.setHeight(pageHeight);
                coverImage.setFixedPosition(0, 0); // Position at top-left corner
                
                document.add(coverImage);
                
                logger.info("Cover photo added successfully - full page");
            } else {
                logger.warn("No cover photo found, adding blank page");
                // Add blank page if no cover photo
                Paragraph placeholder = new Paragraph("");
                placeholder.setHeight(UnitValue.createPercentValue(100));
                document.add(placeholder);
            }
        } catch (Exception e) {
            logger.error("Error adding cover photo: {}", e.getMessage());
            // Add blank page on error
            Paragraph placeholder = new Paragraph("");
            placeholder.setHeight(UnitValue.createPercentValue(100));
            document.add(placeholder);
        }
    }

    /**
     * Add table of contents pages (dynamic - can span multiple pages)
     */
    private int addTableOfContents(Document document, List<CatalogPdfRequest.ProductCard> products) throws Exception {
        logger.info("Adding table of contents...");
        
        if (products == null || products.isEmpty()) {
            // Add single TOC page with just title
            document.add(new AreaBreak());
            Paragraph tocTitle = new Paragraph("Table of Contents");
            tocTitle.setFontSize(24).setBold();
            tocTitle.setTextAlignment(TextAlignment.CENTER);
            tocTitle.setMarginBottom(30);
            document.add(tocTitle);
            addPageNumber(document, 2);
            return 2; // Return next page number (3)
        }
        
        // Calculate how many TOC pages we need
        int productsPerTocPage = 25; // Approximate products per TOC page
        int tocPagesNeeded = (int) Math.ceil((double) products.size() / productsPerTocPage);
        
        // Calculate starting page for products (after cover + all TOC pages)
        int productsStartPage = 2 + tocPagesNeeded; // Cover(1) + TOC pages + 1
        
        // Calculate page numbers for each product
        int productsPerPage = 9; // Default 3x3 grid
        int currentProductPage = productsStartPage;
        
        // Add TOC pages
        for (int tocPageIndex = 0; tocPageIndex < tocPagesNeeded; tocPageIndex++) {
            // Add new page for each TOC page
            document.add(new AreaBreak());
            
            // Table of Contents title (only on first TOC page)
            if (tocPageIndex == 0) {
                Paragraph tocTitle = new Paragraph("Table of Contents");
                tocTitle.setFontSize(24).setBold();
                tocTitle.setTextAlignment(TextAlignment.CENTER);
                tocTitle.setMarginBottom(30);
                document.add(tocTitle);
            }
            
            // Create table for this TOC page
            Table tocTable = new Table(3);
            tocTable.setWidth(UnitValue.createPercentValue(100));
            tocTable.setMarginTop(tocPageIndex == 0 ? 20 : 50); // More margin for subsequent pages
            
            // Add header row (only on first TOC page)
            if (tocPageIndex == 0) {
                Cell headerCell1 = new Cell();
                headerCell1.add(new Paragraph("S.No").setBold().setFontSize(12));
                headerCell1.setBorder(null);
                headerCell1.setTextAlignment(TextAlignment.CENTER);
                tocTable.addCell(headerCell1);
                
                Cell headerCell2 = new Cell();
                headerCell2.add(new Paragraph("Product Name").setBold().setFontSize(12));
                headerCell2.setBorder(null);
                tocTable.addCell(headerCell2);
                
                Cell headerCell3 = new Cell();
                headerCell3.add(new Paragraph("Page").setBold().setFontSize(12));
                headerCell3.setBorder(null);
                headerCell3.setTextAlignment(TextAlignment.CENTER);
                tocTable.addCell(headerCell3);
            }
            
            // Add products for this TOC page
            int startIndex = tocPageIndex * productsPerTocPage;
            int endIndex = Math.min(startIndex + productsPerTocPage, products.size());
            
            for (int i = startIndex; i < endIndex; i++) {
                CatalogPdfRequest.ProductCard product = products.get(i);
                
                // Calculate which product page this product will be on
                int productPageNumber = productsStartPage + (i / productsPerPage);
                
                // Add serial number
                Cell snoCell = new Cell();
                snoCell.add(new Paragraph(String.valueOf(i + 1)).setFontSize(10));
                snoCell.setBorder(null);
                snoCell.setTextAlignment(TextAlignment.CENTER);
                snoCell.setPadding(5);
                tocTable.addCell(snoCell);
                
                // Add product name (will make clickable later)
                Cell nameCell = new Cell();
                Paragraph productNamePara = new Paragraph(product.getProductName()).setFontSize(10);
                nameCell.add(productNamePara);
                nameCell.setBorder(null);
                nameCell.setPadding(5);
                tocTable.addCell(nameCell);
                
                // Add page number
                Cell pageCell = new Cell();
                pageCell.add(new Paragraph(String.valueOf(productPageNumber)).setFontSize(10));
                pageCell.setBorder(null);
                pageCell.setTextAlignment(TextAlignment.CENTER);
                pageCell.setPadding(5);
                tocTable.addCell(pageCell);
            }
            
            document.add(tocTable);
            
            // Add page number for this TOC page
            addPageNumber(document, 2 + tocPageIndex);
        }
        
        return productsStartPage; // Return the page number where products should start
    }


    /**
     * Add products section
     */
    private void addProductsSection(Document document, List<CatalogPdfRequest.ProductCard> products, int productsPerRow, int startPageNumber) throws Exception {
        // Calculate how many products per page (3x3 grid = 9 products per page)
        int productsPerPage = productsPerRow * productsPerRow; // Total products per page
        int totalPages = (int) Math.ceil((double) products.size() / productsPerPage);
        
                 for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
             // Add page break for all pages (including first page) to ensure products start on new page after TOC
             document.add(new AreaBreak());
             
             // Create a grid table for this page
            Table gridTable = new Table(productsPerRow);
            gridTable.setWidth(UnitValue.createPercentValue(100));
            gridTable.setMarginTop(20);
            gridTable.setMarginBottom(20);
            
            int startIndex = pageIndex * productsPerPage;
            int endIndex = Math.min(startIndex + productsPerPage, products.size());
            
            for (int i = startIndex; i < endIndex; i++) {
                CatalogPdfRequest.ProductCard product = products.get(i);
                
                // Create product card cell
                Cell cardCell = createProductCard(product, i + 1);
                gridTable.addCell(cardCell);
            }
            
            // Fill remaining cells with empty content if less than productsPerPage products on this page
            int remainingCells = productsPerPage - (endIndex - startIndex);
            for (int i = 0; i < remainingCells; i++) {
                Cell emptyCell = new Cell();
                emptyCell.setBorder(null);
                emptyCell.setHeight(200);
                gridTable.addCell(emptyCell);
            }
            
            document.add(gridTable);
            
            // Add page number at bottom
            int currentPageNumber = startPageNumber + pageIndex;
            addPageNumber(document, currentPageNumber);
        }
    }
    
    /**
     * Create a single product card
     */
    private Cell createProductCard(CatalogPdfRequest.ProductCard product, int productNumber) {
        Cell cardCell = new Cell();
        cardCell.setBorder(null);
        cardCell.setPadding(10);
        cardCell.setHeight(200);
        cardCell.setVerticalAlignment(VerticalAlignment.TOP);
        
        // Product image (if available) - make it clickable
        if (product.getMaterialId() != null && product.getImageName() != null) {
            try {
                // Fetch image from database
                Optional<byte[]> imageData = materialImageService.getImageData(
                    product.getMaterialId(), 
                    product.getImageName()
                );
                
                if (imageData.isPresent()) {
                    // Create image from database bytes
                    Image productImage = new Image(ImageDataFactory.create(imageData.get()));
                    productImage.setWidth(80).setHeight(80);
                    productImage.setHorizontalAlignment(HorizontalAlignment.CENTER);
                    
                    // Add image (clickable functionality will be handled by the button below)
                    cardCell.add(productImage);
                    
                    logger.debug("Image added successfully from database: materialId={}, imageName={}", 
                        product.getMaterialId(), product.getImageName());
                } else {
                    // Image not found in database, add placeholder
                    Paragraph noImage = new Paragraph("No image").setFontSize(8);
                    noImage.setTextAlignment(TextAlignment.CENTER);
                    cardCell.add(noImage);
                    logger.warn("Image not found in database: materialId={}, imageName={}", 
                        product.getMaterialId(), product.getImageName());
                }
                
            } catch (Exception e) {
                logger.error("Error processing image from database: materialId={}, imageName={}, error={}", 
                    product.getMaterialId(), product.getImageName(), e.getMessage());
                // Image processing failed, add placeholder
                Paragraph noImage = new Paragraph("No image").setFontSize(8);
                noImage.setTextAlignment(TextAlignment.CENTER);
                cardCell.add(noImage);
            }
        } else {
            // No materialId or imageName provided, add placeholder
            Paragraph noImage = new Paragraph("No image").setFontSize(8);
            noImage.setTextAlignment(TextAlignment.CENTER);
            cardCell.add(noImage);
        }
        
        // Product name
        Paragraph productName = new Paragraph(product.getProductName()).setFontSize(10).setBold();
        productName.setTextAlignment(TextAlignment.CENTER);
        productName.setMarginTop(5);
        cardCell.add(productName);
        
        // SKU
        if (product.getSku() != null) {
            Paragraph sku = new Paragraph("SKU: " + product.getSku()).setFontSize(8);
            sku.setTextAlignment(TextAlignment.CENTER);
            sku.setMarginTop(2);
            cardCell.add(sku);
        }
        
        // Category
        if (product.getCategory() != null) {
            Paragraph category = new Paragraph("Category: " + product.getCategory()).setFontSize(8);
            category.setTextAlignment(TextAlignment.CENTER);
            category.setMarginTop(2);
            cardCell.add(category);
        }
        
        // Price
        if (product.getPrice() != null) {
            Paragraph price = new Paragraph("Price: ₹" + product.getPrice()).setFontSize(10).setBold().setFontColor(ColorConstants.RED);
            price.setTextAlignment(TextAlignment.CENTER);
            price.setMarginTop(5);
            cardCell.add(price);
        }
        
        // Add clickable button-like cart icon with product URL
        // Create Cadmium Green color (#026842)
        DeviceRgb cadmiumGreen = new DeviceRgb(2, 104, 66);
        
        if (product.getProductUrl() != null && !product.getProductUrl().isEmpty()) {
            // Create button-like appearance with Cadmium Green background
            Link cartButton = new Link("🛒 Add to Cart", PdfAction.createURI(product.getProductUrl()));
            Paragraph cartButtonPara = new Paragraph(cartButton)
                .setFontSize(9)
                .setBold()
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(cadmiumGreen)
                .setPadding(8)
                .setMarginTop(8)
                .setMarginBottom(4);
            cartButtonPara.setTextAlignment(TextAlignment.CENTER);
            cartButtonPara.setBorder(new SolidBorder(cadmiumGreen, 1));
            cartButtonPara.setBorderRadius(new BorderRadius(5));
            cardCell.add(cartButtonPara);
        } else {
            // Fallback: create URL using materialId if productUrl is not provided
            String fallbackUrl = "http://127.0.0.1:8000/pages/products/" + product.getMaterialId() + "/";
            Link cartButton = new Link("🛒 Add to Cart", PdfAction.createURI(fallbackUrl));
            Paragraph cartButtonPara = new Paragraph(cartButton)
                .setFontSize(9)
                .setBold()
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(cadmiumGreen)
                .setPadding(8)
                .setMarginTop(8)
                .setMarginBottom(4);
            cartButtonPara.setTextAlignment(TextAlignment.CENTER);
            cartButtonPara.setBorder(new SolidBorder(cadmiumGreen, 1));
            cartButtonPara.setBorderRadius(new BorderRadius(5));
            cardCell.add(cartButtonPara);
        }
        
        // Description (if available and not too long)
        if (product.getDescription() != null && product.getDescription().length() < 50) {
            Paragraph description = new Paragraph(product.getDescription()).setFontSize(7);
            description.setTextAlignment(TextAlignment.CENTER);
            description.setMarginTop(2);
            cardCell.add(description);
        }
        
        return cardCell;
    }

    /**
     * Add page number at bottom of page
     */
    private void addPageNumber(Document document, int pageNumber) throws Exception {
        Paragraph pageNumberPara = new Paragraph(String.valueOf(pageNumber));
        pageNumberPara.setFontSize(10);
        pageNumberPara.setTextAlignment(TextAlignment.CENTER);
        pageNumberPara.setMarginTop(20);
        pageNumberPara.setMarginBottom(10);
        document.add(pageNumberPara);
    }

}
