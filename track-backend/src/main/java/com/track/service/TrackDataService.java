package com.track.service;

import com.track.common.Result;
import com.track.entity.TrackData;
import com.track.repository.TrackDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class TrackDataService {

    @Autowired
    private TrackDataRepository trackDataRepository;

    public Result<Void> report(TrackData data) {
        data.setCreateTime(LocalDateTime.now());
        trackDataRepository.save(data);
        return Result.success();
    }

    public Result<Void> batchReport(List<TrackData> dataList) {
        for (TrackData data : dataList) {
            data.setCreateTime(LocalDateTime.now());
        }
        trackDataRepository.saveAll(dataList);
        return Result.success();
    }

    public Result<Page<TrackData>> list(String eventCode, String eventType, String userId, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "eventTime"));

        Page<TrackData> page = trackDataRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (eventCode != null && !eventCode.isEmpty()) {
                predicates.add(cb.equal(root.get("eventCode"), eventCode));
            }

            if (eventType != null && !eventType.isEmpty()) {
                predicates.add(cb.equal(root.get("eventType"), eventType));
            }

            if (userId != null && !userId.isEmpty()) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        return Result.success(page);
    }

    public Result<TrackData> detail(Long id) {
        TrackData data = trackDataRepository.findById(id).orElse(null);
        if (data == null) {
            return Result.error("数据不存在");
        }
        return Result.success(data);
    }

    public Result<Map<String, Object>> statistics() {
        Map<String, Object> result = new HashMap<>();
        long total = trackDataRepository.count();
        result.put("total", total);
        
        List<TrackData> all = trackDataRepository.findAll();
        long pageViewCount = all.stream().filter(d -> "page_view".equals(d.getEventType())).count();
        long clickCount = all.stream().filter(d -> "click".equals(d.getEventType())).count();
        result.put("pageViewCount", pageViewCount);
        result.put("clickCount", clickCount);
        
        long totalDuration = all.stream()
            .filter(d -> d.getDuration() != null)
            .mapToLong(TrackData::getDuration)
            .sum();
        result.put("totalDuration", totalDuration);
        
        return Result.success(result);
    }

    public Result<List<Map<String, Object>>> getTrendData() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<TrackData> all = trackDataRepository.findAll(Sort.by(Sort.Direction.ASC, "eventTime"));
        
        Map<String, Long> dailyCount = new HashMap<>();
        for (TrackData data : all) {
            if (data.getEventTime() != null) {
                String day = data.getEventTime().toLocalDate().toString();
                dailyCount.put(day, dailyCount.getOrDefault(day, 0L) + 1);
            }
        }
        
        for (Map.Entry<String, Long> entry : dailyCount.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", entry.getKey());
            item.put("count", entry.getValue());
            result.add(item);
        }
        
        return Result.success(result);
    }

    public Result<Void> clearAll() {
        trackDataRepository.deleteAll();
        return Result.success();
    }
}
