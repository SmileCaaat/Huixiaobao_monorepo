package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 维保报告记录对象 fire_report_record
 * 
 * @author ruoyi
 * @date 2025-01-05
 */
public class FireReportRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 报告ID */
    private Long reportId;

    /** 关联维保任务ID */
    @Excel(name = "关联维保任务ID")
    private Long taskId;

    /** 维保任务名称 */
    @Excel(name = "维保任务名称")
    private String taskName;

    /** 报告名称 */
    @Excel(name = "报告名称")
    private String reportName;

    /** 文件路径 */
    @Excel(name = "文件路径")
    private String filePath;

    /** 文件大小 */
    @Excel(name = "文件大小")
    private Long fileSize;

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getReportName() {
        return reportName;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Long getFileSize() {
        return fileSize;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("reportId", getReportId())
                .append("taskId", getTaskId())
                .append("taskName", getTaskName())
                .append("reportName", getReportName())
                .append("filePath", getFilePath())
                .append("fileSize", getFileSize())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("remark", getRemark())
                .toString();
    }
}
