package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.dto.gate.ProcessGateEntryDto;
import com.example.multimedia.file_upload_api.dto.gate.SupervisorReleaseDto;

import java.time.LocalDate;

public interface GateEntryService {
    ServiceResponse getExpectedArrivals(LocalDate date, String plant);
    ServiceResponse getArrivalDetails(String asnNumber);
    ServiceResponse processGateEntry(ProcessGateEntryDto processDto, String processedBy);
    ServiceResponse getGeneratedGatePasses(LocalDate date);
    ServiceResponse getGatePassDetails(String gatePassNumber);
    ServiceResponse getAuditLogs(LocalDate date);
    ServiceResponse getHeldVehiclesQueue();
    ServiceResponse releaseHeldVehicle(String asnNumber, SupervisorReleaseDto releaseDto, String processedBy);
    ServiceResponse getVendorGateStatus(String email);
    ServiceResponse getGateDiscrepancyReport(String asnNumber, String email);
}
