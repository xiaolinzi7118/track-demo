package com.track.controller;

import com.track.common.Result;
import com.track.config.TokenStore;
import com.track.entity.DictParamItem;
import com.track.entity.Menu;
import com.track.entity.Role;
import com.track.entity.User;
import com.track.entity.UserDataDept;
import com.track.entity.UserRole;
import com.track.repository.DictParamItemRepository;
import com.track.repository.RoleRepository;
import com.track.repository.UserDataDeptRepository;
import com.track.repository.UserRoleRepository;
import com.track.service.DataPermissionService;
import com.track.service.MenuService;
import com.track.service.UserService;
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

    @Autowired
    private UserDataDeptRepository userDataDeptRepository;

    @Autowired
    private DictParamItemRepository dictParamItemRepository;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error("User does not exist");
        }
        if (!password.equals(user.getPassword())) {
            return Result.error("Incorrect password");
        }
        if (user.getStatus() != null && user.getStatus() != 1) {
            return Result.error("User is disabled");
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
            return Result.error(401, "User does not exist");
        }

        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        List<Role> roles = roleIds.isEmpty() ? new ArrayList<>() : roleRepository.findAllById(roleIds);
        List<String> roleCodes = roles.stream().map(Role::getRoleCode).filter(Objects::nonNull).collect(Collectors.toList());
        List<String> roleNames = roles.stream().map(Role::getRoleName).filter(Objects::nonNull).collect(Collectors.toList());

        List<String> permissions = menuService.getPermissionsByUserId(userId);

        List<UserDataDept> relations = userDataDeptRepository.findByUserId(userId);
        Set<Long> deptIds = relations.stream()
                .map(UserDataDept::getDeptId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (user.getPrimaryDeptId() != null) {
            deptIds.add(user.getPrimaryDeptId());
        }
        Map<Long, DictParamItem> deptMap = deptIds.isEmpty() ? new HashMap<>() :
                dictParamItemRepository.findByIdInAndParamIdAndStatus(new ArrayList<>(deptIds), DataPermissionService.DEPT_PARAM_ID, 0)
                        .stream()
                        .collect(Collectors.toMap(DictParamItem::getId, d -> d, (a, b) -> a));

        List<String> dataDeptNames = deptIds.stream()
                .map(deptMap::get)
                .filter(Objects::nonNull)
                .map(DictParamItem::getItemName)
                .collect(Collectors.toList());

        DictParamItem primaryDept = user.getPrimaryDeptId() == null ? null : deptMap.get(user.getPrimaryDeptId());

        Map<String, Object> info = new HashMap<>();
        info.put("username", user.getUsername());
        info.put("nickname", user.getNickname());
        info.put("avatar", user.getAvatar());
        info.put("isSuperAdmin", Integer.valueOf(UserService.BUILTIN_SUPER_ADMIN_YES).equals(user.getIsBuiltinSuperAdmin()));
        info.put("roles", roleCodes);
        info.put("roleIds", roleIds);
        info.put("roleNames", roleNames);
        info.put("permissions", permissions);
        info.put("primaryDeptId", user.getPrimaryDeptId());
        info.put("primaryDeptName", primaryDept == null ? null : primaryDept.getItemName());
        info.put("dataDeptIds", new ArrayList<>(deptIds));
        info.put("dataDeptNames", dataDeptNames);

        return Result.success(info);
    }

    @GetMapping("/menus")
    public Result<List<Menu>> getMenus(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        List<Menu> menus = menuService.getMenusByUserId(userId);
        return Result.success(menus);
    }
}
