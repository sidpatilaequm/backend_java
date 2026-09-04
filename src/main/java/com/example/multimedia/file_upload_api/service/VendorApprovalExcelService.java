package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.entity.SupplierRegistration;
import com.example.multimedia.file_upload_api.entity.SupplierRegistrationAttachment;
import com.example.multimedia.file_upload_api.entity.SupplierRegistrationDocument;
import com.example.multimedia.file_upload_api.util.SupplierDocumentConfig;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the one-workbook snapshot of an approved vendor's Become-a-Supplier application —
 * every field they filled in, plus a reference (not the file itself — see FolderItService's
 * download-link javadoc on why not) to every document/attachment they uploaded, resolvable by
 * whoever holds the FolderIt client id/secret. Uploaded into that vendor's own FolderIt folder
 * at approval time (see SupplierRegistrationService.provisionVendorAccount).
 */
@Service
public class VendorApprovalExcelService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    public byte[] buildApprovalExcel(SupplierRegistration reg, List<SupplierRegistrationDocument> docs,
                                      List<SupplierRegistrationAttachment> attachments, JSONArray dynamicAnswers,
                                      String folderItAccountUid) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle sectionStyle = sectionStyle(workbook);
            CellStyle headerStyle = headerStyle(workbook);
            CellStyle labelStyle = labelStyle(workbook);

            buildVendorDetailsSheet(workbook, reg, sectionStyle, labelStyle);
            buildDirectorsSheet(workbook, reg, headerStyle);
            buildMachinerySheet(workbook, reg, headerStyle);
            buildDocumentsSheet(workbook, docs, attachments, headerStyle, folderItAccountUid);
            buildQuestionnaireSheet(workbook, dynamicAnswers, attachments, headerStyle, folderItAccountUid);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ── Sheet 1: every scalar field the vendor filled in, as Field | Value ────────────────

    private void buildVendorDetailsSheet(Workbook wb, SupplierRegistration reg, CellStyle sectionStyle, CellStyle labelStyle) {
        Sheet sheet = wb.createSheet("Vendor Details");
        int[] r = {0};

        section(sheet, r, sectionStyle, "Vendor");
        kv(sheet, r, labelStyle, "Vendor Code", reg.getVendorCode());
        kv(sheet, r, labelStyle, "Vendor Name", reg.getVendorName());
        kv(sheet, r, labelStyle, "Status", reg.getStatus());
        kv(sheet, r, labelStyle, "Vendor Category", reg.getVendorCategory());
        kv(sheet, r, labelStyle, "Approved Date", reg.getApprovedDate() != null ? reg.getApprovedDate().format(TS) : "");
        kv(sheet, r, labelStyle, "Address", reg.getAddress());

        section(sheet, r, sectionStyle, "Primary Contact");
        kv(sheet, r, labelStyle, "Contact Name", reg.getContactName());
        kv(sheet, r, labelStyle, "Designation", reg.getDesignation());
        kv(sheet, r, labelStyle, "Login Email", reg.getEmail());
        kv(sheet, r, labelStyle, "Phone", reg.getPhone());

        section(sheet, r, sectionStyle, "Contact 1");
        kv(sheet, r, labelStyle, "Name", reg.getContact1Name());
        kv(sheet, r, labelStyle, "Role", reg.getContact1Role());
        kv(sheet, r, labelStyle, "Email", reg.getContact1Email());
        kv(sheet, r, labelStyle, "Phone", reg.getContact1Phone());

        if (notBlank(reg.getContact2Name()) || notBlank(reg.getContact2Email())) {
            section(sheet, r, sectionStyle, "Contact 2");
            kv(sheet, r, labelStyle, "Name", reg.getContact2Name());
            kv(sheet, r, labelStyle, "Role", reg.getContact2Role());
            kv(sheet, r, labelStyle, "Email", reg.getContact2Email());
            kv(sheet, r, labelStyle, "Phone", reg.getContact2Phone());
        }

        section(sheet, r, sectionStyle, "Business Profile");
        kv(sheet, r, labelStyle, "Business Types", reg.getBusinessTypes());
        kv(sheet, r, labelStyle, "Business Scope", reg.getBusinessScope());
        kv(sheet, r, labelStyle, "Company Type", reg.getCompanyType());
        kv(sheet, r, labelStyle, "Telephone", reg.getTelephone());
        kv(sheet, r, labelStyle, "Fax", reg.getFax());
        kv(sheet, r, labelStyle, "Weekly Off", reg.getWeeklyOff());
        kv(sheet, r, labelStyle, "Annual Turnover", reg.getAnnualTurnover());
        kv(sheet, r, labelStyle, "Turnover Year", reg.getTurnoverYear());
        kv(sheet, r, labelStyle, "Regulatory Acts", reg.getRegulatoryActs());
        kv(sheet, r, labelStyle, "Manpower — Office", reg.getManpowerOffice());
        kv(sheet, r, labelStyle, "Manpower — Supervisor", reg.getManpowerSupervisor());
        kv(sheet, r, labelStyle, "Manpower — Workmen", reg.getManpowerWorkmen());
        kv(sheet, r, labelStyle, "Shifts Per Day", reg.getShiftsPerDay());
        kv(sheet, r, labelStyle, "Spare Capacity", reg.getSpareCapacity());
        kv(sheet, r, labelStyle, "Floor Space", reg.getFloorSpace());
        kv(sheet, r, labelStyle, "Equipment / Facilities", reg.getEquipmentFacilities());

        section(sheet, r, sectionStyle, "KYC");
        kv(sheet, r, labelStyle, "GST Number", reg.getGstNumber());
        kv(sheet, r, labelStyle, "PAN Number", reg.getPanNumber());
        kv(sheet, r, labelStyle, "MSME / Udyam Number", reg.getMsmeNumber());
        kv(sheet, r, labelStyle, "CIN Number", reg.getCinNumber());

        section(sheet, r, sectionStyle, "Bank");
        kv(sheet, r, labelStyle, "Beneficiary Name", reg.getBeneficiaryName());
        kv(sheet, r, labelStyle, "Account Number", reg.getAccountNumber());
        kv(sheet, r, labelStyle, "IFSC Code", reg.getIfscCode());
        kv(sheet, r, labelStyle, "Bank Name", reg.getBankName());

        section(sheet, r, sectionStyle, "Certifications");
        cert(sheet, r, labelStyle, "ISO 9001", reg.getIsoCertificateNo(), reg.getIsoCertifyingBody(), reg.getIsoExpiry());
        cert(sheet, r, labelStyle, "ISO 14001", reg.getIso14001CertificateNo(), reg.getIso14001CertifyingBody(), reg.getIso14001Expiry());
        cert(sheet, r, labelStyle, "ISO 45001", reg.getIso45001CertificateNo(), reg.getIso45001CertifyingBody(), reg.getIso45001Expiry());
        cert(sheet, r, labelStyle, "ISO 27001", reg.getIso27001CertificateNo(), reg.getIso27001CertifyingBody(), reg.getIso27001Expiry());
        cert(sheet, r, labelStyle, "AS9100D", reg.getAs9100dCertificateNo(), reg.getAs9100dCertifyingBody(), reg.getAs9100dExpiry());
        kv(sheet, r, labelStyle, "NADCAP — Certificate No", reg.getNadcapCertificateNo());
        kv(sheet, r, labelStyle, "NADCAP — Expiry", reg.getNadcapExpiry());

        sheet.setColumnWidth(0, 32 * 256);
        sheet.setColumnWidth(1, 60 * 256);
    }

    private void cert(Sheet sheet, int[] r, CellStyle labelStyle, String name, String no, String body, String expiry) {
        if (!notBlank(no) && !notBlank(body) && !notBlank(expiry)) return;
        kv(sheet, r, labelStyle, name + " — Certificate No", no);
        kv(sheet, r, labelStyle, name + " — Certifying Body", body);
        kv(sheet, r, labelStyle, name + " — Valid To", expiry);
    }

    // ── Sheet 2: directorsJson ([{name, qualification, experience, commencementDate, capitalEmployed}]) ──

    private void buildDirectorsSheet(Workbook wb, SupplierRegistration reg, CellStyle headerStyle) {
        Sheet sheet = wb.createSheet("Directors");
        String[] cols = {"Name", "Qualification", "Experience", "Commencement Date", "Capital Employed"};
        String[] keys = {"name", "qualification", "experience", "commencementDate", "capitalEmployed"};
        headerRow(sheet, headerStyle, cols);
        JSONArray rows = parseArray(reg.getDirectorsJson());
        for (int i = 0; i < rows.length(); i++) {
            JSONObject o = rows.getJSONObject(i);
            Row row = sheet.createRow(i + 1);
            for (int c = 0; c < keys.length; c++) row.createCell(c).setCellValue(o.optString(keys[c], ""));
        }
        autoSize(sheet, cols.length);
    }

    // ── Sheet 3: machineryJson ([{description, capacity, makeName, makeYear, numbers, remarks}]) ──

    private void buildMachinerySheet(Workbook wb, SupplierRegistration reg, CellStyle headerStyle) {
        Sheet sheet = wb.createSheet("Machinery");
        String[] cols = {"Description", "Capacity", "Make Name", "Make Year", "Numbers", "Remarks"};
        String[] keys = {"description", "capacity", "makeName", "makeYear", "numbers", "remarks"};
        headerRow(sheet, headerStyle, cols);
        JSONArray rows = parseArray(reg.getMachineryJson());
        for (int i = 0; i < rows.length(); i++) {
            JSONObject o = rows.getJSONObject(i);
            Row row = sheet.createRow(i + 1);
            for (int c = 0; c < keys.length; c++) row.createCell(c).setCellValue(o.optString(keys[c], ""));
        }
        autoSize(sheet, cols.length);
    }

    // ── Sheet 4: every fixed document + free-form attachment, with a FolderIt reference ───

    private void buildDocumentsSheet(Workbook wb, List<SupplierRegistrationDocument> docs,
                                      List<SupplierRegistrationAttachment> attachments, CellStyle headerStyle,
                                      String folderItAccountUid) {
        Sheet sheet = wb.createSheet("Documents");
        String[] cols = {"Document", "File Name", "Verify Status", "Uploaded", "FolderIt File UID", "FolderIt API Reference"};
        headerRow(sheet, headerStyle, cols);
        int r = 1;
        for (SupplierRegistrationDocument d : docs) {
            String docName;
            try {
                docName = SupplierDocumentConfig.byId(d.getDocType()).name();
            } catch (IllegalArgumentException e) {
                docName = d.getDocType();
            }
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(docName);
            row.createCell(1).setCellValue(orEmpty(d.getFileName()));
            row.createCell(2).setCellValue(orEmpty(d.getVerifyStatus()));
            row.createCell(3).setCellValue(d.getCreatedDate() != null ? d.getCreatedDate().format(TS) : "");
            row.createCell(4).setCellValue(orEmpty(d.getFolderItFileUid()));
            row.createCell(5).setCellValue(folderItReference(d.getFolderItFileUid(), folderItAccountUid));
        }
        for (SupplierRegistrationAttachment a : attachments) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(a.getQuestionId() != null ? "Questionnaire attachment" : "Other document");
            row.createCell(1).setCellValue(orEmpty(a.getFileName()));
            row.createCell(2).setCellValue("");
            row.createCell(3).setCellValue(a.getCreatedDate() != null ? a.getCreatedDate().format(TS) : "");
            row.createCell(4).setCellValue(orEmpty(a.getFolderItFileUid()));
            row.createCell(5).setCellValue(folderItReference(a.getFolderItFileUid(), folderItAccountUid));
        }
        autoSize(sheet, cols.length);
    }

    private String folderItReference(String fileUid, String accountUid) {
        if (fileUid == null) return "";
        // A stable identifier, not a working link — FolderIt's actual download URL is a
        // presigned S3 link valid for ~60 seconds, so it can't be baked into a static file.
        // Whoever holds the FolderIt client id/secret exchanges those for a bearer token and
        // calls this same endpoint to resolve it (see FolderItService.getDownloadUrl).
        return "https://api.folderit.com/v2/accounts/" + accountUid + "/files/" + fileUid + "/download";
    }

    // ── Sheet 5: dynamic questionnaire answers (QuestionnaireService.getAnswersForReview shape) ──

    private void buildQuestionnaireSheet(Workbook wb, JSONArray answers, List<SupplierRegistrationAttachment> attachments,
                                          CellStyle headerStyle, String folderItAccountUid) {
        if (answers == null || answers.isEmpty()) return;
        // A file_upload question's answer is answered by an attachment carrying that questionId
        // (see SupplierRegistrationAttachment.questionId) — same lookup QuestionnaireService.
        // getAnswersForReview uses to build previewUrl, but we want the FolderIt reference here
        // instead of our own app's proxy link.
        Map<Integer, SupplierRegistrationAttachment> attachmentByQuestionId = new HashMap<>();
        for (SupplierRegistrationAttachment a : attachments) {
            if (a.getQuestionId() != null) attachmentByQuestionId.put(a.getQuestionId(), a);
        }

        Sheet sheet = wb.createSheet("Questionnaire Answers");
        String[] cols = {"Question", "Answer", "FolderIt File UID", "FolderIt API Reference"};
        headerRow(sheet, headerStyle, cols);
        int r = 1;
        for (int i = 0; i < answers.length(); i++) {
            JSONObject a = answers.getJSONObject(i);
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(a.optString("prompt", ""));
            row.createCell(1).setCellValue(answerText(a));
            SupplierRegistrationAttachment att = attachmentByQuestionId.get(a.optInt("questionId", -1));
            if (att != null && att.getFolderItFileUid() != null) {
                row.createCell(2).setCellValue(att.getFolderItFileUid());
                row.createCell(3).setCellValue(folderItReference(att.getFolderItFileUid(), folderItAccountUid));
            }
        }
        autoSize(sheet, cols.length);
    }

    private String answerText(JSONObject a) {
        String type = a.optString("questionType", "");
        if ("table".equals(type)) {
            JSONArray rows = a.optJSONArray("rows");
            if (rows == null || rows.isEmpty()) return "(not answered)";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < rows.length(); i++) {
                JSONObject row = rows.getJSONObject(i);
                if (i > 0) sb.append(" | ");
                sb.append(row.keySet().stream()
                        .map(k -> k + ": " + row.optString(k, ""))
                        .reduce((x, y) -> x + ", " + y).orElse(""));
            }
            return sb.toString();
        }
        JSONArray labels = a.optJSONArray("selectedLabels");
        if (labels != null && !labels.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < labels.length(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(labels.getString(i));
            }
            return sb.toString();
        }
        String text = a.optString("textValue", null);
        return text != null ? text : "(not answered)";
    }

    // ── small helpers ──────────────────────────────────────────────────────────────────────

    private JSONArray parseArray(String json) {
        if (json == null || json.isBlank()) return new JSONArray();
        try {
            return new JSONArray(json);
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    private void section(Sheet sheet, int[] r, CellStyle style, String title) {
        Row row = sheet.createRow(r[0]++);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(style);
    }

    private void kv(Sheet sheet, int[] r, CellStyle labelStyle, String label, String value) {
        if (!notBlank(value)) return;
        Row row = sheet.createRow(r[0]++);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);
        row.createCell(1).setCellValue(value);
    }

    private void headerRow(Sheet sheet, CellStyle style, String[] cols) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < cols.length; i++) {
            Cell c = row.createCell(i);
            c.setCellValue(cols[i]);
            c.setCellStyle(style);
        }
    }

    private void autoSize(Sheet sheet, int cols) {
        for (int i = 0; i < cols; i++) sheet.autoSizeColumn(i);
    }

    private boolean notBlank(String s) { return s != null && !s.isBlank(); }
    private String orEmpty(String s) { return s != null ? s : ""; }

    private CellStyle sectionStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle headerStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle labelStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }
}
