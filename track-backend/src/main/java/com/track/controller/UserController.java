package com.track.controller;

import com.track.common.PermissionChecker;
import com.track.common.Result;
import com.track.entity.DictParamItem;
import com.track.entity.Role;
import com.track.entity.User;
import com.track.entity.UserDataDept;
import com.track.entity.UserRole;
import com.track.repository.DictParamItemRepository;
import com.track.repository.RoleRepository;
import com.track.repository.UserDataDeptRepository;
import com.track.repository.UserRoleRepository;
import com.track.service.DataPermissionService;
import com.track.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PermissionChecker permissionChecker;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserDataDeptRepository userDataDeptRepository;

    @Autowired
    private DictParamItemRepository dictParamItemRepository;

    @Autowired
    private DataPermissionService dataPermissionService;

    @GetMapping("/list")
    public Result<Page<Map<String, Object>>> list(@RequestParam(required = false) String keyword,
                                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        permissionChecker.checkPermission("system-user:view");
        Page<User> userPage = userService.pageList(keyword, pageNum, pageSize);
        List<Map<String, Object>> rows = buildUserListResponse(userPage.getContent());
        Page<Map<String, Object>> resultPage = new PageImpl<>(rows, userPage.getPageable(), userPage.getTotalElements());
        return Result.success(resultPage);
    }

    @GetMapping("/detail")
    public Result<Map<String, Object>> detail(@RequestParam Long id) {
        permissionChecker.checkPermission("system-user:view");

        User user = userService.findById(id);
        if (user == null || userService.isBuiltInSuperAdmin(user)) {
            return Result.error("User does not exist");
        }

        List<Map<String, Object>> rows = buildUserListResponse(Collections.singletonList(user));
        return rows.isEmpty() ? Result.error("User does not exist") : Result.success(rows.get(0));
    }

    @PostMapping("/add")
    @Transactional
    public Result<User> add(@RequestBody Map<String, Object> request) {
        permissionChecker.checkPermission("system-user:add");

        User user = new User();
        user.setUsername((String) request.get("username"));
        user.setPassword((String) request.get("password"));
        user.setNickname((String) request.get("nickname"));
        user.setPrimaryDeptId(parseLong(request.get("primaryDeptId")));
        user.setStatus(request.get("status") != null ? Integer.valueOf(request.get("status").toString()) : 1);

        Result<User> result = userService.addUser(user);
        if (result.getCode() != 200 || result.getData() == null) {
            return result;
        }

        Long userId = result.getData().getId();
        List<Long> roleIds = parseRoleIds(request);
        Result<Void> roleCheck = validateRoleIds(roleIds);
        if (roleCheck.getCode() != 200) {
            return Result.error(roleCheck.getMessage());
        }
        saveUserRoles(userId, roleIds);

        List<Long> requestDataDeptIds = parseLongList(request.get("dataDeptIds"));
        saveUserDataDepts(userId, result.getData().getPrimaryDeptId(), requestDataDeptIds, getCurrentOperator());

        return result;
    }

    @PostMapping("/update")
    @Transactional
    public Result<User> update(@RequestBody Map<String, Object> request) {
        permissionChecker.checkPermission("system-user:edit");

        Long id = parseLong(request.get("id"));
        if (id == null) {
            return Result.error("id is required");
        }
        User existing = userService.findById(id);
        if (existing == null) {
            return Result.error("User does not exist");
        }
        if (userService.isBuiltInSuperAdmin(existing)) {
            return Result.error("Built-in super admin cannot be updated");
        }

        User user = new User();
        user.setId(id);
        user.setNickname((String) request.get("nickname"));
        user.setPrimaryDeptId(parseLong(request.get("primaryDeptId")));
        user.setStatus(request.get("status") != null ? Integer.valueOf(request.get("status").toString()) : 1);

        Result<User> result = userService.updateUser(user);
        if (result.getCode() != 200 || result.getData() == null) {
            return result;
        }

        if (request.containsKey("roleIds") || request.containsKey("roleId")) {
            List<Long> roleIds = parseRoleIds(request);
            Result<Void> roleCheck = validateRoleIds(roleIds);
            if (roleCheck.getCode() != 200) {
                return Result.error(roleCheck.getMessage());
            }
            saveUserRoles(id, roleIds);
        }

        if (request.containsKey("dataDeptIds") || request.containsKey("primaryDeptId")) {
            List<Long> requestDataDeptIds = parseLongList(request.get("dataDeptIds"));
            saveUserDataDepts(id, result.getData().getPrimaryDeptId(), requestDataDeptIds, getCurrentOperator());
        }

        return result;
    }

    @PostMapping("/delete")
    @Transactional
    public Result<Void> delete(@RequestBody Map<String, Long> request) {
        permissionChecker.checkPermission("system-user:delete");
        Long id = request.get("id");
        return userService.deleteUser(id);
    }

    @PostMapping("/update-password")
    public Result<Void> updatePassword(@RequestBody Map<String, Object> request) {
        permissionChecker.checkPermission("system-user:edit");
        Long id = parseLong(request.get("id"));
        String password = (String) request.get("password");
        if (id == null) {
            return Result.error("id is required");
        }
        if (userService.isBuiltInSuperAdmin(id)) {
            return Result.error("Built-in super admin cannot be updated");
        }
        return userService.updatePassword(id, password);
    }

    @PostMapping("/update-status")
    public Result<Void> updateStatus(@RequestBody Map<String, Object> request) {
        permissionChecker.checkPermission("system-user:edit");
        Long id = parseLong(request.get("id"));
        Integer status = request.get("status") == null ? null : Integer.valueOf(request.get("status").toString());
        if (id == null || status == null) {
            return Result.error("id and status are required");
        }
        if (userService.isBuiltInSuperAdmin(id)) {
            return Result.error("Built-in super admin status cannot be changed");
        }
        return userService.updateStatus(id, status);
    }

    private List<Map<String, Object>> buildUserListResponse(List<User> users) {
        if (users == null || users.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());

        Map<Long, List<UserRole>> userRoleMap = new HashMap<>();
        Set<Long> roleIds = new LinkedHashSet<>();
        for (Long userId : userIds) {
            List<UserRole> roles = userRoleRepository.findByUserId(userId);
            userRoleMap.put(userId, roles);
            for (UserRole role : roles) {
                roleIds.add(role.getRoleId());
            }
        }

        Map<Long, Role> roleMap = roleIds.isEmpty() ? new HashMap<>() :
                roleRepository.findAllById(roleIds).stream()
                        .collect(Collectors.toMap(Role::getId, r -> r, (a, b) -> a));

        Map<Long, List<UserDataDept>> userDataDeptMap = userDataDeptRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.groupingBy(UserDataDept::getUserId));

        Set<Long> deptIds = new LinkedHashSet<>();
        for (User user : users) {
            if (user.getPrimaryDeptId() != null) {
                deptIds.add(user.getPrimaryDeptId());
            }
            List<UserDataDept> dataDepts = userDataDeptMap.getOrDefault(user.getId(), new ArrayList<>());
            for (UserDataDept relation : dataDepts) {
                if (relation.getDeptId() != null) {
                    deptIds.add(relation.getDeptId());
                }
            }
        }

        Map<Long, DictParamItem> deptMap = deptIds.isEmpty() ? new HashMap<>() :
                dictParamItemRepository.findByIdInAndParamIdAndStatus(new ArrayList<>(deptIds), DataPermissionService.DEPT_PARAM_ID, 0)
                        .stream()
                        .collect(Collectors.toMap(DictParamItem::getId, d -> d, (a, b) -> a));

        List<Map<String, Object>> result = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", user.getId());
            item.put("username", user.getUsername());
            item.put("nickname", user.getNickname());
            item.put("avatar", user.getAvatar());
            item.put("status", user.getStatus());
            item.put("createTime", user.getCreateTime());
            item.put("isSuperAdmin", userService.isBuiltInSuperAdmin(user));
            item.put("primaryDeptId", user.getPrimaryDeptId());
            DictParamItem primaryDept = user.getPrimaryDeptId() == null ? null : deptMap.get(user.getPrimaryDeptId());
            item.put("primaryDeptName", primaryDept == null ? null : primaryDept.getItemName());

            List<UserRole> roleRelations = userRoleMap.getOrDefault(user.getId(), new ArrayList<>());
            List<Long> currentRoleIds = roleRelations.stream().map(UserRole::getRoleId).collect(Collectors.toList());
            List<String> roleNames = currentRoleIds.stream()
                    .map(roleMap::get)
                    .filter(Objects::nonNull)
                    .map(Role::getRoleName)
                    .collect(Collectors.toList());
            item.put("roleIds", currentRoleIds);
            item.put("roleNames", roleNames);
            item.put("roleId", currentRoleIds.isEmpty() ? null : currentRoleIds.get(0));

            List<UserDataDept> deptRelations = userDataDeptMap.getOrDefault(user.getId(), new ArrayList<>());
            List<Long> dataDeptIds = deptRelations.stream()
                    .map(UserDataDept::getDeptId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            List<String> dataDeptNames = dataDeptIds.stream()
                    .map(deptMap::get)
                    .filter(Objects::nonNull)
                    .map(DictParamItem::getItemName)
                    .collect(Collectors.toList());
            item.put("dataDeptIds", dataDeptIds);
            item.put("dataDeptNames", dataDeptNames);

            result.add(item);
        }
        return result;
    }

    private Result<Void> validateRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Result.success();
        }
        Set<Long> existing = roleRepository.findAllById(roleIds).stream()
                .map(Role::getId)
                .collect(Collectors.toSet());
        for (Long roleId : roleIds) {
            if (!existing.contains(roleId)) {
                return Result.error("Role does not exist: " + roleId);
            }
        }
        return Result.success();
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        if (userService.isBuiltInSuperAdmin(userId)) {
            return;
        }
        List<Long> normalizedRoleIds = roleIds == null
                ? new ArrayList<>()
                : roleIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());

        userRoleRepository.deleteByUserId(userId);
        // Ensure delete SQL executes before inserts in current transaction.
        userRoleRepository.flush();
        if (normalizedRoleIds.isEmpty()) {
            return;
        }
        for (Long roleId : normalizedRoleIds) {
            UserRole ur = new UserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoleRepository.save(ur);
        }
    }

    private void saveUserDataDepts(Long userId, Long primaryDeptId, List<Long> requestDataDeptIds, String operator) {
        if (userService.isBuiltInSuperAdmin(userId)) {
            return;
        }
        userDataDeptRepository.deleteByUserId(userId);

        Set<Long> targetDeptIds = new LinkedHashSet<>(dataPermissionService.normalizeDeptIds(requestDataDeptIds));
        if (primaryDeptId != null) {
            targetDeptIds.add(primaryDeptId);
        }
        if (targetDeptIds.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (Long deptId : targetDeptIds) {
            UserDataDept relation = new UserDataDept();
            relation.setUserId(userId);
            relation.setDeptId(deptId);
            relation.setCreateBy(operator);
            relation.setCreateTime(now);
            userDataDeptRepository.save(relation);
        }
    }

    private List<Long> parseRoleIds(Map<String, Object> request) {
        List<Long> roleIds = parseLongList(request.get("roleIds"));
        if (!roleIds.isEmpty()) {
            return roleIds;
        }
        Long roleId = parseLong(request.get("roleId"));
        return roleId == null ? new ArrayList<>() : Collections.singletonList(roleId);
    }

    private List<Long> parseLongList(Object value) {
        if (value == null) {
            return new ArrayList<>();
        }
        List<Long> result = new ArrayList<>();
        if (value instanceof List<?>) {
            for (Object item : (List<?>) value) {
                Long parsed = parseLong(item);
                if (parsed != null) {
                    result.add(parsed);
                }
            }
            return result;
        }
        Long parsed = parseLong(value);
        if (parsed != null) {
            result.add(parsed);
        }
        return result;
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(value.toString());
        } catch (Exception ignored) {
            return null;
        }
    }

    private String getCurrentOperator() {
        Long userId = permissionChecker.getCurrentUserId();
        if (userId == null) {
            return "system";
        }
        User user = userService.findById(userId);
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return "system";
        }
        return user.getUsername();
    }
}
