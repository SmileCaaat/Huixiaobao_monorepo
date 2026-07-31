package com.ruoyi.fire.domain.dto;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.fire.domain.FireMaintenanceTask;

/**
 * Ѳ�����ͳһ���飨������Ϣ + ȥ����Ŀ����
 */
public class FireInspectionTestDetailVO
{
    private FireMaintenanceTask taskInfo;
    private List<FireInspectionTestCategoryGroup> categories = new ArrayList<>();
    private Integer totalItems = 0;
    private Integer completedItems = 0;
    private Integer uncompletedItems = 0;

    public FireMaintenanceTask getTaskInfo()
    {
        return taskInfo;
    }

    public void setTaskInfo(FireMaintenanceTask taskInfo)
    {
        this.taskInfo = taskInfo;
    }

    public List<FireInspectionTestCategoryGroup> getCategories()
    {
        return categories;
    }

    public void setCategories(List<FireInspectionTestCategoryGroup> categories)
    {
        this.categories = categories;
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
}
