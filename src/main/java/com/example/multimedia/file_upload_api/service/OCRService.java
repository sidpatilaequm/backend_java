package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.CardDataDTO;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OCRService {

    private final Tesseract tesseract;

    public OCRService() {
        tesseract = new Tesseract();
        // Get the project root directory and append tessdata
        String projectRoot = System.getProperty("user.dir");
        String tessdataPath = Paths.get(projectRoot, "tessdata").toString();
        tesseract.setDatapath(tessdataPath);
        tesseract.setLanguage("eng");
    }

    public CardDataDTO extractCardData(MultipartFile file) throws IOException, TesseractException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        String extractedText = tesseract.doOCR(image);
        return parseExtractedText(extractedText);
    }

    private CardDataDTO parseExtractedText(String text) {
        CardDataDTO cardData = new CardDataDTO();
        String[] lines = text.split("\\n");

        List<String> emailAddresses = new ArrayList<>();
        List<String> phoneNumbers = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();

            // Extract email addresses
            if (line.contains("@")) {
                Pattern emailPattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}");
                Matcher matcher = emailPattern.matcher(line);
                while (matcher.find()) {
                    emailAddresses.add(matcher.group());
                }
            }

            // Extract phone numbers
            Pattern phonePattern = Pattern.compile("\\+?\\d[\\d -]{8,12}\\d");
            Matcher phoneMatcher = phonePattern.matcher(line);
            while (phoneMatcher.find()) {
                phoneNumbers.add(phoneMatcher.group());
            }

            // Extract website
            if (line.contains("www.") || line.contains("http")) {
                cardData.setWebsiteUrl(line);
            }

            // Extract pincode
            Pattern pincodePattern = Pattern.compile("\\b\\d{6}\\b");
            Matcher pincodeMatcher = pincodePattern.matcher(line);
            if (pincodeMatcher.find()) {
                cardData.setPincode(pincodeMatcher.group());
                
                // Usually city and state come before pincode
                String[] addressParts = line.split(",");
                if (addressParts.length > 1) {
                    cardData.setCity(addressParts[addressParts.length - 2].trim());
                }
                if (addressParts.length > 2) {
                    cardData.setState(addressParts[addressParts.length - 3].trim());
                }
            }
        }

        // Set the collected data
        cardData.setEmailAddresses(emailAddresses);
        cardData.setMobileNumbers(phoneNumbers);

        // Try to extract name and designation (usually first two lines)
        if (lines.length > 0) {
            cardData.setCardHolderName(lines[0].trim());
        }
        if (lines.length > 1) {
            cardData.setDesignation(lines[1].trim());
        }
        if (lines.length > 2) {
            cardData.setCompanyName(lines[2].trim());
        }

        return cardData;
    }
} 