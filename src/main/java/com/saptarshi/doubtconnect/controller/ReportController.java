package com.saptarshi.doubtconnect.controller;

import com.saptarshi.doubtconnect.entity.Report;
import com.saptarshi.doubtconnect.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping
    public ResponseEntity<Boolean> addReport(
            @RequestParam Long studentProfileId,
            @RequestParam Long teacherProfileId,
            @RequestParam String reason,
            @RequestParam(required = false) String description,
            Authentication authentication) {

        return ResponseEntity.ok(
                reportService.addReport(
                        studentProfileId,
                        teacherProfileId,
                        reason,
                        description,
                        authentication
                )
        );
    }

    @DeleteMapping
    public ResponseEntity<Boolean> removeReport(
            @RequestParam Long studentProfileId,
            @RequestParam Long teacherProfileId,
            Authentication authentication) {

        return ResponseEntity.ok(
                reportService.removeReport(
                        studentProfileId,
                        teacherProfileId,
                        authentication
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<Report>> getReports(
            @RequestParam Long studentProfileId,
            Authentication authentication) {

        return ResponseEntity.ok(
                reportService.getReports(
                        studentProfileId,
                        authentication
                )
        );
    }
    @GetMapping("/all")
    public List<Report> getAllReports() {
        return reportService.getAllReports();
    }

    @DeleteMapping("/admin/{reportId}")
    public boolean deleteReport(
            @PathVariable Long reportId) {

        return reportService.deleteReport(reportId);
    }
}