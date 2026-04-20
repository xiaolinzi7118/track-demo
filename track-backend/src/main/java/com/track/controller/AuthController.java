package com.track.controller;

import com.track.common.Result;
import com.track.config.TokenStore;
import com.track.entity.User;
import com.track.entity.UserRole;
import com.track.entity.Role;
import com.track.entity.RoleMenu;
import com.track.entity.Menu;
import com.track.repository.UserRoleRepository;
import com.track.repository.RoleRepository;
import com.track.repository.RoleMenuRepository;
import com.track.repository.MenuRepository;
import com.track.service.UserService;
import com.track.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MenuService menuService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!password.equals(user.getPassword())) {
            return Result.error("密码错误");
        }
        if (user.getStatus() != null && user.getStatus() != 1) {
            return Result.error("用户已禁用");
        }

        String token = tokenStore.createToken(user.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        return Result.success(data);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            tokenStore.removeToken(authHeader.substring(7));
        }
        return Result.success();
    }

    @GetMapping("/userinfo")
    public Result<Map<String, Object>> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        User user = userService.findById(userId);
        if (user == null) {
            return Result.error(401, "用户不存在");
        }

        // 获取用户角色
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        List<String> roleCodes = new ArrayList<>();
        List<String> roleNames = new ArrayList<>();
        for (UserRole ur : userRoles) {
            Role role = roleRepository.findById(ur.getRoleId()).orElse(null);
            if (role != null) {
                roleCodes.add(role.getRoleCode());
                roleNames.add(role.getRoleName());
            }
        }

        // 获取用户权限列表
        List<String> permissions = menuService.getPermissionsByUserId(userId);

        Map<String, Object> info = new HashMap<>();
        info.put("username", user.getUsername());
        info.put("nickname", user.getNickname());
        info.put("avatar", user.getAvatar());
        info.put("roles", roleCodes);
        info.put("roleNames", roleNames);
        info.put("permissions", permissions);

        return Result.success(info);
    }

    @GetMapping("/menus")
    public Result<List<Menu>> getMenus(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        List<Menu> menus = menuService.getMenusByUserId(userId);
        return Result.success(menus);
    }
}
