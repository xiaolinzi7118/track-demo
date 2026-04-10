package com.track.service;

import com.track.common.Result;
import com.track.entity.*;
import com.track.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private RoleMenuRepository roleMenuRepository;

    @Autowired
    private MenuService menuService;

    @PostConstruct
    public void init() {
        initMenus();
        initRoles();

        if (userRepository.findByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("123456");
            admin.setNickname("管理员");
            admin.setStatus(1);
            admin.setCreateTime(LocalDateTime.now());
            admin.setUpdateTime(LocalDateTime.now());
            userRepository.save(admin);

            Role adminRole = roleRepository.findByRoleCode("ADMIN");
            if (adminRole != null) {
                UserRole ur = new UserRole();
                ur.setUserId(admin.getId());
                ur.setRoleId(adminRole.getId());
                userRoleRepository.save(ur);
            }
        }
    }

    private void initRoles() {
        if (roleRepository.count() > 0) return;

        Role adminRole = new Role();
        adminRole.setRoleCode("ADMIN");
        adminRole.setRoleName("管理员");
        adminRole.setStatus(1);
        adminRole.setDescription("系统管理员，拥有所有权限");
        adminRole.setCreateTime(LocalDateTime.now());
        adminRole.setUpdateTime(LocalDateTime.now());
        roleRepository.save(adminRole);

        Role businessRole = new Role();
        businessRole.setRoleCode("BUSINESS");
        businessRole.setRoleName("业务人员");
        businessRole.setStatus(1);
        businessRole.setDescription("业务人员，可查看埋点数据");
        businessRole.setCreateTime(LocalDateTime.now());
        businessRole.setUpdateTime(LocalDateTime.now());
        roleRepository.save(businessRole);

        Role devRole = new Role();
        devRole.setRoleCode("DEVELOPER");
        devRole.setRoleName("开发人员");
        devRole.setStatus(1);
        devRole.setDescription("开发人员，可管理埋点配置和数据");
        devRole.setCreateTime(LocalDateTime.now());
        devRole.setUpdateTime(LocalDateTime.now());
        roleRepository.save(devRole);

        // Assign all permissions to ADMIN
        List<Menu> allMenus = menuRepository.findAll();
        for (Menu menu : allMenus) {
            RoleMenu rm = new RoleMenu();
            rm.setRoleId(adminRole.getId());
            rm.setMenuId(menu.getId());
            roleMenuRepository.save(rm);
        }

        // Assign view permissions to BUSINESS
        List<String> businessCodes = Arrays.asList(
                "dashboard", "track-config", "track-config:view",
                "track-data", "track-data:view",
                "api-interface", "api-interface:view"
        );
        for (String code : businessCodes) {
            Menu m = menuRepository.findByMenuCode(code);
            if (m != null) {
                RoleMenu rm = new RoleMenu();
                rm.setRoleId(businessRole.getId());
                rm.setMenuId(m.getId());
                roleMenuRepository.save(rm);
            }
        }

        // Assign permissions to DEVELOPER
        List<String> devCodes = Arrays.asList(
                "dashboard",
                "track-config", "track-config:view", "track-config:add", "track-config:edit", "track-config:delete",
                "track-data", "track-data:view",
                "api-interface", "api-interface:view", "api-interface:add", "api-interface:edit", "api-interface:delete"
        );
        for (String code : devCodes) {
            Menu m = menuRepository.findByMenuCode(code);
            if (m != null) {
                RoleMenu rm = new RoleMenu();
                rm.setRoleId(devRole.getId());
                rm.setMenuId(m.getId());
                roleMenuRepository.save(rm);
            }
        }
    }

    private void initMenus() {
        if (menuRepository.count() > 0) return;

        List<Object[]> menuDefs = Arrays.asList(
                new Object[]{0L, "仪表盘", "dashboard", "/dashboard", 1, "House", 1, 1},
                new Object[]{0L, "埋点配置", "track-config", "/track-config", 1, "Setting", 2, 1},
                new Object[]{2L, "查看", "track-config:view", null, 2, null, 1, 1},
                new Object[]{2L, "新增", "track-config:add", null, 2, null, 2, 1},
                new Object[]{2L, "编辑", "track-config:edit", null, 2, null, 3, 1},
                new Object[]{2L, "删除", "track-config:delete", null, 2, null, 4, 1},
                new Object[]{0L, "接口来源管理", "api-interface", "/api-interface", 1, "Link", 3, 1},
                new Object[]{7L, "查看", "api-interface:view", null, 2, null, 1, 1},
                new Object[]{7L, "新增", "api-interface:add", null, 2, null, 2, 1},
                new Object[]{7L, "编辑", "api-interface:edit", null, 2, null, 3, 1},
                new Object[]{7L, "删除", "api-interface:delete", null, 2, null, 4, 1},
                new Object[]{0L, "数据回检", "track-data", "/track-data", 1, "DataLine", 4, 1},
                new Object[]{12L, "查看", "track-data:view", null, 2, null, 1, 1},
                new Object[]{0L, "系统管理", "system", "/system", 1, "Tools", 5, 1},
                new Object[]{14L, "用户管理", "system:user", "/system/user", 1, "User", 1, 1},
                new Object[]{15L, "新增用户", "system:user:add", null, 2, null, 1, 1},
                new Object[]{15L, "编辑用户", "system:user:edit", null, 2, null, 2, 1},
                new Object[]{15L, "删除用户", "system:user:delete", null, 2, null, 3, 1},
                new Object[]{14L, "角色管理", "system:role", "/system/role", 1, "Lock", 2, 1},
                new Object[]{19L, "查看", "system:role:view", null, 2, null, 1, 1},
                new Object[]{19L, "编辑权限", "system:role:edit", null, 2, null, 2, 1},
                new Object[]{14L, "重置数据", "system:reset-data", "/system/reset-data", 1, "Delete", 3, 1}
        );

        // Save all menus first, then fix parent IDs
        List<Menu> savedMenus = new ArrayList<>();
        for (Object[] def : menuDefs) {
            Menu menu = new Menu();
            menu.setParentId((Long) def[0]);
            menu.setMenuName((String) def[1]);
            menu.setMenuCode((String) def[2]);
            menu.setPath((String) def[3]);
            menu.setMenuType((Integer) def[4]);
            menu.setIcon((String) def[5]);
            menu.setSortOrder((Integer) def[6]);
            menu.setStatus((Integer) def[7]);
            menu.setCreateTime(LocalDateTime.now());
            menu.setUpdateTime(LocalDateTime.now());
            savedMenus.add(menuRepository.save(menu));
        }

        // Fix parent_id references based on actual saved IDs
        // Top-level menus (index 0,1,6,11,13) have parent_id=0 — correct
        // Children need parent_id updated to actual parent IDs
        Map<Integer, Integer> childParentIndex = new HashMap<>();
        // track-config children (2,3,4,5) -> parent index 1
        childParentIndex.put(2, 1);
        childParentIndex.put(3, 1);
        childParentIndex.put(4, 1);
        childParentIndex.put(5, 1);
        // api-interface children (7,8,9,10) -> parent index 6
        childParentIndex.put(7, 6);
        childParentIndex.put(8, 6);
        childParentIndex.put(9, 6);
        childParentIndex.put(10, 6);
        // track-data children (12) -> parent index 11
        childParentIndex.put(12, 11);
        // system children (14,18,21) -> parent index 13
        childParentIndex.put(14, 13);
        childParentIndex.put(18, 13);
        childParentIndex.put(21, 13);
        // user children (15,16,17) -> parent index 14
        childParentIndex.put(15, 14);
        childParentIndex.put(16, 14);
        childParentIndex.put(17, 14);
        // role children (19,20) -> parent index 18
        childParentIndex.put(19, 18);
        childParentIndex.put(20, 18);

        for (Map.Entry<Integer, Integer> entry : childParentIndex.entrySet()) {
            Menu child = savedMenus.get(entry.getKey());
            Menu parent = savedMenus.get(entry.getValue());
            child.setParentId(parent.getId());
            menuRepository.save(child);
        }
    }

    public Result<User> login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!password.equals(user.getPassword())) {
            return Result.error("密码错误");
        }
        if (user.getStatus() != 1) {
            return Result.error("用户已禁用");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    public Result<Page<User>> list(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<User> page = userRepository.findAll(pageable);
        page.getContent().forEach(u -> {
            u.setPassword(null);
            UserRole userRole = userRoleRepository.findByUserId(u.getId());
            if (userRole != null) {
                u.setRoleId(userRole.getRoleId());
                Role role = roleRepository.findById(userRole.getRoleId()).orElse(null);
                if (role != null) {
                    u.setRoleName(role.getRoleName());
                }
            }
        });
        return Result.success(page);
    }

    public Result<User> add(User user, Long roleId) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return Result.error("用户名已存在");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword("123456");
        }
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        User saved = userRepository.save(user);

        if (roleId != null) {
            UserRole ur = new UserRole();
            ur.setUserId(saved.getId());
            ur.setRoleId(roleId);
            userRoleRepository.save(ur);
        }

        saved.setPassword(null);
        return Result.success(saved);
    }

    public Result<User> update(User user, Long roleId) {
        User existing = userRepository.findById(user.getId()).orElse(null);
        if (existing == null) {
            return Result.error("用户不存在");
        }
        existing.setNickname(user.getNickname());
        if (user.getStatus() != null) {
            existing.setStatus(user.getStatus());
        }
        existing.setUpdateTime(LocalDateTime.now());
        User saved = userRepository.save(existing);

        if (roleId != null) {
            userRoleRepository.deleteByUserId(saved.getId());
            UserRole ur = new UserRole();
            ur.setUserId(saved.getId());
            ur.setRoleId(roleId);
            userRoleRepository.save(ur);
        }

        saved.setPassword(null);
        return Result.success(saved);
    }

    public Result<Void> delete(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if ("admin".equals(user.getUsername())) {
            return Result.error("不能删除管理员账号");
        }
        userRoleRepository.deleteByUserId(id);
        userRepository.deleteById(id);
        return Result.success();
    }

    public Result<Void> resetPassword(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword("123456");
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
        return Result.success();
    }

    public Map<String, Object> getUserInfoWithPermissions(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("nickname", user.getNickname());
        info.put("avatar", user.getAvatar());
        info.put("status", user.getStatus());

        UserRole userRole = userRoleRepository.findByUserId(userId);
        if (userRole != null) {
            Role role = roleRepository.findById(userRole.getRoleId()).orElse(null);
            if (role != null) {
                info.put("role", role.getRoleCode());
                info.put("roleName", role.getRoleName());
                info.put("roleId", role.getId());
            }
            List<String> permissions = menuService.getPermissionCodesByRoleIds(Collections.singletonList(userRole.getRoleId()));
            info.put("permissions", permissions);
        } else {
            info.put("role", "");
            info.put("roleName", "");
            info.put("permissions", new ArrayList<>());
        }

        return info;
    }

    public List<Menu> getUserMenus(Long userId) {
        UserRole userRole = userRoleRepository.findByUserId(userId);
        if (userRole == null) return new ArrayList<>();
        return menuService.getMenuTreeByRoleIds(Collections.singletonList(userRole.getRoleId()));
    }
}
