package com.ruoyi.fire.domain.dto;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.fire.domain.FireMaintenanceRecord;

/**
 * Ѳ�����һ����Ŀ���飨ͬҵ����ϲ� type0/type1 ��Դ����
 */
public class FireInspectionTestCategoryGroup
{
    private String categoryKey;
    private String categoryName;
    private Integer sortOrder;
    private Long maintenanceRecordId;
    private Long fireTestRecordId;
    private List<FireMaintenanceRecord> sourceRecords = new ArrayList<>();
    private List<FireInspectionTestEquipmentGroup> equipments = new ArrayList<>();
    private Integer totalItems = 0;
    private Integer completedItems = 0;
    private Integer uncompletedItems = 0;
    private String status;

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

    public Long getMaintenanceRecordId()
    {
        return maintenanceRecordId;
    }

    public void setMaintenanceRecordId(Long maintenanceRecordId)
    {
        this.maintenanceRecordId = maintenanceRecordId;
    }

    public Long getFireTestRecordId()
    {
        return fireTestRecordId;
    }

    public void setFireTestRecordId(Long fireTestRecordId)
    {
        this.fireTestRecordId = fireTestRecordId;
    }

    public List<FireMaintenanceRecord> getSourceRecords()
    {
        return sourceRecords;
    }

    public void setSourceRecords(List<FireMaintenanceRecord> sourceRecords)
    {
        this.sourceRecords = sourceRecords;
    }

    public List<FireInspectionTestEquipmentGroup> getEquipments()
    {
        return equipments;
    }

    public void setEquipments(List<FireInspectionTestEquipmentGroup> equipments)
    {
        this.equipments = equipments;
    }

    public Integer getTotalItems()
    {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems)
    {
        this.totalItems = totalItems;
    }

    public Integer getCompletedItems()
    {
        return completedItems;
    }

    public void setCompletedItems(Integer completedItems)
    {
        this.completedItems = completedItems;
    }

    public Integer getUncompletedItems()
    {
        return uncompletedItems;
    }

    public void setUncompletedItems(Integer uncompletedItems)
    {
        this.uncompletedItems = uncompletedItems;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}
