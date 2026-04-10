package com.track.controller;

import com.track.common.Result;
import com.track.entity.User;
import com.track.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<Page<User>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return userService.list(pageNum, pageSize);
    }

    @PostMapping("/add")
    public Result<User> add(@RequestBody Map<String, Object> request) {
        User user = new User();
        user.setUsername((String) request.get("username"));
        user.setPassword((String) request.get("password"));
        user.setNickname((String) request.get("nickname"));
        if (request.get("status") != null) {
            user.setStatus(((Number) request.get("status")).intValue());
        }
        Long roleId = request.get("roleId") != null ? ((Number) request.get("roleId")).longValue() : null;
        return userService.add(user, roleId);
    }

    @PostMapping("/update")
    public Result<User> update(@RequestBody Map<String, Object> request) {
        User user = new User();
        user.setId(((Number) request.get("id")).longValue());
        user.setNickname((String) request.get("nickname"));
        if (request.get("status") != null) {
            user.setStatus(((Number) request.get("status")).intValue());
        }
        Long roleId = request.get("roleId") != null ? ((Number) request.get("roleId")).longValue() : null;
        return userService.update(user, roleId);
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody Map<String, Long> request) {
        return userService.delete(request.get("id"));
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody Map<String, Long> request) {
        return userService.resetPassword(request.get("id"));
    }
}
