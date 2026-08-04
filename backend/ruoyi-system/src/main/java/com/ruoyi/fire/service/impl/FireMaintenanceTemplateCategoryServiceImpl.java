package com.ruoyi.fire.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.fire.domain.FireMaintenanceTemplate;
import com.ruoyi.fire.domain.dto.FireInspectionTemplateCategoryVO;
import com.ruoyi.fire.domain.dto.FireInspectionTemplateEquipmentVO;
import com.ruoyi.fire.service.IFireMaintenanceTaskService;
import com.ruoyi.fire.service.IFireMaintenanceTemplateCategoryService;
import com.ruoyi.fire.service.support.FireInspectionTestKeys;

/**
 * ??????????????????????? /fire/task/templates/inspection/level1 ??????????
 */
@Service
public class FireMaintenanceTemplateCategoryServiceImpl implements IFireMaintenanceTemplateCategoryService {

    @Autowired
    private IFireMaintenanceTaskService fireMaintenanceTaskService;

    @Override
    public List<FireInspectionTemplateCategoryVO> listInspectionLevel1Categories() {
        List<FireMaintenanceTemplate> templates = fireMaintenanceTaskService.getAllTemplatesWithCache();
        LinkedHashMap<String, FireInspectionTemplateCategoryVO> map = new LinkedHashMap<>();
        for (FireMaintenanceTemplate template : templates) {
            if (template == null || template.getLevel() == null || template.getLevel() != 1) {
                continue;
            }
            String type = template.getTemplateType() == null ? "0" : template.getTemplateType();
            if (!"0".equals(type) && !"1".equals(type)) {
                continue;
            }
            String mergeKey = buildLevel1MergeKey(template);
            FireInspectionTemplateCategoryVO vo = map.get(mergeKey);
            String normalizedName = FireInspectionTestKeys.normalizeText(template.getItemName());
            if (vo == null) {
                vo = new FireInspectionTemplateCategoryVO();
                vo.setCategoryKey(mergeKey);
                vo.setCategoryName(normalizedName);
                vo.setSortOrder(template.getSortOrder());
                map.put(mergeKey, vo);
            }
            else if (StringUtils.isEmpty(vo.getCategoryName()) && StringUtils.isNotEmpty(normalizedName)) {
                vo.setCategoryName(normalizedName);
            }
            if (template.getSortOrder() != null
                    && (vo.getSortOrder() == null || template.getSortOrder() < vo.getSortOrder())) {
                vo.setSortOrder(template.getSortOrder());
            }
            if ("1".equals(type)) {
                if (!vo.getFireTestTemplateIds().contains(template.getId())) {
                    vo.getFireTestTemplateIds().add(template.getId());
                }
            }
            else if (!vo.getMaintenanceTemplateIds().contains(template.getId())) {
                vo.getMaintenanceTemplateIds().add(template.getId());
            }
        }
        List<FireInspectionTemplateCategoryVO> result = new ArrayList<>(map.values());
        result.sort((a, b) -> {
            int sa = a.getSortOrder() == null ? Integer.MAX_VALUE : a.getSortOrder();
            int sb = b.getSortOrder() == null ? Integer.MAX_VALUE : b.getSortOrder();
            if (sa != sb) {
                return Integer.compare(sa, sb);
            }
            String na = a.getCategoryName() == null ? "" : a.getCategoryName();
            String nb = b.getCategoryName() == null ? "" : b.getCategoryName();
            return na.compareTo(nb);
        });
        return result;
    }

    @Override
    public List<FireInspectionTemplateEquipmentVO> listEquipmentsByCategoryKey(String categoryKey) {
        if (StringUtils.isEmpty(categoryKey)) {
            return new ArrayList<>();
        }
        List<FireMaintenanceTemplate> templates = fireMaintenanceTaskService.getAllTemplatesWithCache();
        Set<Long> parentIds = collectLevel1TemplateIds(templates, categoryKey);
        if (parentIds.isEmpty()) {
            return new ArrayList<>();
        }
        LinkedHashMap<String, FireInspectionTemplateEquipmentVO> map = new LinkedHashMap<>();
        for (FireMaintenanceTemplate template : templates) {
            if (template == null || template.getLevel() == null || template.getLevel() != 2) {
                continue;
            }
            String type = template.getTemplateType() == null ? "0" : template.getTemplateType();
            if (!"0".equals(type) && !"1".equals(type)) {
                continue;
            }
            if (template.getParentId() == null || !parentIds.contains(template.getParentId())) {
                continue;
            }
            String mergeKey = buildEquipmentMergeKey(template);
            FireInspectionTemplateEquipmentVO vo = map.get(mergeKey);
            String normalizedName = FireInspectionTestKeys.normalizeText(template.getItemName());
            if (vo == null) {
                vo = new FireInspectionTemplateEquipmentVO();
                vo.setEquipmentKey(mergeKey);
                vo.setEquipmentName(normalizedName);
                vo.setSortOrder(template.getSortOrder());
                map.put(mergeKey, vo);
            }
            else if (StringUtils.isEmpty(vo.getEquipmentName()) && StringUtils.isNotEmpty(normalizedName)) {
                vo.setEquipmentName(normalizedName);
            }
            if (template.getSortOrder() != null
                    && (vo.getSortOrder() == null || template.getSortOrder() < vo.getSortOrder())) {
                vo.setSortOrder(template.getSortOrder());
            }
        }
        List<FireInspectionTemplateEquipmentVO> result = new ArrayList<>(map.values());
        result.sort((a, b) -> {
            int sa = a.getSortOrder() == null ? Integer.MAX_VALUE : a.getSortOrder();
            int sb = b.getSortOrder() == null ? Integer.MAX_VALUE : b.getSortOrder();
            if (sa != sb) {
                return Integer.compare(sa, sb);
            }
            String na = a.getEquipmentName() == null ? "" : a.getEquipmentName();
            String nb = b.getEquipmentName() == null ? "" : b.getEquipmentName();
            return na.compareTo(nb);
        });
        return result;
    }

