package com.ruoyi.fire.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.fire.mapper.FireMaintenanceTaskMapper;
import com.ruoyi.fire.mapper.FireMaintenanceTemplateMapper;
import com.ruoyi.fire.mapper.FireMaintenanceRecordMapper;
import com.ruoyi.fire.domain.FireCompany;
import com.ruoyi.fire.domain.FireMaintenanceTask;
import com.ruoyi.fire.domain.FireMaintenanceTemplate;
import com.ruoyi.fire.domain.FireMaintenanceRecord;
import com.ruoyi.fire.domain.dto.FireInspectionTestCategoryGroup;
import com.ruoyi.fire.domain.dto.FireInspectionTestDetailVO;
import com.ruoyi.fire.domain.dto.FireInspectionTestEquipmentGroup;
import com.ruoyi.fire.service.IFireMaintenanceTaskService;
import com.ruoyi.fire.service.support.FireInspectionTestGroupKeys;
import com.ruoyi.fire.service.support.FireInspectionTestKeys;

/**
 * 维保任务Service业务层处理
 *
 * @author ruoyi
 * @date 2024-01-01
 */
@Service
public class FireMaintenanceTaskServiceImpl implements IFireMaintenanceTaskService {
    private static final Logger log = LoggerFactory.getLogger(FireMaintenanceTaskServiceImpl.class);

    @Autowired
    private FireMaintenanceTaskMapper fireMaintenanceTaskMapper;

    @Autowired
    private FireMaintenanceTemplateMapper fireMaintenanceTemplateMapper;

    @Autowired
    private FireMaintenanceRecordMapper fireMaintenanceRecordMapper;

    @Override
    public FireMaintenanceTask selectFireMaintenanceTaskByTaskId(Long taskId) {
        return selectFireMaintenanceTaskByTaskId(taskId, null);
    }

    @Override
    public FireMaintenanceTask selectFireMaintenanceTaskByTaskId(Long taskId, String recordType) {
        FireMaintenanceTask task = fireMaintenanceTaskMapper.selectFireMaintenanceTaskByTaskId(taskId);
        if (task != null) {
            List<FireMaintenanceTemplate> templates = getAllTemplatesWithCache();
            if (task.getSelectedSystemIds() == null) {
                task.setSelectedSystemIds(toCsv(resolveSelectedLevel1Ids(null, templates, false, taskId, "0")));
            }
            if (task.getSelectedFireTestIds() == null) {
                task.setSelectedFireTestIds(toCsv(resolveSelectedLevel1Ids(null, templates, true, taskId, "1")));
            }
            FireMaintenanceRecord query = new FireMaintenanceRecord();
            query.setTaskId(taskId);
            query.setLevel(1);
            if (recordType != null) {
                query.setRecordType(recordType);
            }
            List<FireMaintenanceRecord> systems = fireMaintenanceRecordMapper.selectFireMaintenanceRecordList(query);
            for (FireMaintenanceRecord system : systems) {
                calculateSystemStats(system, taskId);
            }
            task.setSystems(systems);
        }
        return task;
    }

