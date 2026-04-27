package com.track.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "track_config")
public class TrackConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventName;

    @Column(nullable = false)
    private String eventCode;

    @Column(nullable = false)
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 36)
    private String pageScreenshotFileId;

    @Column(columnDefinition = "TEXT")
    private String params;

    private String requirementId;

    @Column(columnDefinition = "TEXT")
    private String urlPattern;

    private Long deptId;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Transient
    private String requirementTitle;

    @Transient
    private String requirementStatus;

    @Transient
    private String requirementStatusLabel;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPageScreenshotFileId() {
        return pageScreenshotFileId;
    }

    public void setPageScreenshotFileId(String pageScreenshotFileId) {
        this.pageScreenshotFileId = pageScreenshotFileId;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(String requirementId) {
        this.requirementId = requirementId;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
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

    public String getRequirementTitle() {
        return requirementTitle;
    }

    public void setRequirementTitle(String requirementTitle) {
        this.requirementTitle = requirementTitle;
    }

    public String getRequirementStatus() {
        return requirementStatus;
    }

    public void setRequirementStatus(String requirementStatus) {
        this.requirementStatus = requirementStatus;
    }

    public String getRequirementStatusLabel() {
        return requirementStatusLabel;
    }

    public void setRequirementStatusLabel(String requirementStatusLabel) {
        this.requirementStatusLabel = requirementStatusLabel;
    }
}
