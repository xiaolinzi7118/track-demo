package com.track.controller;

import com.track.common.Result;
import com.track.config.TokenStore;
import com.track.entity.User;
import com.track.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenStore tokenStore;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        Result<User> loginResult = userService.login(username, password);
        if (loginResult.getCode() != 200) {
            return Result.error(loginResult.getCode(), loginResult.getMessage());
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, loginResult.getData().getId());

        Map<String, Object> data = new java.util.HashMap<>();
        data.put("token", token);
        data.put("user", loginResult.getData());
        return Result.success(data);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null) {
            tokenStore.remove(token);
        }
        return Result.success();
    }

    @GetMapping("/userinfo")
    public Result<Map<String, Object>> getUserInfo(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        Map<String, Object> userInfo = userService.getUserInfoWithPermissions(userId);
        if (userInfo == null) {
            return Result.error(401, "用户不存在");
        }
        return Result.success(userInfo);
    }

    @GetMapping("/menus")
    public Result<List<com.track.entity.Menu>> getMenus(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        List<com.track.entity.Menu> menus = userService.getUserMenus(userId);
        return Result.success(menus);
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) return null;
        return tokenStore.getUserId(token);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
