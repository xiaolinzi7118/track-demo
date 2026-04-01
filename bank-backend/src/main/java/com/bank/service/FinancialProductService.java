package com.bank.service;

import com.bank.entity.FinancialProduct;
import com.bank.repository.FinancialProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
public class FinancialProductService {

    @Autowired
    private FinancialProductRepository productRepository;

    @PostConstruct
    public void init() {
        if (productRepository.count() == 0) {
            seedProducts();
        }
    }

    private void seedProducts() {
        Object[][] data = {
                {"招银理财日日享1号", "稳健型", 3.25, "灵活申赎", "低风险", 1000.0, "现金管理类理财产品，申赎灵活"},
                {"金葵花稳健增值计划", "稳健型", 4.10, "180天", "低风险", 5000.0, "中短期稳健理财，适合保守型投资者"},
                {"招银月月宝", "稳健型", 3.80, "30天", "低风险", 1000.0, "月度开放式理财，每月可赎回"},
                {"沪深300指数增强", "进取型", 12.50, "365天", "中高风险", 10000.0, "跟踪沪深300指数，追求超额收益"},
                {"科创主题混合基金", "进取型", 15.20, "灵活申赎", "中风险", 5000.0, "聚焦科技创新主题，长期增长潜力"},
                {"招商中证白酒指数", "基金", 8.75, "灵活申赎", "中风险", 1000.0, "跟踪中证白酒指数，行业主题基金"},
                {"金生康健重疾险", "保险", 0.0, "20年", "低风险", 3000.0, "保障120种重大疾病，返还保费"},
                {"安心宝意外保障", "保险", 0.0, "1年", "低风险", 100.0, "全面意外保障，高性价比"}
        };
        for (Object[] d : data) {
            FinancialProduct p = new FinancialProduct();
            p.setName((String) d[0]);
            p.setCategory((String) d[1]);
            p.setAnnualRate((Double) d[2]);
            p.setTerm((String) d[3]);
            p.setRiskLevel((String) d[4]);
            p.setMinAmount((Double) d[5]);
            p.setDescription((String) d[6]);
            p.setStatus(1);
            productRepository.save(p);
        }
    }

    public Page<FinancialProduct> getList(String category, Integer pageNum, Integer pageSize) {
        Specification<FinancialProduct> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), 1));
            if (category != null && !category.isEmpty()) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return productRepository.findAll(spec, PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "id")));
    }

    public FinancialProduct getDetail(Long id) {
        return productRepository.findById(id).orElse(null);
    }
}