    @Override
    public FireInspectionTemplateCategoryVO findLevel1ByCategoryKey(String categoryKey) {
        if (StringUtils.isEmpty(categoryKey)) {
            return null;
        }
        for (FireInspectionTemplateCategoryVO vo : listInspectionLevel1Categories()) {
            if (categoryKey.equals(vo.getCategoryKey())) {
                return vo;
            }
        }
        // 兼容消防维护任务树传入的 Base64 编码键
        String normalized = normalizeIncomingCategoryKey(categoryKey);
        if (StringUtils.isNotEmpty(normalized) && !normalized.equals(categoryKey)) {
            for (FireInspectionTemplateCategoryVO vo : listInspectionLevel1Categories()) {
                if (normalized.equals(vo.getCategoryKey())) {
                    return vo;
                }
            }
        }
        return null;
    }

    @Override
    public FireInspectionTemplateEquipmentVO findEquipment(String categoryKey, String equipmentKey) {
        if (StringUtils.isEmpty(categoryKey) || StringUtils.isEmpty(equipmentKey)) {
            return null;
        }
        String resolvedCategoryKey = categoryKey;
        FireInspectionTemplateCategoryVO category = findLevel1ByCategoryKey(categoryKey);
        if (category != null) {
            resolvedCategoryKey = category.getCategoryKey();
        }
        String resolvedEquipmentKey = normalizeIncomingEquipmentKey(equipmentKey);
        for (FireInspectionTemplateEquipmentVO vo : listEquipmentsByCategoryKey(resolvedCategoryKey)) {
            if (equipmentKey.equals(vo.getEquipmentKey()) || resolvedEquipmentKey.equals(vo.getEquipmentKey())) {
                return vo;
            }
        }
        return null;
    }

    private String normalizeIncomingCategoryKey(String categoryKey) {
        if (StringUtils.isEmpty(categoryKey)) {
            return categoryKey;
        }
        if (categoryKey.startsWith("n:") || categoryKey.startsWith("c:") || categoryKey.startsWith("id:")) {
            return categoryKey;
        }
        try {
            return FireInspectionTestKeys.decodeKey(categoryKey);
        }
        catch (IllegalArgumentException ex) {
            return categoryKey;
        }
    }

    private String normalizeIncomingEquipmentKey(String equipmentKey) {
        if (StringUtils.isEmpty(equipmentKey)) {
            return equipmentKey;
        }
        String key = equipmentKey;
        if (!(key.startsWith("n:") || key.startsWith("c:") || key.startsWith("id:") || key.startsWith("t:"))) {
            try {
                key = FireInspectionTestKeys.decodeKey(key);
            }
            catch (IllegalArgumentException ignored) {
                return equipmentKey;
            }
        }
        // 任务树二级键形如 t:1|n:消防炮
        if (key.startsWith("t:") && key.indexOf('|') > 0) {
            return key.substring(key.indexOf('|') + 1);
        }
        return key;
    }

    private Set<Long> collectLevel1TemplateIds(List<FireMaintenanceTemplate> templates, String categoryKey) {
        Set<Long> ids = new HashSet<>();
        String resolvedKey = categoryKey;
        FireInspectionTemplateCategoryVO matched = findLevel1ByCategoryKey(categoryKey);
        if (matched != null) {
            resolvedKey = matched.getCategoryKey();
        }
        for (FireMaintenanceTemplate template : templates) {
            if (template == null || template.getLevel() == null || template.getLevel() != 1) {
                continue;
            }
            String type = template.getTemplateType() == null ? "0" : template.getTemplateType();
            if (!"0".equals(type) && !"1".equals(type)) {
                continue;
            }
            if (resolvedKey.equals(buildLevel1MergeKey(template))) {
                ids.add(template.getId());
            }
        }
        return ids;
    }

    private String buildLevel1MergeKey(FireMaintenanceTemplate template) {
        String normalizedName = FireInspectionTestKeys.normalizeText(template.getItemName());
        if (StringUtils.isNotEmpty(normalizedName)) {
            return "n:" + normalizedName.toLowerCase();
        }
        return "id:" + template.getId();
    }

    private String buildEquipmentMergeKey(FireMaintenanceTemplate template) {
        // Prefer normalized name so duplicate equipment labels collapse to one option.
        String normalizedName = FireInspectionTestKeys.normalizeText(template.getItemName());
        if (StringUtils.isNotEmpty(normalizedName)) {
            return "n:" + normalizedName.toLowerCase();
        }
        String code = FireInspectionTestKeys.normalizeText(template.getItemCode());
        if (StringUtils.isNotEmpty(code)) {
            return "c:" + code.toLowerCase();
        }
        return "id:" + template.getId();
    }
}
