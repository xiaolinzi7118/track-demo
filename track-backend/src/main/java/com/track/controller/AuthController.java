package com.track.controller;

import com.track.common.Result;
import com.track.entity.User;
import com.track.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

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
    public Result<Map<String, Object>> getUserInfo() {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", "admin");
        userInfo.put("nickname", "管理员");
        userInfo.put("avatar", "");
        userInfo.put("roles", new String[]{"admin"});
        return Result.success(userInfo);
    }

    @GetMapping("/menus")
    public Result<Map<String, Object>> getMenus() {
        Map<String, Object> menus = new HashMap<>();
        menus.put("menus", new Object[]{
            createMenu("dashboard", "仪表盘", "Dashboard", "el-icon-s-home"),
            createMenu("track-config", "埋点配置", "TrackConfig", "el-icon-setting"),
            createMenu("track-data", "数据回检", "TrackData", "el-icon-data-line"),
            createMenu("system", "系统管理", null, "el-icon-s-tools", new Object[]{
                createMenu("user", "用户管理", "SystemUser", "el-icon-user")
            })
        });
        return Result.success(menus);
    }

    private Map<String, Object> createMenu(String path, String title, String component, String icon) {
        return createMenu(path, title, component, icon, null);
    }

    private Map<String, Object> createMenu(String path, String title, String component, String icon, Object[] children) {
        Map<String, Object> menu = new HashMap<>();
        menu.put("path", path);
        menu.put("title", title);
        menu.put("component", component);
        menu.put("icon", icon);
        if (children != null) {
            menu.put("children", children);
        }
        return menu;
    }
}
