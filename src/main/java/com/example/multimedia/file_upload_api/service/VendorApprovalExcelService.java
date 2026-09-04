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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds the one-workbook snapshot of an approved vendor's Become-a-Supplier application —
 * every field they filled in, plus a reference (not the file itself — see FolderItService's
 * download-link javadoc on why not) to every document/attachment they uploaded, resolvable by
 * whoever holds the FolderIt client id/secret. Uploaded into that vendor's own FolderIt folder
 * at approval time (see SupplierRegistrationService.provisionVendorAccount).
 *
 * Every sheet is a real table — one fixed set of column headers on row 1, one data row per
 * record beneath it — rather than label/value pairs, so this can be read programmatically (e.g.
 * imported into SAP) and not just by a person skimming it.
 */
@Service
public class VendorApprovalExcelService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    public byte[] buildApprovalExcel(SupplierRegistration reg, List<SupplierRegistrationDocument> docs,
                                      List<SupplierRegistrationAttachment> attachments, JSONArray dynamicAnswers,
                                      String folderItAccountUid) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = headerStyle(workbook);

            buildVendorMasterSheet(workbook, reg, headerStyle);
            buildDirectorsSheet(workbook, docs, headerStyle);
            buildMachinerySheet(workbook, reg, headerStyle);
            buildDocumentsSheet(workbook, docs, attachments, headerStyle, folderItAccountUid);
            buildQuestionnaireSheets(workbook, dynamicAnswers, attachments, headerStyle, folderItAccountUid);
            buildVerificationDetailsSheet(workbook, docs, headerStyle);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ── Sheet 1: one row for this vendor, every scalar field as its own column — a flat master
    // record the way SAP's own vendor-master tables (LFA1/LFB1/LFBK-style) are shaped, rather
    // than the field-per-row layout this used to be. Every column is always present (even blank)
    // so a downstream reader can rely on a fixed header row regardless of what this vendor filled in.

    private static final String[] VENDOR_MASTER_COLUMNS = {
            "Vendor Code", "Vendor Name", "Status", "Vendor Category", "Approved Date", "Address",
            "Contact Name", "Designation", "Login Email", "Phone",
            "Contact 1 Name", "Contact 1 Role", "Contact 1 Email", "Contact 1 Phone",
            "Contact 2 Name", "Contact 2 Role", "Contact 2 Email", "Contact 2 Phone",
            "Business Types", "Business Scope", "Company Type", "Telephone", "Fax", "Weekly Off",
            "Annual Turnover", "Turnover Year", "Regulatory Acts",
            "Manpower Office", "Manpower Supervisor", "Manpower Workmen", "Shifts Per Day",
            "Spare Capacity", "Floor Space", "Equipment Facilities",
            "GST Number", "PAN Number", "MSME Number", "CIN Number",
            "Beneficiary Name", "Account Number", "IFSC Code", "Bank Name",
            "ISO 9001 Certificate No", "ISO 9001 Certifying Body", "ISO 9001 Valid To",
            "ISO 14001 Certificate No", "ISO 14001 Certifying Body", "ISO 14001 Valid To",
            "ISO 45001 Certificate No", "ISO 45001 Certifying Body", "ISO 45001 Valid To",
            "ISO 27001 Certificate No", "ISO 27001 Certifying Body", "ISO 27001 Valid To",
            "AS9100D Certificate No", "AS9100D Certifying Body", "AS9100D Valid To",
            "NADCAP Certificate No", "NADCAP Expiry",
    };

    private void buildVendorMasterSheet(Workbook wb, SupplierRegistration reg, CellStyle headerStyle) {
        Sheet sheet = wb.createSheet("Vendor Master");
        headerRow(sheet, headerStyle, VENDOR_MASTER_COLUMNS);
        Row row = sheet.createRow(1);
        String[] values = {
                orEmpty(reg.getVendorCode()), orEmpty(reg.getVendorName()), orEmpty(reg.getStatus()),
                orEmpty(reg.getVendorCategory()), reg.getApprovedDate() != null ? reg.getApprovedDate().format(TS) : "",
                orEmpty(reg.getAddress()),
                orEmpty(reg.getContactName()), orEmpty(reg.getDesignation()), orEmpty(reg.getEmail()), orEmpty(reg.getPhone()),
                orEmpty(reg.getContact1Name()), orEmpty(reg.getContact1Role()), orEmpty(reg.getContact1Email()), orEmpty(reg.getContact1Phone()),
                orEmpty(reg.getContact2Name()), orEmpty(reg.getContact2Role()), orEmpty(reg.getContact2Email()), orEmpty(reg.getContact2Phone()),
                orEmpty(reg.getBusinessTypes()), orEmpty(reg.getBusinessScope()), orEmpty(reg.getCompanyType()),
                orEmpty(reg.getTelephone()), orEmpty(reg.getFax()), orEmpty(reg.getWeeklyOff()),
                orEmpty(reg.getAnnualTurnover()), orEmpty(reg.getTurnoverYear()), orEmpty(reg.getRegulatoryActs()),
                orEmpty(reg.getManpowerOffice()), orEmpty(reg.getManpowerSupervisor()), orEmpty(reg.getManpowerWorkmen()),
                orEmpty(reg.getShiftsPerDay()), orEmpty(reg.getSpareCapacity()), orEmpty(reg.getFloorSpace()),
                orEmpty(reg.getEquipmentFacilities()),
                orEmpty(reg.getGstNumber()), orEmpty(reg.getPanNumber()), orEmpty(reg.getMsmeNumber()), orEmpty(reg.getCinNumber()),
                orEmpty(reg.getBeneficiaryName()), orEmpty(reg.getAccountNumber()), orEmpty(reg.getIfscCode()), orEmpty(reg.getBankName()),
                orEmpty(reg.getIsoCertificateNo()), orEmpty(reg.getIsoCertifyingBody()), orEmpty(reg.getIsoExpiry()),
                orEmpty(reg.getIso14001CertificateNo()), orEmpty(reg.getIso14001CertifyingBody()), orEmpty(reg.getIso14001Expiry()),
                orEmpty(reg.getIso45001CertificateNo()), orEmpty(reg.getIso45001CertifyingBody()), orEmpty(reg.getIso45001Expiry()),
                orEmpty(reg.getIso27001CertificateNo()), orEmpty(reg.getIso27001CertifyingBody()), orEmpty(reg.getIso27001Expiry()),
                orEmpty(reg.getAs9100dCertificateNo()), orEmpty(reg.getAs9100dCertifyingBody()), orEmpty(reg.getAs9100dExpiry()),
                orEmpty(reg.getNadcapCertificateNo()), orEmpty(reg.getNadcapExpiry()),
        };
        for (int c = 0; c < values.length; c++) row.createCell(c).setCellValue(values[c]);
        autoSize(sheet, VENDOR_MASTER_COLUMNS.length);
    }

    // ── Sheet 2: directors — sourced from Microvista's CIN verification (run automatically
    // against the COI document at upload time), not typed in by anyone. MicrovistaService.
    // verifyCin stores one "Director {name} — PAN/DIN" detail per director it finds into that
    // document's verify_details_json; this just picks those back out. There's no separate
    // "add a director" form field anywhere in the app.

    private static final String DIRECTOR_LABEL_PREFIX = "Director ";
    private static final String DIRECTOR_LABEL_SUFFIX = " — PAN/DIN";

    private void buildDirectorsSheet(Workbook wb, List<SupplierRegistrationDocument> docs, CellStyle headerStyle) {
        Sheet sheet = wb.createSheet("Directors");
        String[] cols = {"Name", "PAN / DIN"};
        headerRow(sheet, headerStyle, cols);
        int r = 1;
        for (SupplierRegistrationDocument d : docs) {
            if (!"coi".equals(d.getDocType()) || d.getVerifyDetailsJson() == null) continue;
            JSONObject vd = new JSONObject(d.getVerifyDetailsJson());
            JSONArray details = vd.optJSONArray("details");
            if (details == null) continue;
            for (int i = 0; i < details.length(); i++) {
                JSONObject det = details.getJSONObject(i);
                String label = det.optString("label", "");
                if (!label.startsWith(DIRECTOR_LABEL_PREFIX) || !label.endsWith(DIRECTOR_LABEL_SUFFIX)) continue;
                String name = label.substring(DIRECTOR_LABEL_PREFIX.length(), label.length() - DIRECTOR_LABEL_SUFFIX.length());
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(name);
                row.createCell(1).setCellValue(det.optString("value", ""));
            }
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

    // ── Sheet 6: every field Microvista actually returned when verifying each document — not
    // just the pass/fail status shown elsewhere. Same verify_details_json each document already
    // stores (message + labeled details — see MicrovistaService.verify*), just flattened out.
    // Long/melted shape (Document, Field, Value) rather than one-column-per-field, since each
    // document type returns a different, variable-length set of fields — this keeps every row
    // under the same three headers instead of a ragged table.

    private void buildVerificationDetailsSheet(Workbook wb, List<SupplierRegistrationDocument> docs, CellStyle headerStyle) {
        Sheet sheet = wb.createSheet("Verification Details");
        String[] cols = {"Document", "Field", "Value"};
        headerRow(sheet, headerStyle, cols);
        int r = 1;
        for (SupplierRegistrationDocument d : docs) {
            if (d.getVerifyDetailsJson() == null) continue;
            String docName;
            try {
                docName = SupplierDocumentConfig.byId(d.getDocType()).name();
            } catch (IllegalArgumentException e) {
                docName = d.getDocType();
            }
            JSONObject vd = new JSONObject(d.getVerifyDetailsJson());
            String message = vd.optString("message", null);
            if (notBlank(message)) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(docName);
                row.createCell(1).setCellValue("Result");
                row.createCell(2).setCellValue(message);
            }
            JSONArray details = vd.optJSONArray("details");
            if (details != null) {
                for (int i = 0; i < details.length(); i++) {
                    JSONObject det = details.getJSONObject(i);
                    Row row = sheet.createRow(r++);
                    row.createCell(0).setCellValue(docName);
                    row.createCell(1).setCellValue(det.optString("label", ""));
                    row.createCell(2).setCellValue(det.optString("value", ""));
                }
            }
        }
        autoSize(sheet, cols.length);
    }

    // ── Sheet 5(+): dynamic questionnaire answers (QuestionnaireService.getAnswersForReview
    // shape). Every non-table question becomes one column on a single flat "Questionnaire
    // Answers" row (one vendor = one row), same flat-table shape as Vendor Master. Each
    // table-type question (e.g. "Head Office Details" with its own City/State/Country/Zip
    // Code/... columns) gets its own dedicated sheet, named after the question, with real
    // columns matching that question's own column defs and one row per table row the vendor
    // filled in — instead of every answer being squeezed into one "Answer" text cell.

    private void buildQuestionnaireSheets(Workbook wb, JSONArray answers, List<SupplierRegistrationAttachment> attachments,
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

        List<JSONObject> flat = new ArrayList<>();
        List<JSONObject> tables = new ArrayList<>();
        for (int i = 0; i < answers.length(); i++) {
            JSONObject a = answers.getJSONObject(i);
            (("table".equals(a.optString("questionType", ""))) ? tables : flat).add(a);
        }

        Set<String> usedSheetNames = new HashSet<>();

        // One sheet per questionnaire section (Form Studio's own grouping — sections.title via
        // questions.section_id) rather than one giant "Questionnaire Answers" sheet — same flat
        // one-row-per-vendor shape as before, just split so each sheet only has one section's
        // questions as columns.
        Map<String, List<JSONObject>> flatBySection = new LinkedHashMap<>();
        for (JSONObject a : flat) {
            flatBySection.computeIfAbsent(a.optString("sectionTitle", "Other"), k -> new ArrayList<>()).add(a);
        }
        for (Map.Entry<String, List<JSONObject>> entry : flatBySection.entrySet()) {
            Sheet sheet = wb.createSheet(uniqueSheetName(usedSheetNames, entry.getKey()));
            Row header = sheet.createRow(0);
            Row data = sheet.createRow(1);
            int col = 0;
            for (JSONObject a : entry.getValue()) {
                setHeaderCell(header, col, a.optString("prompt", ""), headerStyle);
                data.createCell(col).setCellValue(answerText(a));
                col++;
                SupplierRegistrationAttachment att = attachmentByQuestionId.get(a.optInt("questionId", -1));
                if (att != null && att.getFolderItFileUid() != null) {
                    setHeaderCell(header, col, a.optString("prompt", "") + " — FolderIt File UID", headerStyle);
                    data.createCell(col).setCellValue(att.getFolderItFileUid());
                    col++;
                    setHeaderCell(header, col, a.optString("prompt", "") + " — FolderIt API Reference", headerStyle);
                    data.createCell(col).setCellValue(folderItReference(att.getFolderItFileUid(), folderItAccountUid));
                    col++;
                }
            }
            autoSize(sheet, col);
        }

        for (JSONObject a : tables) {
            JSONArray columnLabels = a.optJSONArray("columnLabels");
            List<String> cols = new ArrayList<>();
            if (columnLabels != null) for (int i = 0; i < columnLabels.length(); i++) cols.add(columnLabels.getString(i));
            if (cols.isEmpty()) continue;

            Sheet sheet = wb.createSheet(uniqueSheetName(usedSheetNames, a.optString("prompt", "Table")));
            headerRow(sheet, headerStyle, cols.toArray(new String[0]));

            JSONArray rows = a.optJSONArray("rows");
            if (rows != null) {
                for (int i = 0; i < rows.length(); i++) {
                    JSONObject rowObj = rows.getJSONObject(i);
                    Row row = sheet.createRow(i + 1);
                    for (int c = 0; c < cols.size(); c++) row.createCell(c).setCellValue(rowObj.optString(cols.get(c), ""));
                }
            }
            autoSize(sheet, cols.size());
        }
    }

    private String answerText(JSONObject a) {
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
        return text != null ? text : "";
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

    private void headerRow(Sheet sheet, CellStyle style, String[] cols) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < cols.length; i++) setHeaderCell(row, i, cols[i], style);
    }

    private void setHeaderCell(Row row, int col, String value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    private void autoSize(Sheet sheet, int cols) {
        for (int i = 0; i < cols; i++) sheet.autoSizeColumn(i);
    }

    private boolean notBlank(String s) { return s != null && !s.isBlank(); }
    private String orEmpty(String s) { return s != null ? s : ""; }

    /** Excel sheet names: max 31 chars, no \ / ? * [ ] : — and must be unique within the workbook. */
    private String uniqueSheetName(Set<String> used, String rawName) {
        String cleaned = rawName.replaceAll("[\\\\/?*\\[\\]:]", " ").trim();
        if (cleaned.isEmpty()) cleaned = "Table";
        String base = cleaned.length() > 31 ? cleaned.substring(0, 31).trim() : cleaned;
        String candidate = base;
        int n = 2;
        while (!used.add(candidate)) {
            String suffix = " (" + n + ")";
            int cut = Math.max(0, Math.min(base.length(), 31 - suffix.length()));
            candidate = base.substring(0, cut) + suffix;
            n++;
        }
        return candidate;
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
}
