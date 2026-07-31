package com.ruoyi.fire.domain.dto;

/**
 * 消防维护模板二级设备类目（合并 template_type 0/1 后去重）。
 */
public class FireInspectionTemplateEquipmentVO
{
    private String equipmentKey;
    private String equipmentName;
    private Integer sortOrder;

    public String getEquipmentKey()
    {
        return equipmentKey;
    }

    public void setEquipmentKey(String equipmentKey)
    {
        this.equipmentKey = equipmentKey;
    }

    public String getEquipmentName()
    {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName)
    {
        this.equipmentName = equipmentName;
    }

    public Integer getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder)
    {
        this.sortOrder = sortOrder;
    }
}
