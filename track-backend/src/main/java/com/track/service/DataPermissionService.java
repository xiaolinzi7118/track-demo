package com.track.service;

import com.track.entity.DictParamItem;
import com.track.entity.User;
import com.track.entity.UserDataDept;
import com.track.repository.DictParamItemRepository;
import com.track.repository.UserDataDeptRepository;
import com.track.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataPermissionService {

    public static final String DEPT_PARAM_ID = "SYS_DEPT";
    public static final String DEFAULT_DEPT_CODE = "DEFAULT";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDataDeptRepository userDataDeptRepository;

    @Autowired
    private DictParamItemRepository dictParamItemRepository;

    public boolean isBuiltInSuperAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        User user = userRepository.findById(userId).orElse(null);
        return user != null && Integer.valueOf(UserService.BUILTIN_SUPER_ADMIN_YES).equals(user.getIsBuiltinSuperAdmin());
    }

    public DataScope getDataScope(Long userId) {
        if (userId == null) {
            return DataScope.none();
        }
        if (isBuiltInSuperAdmin(userId)) {
            return DataScope.all();
        }
        Set<Long> deptIds = getVisibleDeptIds(userId);
        return deptIds.isEmpty() ? DataScope.none() : DataScope.of(deptIds);
    }

    public Set<Long> getVisibleDeptIds(Long userId) {
        if (userId == null) {
            return new LinkedHashSet<>();
        }

        Set<Long> deptIds = new LinkedHashSet<>();
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.getPrimaryDeptId() != null) {
            deptIds.add(user.getPrimaryDeptId());
        }

        List<UserDataDept> dataDepts = userDataDeptRepository.findByUserId(userId);
        for (UserDataDept relation : dataDepts) {
            if (relation.getDeptId() != null) {
                deptIds.add(relation.getDeptId());
            }
        }

        return normalizeDeptIds(deptIds);
    }

    public Set<Long> normalizeDeptIds(Collection<Long> rawDeptIds) {
        if (rawDeptIds == null || rawDeptIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        List<Long> uniqueIds = rawDeptIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (uniqueIds.isEmpty()) {
            return new LinkedHashSet<>();
        }

        List<DictParamItem> activeDepts =
                dictParamItemRepository.findByIdInAndParamIdAndStatus(uniqueIds, DEPT_PARAM_ID, 0);
        return activeDepts.stream()
                .map(DictParamItem::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Long getDefaultDeptId() {
        DictParamItem dept = dictParamItemRepository
                .findFirstByParamIdAndItemCodeAndStatusOrderByIdAsc(DEPT_PARAM_ID, DEFAULT_DEPT_CODE, 0)
                .orElse(null);
        return dept == null ? null : dept.getId();
    }

    public Long resolvePrimaryDeptId(Long requestPrimaryDeptId) {
        if (requestPrimaryDeptId != null) {
            Set<Long> valid = normalizeDeptIds(Collections.singleton(requestPrimaryDeptId));
            if (!valid.isEmpty()) {
                return requestPrimaryDeptId;
            }
        }
        return getDefaultDeptId();
    }

    public static class DataScope {
        private final boolean allData;
        private final Set<Long> deptIds;

        private DataScope(boolean allData, Set<Long> deptIds) {
            this.allData = allData;
            this.deptIds = deptIds;
        }

        public static DataScope all() {
            return new DataScope(true, new LinkedHashSet<>());
        }

        public static DataScope of(Set<Long> deptIds) {
            return new DataScope(false, deptIds == null ? new LinkedHashSet<>() : deptIds);
        }

        public static DataScope none() {
            return new DataScope(false, new LinkedHashSet<>());
        }

        public boolean isAllData() {
            return allData;
        }

        public Set<Long> getDeptIds() {
            return deptIds;
        }
    }
}
