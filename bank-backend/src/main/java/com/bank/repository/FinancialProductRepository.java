package com.bank.repository;

import com.bank.entity.FinancialProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FinancialProductRepository extends JpaRepository<FinancialProduct, Long>, JpaSpecificationExecutor<FinancialProduct> {
}
