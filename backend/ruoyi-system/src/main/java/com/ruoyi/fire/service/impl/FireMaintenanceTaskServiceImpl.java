package com.ruoyi.fire.service.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.fire.mapper.FireMaintenanceTaskMapper;
import com.ruoyi.fire.mapper.FireMaintenanceTemplateMapper;
import com.ruoyi.fire.mapper.FireMaintenanceRecordMapper;
import com.ruoyi.fire.domain.FireCompany;
import com.ruoyi.fire.domain.FireMaintenanceTask;
import com.ruoyi.fire.domain.FireMaintenanceTemplate;
import com.ruoyi.fire.domain.FireMaintenanceRecord;
import com.ruoyi.fire.service.IFireMaintenanceTaskService;

/**
 * 维保任务Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Service
public class FireMaintenanceTaskServiceImpl implements IFireMaintenanceTaskService {
    @Autowired
    private FireMaintenanceTaskMapper fireMaintenanceTaskMapper;

    @Autowired
    private FireMaintenanceTemplateMapper fireMaintenanceTemplateMapper;

    @Autowired
    private FireMaintenanceRecordMapper fireMaintenanceRecordMapper;

    /**
     * 查询维保任务
     * 
     * @param taskId 维保任务主键
     * @return 维保任务
     */
    @Override
    public FireMaintenanceTask selectFireMaintenanceTaskByTaskId(Long taskId) {
        return selectFireMaintenanceTaskByTaskId(taskId, null);
    }

    /**
     * 查询维保任务（按记录类型过滤系统列表）
     * 
     * @param taskId     任务ID
     * @param recordType 记录类型（0=常规维保 1=消防设施测试，null=全部）
     */
    @Override
    public FireMaintenanceTask selectFireMaintenanceTaskByTaskId(Long taskId, String recordType) {
        FireMaintenanceTask task = fireMaintenanceTaskMapper.selectFireMaintenanceTaskByTaskId(taskId);
        if (task != null) {
            // 查询一级记录（系统列表）
            FireMaintenanceRecord query = new FireMaintenanceRecord();
            query.setTaskId(taskId);
            query.setLevel(1);
            if (recordType != null) {
                query.setRecordType(recordType);
            }
            List<FireMaintenanceRecord> systems = fireMaintenanceRecordMapper.selectFireMaintenanceRecordList(query);

            // 为每个系统统计检查项数据
            for (FireMaintenanceRecord system : systems) {
                calculateSystemStats(system, taskId);
            }

            task.setSystems(systems);
        }
        return task;
    }

    /**
     * 计算系统的统计数据
     */
    private void calculateSystemStats(FireMaintenanceRecord system, Long taskId) {
        // 查询该系统下的所有三级检查项
        List<FireMaintenanceRecord> allRecords = fireMaintenanceRecordMapper.selectRecordsByTaskId(taskId);

        int totalItems = 0;
        int completedItems = 0;
        int uncompletedItems = 0;

        // 找出该系统下的所有三级检查项
        for (FireMaintenanceRecord record : allRecords) {
            if (record.getLevel() == 3 && isUnderSystem(record, system.getRecordId(), allRecords)) {
                totalItems++;
                if (!"0".equals(record.getCheckResult())) {
                    completedItems++;
                } else {
                    uncompletedItems++;
                }
            }
        }

        system.setTotalItems(totalItems);
        system.setCompletedItems(completedItems);
        system.setUncompletedItems(uncompletedItems);
        system.setSystemStatus(uncompletedItems == 0 && totalItems > 0 ? "1" : "0");
    }

    /**
     * 判断记录是否属于指定系统
     */
    private boolean isUnderSystem(FireMaintenanceRecord record, Long systemRecordId,
            List<FireMaintenanceRecord> allRecords) {
        if (record.getParentRecordId() == null) {
            return false;
        }

        // 查找父记录
        for (FireMaintenanceRecord parent : allRecords) {
            if (parent.getRecordId().equals(record.getParentRecordId())) {
                if (parent.getLevel() == 1) {
                    // 父记录是一级，判断是否是目标系统
                    return parent.getRecordId().equals(systemRecordId);
                } else {
                    // 父记录不是一级，继续向上查找
                    return isUnderSystem(parent, systemRecordId, allRecords);
                }
            }
        }

        return false;
    }

    /**
     * 查询维保任务列表
     * 
     * @param fireMaintenanceTask 维保任务
     * @return 维保任务
     */
    @Override
    public List<FireMaintenanceTask> selectFireMaintenanceTaskList(FireMaintenanceTask fireMaintenanceTask) {
        return fireMaintenanceTaskMapper.selectFireMaintenanceTaskList(fireMaintenanceTask);
    }

    /**
     * 新增维保任务（自动生成检查记录）- 优化版
     * 
     * @param fireMaintenanceTask 维保任务
     * @return 结果
     */
    @Override
    @Transactional
    public int insertFireMaintenanceTask(FireMaintenanceTask fireMaintenanceTask) {
        // 1. 插入任务基本信息
        int rows = fireMaintenanceTaskMapper.insertFireMaintenanceTask(fireMaintenanceTask);

        // 2. 查询所有模板数据（使用缓存）
        List<FireMaintenanceTemplate> templates = getAllTemplatesWithCache();

        // 3. 获取选中的系统ID列表
        Set<Long> selectedSystemIds = new HashSet<>();
        if (fireMaintenanceTask.getSelectedSystemIds() != null
                && !fireMaintenanceTask.getSelectedSystemIds().isEmpty()) {
            String[] ids = fireMaintenanceTask.getSelectedSystemIds().split(",");
            for (String id : ids) {
                try {
                    selectedSystemIds.add(Long.parseLong(id.trim()));
                } catch (NumberFormatException e) {
                    // 忽略无效ID
                }
            }
        }

        // 获取选中的消防设施测试ID列表
        Set<Long> selectedFireTestIds = new HashSet<>();
        if (fireMaintenanceTask.getSelectedFireTestIds() != null
                && !fireMaintenanceTask.getSelectedFireTestIds().isEmpty()) {
            String[] ids = fireMaintenanceTask.getSelectedFireTestIds().split(",");
            for (String id : ids) {
                try {
                    selectedFireTestIds.add(Long.parseLong(id.trim()));
                } catch (NumberFormatException e) {
                    // 忽略无效ID
                }
            }
        }

        // 4. 按层级和类型分组
        List<FireMaintenanceTemplate> level1Templates = new ArrayList<>();
        List<FireMaintenanceTemplate> level2Templates = new ArrayList<>();
        List<FireMaintenanceTemplate> level3Templates = new ArrayList<>();

        // 消防设施测试的模板
        List<FireMaintenanceTemplate> fireTestLevel1Templates = new ArrayList<>();
        List<FireMaintenanceTemplate> fireTestLevel2Templates = new ArrayList<>();
        List<FireMaintenanceTemplate> fireTestLevel3Templates = new ArrayList<>();

        int totalLevel3 = 0;

        for (FireMaintenanceTemplate template : templates) {
            // 区分常规维保和消防设施测试
            boolean isFireTest = "1".equals(template.getTemplateType());

            if (isFireTest) {
                // 消防设施测试模板
                if (template.getLevel() == 1) {
                    // 如果指定了选中的消防设施测试系统，只添加选中的
                    if (selectedFireTestIds.isEmpty() || selectedFireTestIds.contains(template.getId())) {
                        fireTestLevel1Templates.add(template);
                    }
                } else if (template.getLevel() == 2) {
                    fireTestLevel2Templates.add(template);
                } else if (template.getLevel() == 3) {
                    fireTestLevel3Templates.add(template);
                }
            } else {
                // 常规维保模板
                if (template.getLevel() == 1) {
                    // 如果指定了选中的系统，只添加选中的
                    if (selectedSystemIds.isEmpty() || selectedSystemIds.contains(template.getId())) {
                        level1Templates.add(template);
                    }
                } else if (template.getLevel() == 2) {
                    level2Templates.add(template);
                } else if (template.getLevel() == 3) {
                    level3Templates.add(template);
                }
            }
        }

        // 5. 批量生成检查记录
        Map<Long, Long> templateToRecordMap = new HashMap<>();
        Set<Long> selectedLevel1TemplateIds = new HashSet<>();

        // 批量处理一级（使用批量插入）
        List<FireMaintenanceRecord> level1Records = new ArrayList<>();
        for (FireMaintenanceTemplate template : level1Templates) {
            FireMaintenanceRecord record = createRecordFromTemplate(template, fireMaintenanceTask.getTaskId(), null,
                    "0");
            level1Records.add(record);
            selectedLevel1TemplateIds.add(template.getId());
        }
        if (!level1Records.isEmpty()) {
            fireMaintenanceRecordMapper.batchInsertFireMaintenanceRecord(level1Records);
            // 批量插入后需要查询回来获取生成的ID
            FireMaintenanceRecord query = new FireMaintenanceRecord();
            query.setTaskId(fireMaintenanceTask.getTaskId());
            query.setLevel(1);
            List<FireMaintenanceRecord> insertedLevel1 = fireMaintenanceRecordMapper
                    .selectFireMaintenanceRecordList(query);
            for (FireMaintenanceRecord record : insertedLevel1) {
                templateToRecordMap.put(record.getTemplateId(), record.getRecordId());
            }
        }

        // 批量处理二级（只处理选中的一级系统下的二级）
        List<FireMaintenanceRecord> level2Records = new ArrayList<>();
        Set<Long> selectedLevel2TemplateIds = new HashSet<>();

        for (FireMaintenanceTemplate template : level2Templates) {
            // 检查父级是否在选中的一级系统中
            if (selectedLevel1TemplateIds.contains(template.getParentId())) {
                Long parentRecordId = templateToRecordMap.get(template.getParentId());
                if (parentRecordId != null) {
                    FireMaintenanceRecord record = createRecordFromTemplate(template, fireMaintenanceTask.getTaskId(),
                            parentRecordId, "0");
                    level2Records.add(record);
                    selectedLevel2TemplateIds.add(template.getId());
                }
            }
        }
        if (!level2Records.isEmpty()) {
            fireMaintenanceRecordMapper.batchInsertFireMaintenanceRecord(level2Records);
            // 批量插入后需要查询回来获取生成的ID
            FireMaintenanceRecord query = new FireMaintenanceRecord();
            query.setTaskId(fireMaintenanceTask.getTaskId());
            query.setLevel(2);
            List<FireMaintenanceRecord> insertedLevel2 = fireMaintenanceRecordMapper
                    .selectFireMaintenanceRecordList(query);
            for (FireMaintenanceRecord record : insertedLevel2) {
                templateToRecordMap.put(record.getTemplateId(), record.getRecordId());
            }
        }

        // 批量处理三级（只处理选中的系统下的三级）
        List<FireMaintenanceRecord> level3Records = new ArrayList<>();
        for (FireMaintenanceTemplate template : level3Templates) {
            // 检查父级（二级）是否在已生成的二级记录中
            if (selectedLevel2TemplateIds.contains(template.getParentId())) {
                Long parentRecordId = templateToRecordMap.get(template.getParentId());
                if (parentRecordId != null) {
                    FireMaintenanceRecord record = createRecordFromTemplate(template, fireMaintenanceTask.getTaskId(),
                            parentRecordId, "0");
                    level3Records.add(record);
                    totalLevel3++;
                }
            }
        }
        if (!level3Records.isEmpty()) {
            fireMaintenanceRecordMapper.batchInsertFireMaintenanceRecord(level3Records);
        }

        // 生成消防设施测试的检查记录（不选默认全部，与常规维保一致）
        if (!fireTestLevel1Templates.isEmpty()) {
            // 处理消防设施测试的一级
            List<FireMaintenanceRecord> fireTestL1Records = new ArrayList<>();
            for (FireMaintenanceTemplate template : fireTestLevel1Templates) {
                FireMaintenanceRecord record = createRecordFromTemplate(template, fireMaintenanceTask.getTaskId(),
                        null, "1");
                fireTestL1Records.add(record);
            }
            if (!fireTestL1Records.isEmpty()) {
                fireMaintenanceRecordMapper.batchInsertFireMaintenanceRecord(fireTestL1Records);
                // 查询回来获取生成的ID
                FireMaintenanceRecord query = new FireMaintenanceRecord();
                query.setTaskId(fireMaintenanceTask.getTaskId());
                query.setLevel(1);
                List<FireMaintenanceRecord> insertedFireTestL1 = fireMaintenanceRecordMapper
                        .selectFireMaintenanceRecordList(query);
                for (FireMaintenanceRecord record : insertedFireTestL1) {
                    // 只处理消防设施测试的记录
                    if (fireTestLevel1Templates.stream().anyMatch(t -> t.getId().equals(record.getTemplateId()))) {
                        templateToRecordMap.put(record.getTemplateId(), record.getRecordId());
                    }
                }
            }

            // 处理消防设施测试的二级（只处理选中的一级系统下的二级）
            List<FireMaintenanceRecord> fireTestL2Records = new ArrayList<>();
            Set<Long> selectedFireTestL2TemplateIds = new HashSet<>();

            for (FireMaintenanceTemplate template : fireTestLevel2Templates) {
                // 检查父级是否在选中的消防设施测试一级系统中
                if (fireTestLevel1Templates.stream().anyMatch(t -> t.getId().equals(template.getParentId()))) {
                    Long parentRecordId = templateToRecordMap.get(template.getParentId());
                    if (parentRecordId != null) {
                        FireMaintenanceRecord record = createRecordFromTemplate(template,
                                fireMaintenanceTask.getTaskId(), parentRecordId, "1");
                        fireTestL2Records.add(record);
                        selectedFireTestL2TemplateIds.add(template.getId());
                    }
                }
            }
            if (!fireTestL2Records.isEmpty()) {
                fireMaintenanceRecordMapper.batchInsertFireMaintenanceRecord(fireTestL2Records);
                // 查询回来获取生成的ID
                FireMaintenanceRecord query = new FireMaintenanceRecord();
                query.setTaskId(fireMaintenanceTask.getTaskId());
                query.setLevel(2);
                List<FireMaintenanceRecord> insertedFireTestL2 = fireMaintenanceRecordMapper
                        .selectFireMaintenanceRecordList(query);
                for (FireMaintenanceRecord record : insertedFireTestL2) {
                    // 只处理消防设施测试的记录
                    if (selectedFireTestL2TemplateIds.contains(record.getTemplateId())) {
                        templateToRecordMap.put(record.getTemplateId(), record.getRecordId());
                    }
                }
            }

            // 处理消防设施测试的三级（只处理选中的系统下的三级）
            List<FireMaintenanceRecord> fireTestL3Records = new ArrayList<>();
            for (FireMaintenanceTemplate template : fireTestLevel3Templates) {
                // 检查父级（二级）是否在已生成的二级记录中
                if (selectedFireTestL2TemplateIds.contains(template.getParentId())) {
                    Long parentRecordId = templateToRecordMap.get(template.getParentId());
                    if (parentRecordId != null) {
                        FireMaintenanceRecord record = createRecordFromTemplate(template,
                                fireMaintenanceTask.getTaskId(), parentRecordId, "1");
                        fireTestL3Records.add(record);
                        totalLevel3++;
                    }
                }
            }
            if (!fireTestL3Records.isEmpty()) {
                fireMaintenanceRecordMapper.batchInsertFireMaintenanceRecord(fireTestL3Records);
            }
        }

        // 6. 更新任务的总项数
        fireMaintenanceTask.setTotalItems(totalLevel3);
        fireMaintenanceTask.setCompletedItems(0);
        fireMaintenanceTask.setNormalItems(0);
        fireMaintenanceTask.setFaultItems(0);
        fireMaintenanceTask.setNoDeviceItems(0);
        fireMaintenanceTaskMapper.updateFireMaintenanceTask(fireMaintenanceTask);

        return rows;
    }

    /**
     * 获取所有模板数据（带缓存）
     * 模板数据不会变化，使用永久缓存
     */
    @Cacheable(value = "maintenanceTemplates", key = "'all'")
    public List<FireMaintenanceTemplate> getAllTemplatesWithCache() {
        return fireMaintenanceTemplateMapper.selectAllTemplates();
    }

    /**
     * 从模板创建检查记录
     */
    private FireMaintenanceRecord createRecordFromTemplate(FireMaintenanceTemplate template, Long taskId,
            Long parentRecordId, String recordType) {
        FireMaintenanceRecord record = new FireMaintenanceRecord();
        record.setTaskId(taskId);
        record.setTemplateId(template.getId());
        record.setLevel(template.getLevel());
        record.setParentRecordId(parentRecordId);
        record.setItemName(template.getItemName());
        record.setItemCode(template.getItemCode());
        record.setCheckResult("0"); // 默认待检查
        record.setSortOrder(template.getSortOrder());
        record.setRecordType(recordType);
        return record;
    }

    /**
     * 修改维保任务
     * 
     * @param fireMaintenanceTask 维保任务
     * @return 结果
     */
    @Override
    public int updateFireMaintenanceTask(FireMaintenanceTask fireMaintenanceTask) {
        return fireMaintenanceTaskMapper.updateFireMaintenanceTask(fireMaintenanceTask);
    }

    /**
     * 批量删除维保任务
     * 
     * @param taskIds 需要删除的维保任务主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteFireMaintenanceTaskByTaskIds(Long[] taskIds) {
        // 先删除关联的检查记录
        for (Long taskId : taskIds) {
            fireMaintenanceRecordMapper.deleteFireMaintenanceRecordByTaskId(taskId);
        }
        // 再删除任务
        return fireMaintenanceTaskMapper.deleteFireMaintenanceTaskByTaskIds(taskIds);
    }

    /**
     * 删除维保任务信息
     * 
     * @param taskId 维保任务主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteFireMaintenanceTaskByTaskId(Long taskId) {
        // 先删除关联的检查记录
        fireMaintenanceRecordMapper.deleteFireMaintenanceRecordByTaskId(taskId);
        // 再删除任务
        return fireMaintenanceTaskMapper.deleteFireMaintenanceTaskByTaskId(taskId);
    }

    /**
     * 查询用户作为负责人或操作员参与的任务所关联的公司列表（去重）
     *
     * @param userId 用户ID
     * @return 公司列表
     */
    @Override
    public List<FireCompany> selectCompanyListByTaskUserId(Long userId) {
        return fireMaintenanceTaskMapper.selectCompanyListByTaskUserId(userId);
    }
}

