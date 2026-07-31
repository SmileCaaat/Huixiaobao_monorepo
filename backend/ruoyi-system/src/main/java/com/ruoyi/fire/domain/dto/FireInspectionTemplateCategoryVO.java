package com.ruoyi.fire.domain.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 统一「选择系统」一级类目（合并 template_type 0/1）。
 */
public class FireInspectionTemplateCategoryVO
{
    private String categoryKey;
    private String categoryName;
    private Integer sortOrder;
    private List<Long> maintenanceTemplateIds = new ArrayList<>();
    private List<Long> fireTestTemplateIds = new ArrayList<>();

    public String getCategoryKey()
    {
        return categoryKey;
    }

    public void setCategoryKey(String categoryKey)
    {
        this.categoryKey = categoryKey;
    }

    public String getCategoryName()
    {
        return categoryName;
    }

    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }

    public Integer getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder)
    {
        this.sortOrder = sortOrder;
    }

    public List<Long> getMaintenanceTemplateIds()
    {
        return maintenanceTemplateIds;
    }

    public void setMaintenanceTemplateIds(List<Long> maintenanceTemplateIds)
    {
        this.maintenanceTemplateIds = maintenanceTemplateIds;
    }

    public List<Long> getFireTestTemplateIds()
    {
        return fireTestTemplateIds;
    }

    public void setFireTestTemplateIds(List<Long> fireTestTemplateIds)
    {
        this.fireTestTemplateIds = fireTestTemplateIds;
    }
}
