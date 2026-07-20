package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.FireReportRecord;

/**
 * 维保报告记录Service接口
 * 
 * @author ruoyi
 * @date 2025-01-05
 */
public interface IFireReportRecordService {
    /**
     * 查询维保报告记录
     * 
     * @param reportId 维保报告记录ID
     * @return 维保报告记录
     */
    public FireReportRecord selectFireReportRecordById(Long reportId);

    /**
     * 查询维保报告记录列表
     * 
     * @param fireReportRecord 维保报告记录
     * @return 维保报告记录集合
     */
    public List<FireReportRecord> selectFireReportRecordList(FireReportRecord fireReportRecord);

    /**
     * 新增维保报告记录
     * 
     * @param fireReportRecord 维保报告记录
     * @return 结果
     */
    public int insertFireReportRecord(FireReportRecord fireReportRecord);

    /**
     * 修改维保报告记录
     * 
     * @param fireReportRecord 维保报告记录
     * @return 结果
     */
    public int updateFireReportRecord(FireReportRecord fireReportRecord);

    /**
     * 批量删除维保报告记录
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteFireReportRecordByIds(String ids);

    /**
     * 删除维保报告记录信息
     * 
     * @param reportId 维保报告记录ID
     * @return 结果
     */
    public int deleteFireReportRecordById(Long reportId);

    /**
     * 根据维保任务生成报告
     * 
     * @param taskId 维保任务ID
     * @return 生成的报告记录
     */
    public FireReportRecord generateReportForTask(Long taskId);
}
