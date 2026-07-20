package com.ruoyi.fire.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 维保任务对象 fire_maintenance_task
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public class FireMaintenanceTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long taskId;

    /** 任务名称 */
    @Excel(name = "任务名称")
    private String taskName;

    /** 消防单位ID */
    @Excel(name = "消防单位ID")
    private Long companyId;

    /** 消防单位名称 */
    @Excel(name = "消防单位名称")
    private String companyName;

    /** 计划开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "计划开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date planStartTime;

    /** 计划结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "计划结束时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date planEndTime;

    /** 实际开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "实际开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date actualStartTime;

    /** 实际完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "实际完成时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date actualEndTime;

    /** 任务状态（0=待开始 1=进行中 2=已完成 3=已逾期） */
    @Excel(name = "任务状态", readConverterExp = "0=待开始,1=进行中,2=已完成,3=已逾期")
    private String taskStatus;

    /** 总检查项数 */
    @Excel(name = "总检查项数")
    private Integer totalItems;

    /** 已完成项数 */
    @Excel(name = "已完成项数")
    private Integer completedItems;

    /** 正常项数 */
    @Excel(name = "正常项数")
    private Integer normalItems;

    /** 故障项数 */
    @Excel(name = "故障项数")
    private Integer faultItems;

    /** 无此设备项数 */
    @Excel(name = "无此设备项数")
    private Integer noDeviceItems;

    /** 执行人ID */
    @Excel(name = "执行人ID")
    private Long executorId;

    /** 执行人姓名 */
    @Excel(name = "执行人姓名")
    private String executorName;

    /** 任务类型（0=周期任务 1=临时任务） */
    @Excel(name = "任务类型", readConverterExp = "0=周期任务,1=临时任务")
    private String taskType;

    /** 周期类型（1=月度 2=季度 3=半年度 4=年度） */
    @Excel(name = "周期类型", readConverterExp = "1=月度,2=季度,3=半年度,4=年度")
    private String periodType;

    /** 周期年份 */
    @Excel(name = "周期年份")
    private Integer periodYear;

    /** 周期序号 */
    @Excel(name = "周期序号")
    private Integer periodNum;

    /** 项目负责人ID */
    @Excel(name = "项目负责人ID")
    private Long managerId;

    /** 项目负责人姓名 */
    @Excel(name = "项目负责人姓名")
    private String managerName;

    /** 项目负责人电话 */
    @Excel(name = "项目负责人电话")
    private String managerPhone;

    /** 维保操作员IDs（多个用逗号分隔） */
    private String operatorIds;

    /** 维保操作员姓名（多个用逗号分隔） */
    @Excel(name = "维保操作员")
    private String operatorNames;

    /** 关联建筑ID */
    @Excel(name = "关联建筑ID")
    private Long buildingId;

    /** 关联建筑名称 */
    @Excel(name = "关联建筑名称")
    private String buildingName;

    /** 维保情况简述 */
    private String maintenanceSummary;

    /** 维保时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date maintenanceTime;

    /** 系统列表（一级记录） */
    private List<FireMaintenanceRecord> systems;

    /** 选中的系统模板IDs（多个用逗号分隔） */
    private String selectedSystemIds;

    /** 选中的消防设施测试模板IDs（多个用逗号分隔） */
    private String selectedFireTestIds;

    public void setTaskId(Long taskId) 
    {
        this.taskId = taskId;
    }

    public Long getTaskId() 
    {
        return taskId;
    }

    public void setTaskName(String taskName) 
    {
        this.taskName = taskName;
    }

    public String getTaskName() 
    {
        return taskName;
    }

    public void setCompanyId(Long companyId) 
    {
        this.companyId = companyId;
    }

    public Long getCompanyId() 
    {
        return companyId;
    }

    public void setCompanyName(String companyName) 
    {
        this.companyName = companyName;
    }

    public String getCompanyName() 
    {
        return companyName;
    }

    public void setPlanStartTime(Date planStartTime) 
    {
        this.planStartTime = planStartTime;
    }

    public Date getPlanStartTime() 
    {
        return planStartTime;
    }

    public void setPlanEndTime(Date planEndTime) 
    {
        this.planEndTime = planEndTime;
    }

    public Date getPlanEndTime() 
    {
        return planEndTime;
    }

    public void setActualStartTime(Date actualStartTime) 
    {
        this.actualStartTime = actualStartTime;
    }

    public Date getActualStartTime() 
    {
        return actualStartTime;
    }

    public void setActualEndTime(Date actualEndTime) 
    {
        this.actualEndTime = actualEndTime;
    }

    public Date getActualEndTime() 
    {
        return actualEndTime;
    }

    public void setTaskStatus(String taskStatus) 
    {
        this.taskStatus = taskStatus;
    }

    public String getTaskStatus() 
    {
        return taskStatus;
    }

    public void setTotalItems(Integer totalItems) 
    {
        this.totalItems = totalItems;
    }

    public Integer getTotalItems() 
    {
        return totalItems;
    }

    public void setCompletedItems(Integer completedItems) 
    {
        this.completedItems = completedItems;
    }

    public Integer getCompletedItems() 
    {
        return completedItems;
    }

    public void setNormalItems(Integer normalItems) 
    {
        this.normalItems = normalItems;
    }

    public Integer getNormalItems() 
    {
        return normalItems;
    }

    public void setFaultItems(Integer faultItems) 
    {
        this.faultItems = faultItems;
    }

    public Integer getFaultItems() 
    {
        return faultItems;
    }

    public void setNoDeviceItems(Integer noDeviceItems) 
    {
        this.noDeviceItems = noDeviceItems;
    }

    public Integer getNoDeviceItems() 
    {
        return noDeviceItems;
    }

    public void setExecutorId(Long executorId) 
    {
        this.executorId = executorId;
    }

    public Long getExecutorId() 
    {
        return executorId;
    }

    public void setExecutorName(String executorName) 
    {
        this.executorName = executorName;
    }

    public String getExecutorName() 
    {
        return executorName;
    }

    public void setTaskType(String taskType) 
    {
        this.taskType = taskType;
    }

    public String getTaskType() 
    {
        return taskType;
    }

    public void setPeriodType(String periodType) 
    {
        this.periodType = periodType;
    }

    public String getPeriodType() 
    {
        return periodType;
    }

    public void setPeriodYear(Integer periodYear) 
    {
        this.periodYear = periodYear;
    }

    public Integer getPeriodYear() 
    {
        return periodYear;
    }

    public void setPeriodNum(Integer periodNum) 
    {
        this.periodNum = periodNum;
    }

    public Integer getPeriodNum() 
    {
        return periodNum;
    }

    public void setManagerId(Long managerId) 
    {
        this.managerId = managerId;
    }

    public Long getManagerId() 
    {
        return managerId;
    }

    public void setManagerName(String managerName) 
    {
        this.managerName = managerName;
    }

    public String getManagerName() 
    {
        return managerName;
    }

    public void setManagerPhone(String managerPhone) 
    {
        this.managerPhone = managerPhone;
    }

    public String getManagerPhone() 
    {
        return managerPhone;
    }

    public void setOperatorIds(String operatorIds) 
    {
        this.operatorIds = operatorIds;
    }

    public String getOperatorIds() 
    {
        return operatorIds;
    }

    public void setOperatorNames(String operatorNames) 
    {
        this.operatorNames = operatorNames;
    }

    public String getOperatorNames() 
    {
        return operatorNames;
    }

    public void setBuildingId(Long buildingId) 
    {
        this.buildingId = buildingId;
    }

    public Long getBuildingId() 
    {
        return buildingId;
    }

    public void setBuildingName(String buildingName) 
    {
        this.buildingName = buildingName;
    }

    public String getBuildingName() 
    {
        return buildingName;
    }

    public void setMaintenanceSummary(String maintenanceSummary) 
    {
        this.maintenanceSummary = maintenanceSummary;
    }

    public String getMaintenanceSummary() 
    {
        return maintenanceSummary;
    }

    public void setMaintenanceTime(Date maintenanceTime) 
    {
        this.maintenanceTime = maintenanceTime;
    }

    public Date getMaintenanceTime() 
    {
        return maintenanceTime;
    }

    public void setSystems(List<FireMaintenanceRecord> systems) 
    {
        this.systems = systems;
    }

    public List<FireMaintenanceRecord> getSystems() 
    {
        return systems;
    }

    public void setSelectedSystemIds(String selectedSystemIds) 
    {
        this.selectedSystemIds = selectedSystemIds;
    }

    public String getSelectedSystemIds() 
    {
        return selectedSystemIds;
    }

    public void setSelectedFireTestIds(String selectedFireTestIds) 
    {
        this.selectedFireTestIds = selectedFireTestIds;
    }

    public String getSelectedFireTestIds() 
    {
        return selectedFireTestIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("taskId", getTaskId())
            .append("taskName", getTaskName())
            .append("companyId", getCompanyId())
            .append("companyName", getCompanyName())
            .append("planStartTime", getPlanStartTime())
            .append("planEndTime", getPlanEndTime())
            .append("actualStartTime", getActualStartTime())
            .append("actualEndTime", getActualEndTime())
            .append("taskStatus", getTaskStatus())
            .append("totalItems", getTotalItems())
            .append("completedItems", getCompletedItems())
            .append("normalItems", getNormalItems())
            .append("faultItems", getFaultItems())
            .append("noDeviceItems", getNoDeviceItems())
            .append("executorId", getExecutorId())
            .append("executorName", getExecutorName())
            .append("taskType", getTaskType())
            .append("periodType", getPeriodType())
            .append("periodYear", getPeriodYear())
            .append("periodNum", getPeriodNum())
            .append("managerId", getManagerId())
            .append("managerName", getManagerName())
            .append("managerPhone", getManagerPhone())
            .append("operatorIds", getOperatorIds())
            .append("operatorNames", getOperatorNames())
            .append("buildingId", getBuildingId())
            .append("buildingName", getBuildingName())
            .append("maintenanceSummary", getMaintenanceSummary())
            .append("maintenanceTime", getMaintenanceTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
