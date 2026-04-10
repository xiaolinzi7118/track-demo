package com.track.controller;

import com.track.common.Result;
import com.track.entity.Menu;
import com.track.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping("/tree")
    public Result<List<Menu>> tree() {
        return Result.success(menuService.getMenuTree());
    }
}