    private void calculateSystemStats(FireMaintenanceRecord system, Long taskId) {
        List<FireMaintenanceRecord> allRecords = fireMaintenanceRecordMapper.selectRecordsByTaskId(taskId);
        int totalItems = 0;
        int completedItems = 0;
        int uncompletedItems = 0;
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

    private boolean isUnderSystem(FireMaintenanceRecord record, Long systemRecordId,
            List<FireMaintenanceRecord> allRecords) {
        if (record.getParentRecordId() == null) {
            return false;
        }
        for (FireMaintenanceRecord parent : allRecords) {
            if (parent.getRecordId().equals(record.getParentRecordId())) {
                if (parent.getLevel() == 1) {
                    return parent.getRecordId().equals(systemRecordId);
                }
                return isUnderSystem(parent, systemRecordId, allRecords);
            }
        }
        return false;
    }

    @Override
    public List<FireMaintenanceTask> selectFireMaintenanceTaskList(FireMaintenanceTask fireMaintenanceTask) {
        return fireMaintenanceTaskMapper.selectFireMaintenanceTaskList(fireMaintenanceTask);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertFireMaintenanceTask(FireMaintenanceTask fireMaintenanceTask) {
        int rows = fireMaintenanceTaskMapper.insertFireMaintenanceTask(fireMaintenanceTask);
        Long taskId = fireMaintenanceTask.getTaskId();

        List<FireMaintenanceTemplate> templates = getAllTemplatesWithCache();
        Set<Long> selectedSystemIds = parseIdSet(fireMaintenanceTask.getSelectedSystemIds());
        Set<Long> selectedFireTestIds = parseIdSet(fireMaintenanceTask.getSelectedFireTestIds());

        // 空选择：与历史新增语义一致，默认全部一级模板
        if (selectedSystemIds.isEmpty()) {
            selectedSystemIds = collectLevel1Ids(templates, false);
        }
        if (selectedFireTestIds.isEmpty()) {
            selectedFireTestIds = collectLevel1Ids(templates, true);
        }

        Set<Long> existingTemplateIds = new HashSet<>();
        int added = generateMissingRecordsForLevel1Ids(taskId, selectedSystemIds, "0", templates, existingTemplateIds);
        added += generateMissingRecordsForLevel1Ids(taskId, selectedFireTestIds, "1", templates, existingTemplateIds);
        Set<Long> selectedUpkeepIds = collectRelatedLevel1Ids(templates, selectedSystemIds, "2");
        added += generateMissingRecordsForLevel1Ids(taskId, selectedUpkeepIds, "2", templates, existingTemplateIds);

        int[] counts = refreshTaskStatistics(taskId);
        log.info("创建维保任务 taskId={}, systems={}, fireTests={}, addedRecords={}, completed={}/{}",
                taskId, selectedSystemIds, selectedFireTestIds, added, counts[0], counts[1]);
        return rows;
    }

    @Cacheable(value = "maintenanceTemplates", key = "'all'")
    public List<FireMaintenanceTemplate> getAllTemplatesWithCache() {
        return fireMaintenanceTemplateMapper.selectAllTemplates();
    }

    private FireMaintenanceRecord createRecordFromTemplate(FireMaintenanceTemplate template, Long taskId,
            Long parentRecordId, String recordType) {
        FireMaintenanceRecord record = new FireMaintenanceRecord();
        record.setTaskId(taskId);
        record.setTemplateId(template.getId());
        record.setLevel(template.getLevel());
        record.setParentRecordId(parentRecordId);
        record.setItemName(template.getItemName());
        record.setItemCode(template.getItemCode());
        record.setCheckResult("0");
        record.setSortOrder(template.getSortOrder());
        record.setRecordType(recordType);
        return record;
    }

    /**
     * 修改维保任务：保存主表选择，并按差异增量同步检查记录与完成度
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateFireMaintenanceTask(FireMaintenanceTask fireMaintenanceTask) {
        if (fireMaintenanceTask == null || fireMaintenanceTask.getTaskId() == null) {
            throw new ServiceException("任务ID不能为空");
        }
        FireMaintenanceTask existing = fireMaintenanceTaskMapper
                .selectFireMaintenanceTaskByTaskId(fireMaintenanceTask.getTaskId());
        if (existing == null) {
            throw new ServiceException("维保任务不存在");
        }

        boolean syncSelection = fireMaintenanceTask.getParams() != null
                && Boolean.TRUE.equals(fireMaintenanceTask.getParams().get("fullEdit"));

        Long taskId = fireMaintenanceTask.getTaskId();
        if (!syncSelection) {
            return fireMaintenanceTaskMapper.updateFireMaintenanceTask(fireMaintenanceTask);
        }

        List<FireMaintenanceTemplate> templates = getAllTemplatesWithCache();

        Set<Long> oldSystemIds = resolveSelectedLevel1Ids(existing.getSelectedSystemIds(), templates, false, taskId,
                "0");
        Set<Long> oldFireIds = resolveSelectedLevel1Ids(existing.getSelectedFireTestIds(), templates, true, taskId,
                "1");
        Set<Long> newSystemIds = resolveSelectedLevel1Ids(fireMaintenanceTask.getSelectedSystemIds(), templates, false,
                taskId, "0");
        Set<Long> newFireIds = resolveSelectedLevel1Ids(fireMaintenanceTask.getSelectedFireTestIds(), templates, true,
                taskId, "1");

        Set<Long> addSystems = diff(newSystemIds, oldSystemIds);
        Set<Long> removeSystems = diff(oldSystemIds, newSystemIds);
        Set<Long> addFires = diff(newFireIds, oldFireIds);
        Set<Long> removeFires = diff(oldFireIds, newFireIds);
        Set<Long> oldUpkeepIds = collectRelatedLevel1Ids(templates, oldSystemIds, "2");
        Set<Long> newUpkeepIds = collectRelatedLevel1Ids(templates, newSystemIds, "2");
        Set<Long> addUpkeep = diff(newUpkeepIds, oldUpkeepIds);
        Set<Long> removeUpkeep = diff(oldUpkeepIds, newUpkeepIds);

        validateSelectedLevel1Ids(newSystemIds, templates, false, "维保系统");
        validateSelectedLevel1Ids(newFireIds, templates, true, "消防设施测试");
        assertRemovableLevel1Trees(taskId, removeSystems, "0");
        assertRemovableLevel1Trees(taskId, removeFires, "1");
        assertRemovableLevel1Trees(taskId, removeUpkeep, "2");

        int rows = fireMaintenanceTaskMapper.updateFireMaintenanceTask(fireMaintenanceTask);
        int normalized = normalizeHistoricalRecordTypes(taskId, templates);

        List<FireMaintenanceRecord> existingRecords = fireMaintenanceRecordMapper.selectRecordsByTaskId(taskId);
        Set<Long> existingTemplateIds = existingRecords.stream()
                .map(FireMaintenanceRecord::getTemplateId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        int added = generateMissingRecordsForLevel1Ids(taskId, addSystems, "0", templates, existingTemplateIds);
        added += generateMissingRecordsForLevel1Ids(taskId, addFires, "1", templates, existingTemplateIds);
        added += generateMissingRecordsForLevel1Ids(taskId, addUpkeep, "2", templates, existingTemplateIds);

        int removed = removeEmptyLevel1Trees(taskId, removeSystems, "0");
        removed += removeEmptyLevel1Trees(taskId, removeFires, "1");
        removed += removeEmptyLevel1Trees(taskId, removeUpkeep, "2");

        int[] counts = refreshTaskStatistics(taskId);
        String syncMessage = String.format("新增检查记录%d条，清理未填写记录%d条，完成度%d/%d",
                added, removed, counts[0], counts[1]);
        if (normalized > 0) {
            syncMessage += "；修正历史记录类型" + normalized + "条";
        }
        fireMaintenanceTask.getParams().put("syncMessage", syncMessage);

        log.info(
                "编辑维保任务 taskId={}, oldSystems={}, newSystems={}, addSystems={}, removeSystems={}, oldFires={}, newFires={}, addFires={}, removeFires={}, added={}, removed={}, completed={}/{}",
                taskId, oldSystemIds, newSystemIds, addSystems, removeSystems, oldFireIds, newFireIds, addFires,
                removeFires, added, removed, counts[0], counts[1]);
        return rows;
    }

    @Override
    public int updateTaskBriefing(Long taskId, String maintenanceSummary, Date maintenanceTime, String updateBy) {
        if (taskId == null) {
            throw new ServiceException("任务ID不能为空");
        }
        FireMaintenanceTask existing = fireMaintenanceTaskMapper.selectFireMaintenanceTaskByTaskId(taskId);
        if (existing == null) {
            throw new ServiceException("维保任务不存在");
        }
        if (StringUtils.isEmpty(maintenanceSummary)) {
            throw new ServiceException("维保情况简述不能为空");
        }
        if (maintenanceTime == null) {
            throw new ServiceException("维保时间不能为空");
        }
        return fireMaintenanceTaskMapper.updateTaskBriefing(taskId, maintenanceSummary, maintenanceTime, updateBy);
    }

    /**
     * 旧版单条插入曾漏写 record_type；按模板类型修正当前任务的历史记录。
     */
    private int normalizeHistoricalRecordTypes(Long taskId, List<FireMaintenanceTemplate> templates) {
        Map<Long, FireMaintenanceTemplate> templateMap = templates.stream()
                .collect(Collectors.toMap(FireMaintenanceTemplate::getId, t -> t, (a, b) -> a));
        int normalized = 0;
        for (FireMaintenanceRecord record : fireMaintenanceRecordMapper.selectRecordsByTaskId(taskId)) {
            FireMaintenanceTemplate template = templateMap.get(record.getTemplateId());
            if (template == null) {
                continue;
            }
            String expected = normalizeTemplateType(template);
            if (!expected.equals(record.getRecordType())) {
                FireMaintenanceRecord update = new FireMaintenanceRecord();
                update.setRecordId(record.getRecordId());
                update.setRecordType(expected);
                fireMaintenanceRecordMapper.updateFireMaintenanceRecord(update);
                normalized++;
            }
        }
        return normalized;
    }

    private Set<Long> parseIdSet(String csv) {
        Set<Long> ids = new HashSet<>();
        if (StringUtils.isEmpty(csv)) {
            return ids;
        }
        for (String part : csv.split(",")) {
            if (StringUtils.isEmpty(part)) {
                continue;
            }
            try {
                ids.add(Long.parseLong(part.trim()));
            } catch (NumberFormatException ignored) {
                // ignore
            }
        }
        return ids;
    }

    private String toCsv(Set<Long> ids) {
        return ids.stream().sorted().map(String::valueOf).collect(Collectors.joining(","));
    }

    private Set<Long> diff(Set<Long> left, Set<Long> right) {
        Set<Long> result = new HashSet<>(left);
        result.removeAll(right);
        return result;
    }

    private Set<Long> collectLevel1Ids(List<FireMaintenanceTemplate> templates, boolean fireTest) {
        Set<Long> ids = new HashSet<>();
        String expectedType = fireTest ? "1" : "0";
        for (FireMaintenanceTemplate template : templates) {
            if (template.getLevel() != null && template.getLevel() == 1
                    && expectedType.equals(normalizeTemplateType(template))) {
                ids.add(template.getId());
            }
        }
        return ids;
    }

    private String normalizeTemplateType(FireMaintenanceTemplate template) {
        if (template == null || template.getTemplateType() == null) {
            return "0";
        }
        return StringUtils.isEmpty(template.getTemplateType()) ? "0" : template.getTemplateType();
    }

    /**
     * 保养模板不单独增加任务选择字段，而是跟随已选择的巡查一级类目。
     */
    private Set<Long> collectRelatedLevel1Ids(List<FireMaintenanceTemplate> templates,
            Set<Long> selectedPatrolIds, String templateType) {
        Set<String> selectedNames = templates.stream()
                .filter(t -> t.getLevel() != null && t.getLevel() == 1)
                .filter(t -> selectedPatrolIds.contains(t.getId()))
                .map(t -> FireInspectionTestKeys.normalizeText(t.getItemName()).toLowerCase())
                .collect(Collectors.toSet());
        return templates.stream()
                .filter(t -> t.getLevel() != null && t.getLevel() == 1)
                .filter(t -> templateType.equals(normalizeTemplateType(t)))
                .filter(t -> selectedNames.contains(
                        FireInspectionTestKeys.normalizeText(t.getItemName()).toLowerCase()))
                .map(FireMaintenanceTemplate::getId)
                .collect(Collectors.toSet());
    }

    /**
     * 空串=清空；有值=解析；null=按现有记录推断，无记录则默认全部
     */
    private Set<Long> resolveSelectedLevel1Ids(String csv, List<FireMaintenanceTemplate> templates, boolean fireTest,
            Long taskId, String recordType) {
        if (csv != null && csv.trim().isEmpty()) {
            return new HashSet<>();
        }
        if (StringUtils.isNotEmpty(csv)) {
            return parseIdSet(csv);
        }
        List<FireMaintenanceRecord> records = fireMaintenanceRecordMapper.selectRecordsByTaskId(taskId);
        Map<Long, FireMaintenanceTemplate> templateMap = templates.stream()
                .collect(Collectors.toMap(FireMaintenanceTemplate::getId, t -> t, (a, b) -> a));
        String expectedType = fireTest ? "1" : "0";
        Set<Long> fromRecords = records.stream()
                .filter(r -> r.getLevel() != null && r.getLevel() == 1)
                // 历史消防测试一级记录曾因插入 SQL 漏写 record_type 被标成 0，
                // 因此回显以模板真实类型为准，不能只信记录上的旧值。
                .filter(r -> {
                    FireMaintenanceTemplate template = templateMap.get(r.getTemplateId());
                    return template != null && expectedType.equals(normalizeTemplateType(template));
                })
                .map(FireMaintenanceRecord::getTemplateId)
                .collect(Collectors.toSet());
        if (!fromRecords.isEmpty()) {
            return fromRecords;
        }
        return collectLevel1Ids(templates, fireTest);
    }

    private void validateSelectedLevel1Ids(Set<Long> ids, List<FireMaintenanceTemplate> templates,
            boolean fireTest, String moduleName) {
        String expectedType = fireTest ? "1" : "0";
        Map<Long, FireMaintenanceTemplate> templateMap = templates.stream()
                .collect(Collectors.toMap(FireMaintenanceTemplate::getId, t -> t, (a, b) -> a));
        List<Long> invalidIds = ids.stream().filter(id -> {
            FireMaintenanceTemplate template = templateMap.get(id);
            return template == null || template.getLevel() == null || template.getLevel() != 1
                    || !expectedType.equals(normalizeTemplateType(template));
        }).collect(Collectors.toList());
        if (!invalidIds.isEmpty()) {
            throw new ServiceException(moduleName + "包含无效类目ID：" + invalidIds);
        }
    }

    /**
     * 为指定一级模板增量生成检查记录（幂等：已存在 templateId 则跳过）
     */
    private int generateMissingRecordsForLevel1Ids(Long taskId, Set<Long> level1Ids, String recordType,
            List<FireMaintenanceTemplate> templates, Set<Long> existingTemplateIds) {
        if (level1Ids == null || level1Ids.isEmpty()) {
            return 0;
        }
        int added = 0;
        Map<Long, FireMaintenanceTemplate> templateMap = templates.stream()
                .collect(Collectors.toMap(FireMaintenanceTemplate::getId, t -> t, (a, b) -> a));

        for (Long level1Id : level1Ids) {
            if (existingTemplateIds.contains(level1Id)) {
                continue;
            }
            FireMaintenanceTemplate level1 = templateMap.get(level1Id);
            if (level1 == null || level1.getLevel() == null || level1.getLevel() != 1) {
                continue;
            }
            if (!normalizeTemplateType(level1).equals(recordType)) {
                continue;
            }

            FireMaintenanceRecord l1Record = createRecordFromTemplate(level1, taskId, null, recordType);
            fireMaintenanceRecordMapper.insertFireMaintenanceRecord(l1Record);
            existingTemplateIds.add(level1Id);
            added++;

            List<FireMaintenanceTemplate> level2List = templates.stream()
                    .filter(t -> t.getLevel() != null && t.getLevel() == 2)
                    .filter(t -> level1Id.equals(t.getParentId()))
                    .filter(t -> recordType.equals(normalizeTemplateType(t)))
                    .collect(Collectors.toList());

            for (FireMaintenanceTemplate level2 : level2List) {
                if (existingTemplateIds.contains(level2.getId())) {
                    continue;
                }
                FireMaintenanceRecord l2Record = createRecordFromTemplate(level2, taskId, l1Record.getRecordId(),
                        recordType);
                fireMaintenanceRecordMapper.insertFireMaintenanceRecord(l2Record);
                existingTemplateIds.add(level2.getId());
                added++;

                List<FireMaintenanceTemplate> level3List = templates.stream()
                        .filter(t -> t.getLevel() != null && t.getLevel() == 3)
                        .filter(t -> level2.getId().equals(t.getParentId()))
                        .filter(t -> recordType.equals(normalizeTemplateType(t)))
                        .collect(Collectors.toList());
                List<FireMaintenanceRecord> l3Records = new ArrayList<>();
                for (FireMaintenanceTemplate level3 : level3List) {
                    if (existingTemplateIds.contains(level3.getId())) {
                        continue;
                    }
                    l3Records.add(createRecordFromTemplate(level3, taskId, l2Record.getRecordId(), recordType));
                    existingTemplateIds.add(level3.getId());
                }
                if (!l3Records.isEmpty()) {
                    fireMaintenanceRecordMapper.batchInsertFireMaintenanceRecord(l3Records);
                    added += l3Records.size();
                }
            }
        }
        return added;
    }

    /**
     * 移除未填写内容的一级系统整棵检查树；已有结果则保留
     */
    private int removeEmptyLevel1Trees(Long taskId, Set<Long> removeLevel1Ids, String recordType) {
        if (removeLevel1Ids == null || removeLevel1Ids.isEmpty()) {
            return 0;
        }
        List<FireMaintenanceRecord> all = fireMaintenanceRecordMapper.selectRecordsByTaskId(taskId);
        int removed = 0;
        for (Long level1TemplateId : removeLevel1Ids) {
            FireMaintenanceRecord l1 = all.stream()
                    .filter(r -> level1TemplateId.equals(r.getTemplateId()))
                    .filter(r -> r.getLevel() != null && r.getLevel() == 1)
                    .findFirst()
                    .orElse(null);
            if (l1 == null) {
                continue;
            }
            List<FireMaintenanceRecord> subtree = new ArrayList<>();
            subtree.add(l1);
            List<FireMaintenanceRecord> l2s = all.stream()
                    .filter(r -> l1.getRecordId().equals(r.getParentRecordId()))
                    .collect(Collectors.toList());
            subtree.addAll(l2s);
            for (FireMaintenanceRecord l2 : l2s) {
                subtree.addAll(all.stream()
                        .filter(r -> l2.getRecordId().equals(r.getParentRecordId()))
                        .collect(Collectors.toList()));
            }

            Long[] ids = subtree.stream().map(FireMaintenanceRecord::getRecordId).toArray(Long[]::new);
            if (ids.length > 0) {
                fireMaintenanceRecordMapper.deleteFireMaintenanceRecordByRecordIds(ids);
                removed += ids.length;
            }
        }
        return removed;
    }

    /**
     * 当前表没有“停用”字段，因此对已有业务数据的类目采用阻止取消策略。
     * 校验在主表更新和新增记录之前执行，异常会让整个编辑事务保持原状。
     */
    private void assertRemovableLevel1Trees(Long taskId, Set<Long> removeLevel1Ids, String recordType) {
        if (removeLevel1Ids == null || removeLevel1Ids.isEmpty()) {
            return;
        }
        List<FireMaintenanceRecord> all = fireMaintenanceRecordMapper.selectRecordsByTaskId(taskId);
        List<String> protectedNames = new ArrayList<>();
        for (Long level1TemplateId : removeLevel1Ids) {
            FireMaintenanceRecord l1 = all.stream()
                    .filter(r -> level1TemplateId.equals(r.getTemplateId()))
                    .filter(r -> r.getLevel() != null && r.getLevel() == 1)
                    .findFirst().orElse(null);
            if (l1 == null) {
                continue;
            }
            Set<Long> subtreeIds = new HashSet<>();
            subtreeIds.add(l1.getRecordId());
            boolean changed;
            do {
                changed = false;
                for (FireMaintenanceRecord record : all) {
                    if (record.getParentRecordId() != null && subtreeIds.contains(record.getParentRecordId())
                            && subtreeIds.add(record.getRecordId())) {
                        changed = true;
                    }
                }
            } while (changed);
            if (all.stream().filter(r -> subtreeIds.contains(r.getRecordId())).anyMatch(this::hasWorkContent)) {
                protectedNames.add(l1.getItemName());
            }
        }
        if (!protectedNames.isEmpty()) {
            throw new ServiceException("以下类目已有检查结果、附件或填写内容，不能取消："
                    + String.join("、", protectedNames) + "。请保留选择后重试。");
        }
    }

    private boolean hasWorkContent(FireMaintenanceRecord record) {
        if (record == null) {
            return false;
        }
        if (StringUtils.isNotEmpty(record.getCheckResult()) && !"0".equals(record.getCheckResult())) {
            return true;
        }
        return StringUtils.isNotEmpty(record.getFaultDescription())
                || StringUtils.isNotEmpty(record.getFaultImages())
                || StringUtils.isNotEmpty(record.getRepairSuggestion())
                || StringUtils.isNotEmpty(record.getOtherNotes())
                || StringUtils.isNotEmpty(record.getDeviceLocation())
                || StringUtils.isNotEmpty(record.getTestSituation())
                || StringUtils.isNotEmpty(record.getTestResult())
                || StringUtils.isNotEmpty(record.getSitePhotos())
                || record.getTestTime() != null
                || record.getCheckTime() != null
                || record.getCheckerId() != null;
    }

    /** @return [completedCount, totalCount] */
    private int[] refreshTaskStatistics(Long taskId) {
        List<FireMaintenanceRecord> level3 = fireMaintenanceRecordMapper.selectLevel3ByTaskId(taskId);
        int total = level3.size();
        int completed = 0;
        int normal = 0;
        int fault = 0;
        int noDevice = 0;
        for (FireMaintenanceRecord record : level3) {
            if (record.getCheckResult() != null && !"0".equals(record.getCheckResult())) {
                completed++;
            }
            if ("1".equals(record.getCheckResult())) {
                normal++;
            } else if ("2".equals(record.getCheckResult())) {
                fault++;
            } else if ("3".equals(record.getCheckResult())) {
                noDevice++;
            }
        }
        FireMaintenanceTask update = new FireMaintenanceTask();
        update.setTaskId(taskId);
        update.setTotalItems(total);
        update.setCompletedItems(completed);
        update.setNormalItems(normal);
        update.setFaultItems(fault);
        update.setNoDeviceItems(noDevice);
        if (total > 0 && completed == total) {
            update.setTaskStatus("2");
        } else if (completed > 0) {
            update.setTaskStatus("1");
        } else {
            update.setTaskStatus("0");
        }
        fireMaintenanceTaskMapper.updateFireMaintenanceTask(update);
        return new int[] { completed, total };
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteFireMaintenanceTaskByTaskIds(Long[] taskIds) {
        for (Long taskId : taskIds) {
            fireMaintenanceRecordMapper.deleteFireMaintenanceRecordByTaskId(taskId);
        }
        return fireMaintenanceTaskMapper.deleteFireMaintenanceTaskByTaskIds(taskIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteFireMaintenanceTaskByTaskId(Long taskId) {
        fireMaintenanceRecordMapper.deleteFireMaintenanceRecordByTaskId(taskId);
        return fireMaintenanceTaskMapper.deleteFireMaintenanceTaskByTaskId(taskId);
    }

    @Override
    public List<FireCompany> selectCompanyListByTaskUserId(Long userId) {
        return fireMaintenanceTaskMapper.selectCompanyListByTaskUserId(userId);
    }

    @Override
    public FireInspectionTestDetailVO buildInspectionTestDetail(Long taskId) {
        FireMaintenanceTask task = requireTask(taskId);
        List<FireMaintenanceRecord> all = fireMaintenanceRecordMapper.selectRecordsByTaskId(taskId);
        List<FireInspectionTestCategoryGroup> categories = buildCategoryGroups(all, false);
        FireInspectionTestDetailVO vo = new FireInspectionTestDetailVO();
        vo.setTaskInfo(task);
        vo.setCategories(categories);
        int total = 0;
        int completed = 0;
        for (FireInspectionTestCategoryGroup g : categories) {
            total += g.getTotalItems() == null ? 0 : g.getTotalItems();
            completed += g.getCompletedItems() == null ? 0 : g.getCompletedItems();
        }
        vo.setTotalItems(total);
        vo.setCompletedItems(completed);
        vo.setUncompletedItems(Math.max(0, total - completed));
        return vo;
    }

    @Override
    public FireInspectionTestCategoryGroup buildInspectionTestSystem(Long taskId, String categoryKeyEncoded) {
        requireTask(taskId);
        decodeBusinessKey(categoryKeyEncoded); // validate encoding
        List<FireMaintenanceRecord> all = fireMaintenanceRecordMapper.selectRecordsByTaskId(taskId);
        List<FireInspectionTestCategoryGroup> groups = buildCategoryGroups(all, true);
        return groups.stream()
                .filter(g -> categoryKeyEncoded.equals(g.getCategoryKey()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("类目不存在或不属于当前任务"));
    }

    @Override
    public FireInspectionTestEquipmentGroup buildInspectionTestEquipment(Long taskId, String categoryKeyEncoded,
            String equipmentKeyEncoded) {
        FireInspectionTestCategoryGroup category = buildInspectionTestSystem(taskId, categoryKeyEncoded);
        decodeBusinessKey(equipmentKeyEncoded);
        return category.getEquipments().stream()
                .filter(e -> equipmentKeyEncoded.equals(e.getEquipmentKey()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("设备不存在或不属于当前类目"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int rebuildInspectionTestRecords(Long taskId) {
        FireMaintenanceTask task = requireTask(taskId);
        String originalStatus = task.getTaskStatus();
        boolean cancelled = "3".equals(originalStatus);

        fireMaintenanceRecordMapper.deleteFireMaintenanceRecordByTaskId(taskId);

        List<FireMaintenanceTemplate> templates = getAllTemplatesWithCache();
        Set<Long> selectedSystemIds = parseIdSet(task.getSelectedSystemIds());
        Set<Long> selectedFireTestIds = parseIdSet(task.getSelectedFireTestIds());
        if (selectedSystemIds.isEmpty()) {
            selectedSystemIds = collectLevel1Ids(templates, false);
        }
        if (selectedFireTestIds.isEmpty()) {
            selectedFireTestIds = collectLevel1Ids(templates, true);
        }

        Set<Long> existingTemplateIds = new HashSet<>();
        int added = generateMissingRecordsForLevel1Ids(taskId, selectedSystemIds, "0", templates, existingTemplateIds);
        added += generateMissingRecordsForLevel1Ids(taskId, selectedFireTestIds, "1", templates, existingTemplateIds);
        Set<Long> selectedUpkeepIds = collectRelatedLevel1Ids(templates, selectedSystemIds, "2");
        added += generateMissingRecordsForLevel1Ids(taskId, selectedUpkeepIds, "2", templates, existingTemplateIds);

        refreshTaskStatistics(taskId);
        if (cancelled) {
            FireMaintenanceTask keep = new FireMaintenanceTask();
            keep.setTaskId(taskId);
            keep.setTaskStatus("3");
            fireMaintenanceTaskMapper.updateFireMaintenanceTask(keep);
        }
        else {
            FireMaintenanceTask reset = new FireMaintenanceTask();
            reset.setTaskId(taskId);
            reset.setTaskStatus("0");
            reset.setCompletedItems(0);
            reset.setNormalItems(0);
            reset.setFaultItems(0);
            reset.setNoDeviceItems(0);
            fireMaintenanceTaskMapper.updateFireMaintenanceTask(reset);
            refreshTaskStatistics(taskId);
            FireMaintenanceTask pending = new FireMaintenanceTask();
            pending.setTaskId(taskId);
            pending.setTaskStatus("0");
            fireMaintenanceTaskMapper.updateFireMaintenanceTask(pending);
        }
        log.info("rebuildInspectionTestRecords taskId={}, added={}, cancelled={}", taskId, added, cancelled);
        return added;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<Long, Integer> rebuildAllInspectionTestRecords() {
        FireMaintenanceTask query = new FireMaintenanceTask();
        List<FireMaintenanceTask> tasks = fireMaintenanceTaskMapper.selectFireMaintenanceTaskList(query);
        Map<Long, Integer> result = new LinkedHashMap<>();
        if (tasks == null) {
            return result;
        }
        for (FireMaintenanceTask task : tasks) {
            if (task == null || task.getTaskId() == null) {
                continue;
            }
            result.put(task.getTaskId(), rebuildInspectionTestRecords(task.getTaskId()));
        }
        return result;
    }

    private FireMaintenanceTask requireTask(Long taskId) {
        if (taskId == null) {
            throw new ServiceException("任务ID不能为空");
        }
        FireMaintenanceTask task = fireMaintenanceTaskMapper.selectFireMaintenanceTaskByTaskId(taskId);
        if (task == null) {
            throw new ServiceException("任务不存在");
        }
        return task;
    }

    private String decodeBusinessKey(String encoded) {
        try {
            return FireInspectionTestKeys.decodeKey(encoded);
        }
        catch (IllegalArgumentException ex) {
            throw new ServiceException("无效的类目/设备标识");
        }
    }

    private List<FireInspectionTestCategoryGroup> buildCategoryGroups(List<FireMaintenanceRecord> all,
            boolean includeEquipments) {
        Map<Long, FireMaintenanceRecord> byId = all.stream()
                .collect(Collectors.toMap(FireMaintenanceRecord::getRecordId, r -> r, (a, b) -> a));
        Map<String, List<FireMaintenanceRecord>> l1ByKey = new LinkedHashMap<>();
        List<FireMaintenanceRecord> level1 = all.stream()
                .filter(r -> r.getLevel() != null && r.getLevel() == 1)
                .sorted(Comparator
                        .comparing((FireMaintenanceRecord r) -> r.getSortOrder() == null ? Integer.MAX_VALUE : r.getSortOrder())
                        .thenComparing(r -> r.getRecordId() == null ? 0L : r.getRecordId()))
                .collect(Collectors.toList());
        for (FireMaintenanceRecord l1 : level1) {
            String key = FireInspectionTestGroupKeys.categoryBusinessKey(l1);
            l1ByKey.computeIfAbsent(key, k -> new ArrayList<>()).add(l1);
        }

        List<FireInspectionTestCategoryGroup> groups = new ArrayList<>();
        for (Map.Entry<String, List<FireMaintenanceRecord>> entry : l1ByKey.entrySet()) {
            FireInspectionTestCategoryGroup group = new FireInspectionTestCategoryGroup();
            String businessKey = entry.getKey();
            List<FireMaintenanceRecord> sources = entry.getValue();
            group.setCategoryKey(FireInspectionTestKeys.encodeKey(businessKey));
            group.setSourceRecords(sources);
            FireMaintenanceRecord first = sources.get(0);
            group.setCategoryName(first.getItemName());
            group.setSortOrder(sources.stream()
                    .map(FireMaintenanceRecord::getSortOrder)
                    .filter(s -> s != null)
                    .min(Integer::compareTo)
                    .orElse(first.getSortOrder()));
            for (FireMaintenanceRecord src : sources) {
                if ("1".equals(src.getRecordType())) {
                    group.setFireTestRecordId(src.getRecordId());
                }
                else {
                    group.setMaintenanceRecordId(src.getRecordId());
                }
            }
            Set<Long> rootIds = sources.stream().map(FireMaintenanceRecord::getRecordId).collect(Collectors.toSet());
            List<FireMaintenanceRecord> l3Under = all.stream()
                    .filter(r -> r.getLevel() != null && r.getLevel() == 3)
                    .filter(r -> belongsToAnyRoot(r, rootIds, byId))
                    .collect(Collectors.toList());
            int total = l3Under.size();
            int completed = (int) l3Under.stream()
                    .filter(r -> r.getCheckResult() != null && !"0".equals(r.getCheckResult()))
                    .count();
            group.setTotalItems(total);
            group.setCompletedItems(completed);
            group.setUncompletedItems(Math.max(0, total - completed));
            group.setStatus(total > 0 && completed == total ? "1" : "0");
            if (includeEquipments) {
                group.setEquipments(buildEquipmentGroups(all, rootIds, byId));
            }
            groups.add(group);
        }
        return groups;
    }

    private List<FireInspectionTestEquipmentGroup> buildEquipmentGroups(List<FireMaintenanceRecord> all,
            Set<Long> categoryRootIds, Map<Long, FireMaintenanceRecord> byId) {
        List<FireMaintenanceRecord> level2 = all.stream()
                .filter(r -> r.getLevel() != null && r.getLevel() == 2)
                .filter(r -> r.getParentRecordId() != null && categoryRootIds.contains(r.getParentRecordId()))
                .sorted(Comparator
                        .comparingInt(this::recordTypeOrder)
                        .thenComparing(r -> r.getSortOrder() == null ? Integer.MAX_VALUE : r.getSortOrder())
                        .thenComparing(r -> r.getRecordId() == null ? 0L : r.getRecordId()))
                .collect(Collectors.toList());
        Map<String, List<FireMaintenanceRecord>> byKey = new LinkedHashMap<>();
        for (FireMaintenanceRecord l2 : level2) {
            String key = FireInspectionTestGroupKeys.equipmentBusinessKey(l2);
            byKey.computeIfAbsent(key, k -> new ArrayList<>()).add(l2);
        }
        List<FireInspectionTestEquipmentGroup> groups = new ArrayList<>();
        for (Map.Entry<String, List<FireMaintenanceRecord>> entry : byKey.entrySet()) {
            FireInspectionTestEquipmentGroup group = new FireInspectionTestEquipmentGroup();
            List<FireMaintenanceRecord> sources = entry.getValue();
            group.setEquipmentKey(FireInspectionTestKeys.encodeKey(entry.getKey()));
            group.setSourceRecords(sources);
            FireMaintenanceRecord first = sources.get(0);
            group.setEquipmentName(first.getItemName());
            group.setSortOrder(sources.stream()
                    .map(FireMaintenanceRecord::getSortOrder)
                    .filter(s -> s != null)
                    .min(Integer::compareTo)
                    .orElse(first.getSortOrder()));
            String groupRecordType = normalizeRecordType(first.getRecordType());
            List<FireMaintenanceRecord> checkItems = new ArrayList<>();
            for (FireMaintenanceRecord src : sources) {
                if ("1".equals(src.getRecordType())) {
                    group.setFireTestRecordId(src.getRecordId());
                    group.setHasMaintenance(true);
                    group.setDeviceLocation(src.getDeviceLocation());
                    group.setTestSituation(src.getTestSituation());
                    group.setTestTime(src.getTestTime());
                    group.setTestResult(src.getTestResult());
                    group.setSitePhotos(src.getSitePhotos());
                }
                else {
                    group.setMaintenanceRecordId(src.getRecordId());
                }
                List<FireMaintenanceRecord> children = all.stream()
                        .filter(r -> r.getLevel() != null && r.getLevel() == 3)
                        .filter(r -> src.getRecordId().equals(r.getParentRecordId()))
                        .sorted(Comparator
                                .comparing((FireMaintenanceRecord r) -> r.getSortOrder() == null ? Integer.MAX_VALUE
                                        : r.getSortOrder())
                                .thenComparing(r -> r.getRecordId() == null ? 0L : r.getRecordId()))
                        .collect(Collectors.toList());
                if (!children.isEmpty()) {
                    group.setHasCheckItems(true);
                    checkItems.addAll(children);
                }
            }
            group.setRecordType(groupRecordType);
            group.setRecordTypeLabel(recordTypeLabel(groupRecordType));
            group.setCheckItems(checkItems);
            int total = checkItems.size();
            int completed = (int) checkItems.stream()
                    .filter(r -> r.getCheckResult() != null && !"0".equals(r.getCheckResult()))
                    .count();
            group.setTotalItems(total);
            group.setCompletedItems(completed);
            group.setUncompletedItems(Math.max(0, total - completed));
            group.setStatus(total > 0 && completed == total ? "1" : "0");
            groups.add(group);
        }
        return groups;
    }

    private int recordTypeOrder(FireMaintenanceRecord record) {
        return Integer.parseInt(normalizeRecordType(record == null ? null : record.getRecordType()));
    }

    private String normalizeRecordType(String recordType) {
        return "1".equals(recordType) || "2".equals(recordType) ? recordType : "0";
    }

    private String recordTypeLabel(String recordType) {
        if ("1".equals(recordType)) {
            return "测试";
        }
        if ("2".equals(recordType)) {
            return "保养";
        }
        return "巡查";
    }

    private boolean belongsToAnyRoot(FireMaintenanceRecord record, Set<Long> rootIds,
            Map<Long, FireMaintenanceRecord> byId) {
        Long parentId = record.getParentRecordId();
        int guard = 0;
        while (parentId != null && guard++ < 8) {
            if (rootIds.contains(parentId)) {
                return true;
            }
            FireMaintenanceRecord parent = byId.get(parentId);
            if (parent == null) {
                return false;
            }
            parentId = parent.getParentRecordId();
        }
        return false;
    }
}
