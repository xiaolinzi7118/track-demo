package com.track.dto;

public class RoleMenuItemResponse {
    private Long id;
    private String menuCode;

    public RoleMenuItemResponse() {
    }

    public RoleMenuItemResponse(Long id, String menuCode) {
        this.id = id;
        this.menuCode = menuCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMenuCode() {
        return menuCode;
    }

    public void setMenuCode(String menuCode) {
        this.menuCode = menuCode;
    }
}
