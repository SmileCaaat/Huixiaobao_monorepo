package com.ruoyi.fire.service;

import java.util.List;
import com.ruoyi.fire.domain.dto.FireInspectionTemplateCategoryVO;
import com.ruoyi.fire.domain.dto.FireInspectionTemplateEquipmentVO;

/**
 * 消防维护模板类目（一级/二级），供维保任务选择页与巡检测试共用。
 */
public interface IFireMaintenanceTemplateCategoryService {

    /**
     * 合并 template_type=0/1 的一级类目，按消防维护现有去重排序规则。
     */
    List<FireInspectionTemplateCategoryVO> listInspectionLevel1Categories();

    /**
     * 指定一级类目下的二级设备类目（合并去重）。
     */
    List<FireInspectionTemplateEquipmentVO> listEquipmentsByCategoryKey(String categoryKey);

    /**
     * 按 categoryKey 查找一级类目，不存在返回 null。
     */
    FireInspectionTemplateCategoryVO findLevel1ByCategoryKey(String categoryKey);

    /**
     * 按 categoryKey + equipmentKey 查找二级设备，不存在返回 null。
     */
    FireInspectionTemplateEquipmentVO findEquipment(String categoryKey, String equipmentKey);
}
