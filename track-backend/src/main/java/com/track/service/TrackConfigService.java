package com.track.service;

import com.track.common.PermissionChecker;
import com.track.common.Result;
import com.track.entity.TrackConfig;
import com.track.entity.User;
import com.track.repository.TrackConfigRepository;
import com.track.repository.UserRepository;
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
public class TrackConfigService {

    @Autowired
    private TrackConfigRepository trackConfigRepository;

    @Autowired
    private PermissionChecker permissionChecker;

    @Autowired
    private DataPermissionService dataPermissionService;

    @Autowired
    private UserRepository userRepository;

    public Result<Page<TrackConfig>> list(String eventType, String keyword, Integer pageNum, Integer pageSize) {
        DataPermissionService.DataScope scope = currentScope();
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

    public Result<List<TrackConfig>> all() {
        DataPermissionService.DataScope scope = currentScope();
        if (scope.isAllData()) {
            return Result.success(trackConfigRepository.findAll());
        }
        if (scope.getDeptIds().isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        return Result.success(trackConfigRepository.findAll((root, query, cb) -> root.get("deptId").in(scope.getDeptIds())));
    }

    public Result<TrackConfig> detail(Long id) {
        TrackConfig config = trackConfigRepository.findById(id).orElse(null);
        if (config == null) {
            return Result.error("Config does not exist");
        }
        if (!canAccess(config.getDeptId(), currentScope())) {
            return Result.error("No permission to access this record");
        }
        return Result.success(config);
    }

    public Result<TrackConfig> add(TrackConfig config) {
        Long deptId = resolveWriteDeptId(config.getDeptId());
        if (deptId == null) {
            return Result.error("No writable department found");
        }
        config.setId(null);
        config.setDeptId(deptId);
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
            return Result.error("Config does not exist");
        }
        if (!canAccess(existing.getDeptId(), currentScope())) {
            return Result.error("No permission to modify this record");
        }

        Long deptId = resolveWriteDeptId(config.getDeptId());
        if (deptId == null) {
            deptId = existing.getDeptId();
        }
        if (deptId == null) {
            return Result.error("No writable department found");
        }

        config.setDeptId(deptId);
        config.setUpdateTime(LocalDateTime.now());
        config.setCreateTime(existing.getCreateTime());
        return Result.success(trackConfigRepository.save(config));
    }

    public Result<Void> delete(Long id) {
        TrackConfig existing = trackConfigRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error("Config does not exist");
        }
        if (!canAccess(existing.getDeptId(), currentScope())) {
            return Result.error("No permission to delete this record");
        }
        trackConfigRepository.deleteById(id);
        return Result.success();
    }

    public Result<Map<String, Object>> statistics() {
        DataPermissionService.DataScope scope = currentScope();
        List<TrackConfig> all;
        if (scope.isAllData()) {
            all = trackConfigRepository.findAll();
        } else if (scope.getDeptIds().isEmpty()) {
            all = new ArrayList<>();
        } else {
            all = trackConfigRepository.findAll((root, query, cb) -> root.get("deptId").in(scope.getDeptIds()));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", (long) all.size());
        long pageViewCount = all.stream().filter(c -> "page_view".equals(c.getEventType())).count();
        long clickCount = all.stream().filter(c -> "click".equals(c.getEventType())).count();
        result.put("pageViewCount", pageViewCount);
        result.put("clickCount", clickCount);
        return Result.success(result);
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

    private Long resolveWriteDeptId(Long requestedDeptId) {
        Long currentUserId = permissionChecker.getCurrentUserId();
        DataPermissionService.DataScope scope = currentScope();
        if (scope.isAllData()) {
            return dataPermissionService.resolvePrimaryDeptId(requestedDeptId);
        }

        if (requestedDeptId != null && scope.getDeptIds().contains(requestedDeptId)) {
            return requestedDeptId;
        }

        User user = currentUserId == null ? null : userRepository.findById(currentUserId).orElse(null);
        if (user != null && user.getPrimaryDeptId() != null && scope.getDeptIds().contains(user.getPrimaryDeptId())) {
            return user.getPrimaryDeptId();
        }

        return scope.getDeptIds().stream().findFirst().orElse(null);
    }
}
