package com.ruoyi.quartz.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.IFireReportRecordService;

/**
 * 维保报告生成定时任务
 * 
 * @author ruoyi
 */
@Component("fireReportTask")
public class FireReportTask {
    @Autowired
    private IFireReportRecordService fireReportRecordService;

    /**
     * 执行报告生成
     * 
     * @param taskId 维保任务ID
     */
    public void execute(String taskId) {
        if (StringUtils.isNotEmpty(taskId)) {
            System.out.println("开始执行维保报告生成任务，任务ID：" + taskId);
            try {
                fireReportRecordService.generateReportForTask(Long.valueOf(taskId));
                System.out.println("维保报告生成任务执行成功");
            } catch (Exception e) {
                System.err.println("维保报告生成任务执行失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
