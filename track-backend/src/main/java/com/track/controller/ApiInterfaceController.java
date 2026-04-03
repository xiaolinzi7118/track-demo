package com.track.controller;

import com.track.common.Result;
import com.track.entity.ApiInterface;
import com.track.service.ApiInterfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/api-interface")
public class ApiInterfaceController {

    @Autowired
    private ApiInterfaceService apiInterfaceService;

    @GetMapping("/list")
    public Result<Page<ApiInterface>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return apiInterfaceService.list(keyword, pageNum, pageSize);
    }

    @GetMapping("/all")
    public Result<List<ApiInterface>> all() {
        return apiInterfaceService.all();
    }

    @GetMapping("/detail")
    public Result<ApiInterface> detail(@RequestParam Long id) {
        return apiInterfaceService.detail(id);
    }

    @PostMapping("/add")
    public Result<ApiInterface> add(@RequestBody ApiInterface apiInterface) {
        return apiInterfaceService.add(apiInterface);
    }

    @PostMapping("/update")
    public Result<ApiInterface> update(@RequestBody ApiInterface apiInterface) {
        return apiInterfaceService.update(apiInterface);
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody Map<String, Long> request) {
        return apiInterfaceService.delete(request.get("id"));
    }

    @GetMapping("/referenced-paths")
    public Result<List<String>> referencedPaths() {
        return apiInterfaceService.getReferencedInterfacePaths();
    }
}
