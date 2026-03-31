package com.track.service;

import com.track.common.Result;
import com.track.entity.TrackConfig;
import com.track.repository.TrackConfigRepository;
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
public class TrackConfigService {

    @Autowired
    private TrackConfigRepository trackConfigRepository;

    public Result<Page<TrackConfig>> list(String eventType, String keyword, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));

        Page<TrackConfig> page = trackConfigRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (eventType != null && !eventType.isEmpty()) {
                predicates.add(cb.equal(root.get("eventType"), eventType));
            }

            if (keyword != null && !keyword.isEmpty()) {
                predicates.add(cb.or(
                    cb.like(root.get("eventName"), "%" + keyword + "%"),
                    cb.like(root.get("eventCode"), "%" + keyword + "%")
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        return Result.success(page);
    }

    public Result<List<TrackConfig>> all() {
        return Result.success(trackConfigRepository.findAll());
    }

    public Result<TrackConfig> detail(Long id) {
        TrackConfig config = trackConfigRepository.findById(id).orElse(null);
        if (config == null) {
            return Result.error("配置不存在");
        }
        return Result.success(config);
    }

    public Result<TrackConfig> add(TrackConfig config) {
        config.setId(null); // 确保是新增操作
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        if (config.getStatus() == null) {
            config.setStatus(1);
        }
        return Result.success(trackConfigRepository.save(config));
    }

    public Result<TrackConfig> update(TrackConfig config) {
        TrackConfig existing = trackConfigRepository.findById(config.getId()).orElse(null);
        if (existing == null) {
            return Result.error("配置不存在");
        }
        config.setUpdateTime(LocalDateTime.now());
        config.setCreateTime(existing.getCreateTime());
        return Result.success(trackConfigRepository.save(config));
    }

    public Result<Void> delete(Long id) {
        trackConfigRepository.deleteById(id);
        return Result.success();
    }

    public Result<Map<String, Object>> statistics() {
        Map<String, Object> result = new HashMap<>();
        long total = trackConfigRepository.count();
        result.put("total", total);
        
        List<TrackConfig> all = trackConfigRepository.findAll();
        long pageViewCount = all.stream().filter(c -> "page_view".equals(c.getEventType())).count();
        long clickCount = all.stream().filter(c -> "click".equals(c.getEventType())).count();
        result.put("pageViewCount", pageViewCount);
        result.put("clickCount", clickCount);
        
        return Result.success(result);
    }

    public Result<Void> clearAll() {
        trackConfigRepository.deleteAll();
        return Result.success();
    }
}
