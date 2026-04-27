package com.track.controller;

import com.track.common.PermissionChecker;
import com.track.common.Result;
import com.track.entity.TrackAttribute;
import com.track.service.TrackAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attribute")
public class TrackAttributeController {

    @Autowired
    private TrackAttributeService trackAttributeService;

    @Autowired
    private PermissionChecker permissionChecker;

    @GetMapping("/list")
    public Result<Page<TrackAttribute>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String attributeType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        permissionChecker.checkPermission("attribute:view");
        return trackAttributeService.list(keyword, attributeType, pageNum, pageSize);
    }

    @GetMapping("/detail")
    public Result<TrackAttribute> detail(@RequestParam Long id) {
        permissionChecker.checkPermission("attribute:view");
        return trackAttributeService.detail(id);
    }

    @GetMapping("/all")
    public Result<List<TrackAttribute>> all() {
        permissionChecker.checkPermission("attribute:view");
        return trackAttributeService.all();
    }

    @PostMapping("/add")
    public Result<TrackAttribute> add(@RequestBody TrackAttribute request) {
        permissionChecker.checkPermission("attribute:add");
        return trackAttributeService.add(request);
    }

    @PostMapping("/update")
    public Result<TrackAttribute> update(@RequestBody TrackAttribute request) {
        permissionChecker.checkPermission("attribute:edit");
        return trackAttributeService.update(request);
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody Map<String, Long> request) {
        permissionChecker.checkPermission("attribute:delete");
        return trackAttributeService.delete(request.get("id"));
    }

    @PostMapping("/import")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) {
        permissionChecker.checkPermission("attribute:add");
        return trackAttributeService.importExcel(file);
    }
}
