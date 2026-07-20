package com.ruoyi.fire.mapper;

import java.util.List;
import com.ruoyi.fire.domain.FireMaintenanceTemplate;

/**
 * 维保模板Mapper接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public interface FireMaintenanceTemplateMapper 
{
    /**
     * 查询维保模板
     * 
     * @param id 维保模板主键
     * @return 维保模板
     */
    public FireMaintenanceTemplate selectFireMaintenanceTemplateById(Long id);

    /**
     * 查询维保模板列表
     * 
     * @param fireMaintenanceTemplate 维保模板
     * @return 维保模板集合
     */
    public List<FireMaintenanceTemplate> selectFireMaintenanceTemplateList(FireMaintenanceTemplate fireMaintenanceTemplate);

    /**
     * 查询所有维保模板
     * 
     * @return 维保模板集合
     */
    public List<FireMaintenanceTemplate> selectAllTemplates();

    /**
     * 根据层级查询维保模板
     * 
     * @param level 层级
     * @return 维保模板集合
     */
    public List<FireMaintenanceTemplate> selectTemplatesByLevel(Integer level);

    /**
     * 根据父级ID查询维保模板
     * 
     * @param parentId 父级ID
     * @return 维保模板集合
     */
    public List<FireMaintenanceTemplate> selectTemplatesByParentId(Long parentId);

    /**
     * 新增维保模板
     * 
     * @param fireMaintenanceTemplate 维保模板
     * @return 结果
     */
    public int insertFireMaintenanceTemplate(FireMaintenanceTemplate fireMaintenanceTemplate);

    /**
     * 批量新增维保模板
     * 
     * @param list 维保模板集合
     * @return 结果
     */
    public int batchInsertFireMaintenanceTemplate(List<FireMaintenanceTemplate> list);

    /**
     * 修改维保模板
     * 
     * @param fireMaintenanceTemplate 维保模板
     * @return 结果
     */
    public int updateFireMaintenanceTemplate(FireMaintenanceTemplate fireMaintenanceTemplate);

    /**
     * 删除维保模板
     * 
     * @param id 维保模板主键
     * @return 结果
     */
    public int deleteFireMaintenanceTemplateById(Long id);

    /**
     * 批量删除维保模板
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteFireMaintenanceTemplateByIds(Long[] ids);
}
