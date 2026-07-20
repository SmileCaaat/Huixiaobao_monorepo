package com.ruoyi.fire.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.fire.mapper.FireMaintenanceRecordMapper;
import com.ruoyi.fire.mapper.FireMaintenanceTaskMapper;
import com.ruoyi.fire.domain.FireMaintenanceRecord;
import com.ruoyi.fire.domain.FireMaintenanceTask;
import com.ruoyi.fire.service.IFireMaintenanceRecordService;

/**
 * 维保任务检查记录Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Service
public class FireMaintenanceRecordServiceImpl implements IFireMaintenanceRecordService {
    @Autowired
    private FireMaintenanceRecordMapper fireMaintenanceRecordMapper;

    @Autowired
    private FireMaintenanceTaskMapper fireMaintenanceTaskMapper;

    /**
     * 查询维保任务检查记录
     * 
     * @param recordId 维保任务检查记录主键
     * @return 维保任务检查记录
     */
    @Override
    public FireMaintenanceRecord selectFireMaintenanceRecordByRecordId(Long recordId) {
        return fireMaintenanceRecordMapper.selectFireMaintenanceRecordByRecordId(recordId);
    }

    /**
     * 查询维保任务检查记录列表
     * 
     * @param fireMaintenanceRecord 维保任务检查记录
     * @return 维保任务检查记录
     */
    @Override
    public List<FireMaintenanceRecord> selectFireMaintenanceRecordList(FireMaintenanceRecord fireMaintenanceRecord) {
        return fireMaintenanceRecordMapper.selectFireMaintenanceRecordList(fireMaintenanceRecord);
    }

    /**
     * 查询一级列表
     * 
     * @param taskId 任务ID
     * @return 维保任务检查记录集合
     */
    @Override
    public List<FireMaintenanceRecord> selectLevel1List(Long taskId) {
        return fireMaintenanceRecordMapper.selectByTaskIdAndLevel(taskId, 1);
    }

    /**
     * 查询二级列表
     * 
     * @param taskId         任务ID
     * @param parentRecordId 父级记录ID
     * @return 维保任务检查记录集合
     */
    @Override
    public List<FireMaintenanceRecord> selectLevel2List(Long taskId, Long parentRecordId) {
        return fireMaintenanceRecordMapper.selectByTaskIdAndParentId(taskId, parentRecordId, 2);
    }

    /**
     * 查询三级列表
     * 
     * @param taskId         任务ID
     * @param parentRecordId 父级记录ID
     * @return 维保任务检查记录集合
     */
    @Override
    public List<FireMaintenanceRecord> selectLevel3List(Long taskId, Long parentRecordId) {
        return fireMaintenanceRecordMapper.selectByTaskIdAndParentId(taskId, parentRecordId, 3);
    }

    /**
     * 新增维保任务检查记录
     * 
     * @param fireMaintenanceRecord 维保任务检查记录
     * @return 结果
     */
    @Override
    public int insertFireMaintenanceRecord(FireMaintenanceRecord fireMaintenanceRecord) {
        return fireMaintenanceRecordMapper.insertFireMaintenanceRecord(fireMaintenanceRecord);
    }

    /**
     * 修改维保任务检查记录
     * 
     * @param fireMaintenanceRecord 维保任务检查记录
     * @return 结果
     */
    @Override
    public int updateFireMaintenanceRecord(FireMaintenanceRecord fireMaintenanceRecord) {
        return fireMaintenanceRecordMapper.updateFireMaintenanceRecord(fireMaintenanceRecord);
    }

    /**
     * 更新检查结果（自动更新任务统计）
     * 
     * @param fireMaintenanceRecord 维保任务检查记录
     * @return 结果
     */
    @Override
    @Transactional
    public int updateCheckResult(FireMaintenanceRecord fireMaintenanceRecord) {
        // 1. 如果没有taskId，从数据库查询获取
        Long taskId = fireMaintenanceRecord.getTaskId();
        if (taskId == null && fireMaintenanceRecord.getRecordId() != null) {
            FireMaintenanceRecord existing = fireMaintenanceRecordMapper
                    .selectFireMaintenanceRecordByRecordId(fireMaintenanceRecord.getRecordId());
            if (existing != null) {
                taskId = existing.getTaskId();
            }
        }

        // 2. 更新检查记录
        fireMaintenanceRecord.setCheckTime(new Date());
        int rows = fireMaintenanceRecordMapper.updateFireMaintenanceRecord(fireMaintenanceRecord);

        // 3. 更新任务统计
        if (taskId != null) {
            updateTaskStatistics(taskId);
        }

        return rows;
    }

    /**
     * 更新任务统计信息
     */
    private void updateTaskStatistics(Long taskId) {
        // 查询所有三级项目的检查结果
        List<FireMaintenanceRecord> level3Records = fireMaintenanceRecordMapper.selectLevel3ByTaskId(taskId);

        int completed = 0;
        int normal = 0;
        int fault = 0;
        int noDevice = 0;

        for (FireMaintenanceRecord record : level3Records) {
            String result = record.getCheckResult();
            if (!"0".equals(result)) {
                completed++;
            }
            if ("1".equals(result)) {
                normal++;
            } else if ("2".equals(result)) {
                fault++;
            } else if ("3".equals(result)) {
                noDevice++;
            }
        }

        // 更新任务统计
        FireMaintenanceTask task = new FireMaintenanceTask();
        task.setTaskId(taskId);
        task.setCompletedItems(completed);
        task.setNormalItems(normal);
        task.setFaultItems(fault);
        task.setNoDeviceItems(noDevice);

        // 更新任务状态
        if (completed == level3Records.size() && level3Records.size() > 0) {
            task.setTaskStatus("2"); // 已完成
            task.setActualEndTime(new Date());
        } else if (completed > 0) {
            task.setTaskStatus("1"); // 进行中
            // 如果是第一次检查，设置实际开始时间
            FireMaintenanceTask existingTask = fireMaintenanceTaskMapper.selectFireMaintenanceTaskByTaskId(taskId);
            if (existingTask != null && existingTask.getActualStartTime() == null) {
                task.setActualStartTime(new Date());
            }
        }

        fireMaintenanceTaskMapper.updateFireMaintenanceTask(task);
    }

    /**
     * 批量删除维保任务检查记录
     * 
     * @param recordIds 需要删除的维保任务检查记录主键
     * @return 结果
     */
    @Override
    public int deleteFireMaintenanceRecordByRecordIds(Long[] recordIds) {
        return fireMaintenanceRecordMapper.deleteFireMaintenanceRecordByRecordIds(recordIds);
    }

    /**
     * 删除维保任务检查记录信息
     * 
     * @param recordId 维保任务检查记录主键
     * @return 结果
     */
    @Override
    public int deleteFireMaintenanceRecordByRecordId(Long recordId) {
        return fireMaintenanceRecordMapper.deleteFireMaintenanceRecordByRecordId(recordId);
    }

    /**
     * 一键完成：将任务的所有未完成检查项标记为正常，并更新任务统计
     */
    @Override
    @Transactional
    public int completeAllByTaskId(Long taskId) {
        int rows = fireMaintenanceRecordMapper.completeAllByTaskId(taskId);
        updateTaskStatistics(taskId);
        return rows;
    }
}
