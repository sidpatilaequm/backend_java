package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.dto.gate.ProcessGateEntryDto;
import com.example.multimedia.file_upload_api.dto.gate.SupervisorReleaseDto;
import com.example.multimedia.file_upload_api.service.GateEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/employee/gate-entry")
public class EmployeeGateEntryController {

    @Autowired
    private GateEntryService gateEntryService;

    private String getProcessedBy() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName().equals("anonymousUser")) {
            return auth.getName();
        }
        return "System / Security Guard";
    }

    @GetMapping("/arrivals/expected")
    public ResponseEntity<ServiceResponse> getExpectedArrivals(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String plant) {
        if (date == null) date = LocalDate.now();
        return ResponseEntity.ok(gateEntryService.getExpectedArrivals(date, plant));
    }

    @GetMapping("/arrivals/{asnNumber}")
    public ResponseEntity<ServiceResponse> getArrivalDetails(@PathVariable String asnNumber) {
        return ResponseEntity.ok(gateEntryService.getArrivalDetails(asnNumber));
    }

    @PostMapping("/process")
    public ResponseEntity<ServiceResponse> processGateEntry(@RequestBody ProcessGateEntryDto processDto) {
        return ResponseEntity.ok(gateEntryService.processGateEntry(processDto, getProcessedBy()));
    }

    @GetMapping("/passes")
    public ResponseEntity<ServiceResponse> getGeneratedGatePasses(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) date = LocalDate.now();
        return ResponseEntity.ok(gateEntryService.getGeneratedGatePasses(date));
    }

    @GetMapping("/passes/{gatePassNumber}")
    public ResponseEntity<ServiceResponse> getSpecificGatePassDetails(@PathVariable String gatePassNumber) {
        return ResponseEntity.ok(gateEntryService.getGatePassDetails(gatePassNumber));
    }

    @GetMapping("/logs")
    public ResponseEntity<ServiceResponse> getAuditLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) date = LocalDate.now();
        return ResponseEntity.ok(gateEntryService.getAuditLogs(date));
    }

    @GetMapping("/hold-queue")
    public ResponseEntity<ServiceResponse> getHeldVehiclesQueue() {
        return ResponseEntity.ok(gateEntryService.getHeldVehiclesQueue());
    }

    @PostMapping("/hold-queue/{asnNumber}/release")
    public ResponseEntity<ServiceResponse> releaseHeldVehicle(
            @PathVariable String asnNumber,
            @RequestBody SupervisorReleaseDto releaseDto) {
        return ResponseEntity.ok(gateEntryService.releaseHeldVehicle(asnNumber, releaseDto, getProcessedBy()));
    }
}
