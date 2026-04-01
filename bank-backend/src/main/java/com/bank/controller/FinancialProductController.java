package com.bank.controller;

import com.bank.common.Result;
import com.bank.entity.FinancialProduct;
import com.bank.service.FinancialProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/financial-product")
public class FinancialProductController {

    @Autowired
    private FinancialProductService productService;

    @GetMapping("/list")
    public Result<Page<FinancialProduct>> getList(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(productService.getList(category, pageNum, pageSize));
    }

    @GetMapping("/detail")
    public Result<FinancialProduct> getDetail(@RequestParam Long id) {
        FinancialProduct product = productService.getDetail(id);
        if (product == null) {
            return Result.error("产品不存在");
        }
        return Result.success(product);
    }
}
