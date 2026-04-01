package com.bank.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_product")
public class FinancialProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(name = "annual_rate")
    private Double annualRate;

    private String term;

    @Column(name = "risk_level")
    private String riskLevel;

    @Column(name = "min_amount")
    private Double minAmount;

    private Integer status = 1;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Double getAnnualRate() { return annualRate; }
    public void setAnnualRate(Double annualRate) { this.annualRate = annualRate; }
    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public Double getMinAmount() { return minAmount; }
    public void setMinAmount(Double minAmount) { this.minAmount = minAmount; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
