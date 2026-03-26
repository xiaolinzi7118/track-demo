package com.track.controller;

import com.track.common.Result;
import com.track.entity.TrackData;
import com.track.service.TrackDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/track-data")
public class TrackDataController {

    @Autowired
    private TrackDataService trackDataService;

    @PostMapping("/report")
    public Result<Void> report(@RequestBody TrackData data) {
        return trackDataService.report(data);
    }

    @PostMapping("/batch-report")
    public Result<Void> batchReport(@RequestBody List<TrackData> dataList) {
        return trackDataService.batchReport(dataList);
    }

    @GetMapping("/list")
    public Result<Page<TrackData>> list(
            @RequestParam(required = false) String eventCode,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return trackDataService.list(eventCode, eventType, userId, pageNum, pageSize);
    }

    @GetMapping("/detail")
    public Result<TrackData> detail(@RequestParam Long id) {
        return trackDataService.detail(id);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        return trackDataService.statistics();
    }

    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> getTrendData() {
        return trackDataService.getTrendData();
    }
}
