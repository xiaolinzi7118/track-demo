package com.track.controller;

import com.track.common.PermissionChecker;
import com.track.common.Result;
import com.track.dto.RequirementCreateRequest;
import com.track.dto.RequirementResubmitRequest;
import com.track.dto.RequirementStatusChangeRequest;
import com.track.entity.TrackRequirement;
import com.track.service.RequirementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requirement")
public class RequirementController {

    @Autowired
    private RequirementService requirementService;

    @Autowired
    private PermissionChecker permissionChecker;

    @GetMapping("/list")
    public Result<Page<TrackRequirement>> list(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String statusList,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String proposerName,
            @RequestParam(required = false) String businessLineCode,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        permissionChecker.checkPermission("requirement-manage:view");
        return requirementService.list(title, statusList, priority, proposerName, businessLineCode, department,
                sortField, sortOrder, pageNum, pageSize);
    }

    @GetMapping("/dashboard-statistics")
    public Result<Map<String, Object>> dashboardStatistics() {
        permissionChecker.checkPermission("requirement-manage:view");
        return requirementService.dashboardStatistics();
    }

    @GetMapping("/dashboard-trend")
    public Result<List<Map<String, Object>>> dashboardTrend(@RequestParam(defaultValue = "7") Integer days) {
        permissionChecker.checkPermission("requirement-manage:view");
        return requirementService.dashboardTrend(days);
    }

    @GetMapping("/detail")
    public Result<TrackRequirement> detail(@RequestParam String requirementId) {
        permissionChecker.checkPermission("requirement-manage:view");
        return requirementService.detail(requirementId);
    }

    @PostMapping("/add")
    public Result<TrackRequirement> add(@RequestBody RequirementCreateRequest request) {
        permissionChecker.checkPermission("requirement-manage:add");
        return requirementService.add(request);
    }

    @PostMapping("/status-change")
    public Result<TrackRequirement> changeStatus(@RequestBody RequirementStatusChangeRequest request) {
        permissionChecker.checkPermission("requirement-manage:status");
        return requirementService.changeStatus(request);
    }

    @PostMapping("/resubmit")
    public Result<TrackRequirement> resubmit(@RequestBody RequirementResubmitRequest request) {
        permissionChecker.checkPermission("requirement-manage:resubmit");
        return requirementService.resubmit(request);
    }
}
