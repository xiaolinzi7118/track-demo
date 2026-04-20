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
        initRoles();
        initMenus();
        initAdminUser();
    }

    private void initRoles() {
        if (roleRepository.count() > 0) return;

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
        if (menuRepository.count() > 0) return;

        List<Menu> menus = new ArrayList<>();

        // 仪表盘
        Menu dashboard = createMenu(0L, "仪表盘", "dashboard", "/dashboard", "House", 1, 2, null);
        menus.add(dashboard);
        menus.add(createMenu(null, "查看", "dashboard:view", null, null, 1, 3, "dashboard:view"));

        // 埋点配置
        Menu trackConfig = createMenu(0L, "埋点配置", "track-config", "/track-config", "Setting", 2, 2, null);
        menus.add(trackConfig);
        menus.add(createMenu(null, "查看", "track-config:view", null, null, 1, 3, "track-config:view"));
        menus.add(createMenu(null, "新增", "track-config:add", null, null, 2, 3, "track-config:add"));
        menus.add(createMenu(null, "编辑", "track-config:edit", null, null, 3, 3, "track-config:edit"));
        menus.add(createMenu(null, "删除", "track-config:delete", null, null, 4, 3, "track-config:delete"));

        // 接口来源管理
        Menu apiInterface = createMenu(0L, "接口来源管理", "api-interface", "/api-interface", "Link", 3, 2, null);
        menus.add(apiInterface);
        menus.add(createMenu(null, "查看", "api-interface:view", null, null, 1, 3, "api-interface:view"));
        menus.add(createMenu(null, "新增", "api-interface:add", null, null, 2, 3, "api-interface:add"));
        menus.add(createMenu(null, "编辑", "api-interface:edit", null, null, 3, 3, "api-interface:edit"));
        menus.add(createMenu(null, "删除", "api-interface:delete", null, null, 4, 3, "api-interface:delete"));

        // 数据回检
        Menu trackData = createMenu(0L, "数据回检", "track-data", "/track-data", "DataLine", 4, 2, null);
        menus.add(trackData);
        menus.add(createMenu(null, "查看", "track-data:view", null, null, 1, 3, "track-data:view"));
        menus.add(createMenu(null, "清空", "track-data:clear", null, null, 2, 3, "track-data:clear"));

        // 系统管理
        Menu system = createMenu(0L, "系统管理", "system", "/system", "Tools", 5, 1, null);
        menus.add(system);

        // 用户管理
        Menu systemUser = createMenu(null, "用户管理", "system-user", "/system/user", "User", 1, 2, null);
        menus.add(systemUser);
        menus.add(createMenu(null, "查看", "system-user:view", null, null, 1, 3, "system-user:view"));
        menus.add(createMenu(null, "新增", "system-user:add", null, null, 2, 3, "system-user:add"));
        menus.add(createMenu(null, "编辑", "system-user:edit", null, null, 3, 3, "system-user:edit"));
        menus.add(createMenu(null, "删除", "system-user:delete", null, null, 4, 3, "system-user:delete"));

        // 角色管理
        Menu systemRole = createMenu(null, "角色管理", "system-role", "/system/role", "Lock", 2, 2, null);
        menus.add(systemRole);
        menus.add(createMenu(null, "查看", "system-role:view", null, null, 1, 3, "system-role:view"));
        menus.add(createMenu(null, "新增", "system-role:add", null, null, 2, 3, "system-role:add"));
        menus.add(createMenu(null, "编辑", "system-role:edit", null, null, 3, 3, "system-role:edit"));
        menus.add(createMenu(null, "删除", "system-role:delete", null, null, 4, 3, "system-role:delete"));

        // 重置数据
        Menu resetData = createMenu(null, "重置数据", "system-reset-data", "/system/reset-data", "Delete", 3, 2, null);
        menus.add(resetData);
        menus.add(createMenu(null, "查看", "system-reset-data:view", null, null, 1, 3, "system-reset-data:view"));
        menus.add(createMenu(null, "操作", "system-reset-data:operate", null, null, 2, 3, "system-reset-data:operate"));

        // 保存所有菜单，先保存父级再保存子级
        // 第一轮：保存目录和顶级页面（parentId=0）
        List<Menu> savedTopLevel = new ArrayList<>();
        for (Menu m : menus) {
            if (m.getParentId() != null && m.getParentId() == 0L) {
                savedTopLevel.add(menuRepository.save(m));
            }
        }

        // 建立menuCode到id的映射
        Map<String, Long> codeToId = new HashMap<>();
        for (Menu m : savedTopLevel) {
            codeToId.put(m.getMenuCode(), m.getId());
        }

        // 第二轮：保存子页面和按钮，设置正确的parentId
        Map<String, Long> pageToId = new HashMap<>();
        for (Menu m : menus) {
            if (m.getParentId() == null) {
                // 判断属于哪个父级页面
                Long parentId = resolveParentId(m.getMenuCode(), codeToId);
                if (parentId != null) {
                    m.setParentId(parentId);
                    Menu saved = menuRepository.save(m);
                    pageToId.put(m.getMenuCode(), saved.getId());
                }
            }
        }

        // 第三轮：保存按钮级别，按钮属于页面
        for (Menu m : menus) {
            if (m.getParentId() == null) {
                Long parentId = resolveButtonParentId(m.getMenuCode(), pageToId, codeToId);
                if (parentId != null) {
                    m.setParentId(parentId);
                    menuRepository.save(m);
                }
            }
        }

        // 给admin角色分配所有菜单
        assignAllMenusToAdmin();
        assignBusinessMenus();
        assignDeveloperMenus();
    }

    private Long resolveParentId(String menuCode, Map<String, Long> codeToId) {
        if (menuCode.startsWith("system-user")) return codeToId.get("system");
        if (menuCode.startsWith("system-role")) return codeToId.get("system");
        if (menuCode.startsWith("system-reset-data")) return codeToId.get("system");
        if (menuCode.startsWith("dashboard")) return codeToId.get("dashboard");
        if (menuCode.startsWith("track-config")) return codeToId.get("track-config");
        if (menuCode.startsWith("api-interface")) return codeToId.get("api-interface");
        if (menuCode.startsWith("track-data")) return codeToId.get("track-data");
        return null;
    }

    private Long resolveButtonParentId(String menuCode, Map<String, Long> pageToId, Map<String, Long> codeToId) {
        // 按钮的menuCode格式: "xxx:yyy"，对应的页面menuCode为 "xxx"
        String pageCode = menuCode.contains(":") ? menuCode.substring(0, menuCode.indexOf(':')) : null;
        if (pageCode != null) {
            // 先查pageToId（已保存的子页面），再查codeToId（顶级页面）
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

    private void assignAllMenusToAdmin() {
        Role adminRole = roleRepository.findByRoleCode("admin");
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
        if (businessRole == null) return;

        List<RoleMenu> existing = roleMenuRepository.findByRoleId(businessRole.getId());
        if (!existing.isEmpty()) return;

        // 业务人员：仪表盘(全部), 埋点配置(全部), 接口来源管理(全部), 数据回检(查看), 重置数据(查看)
        String[] perms = {
            "dashboard", "dashboard:view",
            "track-config", "track-config:view", "track-config:add", "track-config:edit", "track-config:delete",
            "api-interface", "api-interface:view", "api-interface:add", "api-interface:edit", "api-interface:delete",
            "track-data", "track-data:view",
            "system", "system-reset-data", "system-reset-data:view"
        };
        assignMenusByCode(businessRole.getId(), perms);
    }

    private void assignDeveloperMenus() {
        Role developerRole = roleRepository.findByRoleCode("developer");
        if (developerRole == null) return;

        List<RoleMenu> existing = roleMenuRepository.findByRoleId(developerRole.getId());
        if (!existing.isEmpty()) return;

        // 开发人员：仪表盘(全部), 埋点配置(查看), 数据回检(全部), 接口来源管理(查看)
        String[] perms = {
            "dashboard", "dashboard:view",
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

    private void initAdminUser() {
        User admin = userRepository.findByUsername("admin");
        if (admin == null) return;

        List<UserRole> existing = userRoleRepository.findByUserId(admin.getId());
        if (!existing.isEmpty()) return;

        Role adminRole = roleRepository.findByRoleCode("admin");
        if (adminRole == null) return;

        UserRole ur = new UserRole();
        ur.setUserId(admin.getId());
        ur.setRoleId(adminRole.getId());
        userRoleRepository.save(ur);
    }
}
