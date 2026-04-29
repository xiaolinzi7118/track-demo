package com.track.entity;

public class TrackRequirementAction {
    private String actionType;
    private String targetStatus;
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

    public Boolean getNeedOpinion() {
        return needOpinion;
    }

    public void setNeedOpinion(Boolean needOpinion) {
        this.needOpinion = needOpinion;
    }
}

