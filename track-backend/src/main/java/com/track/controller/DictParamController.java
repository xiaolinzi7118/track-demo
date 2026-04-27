package com.track.controller;

import com.track.common.PermissionChecker;
import com.track.common.Result;
import com.track.entity.DictParam;
import com.track.entity.DictParamItem;
import com.track.service.DictParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dict-param")
public class DictParamController {

    @Autowired
    private DictParamService dictParamService;

    @Autowired
    private PermissionChecker permissionChecker;

    @GetMapping("/list")
    public Result<Page<DictParam>> list(@RequestParam(required = false) String keyword,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        permissionChecker.checkAnyRole("developer");
        return dictParamService.list(keyword, pageNum, pageSize);
    }

    @GetMapping("/detail")
    public Result<DictParam> detail(@RequestParam Long id) {
        permissionChecker.checkAnyRole("developer");
        return dictParamService.detail(id);
    }

    @PostMapping("/add")
    public Result<DictParam> add(@RequestBody DictParam request) {
        permissionChecker.checkAnyRole("developer");
        return dictParamService.add(request);
    }

    @PostMapping("/update")
    public Result<DictParam> update(@RequestBody DictParam request) {
        permissionChecker.checkAnyRole("developer");
        return dictParamService.update(request);
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody Map<String, Long> request) {
        permissionChecker.checkAnyRole("developer");
        return dictParamService.delete(request.get("id"));
    }

    @PostMapping("/ids-list")
    public Result<List<DictParam>> idsList(@RequestBody Map<String, List<String>> request) {
        List<String> paramIds = request == null ? new ArrayList<>() : request.get("paramIds");
        return dictParamService.idsList(paramIds);
    }

    @GetMapping("/dept-options")
    public Result<List<DictParamItem>> deptOptions() {
        permissionChecker.checkPermission("system-user:view");
        return dictParamService.deptOptions();
    }
}
