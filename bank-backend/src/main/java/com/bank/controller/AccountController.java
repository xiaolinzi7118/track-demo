package com.bank.controller;

import com.bank.common.Result;
import com.bank.service.AccountInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    private AccountInfoService accountInfoService;

    @GetMapping("/summary")
    public Result<Map<String, Object>> getSummary(@RequestParam Long userId) {
        return Result.success(accountInfoService.getSummary(userId));
    }
}
