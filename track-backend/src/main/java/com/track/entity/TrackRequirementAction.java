package com.track.entity;

public class TrackRequirementAction {
    private String actionType;
    private String targetStatus;
    private String targetStatusName;
    private String label;
    private Boolean needOpinion;

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getTargetStatus() {
        return targetStatus;
    }

    public void setTargetStatus(String targetStatus) {
        this.targetStatus = targetStatus;
    }

    public String getTargetStatusName() {
        return targetStatusName;
    }

    public void setTargetStatusName(String targetStatusName) {
        this.targetStatusName = targetStatusName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Boolean getNeedOpinion() {
        return needOpinion;
    }

    public void setNeedOpinion(Boolean needOpinion) {
        this.needOpinion = needOpinion;
    }
}

