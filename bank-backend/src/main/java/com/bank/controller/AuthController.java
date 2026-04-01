package com.bank.controller;

import com.bank.common.Result;
import com.bank.entity.User;
import com.bank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<User> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        return userService.login(username, password);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @GetMapping("/userinfo")
    public Result<Map<String, Object>> getUserInfo(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            User user = userService.getUserById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            Map<String, Object> info = new HashMap<>();
            info.put("id", user.getId());
            info.put("username", user.getUsername());
            info.put("nickname", user.getNickname());
            info.put("avatar", user.getAvatar());
            info.put("status", user.getStatus());
            return Result.success(info);
        }
        Map<String, Object> info = new HashMap<>();
        info.put("username", "admin");
        info.put("nickname", "管理员");
        info.put("avatar", "");
        return Result.success(info);
    }
}
