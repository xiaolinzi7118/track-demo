package com.track.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class RequirementResubmitRequest {
    private String requirementId;
    private String title;
    private String businessLineCode;
    private String priority;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectedOnlineDate;

    private String devTeamCode;
    private String description;

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

    public String getBusinessLineCode() {
        return businessLineCode;
    }

    public void setBusinessLineCode(String businessLineCode) {
        this.businessLineCode = businessLineCode;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDate getExpectedOnlineDate() {
        return expectedOnlineDate;
    }

    public void setExpectedOnlineDate(LocalDate expectedOnlineDate) {
        this.expectedOnlineDate = expectedOnlineDate;
    }

    public String getDevTeamCode() {
        return devTeamCode;
    }

    public void setDevTeamCode(String devTeamCode) {
        this.devTeamCode = devTeamCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

