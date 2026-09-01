package com.example.multimedia.file_upload_api.service.impl;

import com.example.multimedia.file_upload_api.dto.VendorQuotationRequest;
import com.example.multimedia.file_upload_api.dto.VendorQuotationResponse;
import com.example.multimedia.file_upload_api.dto.VendorQuotationComparisonResponse;
import com.example.multimedia.file_upload_api.entity.*;
import com.example.multimedia.file_upload_api.repository.*;
import com.example.multimedia.file_upload_api.service.VendorQuotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VendorQuotationServiceImpl implements VendorQuotationService {

    @Autowired
    private VendorQuotationRepository quotationRepository;

    @Autowired
    private PurchaseRequisitionRepository prRepository;

    @Autowired
    private PurchaseRequisitionItemRepository prItemRepository;

    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    @Autowired
    private FinancialTermsRepository financialTermsRepository;

    @Autowired
    private PurchaseRequisitionItemVendorRepository prItemVendorRepository;

    @Override
    @Transactional
    public VendorQuotationResponse createQuotation(VendorQuotationRequest request, Long vendorId) {
        if (quotationRepository.existsByPurchaseRequisition_IdAndVendor_CompanyId(request.getPrId(), vendorId)) {
            throw new IllegalArgumentException("Quotation already submitted for this Purchase Requisition");
        }

        List<PurchaseRequisitionItemVendor> acceptedAssignments = prItemVendorRepository.findAcceptedByVendorIdAndPrId(vendorId, request.getPrId());
        if (acceptedAssignments == null || acceptedAssignments.isEmpty()) {
            throw new IllegalArgumentException("You cannot submit a quotation for this Purchase Requisition because you have not accepted it.");
        }

        CompanyDetails vendor = companyDetailsRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        PurchaseRequisition pr = prRepository.findById(request.getPrId())
                .orElseThrow(() -> new RuntimeException("Purchase Requisition not found"));

        VendorQuotation quotation = new VendorQuotation();
        quotation.setPurchaseRequisition(pr);
        quotation.setVendor(vendor);
        quotation.setCompanyCode(pr.getCompanyCode());

        if (request.getQuotationHeader() != null) {
            quotation.setQuotationNumber(request.getQuotationHeader().getQuotationNumber());
            quotation.setQuotationDate(request.getQuotationHeader().getQuotationDate());
            quotation.setVendorReferenceNo(request.getQuotationHeader().getVendorReferenceNo());
            quotation.setCurrency(request.getQuotationHeader().getCurrency());
            quotation.setValidityDays(request.getQuotationHeader().getValidityDays());
            quotation.setValidUntil(request.getQuotationHeader().getValidUntil());
        }

        if (request.getPaymentTerms() != null) {
            quotation.setPaymentTermsId(request.getPaymentTerms().getPaymentTermsId());
            quotation.setAdvanceRequiredPercent(request.getPaymentTerms().getAdvanceRequiredPercent());
            quotation.setBankGuaranteeRequired(request.getPaymentTerms().getBankGuaranteeRequired());
        }

        if (request.getDeliveryDetails() != null) {
            quotation.setIncoterm(request.getDeliveryDetails().getIncoterm());
            quotation.setNamedPlace(request.getDeliveryDetails().getNamedPlace());
            quotation.setQuotedDeliveryDate(request.getDeliveryDetails().getQuotedDeliveryDate());
            quotation.setLeadTimeDays(request.getDeliveryDetails().getLeadTimeDays());
            quotation.setShippingMode(request.getDeliveryDetails().getShippingMode());
        }

        if (request.getFreightDetails() != null) {
            quotation.setFreightChargeType(request.getFreightDetails().getFreightChargeType());
            quotation.setFreightAmount(request.getFreightDetails().getFreightAmount());
        }

        if (request.getRemarks() != null) {
            quotation.setCoverNote(request.getRemarks().getCoverNote());
            quotation.setInternalNotes(request.getRemarks().getInternalNotes());
        }

        quotation.setStatus("SUBMITTED");

        BigDecimal subtotalAmount = BigDecimal.ZERO;
        BigDecimal gstTotalAmount = BigDecimal.ZERO;
        BigDecimal itemFreightTotal = BigDecimal.ZERO;

        if (request.getLineItems() != null) {
            for (VendorQuotationRequest.LineItem itemReq : request.getLineItems()) {
                PurchaseRequisitionItem prItem = prItemRepository.findById(itemReq.getPrLineId())
                        .orElseThrow(() -> new RuntimeException("PR Item not found"));

                VendorQuotationItem item = new VendorQuotationItem();
                item.setVendorQuotation(quotation);
                item.setPurchaseRequisitionItem(prItem);
                item.setItemCode(itemReq.getItemCode());
                item.setDescription(itemReq.getDescription());
                item.setPrQty(itemReq.getPrQty());
                item.setQuotedQty(itemReq.getQuotedQty() != null ? itemReq.getQuotedQty() : BigDecimal.ZERO);
                item.setUom(itemReq.getUom());
                item.setUnitPrice(itemReq.getUnitPrice() != null ? itemReq.getUnitPrice() : BigDecimal.ZERO);
                item.setGstPercent(itemReq.getGstPercent() != null ? itemReq.getGstPercent() : BigDecimal.ZERO);
                item.setDeliveryDate(itemReq.getDeliveryDate());
                item.setPaymentTermsId(itemReq.getPaymentTermsId());
                item.setIncoterm(itemReq.getIncoterm());
                item.setFreightAmount(itemReq.getFreightAmount() != null ? itemReq.getFreightAmount() : BigDecimal.ZERO);

                // Calculations
                BigDecimal lineTotal = item.getQuotedQty().multiply(item.getUnitPrice());
                item.setLineTotal(lineTotal);

                BigDecimal gstAmount = lineTotal.multiply(item.getGstPercent()).divide(new BigDecimal("100"));
                item.setGstAmount(gstAmount);

                subtotalAmount = subtotalAmount.add(lineTotal);
                gstTotalAmount = gstTotalAmount.add(gstAmount);
                itemFreightTotal = itemFreightTotal.add(item.getFreightAmount());

                quotation.getItems().add(item);
            }
        }

        quotation.setSubtotalAmount(subtotalAmount);
        quotation.setGstTotalAmount(gstTotalAmount);

        BigDecimal headerFreight = quotation.getFreightAmount() != null ? quotation.getFreightAmount() : BigDecimal.ZERO;
        BigDecimal grandTotal = subtotalAmount.add(gstTotalAmount).add(headerFreight).add(itemFreightTotal);
        quotation.setGrandTotalAmount(grandTotal);

        if (request.getDocuments() != null) {
            quotation.setQuotationPdf(request.getDocuments().getQuotationPdf());
            mapDocuments(request.getDocuments().getTechnicalSpecification(), "TECHNICAL_SPECIFICATION", quotation);
            mapDocuments(request.getDocuments().getQualityCertificate(), "QUALITY_CERTIFICATE", quotation);
            mapDocuments(request.getDocuments().getProductBrochure(), "PRODUCT_BROCHURE", quotation);
            mapDocuments(request.getDocuments().getOtherDocuments(), "OTHER_DOCUMENTS", quotation);
        }

        VendorQuotation saved = quotationRepository.save(quotation);
        return mapToResponse(saved);
    }

    private void mapDocuments(List<VendorQuotationRequest.DocumentMeta> docMetas, String type, VendorQuotation quotation) {
        if (docMetas != null) {
            for (VendorQuotationRequest.DocumentMeta meta : docMetas) {
                VendorQuotationDocument doc = new VendorQuotationDocument();
                doc.setVendorQuotation(quotation);
                doc.setDocumentType(type);
                doc.setFilePath(meta.getFilePath());
                doc.setFileName(meta.getFileName());
                doc.setFileSize(meta.getFileSize());
                doc.setFileType(meta.getFileType());
                quotation.getDocuments().add(doc);
            }
        }
    }

    @Override
    public List<VendorQuotationResponse> getQuotationsByVendorId(Long vendorId, String companyCode) {
        return quotationRepository.findByVendor_CompanyId(vendorId, companyCode).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public VendorQuotationResponse getQuotationById(Long id, Long vendorId) {
        VendorQuotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quotation not found"));
        if (!quotation.getVendor().getCompanyId().equals(vendorId)) {
            throw new RuntimeException("Unauthorized to view this quotation");
        }
        return mapToResponse(quotation);
    }

    @Override
    public VendorQuotationResponse getQuotationByIdForAdmin(Long id) {
        VendorQuotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quotation not found with ID: " + id));
        return mapToResponse(quotation);
    }

    @Override
    public VendorQuotationResponse getQuotationByQuotationNumber(String quotationNumber) {
        VendorQuotation quotation = quotationRepository.findByQuotationNumber(quotationNumber)
                .orElseThrow(() -> new RuntimeException("Quotation not found with number: " + quotationNumber));
        return mapToResponse(quotation);
    }

    @Override
    public VendorQuotationResponse getQuotationByQuotationNumberAndVendorId(String quotationNumber, Long vendorId) {
        VendorQuotation quotation = quotationRepository.findByQuotationNumber(quotationNumber)
                .orElseThrow(() -> new RuntimeException("Quotation not found with number: " + quotationNumber));
        if (!quotation.getVendor().getCompanyId().equals(vendorId)) {
            throw new RuntimeException("Unauthorized to view this quotation");
        }
        return mapToResponse(quotation);
    }

    @Override
    public List<VendorQuotationResponse> getAwardedQuotationsForAdmin(Long adminId) {
        return quotationRepository.findByStatusAndRequestedBy("AWARDED", adminId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VendorQuotationResponse> getAllQuotationsByVendorIdForAdmin(Long vendorId) {
        return quotationRepository.findByVendor_CompanyId(vendorId, null).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VendorQuotationResponse> getAllQuotationsByPrIdForAdmin(Long prId) {
        return quotationRepository.findByPurchaseRequisition_Id(prId).stream()
                .map(this::mapToResponse)
                .sorted((q1, q2) -> {
                    BigDecimal total1 = q1.getGrandTotal() != null ? q1.getGrandTotal() : BigDecimal.ZERO;
                    BigDecimal total2 = q2.getGrandTotal() != null ? q2.getGrandTotal() : BigDecimal.ZERO;
                    return total1.compareTo(total2);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void awardQuotation(Long quotationId, String remarks) {
        VendorQuotation winningQuotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new RuntimeException("Quotation not found with ID: " + quotationId));
        
        winningQuotation.setStatus("AWARDED");
        if (remarks != null && !remarks.trim().isEmpty()) {
            winningQuotation.setInternalNotes(remarks);
        }
        quotationRepository.save(winningQuotation);

        PurchaseRequisition pr = winningQuotation.getPurchaseRequisition();
        if (pr != null) {
            pr.setStatus(com.example.multimedia.file_upload_api.enums.PurchaseRequisitionStatus.QUOTATION_AWARDED);
            prRepository.save(pr);

            List<VendorQuotation> otherQuotations = quotationRepository.findByPurchaseRequisition_Id(pr.getId());
            for (VendorQuotation other : otherQuotations) {
                if (!other.getQuotationId().equals(quotationId)) {
                    other.setStatus("REJECTED");
                    quotationRepository.save(other);
                }
            }
        }
    }

    @Override
    public List<VendorQuotationComparisonResponse> getQuotationComparison(Long prId) {
        List<VendorQuotation> quotations = quotationRepository.findByPurchaseRequisition_Id(prId);
        
        quotations.sort((q1, q2) -> {
            BigDecimal total1 = q1.getGrandTotalAmount() != null ? q1.getGrandTotalAmount() : BigDecimal.ZERO;
            BigDecimal total2 = q2.getGrandTotalAmount() != null ? q2.getGrandTotalAmount() : BigDecimal.ZERO;
            return total1.compareTo(total2);
        });

        List<VendorQuotationComparisonResponse> comparisonList = new ArrayList<>();
        for (int i = 0; i < quotations.size(); i++) {
            VendorQuotation q = quotations.get(i);
            VendorQuotationComparisonResponse res = new VendorQuotationComparisonResponse();
            res.setRank(i + 1);
            res.setQuotationId(q.getQuotationId());
            res.setQuotationNumber(q.getQuotationNumber());
            res.setVendorId(q.getVendor().getCompanyId());
            res.setVendorName(q.getVendor().getCompanyName());
            res.setGrandTotal(q.getGrandTotalAmount());
            res.setDeliveryDays(q.getLeadTimeDays());
            res.setValidUntil(q.getValidUntil());
            res.setStatus(q.getStatus());
            
            if (q.getPaymentTermsId() != null) {
                financialTermsRepository.findById(q.getPaymentTermsId()).ifPresent(terms -> {
                    res.setPaymentTerms(terms.getTermsOfPayment());
                });
            }
            comparisonList.add(res);
        }
        return comparisonList;
    }

    private VendorQuotationResponse mapToResponse(VendorQuotation quotation) {
        VendorQuotationResponse res = new VendorQuotationResponse();
        res.setQuotationId(quotation.getQuotationId());
        res.setPrId(quotation.getPurchaseRequisition().getId());
        res.setVendorId(quotation.getVendor().getCompanyId());
        res.setStatus(quotation.getStatus());
        res.setSubtotal(quotation.getSubtotalAmount());
        res.setGstTotal(quotation.getGstTotalAmount());
        
        BigDecimal headerFreight = quotation.getFreightAmount() != null ? quotation.getFreightAmount() : BigDecimal.ZERO;
        BigDecimal itemFreight = quotation.getItems().stream()
                .map(i -> i.getFreightAmount() != null ? i.getFreightAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        res.setFreightTotal(headerFreight.add(itemFreight));
        
        res.setGrandTotal(quotation.getGrandTotalAmount());
        res.setCreatedAt(quotation.getCreatedDate());

        VendorQuotationRequest.QuotationHeader header = new VendorQuotationRequest.QuotationHeader();
        header.setQuotationNumber(quotation.getQuotationNumber());
        header.setQuotationDate(quotation.getQuotationDate());
        header.setVendorReferenceNo(quotation.getVendorReferenceNo());
        header.setCurrency(quotation.getCurrency());
        header.setValidityDays(quotation.getValidityDays());
        header.setValidUntil(quotation.getValidUntil());
        res.setQuotationHeader(header);

        VendorQuotationRequest.PaymentTerms payment = new VendorQuotationRequest.PaymentTerms();
        payment.setPaymentTermsId(quotation.getPaymentTermsId());
        payment.setAdvanceRequiredPercent(quotation.getAdvanceRequiredPercent());
        payment.setBankGuaranteeRequired(quotation.getBankGuaranteeRequired());
        res.setPaymentTerms(payment);

        VendorQuotationRequest.DeliveryDetails delivery = new VendorQuotationRequest.DeliveryDetails();
        delivery.setIncoterm(quotation.getIncoterm());
        delivery.setNamedPlace(quotation.getNamedPlace());
        delivery.setQuotedDeliveryDate(quotation.getQuotedDeliveryDate());
        delivery.setLeadTimeDays(quotation.getLeadTimeDays());
        delivery.setShippingMode(quotation.getShippingMode());
        res.setDeliveryDetails(delivery);

        VendorQuotationRequest.FreightDetails freight = new VendorQuotationRequest.FreightDetails();
        freight.setFreightChargeType(quotation.getFreightChargeType());
        freight.setFreightAmount(quotation.getFreightAmount());
        res.setFreightDetails(freight);

        VendorQuotationRequest.Remarks remarks = new VendorQuotationRequest.Remarks();
        remarks.setCoverNote(quotation.getCoverNote());
        remarks.setInternalNotes(quotation.getInternalNotes());
        res.setRemarks(remarks);

        List<VendorQuotationResponse.LineItemResponse> lines = quotation.getItems().stream().map(item -> {
            VendorQuotationResponse.LineItemResponse lineRes = new VendorQuotationResponse.LineItemResponse();
            lineRes.setQuotationItemId(item.getQuotationItemId());
            lineRes.setPrLineId(item.getPurchaseRequisitionItem().getId());
            lineRes.setItemCode(item.getItemCode());
            lineRes.setDescription(item.getDescription());
            lineRes.setPrQty(item.getPrQty());
            lineRes.setQuotedQty(item.getQuotedQty());
            lineRes.setUom(item.getUom());
            lineRes.setUnitPrice(item.getUnitPrice());
            lineRes.setGstPercent(item.getGstPercent());
            lineRes.setDeliveryDate(item.getDeliveryDate());
            lineRes.setPaymentTermsId(item.getPaymentTermsId());
            lineRes.setIncoterm(item.getIncoterm());
            lineRes.setFreightAmount(item.getFreightAmount());
            lineRes.setLineTotal(item.getLineTotal());
            lineRes.setGstAmount(item.getGstAmount());
            return lineRes;
        }).collect(Collectors.toList());
        res.setLineItems(lines);

        VendorQuotationRequest.Documents docs = new VendorQuotationRequest.Documents();
        docs.setQuotationPdf(quotation.getQuotationPdf());
        docs.setTechnicalSpecification(extractDocs(quotation.getDocuments(), "TECHNICAL_SPECIFICATION"));
        docs.setQualityCertificate(extractDocs(quotation.getDocuments(), "QUALITY_CERTIFICATE"));
        docs.setProductBrochure(extractDocs(quotation.getDocuments(), "PRODUCT_BROCHURE"));
        docs.setOtherDocuments(extractDocs(quotation.getDocuments(), "OTHER_DOCUMENTS"));
        res.setDocuments(docs);

        return res;
    }

    private List<VendorQuotationRequest.DocumentMeta> extractDocs(List<VendorQuotationDocument> docs, String type) {
        return docs.stream()
                .filter(d -> type.equals(d.getDocumentType()))
                .map(d -> {
                    VendorQuotationRequest.DocumentMeta meta = new VendorQuotationRequest.DocumentMeta();
                    meta.setFilePath(d.getFilePath());
                    meta.setFileName(d.getFileName());
                    meta.setFileSize(d.getFileSize());
                    meta.setFileType(d.getFileType());
                    return meta;
                }).collect(Collectors.toList());
    }
}
