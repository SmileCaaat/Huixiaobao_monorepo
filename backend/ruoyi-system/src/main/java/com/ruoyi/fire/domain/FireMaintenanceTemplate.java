package com.ruoyi.fire.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 维保模板对象 fire_maintenance_template
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public class FireMaintenanceTemplate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 层级（1=一级 2=二级 3=三级） */
    @Excel(name = "层级", readConverterExp = "1=一级,2=二级,3=三级")
    private Integer level;

    /** 父级ID */
    @Excel(name = "父级ID")
    private Long parentId;

    /** 项目名称 */
    @Excel(name = "项目名称")
    private String itemName;

    /** 项目编码 */
    @Excel(name = "项目编码")
    private String itemCode;

    /** 排序 */
    @Excel(name = "排序")
    private Integer sortOrder;

    /** 模板类型（0=常规维保 1=消防设施测试） */
    @Excel(name = "模板类型", readConverterExp = "0=常规维保,1=消防设施测试")
    private String templateType;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setLevel(Integer level) 
    {
        this.level = level;
    }

    public Integer getLevel() 
    {
        return level;
    }

    public void setParentId(Long parentId) 
    {
        this.parentId = parentId;
    }

    public Long getParentId() 
    {
        return parentId;
    }

    public void setItemName(String itemName) 
    {
        this.itemName = itemName;
    }

    public String getItemName() 
    {
        return itemName;
    }

    public void setItemCode(String itemCode) 
    {
        this.itemCode = itemCode;
    }

    public String getItemCode() 
    {
        return itemCode;
    }

    public void setSortOrder(Integer sortOrder) 
    {
        this.sortOrder = sortOrder;
    }

    public Integer getSortOrder() 
    {
        return sortOrder;
    }

    public void setTemplateType(String templateType) 
    {
        this.templateType = templateType;
    }

    public String getTemplateType() 
    {
        return templateType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("level", getLevel())
            .append("parentId", getParentId())
            .append("itemName", getItemName())
            .append("itemCode", getItemCode())
            .append("sortOrder", getSortOrder())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .toString();
    }
}
