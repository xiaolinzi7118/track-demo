package com.track.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sys_menu")
public class Menu {
    @Id
    private Long id;

    private Long parentId;

    @Column(nullable = false, length = 100)
    private String menuName;

    @Column(unique = true, nullable = false, length = 100)
    private String menuCode;

    private String path;

    private String icon;

    private Integer sortOrder;

    @Column(nullable = false)
    private Integer menuType;

    private String perms;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Transient
    private List<Menu> children;

    @Transient
    private Boolean checked;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (status == null) {
            status = 1;
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
        if (parentId == null) {
            parentId = 0L;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }
    public String getMenuCode() { return menuCode; }
    public void setMenuCode(String menuCode) { this.menuCode = menuCode; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getMenuType() { return menuType; }
    public void setMenuType(Integer menuType) { this.menuType = menuType; }
    public String getPerms() { return perms; }
    public void setPerms(String perms) { this.perms = perms; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public List<Menu> getChildren() { return children; }
    public void setChildren(List<Menu> children) { this.children = children; }
    public Boolean getChecked() { return checked; }
    public void setChecked(Boolean checked) { this.checked = checked; }
}
