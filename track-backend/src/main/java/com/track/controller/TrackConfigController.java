package com.track.controller;

import com.track.common.Result;
import com.track.entity.TrackConfig;
import com.track.service.TrackConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/track-config")
public class TrackConfigController {

    @Autowired
    private TrackConfigService trackConfigService;

    @GetMapping("/list")
    public Result<Page<TrackConfig>> list(
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return trackConfigService.list(eventType, keyword, pageNum, pageSize);
    }

    @GetMapping("/all")
    public Result<List<TrackConfig>> all() {
        return trackConfigService.all();
    }

    @GetMapping("/detail")
    public Result<TrackConfig> detail(@RequestParam Long id) {
        return trackConfigService.detail(id);
    }

    @PostMapping("/add")
    public Result<TrackConfig> add(@RequestBody TrackConfig config) {
        return trackConfigService.add(config);
    }

    @PostMapping("/update")
    public Result<TrackConfig> update(@RequestBody TrackConfig config) {
        return trackConfigService.update(config);
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody Map<String, Long> request) {
        return trackConfigService.delete(request.get("id"));
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        return trackConfigService.statistics();
    }
}
