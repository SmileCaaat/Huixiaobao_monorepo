package com.ruoyi.fire.service;

import java.util.List;
import com.ruoyi.fire.domain.FireCompany;
import com.ruoyi.fire.domain.FireMaintenanceTask;

/**
 * 维保任务Service接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public interface IFireMaintenanceTaskService {
    /**
     * 查询维保任务
     * 
     * @param taskId 维保任务主键
     * @return 维保任务
     */
    public FireMaintenanceTask selectFireMaintenanceTaskByTaskId(Long taskId);

    /**
     * 查询维保任务（按记录类型过滤系统列表）
     * 
     * @param taskId     维保任务主键
     * @param recordType 记录类型（0=常规维保 1=消防设施测试，null=全部）
     * @return 维保任务
     */
    public FireMaintenanceTask selectFireMaintenanceTaskByTaskId(Long taskId, String recordType);

    /**
     * 查询维保任务列表
     * 
     * @param fireMaintenanceTask 维保任务
     * @return 维保任务集合
     */
    public List<FireMaintenanceTask> selectFireMaintenanceTaskList(FireMaintenanceTask fireMaintenanceTask);

    /**
     * 新增维保任务（自动生成检查记录）
     * 
     * @param fireMaintenanceTask 维保任务
     * @return 结果
     */
    public int insertFireMaintenanceTask(FireMaintenanceTask fireMaintenanceTask);

    /**
     * 修改维保任务
     * 
     * @param fireMaintenanceTask 维保任务
     * @return 结果
     */
    public int updateFireMaintenanceTask(FireMaintenanceTask fireMaintenanceTask);

    /**
     * 批量删除维保任务
     * 
     * @param taskIds 需要删除的维保任务主键集合
     * @return 结果
     */
    public int deleteFireMaintenanceTaskByTaskIds(Long[] taskIds);

    /**
     * 删除维保任务信息
     * 
     * @param taskId 维保任务主键
     * @return 结果
     */
    public int deleteFireMaintenanceTaskByTaskId(Long taskId);

    /**
     * 获取所有模板数据（带缓存）
     * 
     * @return 模板列表
     */
    public List<com.ruoyi.fire.domain.FireMaintenanceTemplate> getAllTemplatesWithCache();

    /**
     * 查询用户作为负责人或操作员参与的任务所关联的公司列表（去重）
     *
     * @param userId 用户ID
     * @return 公司列表
     */
    public List<FireCompany> selectCompanyListByTaskUserId(Long userId);
}
