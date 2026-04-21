package com.track.service;

import com.track.entity.*;
import com.track.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Order(1)
public class DataInitService implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public DataInitService(RoleRepository roleRepository, MenuRepository menuRepository,
                           RoleMenuRepository roleMenuRepository, UserRepository userRepository,
                           UserRoleRepository userRoleRepository) {
        this.roleRepository = roleRepository;
        this.menuRepository = menuRepository;
        this.roleMenuRepository = roleMenuRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!isDataFormatCorrect()) {
            cleanRbacData();
            initRoles();
            initMenus();
        }
        ensureAdminAssignments();
    }

    /**
     * 检查数据格式是否与当前代码匹配。
     * 旧版本数据没有 menuType=3 的按钮权限项，也没有 perms 字段值。
     */
    private boolean isDataFormatCorrect() {
        if (roleRepository.count() == 0) return false;
        if (menuRepository.count() == 0) return false;
        Menu trackDir = menuRepository.findByMenuCode("track");
        return trackDir != null && trackDir.getMenuType() == 1;
    }

    /**
     * 清理 RBAC 表数据（不影响业务表）
     */
    private void cleanRbacData() {
        roleMenuRepository.deleteAllInBatch();
        userRoleRepository.deleteAllInBatch();
        menuRepository.deleteAllInBatch();
        roleRepository.deleteAllInBatch();
    }

    private void initRoles() {
        Role admin = new Role();
        admin.setRoleCode("admin");
        admin.setRoleName("管理员");
        admin.setDescription("系统管理员，拥有所有权限");
        admin.setStatus(1);
        roleRepository.save(admin);

        Role business = new Role();
        business.setRoleCode("business");
        business.setRoleName("业务人员");
        business.setDescription("业务配置人员");
        business.setStatus(1);
        roleRepository.save(business);

        Role developer = new Role();
        developer.setRoleCode("developer");
        developer.setRoleName("开发人员");
        developer.setDescription("开发人员");
        developer.setStatus(1);
        roleRepository.save(developer);
    }

    private void initMenus() {
        List<Menu> menus = new ArrayList<>();

        // 仪表盘（默认页面，所有角色可见，无需权限编辑）
        menus.add(createMenu(0L, "仪表盘", "dashboard", "/dashboard", "House", 1, 2, null));

        // 埋点管理（目录）
        menus.add(createMenu(0L, "埋点管理", "track", "/track", "DataAnalysis", 2, 1, null));

        // 埋点配置
        menus.add(createMenu(null, "埋点配置", "track-config", "/track-config", "Setting", 1, 2, null));
        menus.add(createMenu(null, "查看", "track-config:view", null, null, 1, 3, "track-config:view"));
        menus.add(createMenu(null, "新增", "track-config:add", null, null, 2, 3, "track-config:add"));
        menus.add(createMenu(null, "编辑", "track-config:edit", null, null, 3, 3, "track-config:edit"));
        menus.add(createMenu(null, "删除", "track-config:delete", null, null, 4, 3, "track-config:delete"));

        // 接口来源管理
        menus.add(createMenu(null, "接口来源管理", "api-interface", "/api-interface", "Link", 2, 2, null));
        menus.add(createMenu(null, "查看", "api-interface:view", null, null, 1, 3, "api-interface:view"));
        menus.add(createMenu(null, "新增", "api-interface:add", null, null, 2, 3, "api-interface:add"));
        menus.add(createMenu(null, "编辑", "api-interface:edit", null, null, 3, 3, "api-interface:edit"));
        menus.add(createMenu(null, "删除", "api-interface:delete", null, null, 4, 3, "api-interface:delete"));

        // 数据回检
        menus.add(createMenu(null, "数据回检", "track-data", "/track-data", "DataLine", 3, 2, null));
        menus.add(createMenu(null, "查看", "track-data:view", null, null, 1, 3, "track-data:view"));
        menus.add(createMenu(null, "清空", "track-data:clear", null, null, 2, 3, "track-data:clear"));

        // 系统管理
        menus.add(createMenu(0L, "系统管理", "system", "/system", "Tools", 3, 1, null));

        // 用户管理
        menus.add(createMenu(null, "用户管理", "system-user", "/system/user", "User", 1, 2, null));
        menus.add(createMenu(null, "查看", "system-user:view", null, null, 1, 3, "system-user:view"));
        menus.add(createMenu(null, "新增", "system-user:add", null, null, 2, 3, "system-user:add"));
        menus.add(createMenu(null, "编辑", "system-user:edit", null, null, 3, 3, "system-user:edit"));
        menus.add(createMenu(null, "删除", "system-user:delete", null, null, 4, 3, "system-user:delete"));

        // 角色管理
        menus.add(createMenu(null, "角色管理", "system-role", "/system/role", "Lock", 2, 2, null));
        menus.add(createMenu(null, "查看", "system-role:view", null, null, 1, 3, "system-role:view"));
        menus.add(createMenu(null, "新增", "system-role:add", null, null, 2, 3, "system-role:add"));
        menus.add(createMenu(null, "编辑", "system-role:edit", null, null, 3, 3, "system-role:edit"));
        menus.add(createMenu(null, "删除", "system-role:delete", null, null, 4, 3, "system-role:delete"));

        // 重置数据
        menus.add(createMenu(null, "重置数据", "system-reset-data", "/system/reset-data", "Delete", 3, 2, null));
        menus.add(createMenu(null, "查看", "system-reset-data:view", null, null, 1, 3, "system-reset-data:view"));
        menus.add(createMenu(null, "操作", "system-reset-data:operate", null, null, 2, 3, "system-reset-data:operate"));

        // 保存：先保存顶级页面/目录（parentId=0），再保存子页面，最后保存按钮
        Map<String, Long> codeToId = new HashMap<>();
        Map<String, Long> pageToId = new HashMap<>();

        // 第一轮：保存 parentId=0 的顶级项
        for (Menu m : menus) {
            if (m.getParentId() != null && m.getParentId() == 0L) {
                menuRepository.save(m);
                codeToId.put(m.getMenuCode(), m.getId());
            }
        }

        // 第二轮：保存子页面（menuType=2 且 parentId=null）
        for (Menu m : menus) {
            if (m.getParentId() == null && m.getMenuType() == 2) {
                Long parentId = resolveParentId(m.getMenuCode(), codeToId);
                if (parentId != null) {
                    m.setParentId(parentId);
                    menuRepository.save(m);
                    pageToId.put(m.getMenuCode(), m.getId());
                }
            }
        }

        // 第三轮：保存按钮（menuType=3 且 parentId=null）
        for (Menu m : menus) {
            if (m.getParentId() == null && m.getMenuType() == 3) {
                Long parentId = resolveButtonParentId(m.getMenuCode(), pageToId, codeToId);
                if (parentId != null) {
                    m.setParentId(parentId);
                    menuRepository.save(m);
                }
            }
        }

        // 给各角色分配菜单权限
        assignAllMenusToAdmin();
        assignBusinessMenus();
        assignDeveloperMenus();
    }

    private Long resolveParentId(String menuCode, Map<String, Long> codeToId) {
        if (menuCode.startsWith("track-config")) return codeToId.get("track");
        if (menuCode.startsWith("api-interface")) return codeToId.get("track");
        if (menuCode.startsWith("track-data")) return codeToId.get("track");
        if (menuCode.startsWith("system-user")) return codeToId.get("system");
        if (menuCode.startsWith("system-role")) return codeToId.get("system");
        if (menuCode.startsWith("system-reset-data")) return codeToId.get("system");
        return null;
    }

    private Long resolveButtonParentId(String menuCode, Map<String, Long> pageToId, Map<String, Long> codeToId) {
        String pageCode = menuCode.contains(":") ? menuCode.substring(0, menuCode.indexOf(':')) : null;
        if (pageCode != null) {
            Long id = pageToId.get(pageCode);
            if (id != null) return id;
            return codeToId.get(pageCode);
        }
        return null;
    }

    private Menu createMenu(Long parentId, String menuName, String menuCode, String path,
                            String icon, int sortOrder, int menuType, String perms) {
        Menu m = new Menu();
        m.setParentId(parentId);
        m.setMenuName(menuName);
        m.setMenuCode(menuCode);
        m.setPath(path);
        m.setIcon(icon);
        m.setSortOrder(sortOrder);
        m.setMenuType(menuType);
        m.setPerms(perms);
        m.setStatus(1);
        return m;
    }

    /**
     * 确保 admin 用户拥有 admin 角色和所有菜单权限。
     * 无论数据库中角色编码的大小写如何，都能正确匹配。
     */
    private void ensureAdminAssignments() {
        User admin = userRepository.findByUsername("admin");
        if (admin == null) return;

        // 大小写不敏感地查找 admin 角色
        Role adminRole = findAdminRole();
        if (adminRole == null) return;

        // 确保用户-角色关联
        List<UserRole> existingUR = userRoleRepository.findByUserId(admin.getId());
        boolean hasRole = false;
        for (UserRole ur : existingUR) {
            Role r = roleRepository.findById(ur.getRoleId()).orElse(null);
            if (r != null && "admin".equalsIgnoreCase(r.getRoleCode())) {
                hasRole = true;
                break;
            }
        }
        if (!hasRole) {
            UserRole ur = new UserRole();
            ur.setUserId(admin.getId());
            ur.setRoleId(adminRole.getId());
            userRoleRepository.save(ur);
        }

        // 确保 admin 角色拥有所有菜单权限
        List<RoleMenu> existingRM = roleMenuRepository.findByRoleId(adminRole.getId());
        if (existingRM.isEmpty()) {
            List<Menu> allMenus = menuRepository.findAll();
            for (Menu menu : allMenus) {
                RoleMenu rm = new RoleMenu();
                rm.setRoleId(adminRole.getId());
                rm.setMenuId(menu.getId());
                roleMenuRepository.save(rm);
            }
        }
    }

    private Role findAdminRole() {
        Role role = roleRepository.findByRoleCode("admin");
        if (role != null) return role;
        return roleRepository.findByRoleCode("ADMIN");
    }

    private void assignAllMenusToAdmin() {
        Role adminRole = findAdminRole();
        if (adminRole == null) return;

        List<RoleMenu> existing = roleMenuRepository.findByRoleId(adminRole.getId());
        if (!existing.isEmpty()) return;

        List<Menu> allMenus = menuRepository.findAll();
        for (Menu menu : allMenus) {
            RoleMenu rm = new RoleMenu();
            rm.setRoleId(adminRole.getId());
            rm.setMenuId(menu.getId());
            roleMenuRepository.save(rm);
        }
    }

    private void assignBusinessMenus() {
        Role businessRole = roleRepository.findByRoleCode("business");
        if (businessRole == null) businessRole = roleRepository.findByRoleCode("BUSINESS");
        if (businessRole == null) return;

        List<RoleMenu> existing = roleMenuRepository.findByRoleId(businessRole.getId());
        if (!existing.isEmpty()) return;

        String[] perms = {
            "track",
            "track-config", "track-config:view", "track-config:add", "track-config:edit", "track-config:delete",
            "api-interface", "api-interface:view", "api-interface:add", "api-interface:edit", "api-interface:delete",
            "track-data", "track-data:view",
            "system", "system-reset-data", "system-reset-data:view"
        };
        assignMenusByCode(businessRole.getId(), perms);
    }

    private void assignDeveloperMenus() {
        Role developerRole = roleRepository.findByRoleCode("developer");
        if (developerRole == null) developerRole = roleRepository.findByRoleCode("DEVELOPER");
        if (developerRole == null) return;

        List<RoleMenu> existing = roleMenuRepository.findByRoleId(developerRole.getId());
        if (!existing.isEmpty()) return;

        String[] perms = {
            "track",
            "track-config", "track-config:view",
            "api-interface", "api-interface:view",
            "track-data", "track-data:view", "track-data:clear"
        };
        assignMenusByCode(developerRole.getId(), perms);
    }

    private void assignMenusByCode(Long roleId, String[] menuCodes) {
        for (String code : menuCodes) {
            Menu menu = menuRepository.findByMenuCode(code);
            if (menu != null) {
                RoleMenu rm = new RoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menu.getId());
                roleMenuRepository.save(rm);
            }
        }
    }
}
