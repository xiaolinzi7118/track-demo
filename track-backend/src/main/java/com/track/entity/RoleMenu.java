package com.track.entity;

import javax.persistence.*;

@Entity
@Table(name = "sys_role_menu")
public class RoleMenu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long roleId;

    private Long menuId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public Long getMenuId() { return menuId; }
    public void setMenuId(Long menuId) { this.menuId = menuId; }
}
