package com.track.service;

import com.track.common.PermissionChecker;
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
import java.util.*;

@Service
public class TrackDataService {

    @Autowired
    private TrackDataRepository trackDataRepository;

    @Autowired
    private PermissionChecker permissionChecker;

    @Autowired
    private DataPermissionService dataPermissionService;

    public Result<Void> report(TrackData data) {
        if (data.getDeptId() == null) {
            data.setDeptId(dataPermissionService.getDefaultDeptId());
        }
        data.setCreateTime(LocalDateTime.now());
        trackDataRepository.save(data);
        return Result.success();
    }

    public Result<Void> batchReport(List<TrackData> dataList) {
        Long defaultDeptId = dataPermissionService.getDefaultDeptId();
        for (TrackData data : dataList) {
            if (data.getDeptId() == null) {
                data.setDeptId(defaultDeptId);
            }
            data.setCreateTime(LocalDateTime.now());
        }
        trackDataRepository.saveAll(dataList);
        return Result.success();
    }

    public Result<Page<TrackData>> list(String eventCode, String eventType, String userId, Integer pageNum, Integer pageSize) {
        DataPermissionService.DataScope scope = currentScope();
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

            if (!scope.isAllData()) {
                if (scope.getDeptIds().isEmpty()) {
                    return cb.disjunction();
                }
                predicates.add(root.get("deptId").in(scope.getDeptIds()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        return Result.success(page);
    }

    public Result<TrackData> detail(Long id) {
        TrackData data = trackDataRepository.findById(id).orElse(null);
        if (data == null) {
            return Result.error("Data does not exist");
        }
        if (!canAccess(data.getDeptId(), currentScope())) {
            return Result.error("No permission to access this record");
        }
        return Result.success(data);
    }

    public Result<Map<String, Object>> statistics() {
        List<TrackData> all = findScopedData();

        Map<String, Object> result = new HashMap<>();
        result.put("total", (long) all.size());

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
        List<TrackData> all = findScopedData();
        all.sort(Comparator.comparing(TrackData::getEventTime, Comparator.nullsLast(Comparator.naturalOrder())));

        Map<String, Long> dailyCount = new LinkedHashMap<>();
        for (TrackData data : all) {
            if (data.getEventTime() != null) {
                String day = data.getEventTime().toLocalDate().toString();
                dailyCount.put(day, dailyCount.getOrDefault(day, 0L) + 1);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : dailyCount.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", entry.getKey());
            item.put("count", entry.getValue());
            result.add(item);
        }

        return Result.success(result);
    }

    private List<TrackData> findScopedData() {
        DataPermissionService.DataScope scope = currentScope();
        if (scope.isAllData()) {
            return trackDataRepository.findAll();
        }
        if (scope.getDeptIds().isEmpty()) {
            return new ArrayList<>();
        }
        return trackDataRepository.findAll((root, query, cb) -> root.get("deptId").in(scope.getDeptIds()));
    }

    private DataPermissionService.DataScope currentScope() {
        Long userId = permissionChecker.getCurrentUserId();
        return dataPermissionService.getDataScope(userId);
    }

    private boolean canAccess(Long deptId, DataPermissionService.DataScope scope) {
        if (scope.isAllData()) {
            return true;
        }
        return deptId != null && scope.getDeptIds().contains(deptId);
    }
}
