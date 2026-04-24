package com.track.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "track_requirement")
public class TrackRequirement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String requirementId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(nullable = false, length = 8)
    private String priority;

    @Column(nullable = false, length = 64)
    private String businessLineCode;

    @Column(nullable = false, length = 128)
    private String businessLineName;

    @Column(nullable = false, length = 64)
    private String devTeamCode;

    @Column(nullable = false, length = 128)
    private String devTeamName;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectedOnlineDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Long proposerId;

    @Column(nullable = false, length = 64)
    private String proposerName;

    @Column(nullable = false, length = 128)
    private String department;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Transient
    private List<TrackRequirementAction> availableActions;

    @Transient
    private List<TrackLog> logs;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(String requirementId) {
        this.requirementId = requirementId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getBusinessLineCode() {
        return businessLineCode;
    }

    public void setBusinessLineCode(String businessLineCode) {
        this.businessLineCode = businessLineCode;
    }

    public String getBusinessLineName() {
        return businessLineName;
    }

    public void setBusinessLineName(String businessLineName) {
        this.businessLineName = businessLineName;
    }

    public String getDevTeamCode() {
        return devTeamCode;
    }

    public void setDevTeamCode(String devTeamCode) {
        this.devTeamCode = devTeamCode;
    }

    public String getDevTeamName() {
        return devTeamName;
    }

    public void setDevTeamName(String devTeamName) {
        this.devTeamName = devTeamName;
    }

    public LocalDate getExpectedOnlineDate() {
        return expectedOnlineDate;
    }

    public void setExpectedOnlineDate(LocalDate expectedOnlineDate) {
        this.expectedOnlineDate = expectedOnlineDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getProposerId() {
        return proposerId;
    }

    public void setProposerId(Long proposerId) {
        this.proposerId = proposerId;
    }

    public String getProposerName() {
        return proposerName;
    }

    public void setProposerName(String proposerName) {
        this.proposerName = proposerName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public List<TrackRequirementAction> getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(List<TrackRequirementAction> availableActions) {
        this.availableActions = availableActions;
    }

    public List<TrackLog> getLogs() {
        return logs;
    }

    public void setLogs(List<TrackLog> logs) {
        this.logs = logs;
    }
}

