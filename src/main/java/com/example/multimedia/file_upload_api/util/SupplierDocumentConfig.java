package com.example.multimedia.file_upload_api.util;

import java.util.List;
import java.util.Map;

/**
 * Java-side mirror of become-a-supplier/app/become-a-supplier/lib/data.ts's
 * DOCS constant — the single source of truth for which fields OCR should
 * extract per document type, and which Microvista call (if any) verifies
 * them. Field keys match the Next.js version exactly so the two stay
 * conceptually interchangeable; vendor_portal's own JS port
 * (src/components/supplier/data.js) mirrors this same list independently on
 * the frontend side.
 */
public final class SupplierDocumentConfig {

    public record FieldDef(String key, String label) {}

    public record DocDef(String id, String name, boolean required, List<FieldDef> fields, String verifyKind, List<String> doubleCheck) {}

    public static final List<DocDef> DOCS = List.of(
            new DocDef("coi", "Certificate of incorporation", true,
                    List.of(new FieldDef("cin", "CIN or LLPIN")), "cin", List.of()),
            new DocDef("pan", "PAN card", true,
                    List.of(new FieldDef("pan", "PAN")), "pan", List.of()),
            new DocDef("gst", "GST registration certificate", true,
                    List.of(new FieldDef("gstin", "GSTIN")), "gstin", List.of()),
            new DocDef("chq", "Cancelled cheque", true,
                    List.of(new FieldDef("benName", "Account name as printed"),
                            new FieldDef("acctNo", "Account number"),
                            new FieldDef("ifsc", "IFSC")),
                    "bank", List.of("acctNo", "ifsc")),
            new DocDef("udyam", "MSME / Udyam certificate", false,
                    List.of(new FieldDef("udyam", "Udyam registration number")), "udyam", List.of()),
            new DocDef("iso", "ISO 9001 certificate", false,
                    List.of(new FieldDef("isoNo", "Certificate number"),
                            new FieldDef("isoBody", "Certifying body"),
                            new FieldDef("isoExpiry", "Valid to")),
                    null, List.of()),
            new DocDef("as", "AS9100D certificate", false,
                    List.of(new FieldDef("asNo", "Certificate number"),
                            new FieldDef("asBody", "Certifying body"),
                            new FieldDef("asExpiry", "Valid to")),
                    null, List.of())
    );

    private static final Map<String, DocDef> BY_ID = DOCS.stream()
            .collect(java.util.stream.Collectors.toMap(DocDef::id, d -> d));

    public static DocDef byId(String docType) {
        DocDef d = BY_ID.get(docType);
        if (d == null) throw new IllegalArgumentException("Unknown document type: " + docType);
        return d;
    }

    private SupplierDocumentConfig() {}
}
