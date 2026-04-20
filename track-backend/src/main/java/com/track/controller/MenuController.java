package com.track.controller;

import com.track.common.Result;
import com.track.entity.Menu;
import com.track.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping("/tree")
    public Result<List<Menu>> getMenuTree() {
        return Result.success(menuService.getMenuTree());
    }

    @GetMapping("/user-menus")
    public Result<List<Menu>> getUserMenus(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        return Result.success(menuService.getMenusByUserId(userId));
    }

    @GetMapping("/user-permissions")
    public Result<List<String>> getUserPermissions(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        return Result.success(menuService.getPermissionsByUserId(userId));
    }
}
