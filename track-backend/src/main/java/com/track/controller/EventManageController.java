package com.track.controller;

import com.track.common.PermissionChecker;
import com.track.common.Result;
import com.track.entity.TrackConfig;
import com.track.service.TrackConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/event-manage")
public class EventManageController {

    @Autowired
    private TrackConfigService trackConfigService;

    @Autowired
    private PermissionChecker permissionChecker;

    @GetMapping("/list")
    public Result<Page<TrackConfig>> list(
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        permissionChecker.checkPermission("event-manage:view");
        return trackConfigService.list(eventType, keyword, pageNum, pageSize);
    }

    @GetMapping("/all")
    public Result<List<TrackConfig>> all() {
        return trackConfigService.all();
    }

    @GetMapping("/detail")
    public Result<TrackConfig> detail(@RequestParam Long id) {
        permissionChecker.checkPermission("event-manage:view");
        return trackConfigService.detail(id);
    }

    @GetMapping("/requirement-options")
    public Result<List<Map<String, Object>>> requirementOptions(@RequestParam(required = false) String keyword) {
        permissionChecker.checkPermission("event-manage:view");
        return trackConfigService.requirementOptions(keyword);
    }

    @PostMapping("/add")
    public Result<TrackConfig> add(@RequestBody TrackConfig config) {
        permissionChecker.checkPermission("event-manage:add");
        return trackConfigService.add(config);
    }

    @PostMapping("/update")
    public Result<TrackConfig> update(@RequestBody TrackConfig config) {
        permissionChecker.checkPermission("event-manage:edit");
        return trackConfigService.update(config);
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody Map<String, Long> request) {
        permissionChecker.checkPermission("event-manage:delete");
        return trackConfigService.delete(request.get("id"));
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        permissionChecker.checkPermission("event-manage:view");
        return trackConfigService.statistics();
    }
}
