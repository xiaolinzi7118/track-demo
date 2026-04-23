package com.track.controller;

import com.track.common.Result;
import com.track.common.PermissionChecker;
import com.track.entity.Role;
import com.track.entity.User;
import com.track.entity.UserRole;
import com.track.repository.RoleRepository;
import com.track.repository.UserRoleRepository;
import com.track.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list(@RequestParam(required = false) String keyword) {
        permissionChecker.checkPermission("system-user:view");

        List<User> users;
        if (keyword != null && !keyword.isEmpty()) {
            users = userService.findAll().stream()
                    .filter(u -> u.getUsername().contains(keyword) || (u.getNickname() != null && u.getNickname().contains(keyword)))
                    .collect(Collectors.toList());
        } else {
            users = userService.findAll();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", user.getId());
            item.put("username", user.getUsername());
            item.put("nickname", user.getNickname());
            item.put("avatar", user.getAvatar());
            item.put("status", user.getStatus());
            item.put("createTime", user.getCreateTime());

            // 获取角色信息
            List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
            if (!userRoles.isEmpty()) {
                Long roleId = userRoles.get(0).getRoleId();
                item.put("roleId", roleId);
            }
            result.add(item);
        }

        return Result.success(result);
    }

    @GetMapping("/detail")
    public Result<Map<String, Object>> detail(@RequestParam Long id) {
        permissionChecker.checkPermission("system-user:view");

        User user = userService.findById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar());
        data.put("status", user.getStatus());
        data.put("createTime", user.getCreateTime());

        List<UserRole> userRoles = userRoleRepository.findByUserId(id);
        if (!userRoles.isEmpty()) {
            data.put("roleId", userRoles.get(0).getRoleId());
        }

        return Result.success(data);
    }

    @PostMapping("/add")
    @Transactional
    public Result<User> add(@RequestBody Map<String, Object> request) {
        permissionChecker.checkPermission("system-user:add");

        User user = new User();
        user.setUsername((String) request.get("username"));
        user.setPassword((String) request.get("password"));
        user.setNickname((String) request.get("nickname"));
        user.setStatus(request.get("status") != null ? Integer.valueOf(request.get("status").toString()) : 1);

        Result<User> result = userService.addUser(user);
        if (result.getCode() == 200 && request.get("roleId") != null) {
            Long roleId = Long.valueOf(request.get("roleId").toString());
            if (!roleRepository.existsById(roleId)) {
                return Result.error("角色不存在");
            }
            UserRole ur = new UserRole();
            ur.setUserId(result.getData().getId());
            ur.setRoleId(roleId);
            userRoleRepository.save(ur);
        }
        return result;
    }

    @PostMapping("/update")
    @Transactional
    public Result<User> update(@RequestBody Map<String, Object> request) {
        permissionChecker.checkPermission("system-user:edit");

        Long id = Long.valueOf(request.get("id").toString());
        User user = new User();
        user.setId(id);
        user.setNickname((String) request.get("nickname"));
        user.setStatus(request.get("status") != null ? Integer.valueOf(request.get("status").toString()) : 1);

        Result<User> result = userService.updateUser(user);

        if (request.get("roleId") != null) {
            Long roleId = Long.valueOf(request.get("roleId").toString());
            if (!roleRepository.existsById(roleId)) {
                return Result.error("角色不存在");
            }
            userRoleRepository.deleteByUserId(id);
            UserRole ur = new UserRole();
            ur.setUserId(id);
            ur.setRoleId(roleId);
            userRoleRepository.save(ur);
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
        Long id = Long.valueOf(request.get("id").toString());
        String password = (String) request.get("password");
        return userService.updatePassword(id, password);
    }

    @PostMapping("/update-status")
    public Result<Void> updateStatus(@RequestBody Map<String, Object> request) {
        permissionChecker.checkPermission("system-user:edit");
        Long id = Long.valueOf(request.get("id").toString());
        Integer status = Integer.valueOf(request.get("status").toString());
        return userService.updateStatus(id, status);
    }
}
