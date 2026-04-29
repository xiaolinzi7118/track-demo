package com.track.controller;

import com.track.common.Result;
import com.track.common.PermissionChecker;
import com.track.entity.ApiInterface;
import com.track.service.ApiInterfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/api-interface")
public class ApiInterfaceController {

    @Autowired
    private ApiInterfaceService apiInterfaceService;

    @Autowired
    private PermissionChecker permissionChecker;

    @GetMapping("/list")
    public Result<Page<ApiInterface>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        permissionChecker.checkPermission("api-interface:view");
        return apiInterfaceService.list(keyword, pageNum, pageSize);
    }

    @GetMapping("/all")
    public Result<List<ApiInterface>> all() {
        return apiInterfaceService.all();
    }

    @GetMapping("/detail")
    public Result<ApiInterface> detail(@RequestParam Long id) {
        permissionChecker.checkPermission("api-interface:view");
        return apiInterfaceService.detail(id);
    }

    @PostMapping("/add")
    public Result<ApiInterface> add(@RequestBody ApiInterface apiInterface) {
        permissionChecker.checkPermission("api-interface:add");
        return apiInterfaceService.add(apiInterface);
    }

    @PostMapping("/update")
    public Result<ApiInterface> update(@RequestBody ApiInterface apiInterface) {
        permissionChecker.checkPermission("api-interface:edit");
        return apiInterfaceService.update(apiInterface);
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody Map<String, Long> request) {
        permissionChecker.checkPermission("api-interface:delete");
        return apiInterfaceService.delete(request.get("id"));
    }

    @PostMapping("/import")
    public Result<Map<String, Object>> importTxt(@RequestParam("file") MultipartFile file) {
        permissionChecker.checkPermission("api-interface:add");
        return apiInterfaceService.importTxt(file);
    }

    @GetMapping("/referenced-paths")
    public Result<List<String>> referencedPaths() {
        return apiInterfaceService.getReferencedInterfacePaths();
    }
}
