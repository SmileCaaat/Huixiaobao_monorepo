package com.ruoyi.fire.mapper;

import java.util.List;
import com.ruoyi.fire.domain.FireEquipment;

/**
 * 消防设备 数据层
 * 
 * @author ruoyi
 */
public interface FireEquipmentMapper {
    /**
     * 查询消防设备列表
     * 
     * @param equipment 消防设备
     * @return 消防设备集合
     */
    public List<FireEquipment> selectEquipmentList(FireEquipment equipment);

    /**
     * 查询所有消防设备
     * 
     * @return 消防设备集合
     */
    public List<FireEquipment> selectEquipmentAll();

    /**
     * 根据设备ID查询消防设备
     * 
     * @param equipmentId 设备ID
     * @return 消防设备
     */
    public FireEquipment selectEquipmentById(Long equipmentId);

    /**
     * 根据设备编码查询消防设备
     * 
     * @param equipmentCode 设备编码
     * @return 消防设备
     */
    public FireEquipment selectEquipmentByCode(String equipmentCode);

    /**
     * 校验设备编码是否唯一
     * 
     * @param equipmentCode 设备编码
     * @return 消防设备
     */
    public FireEquipment checkEquipmentCodeUnique(String equipmentCode);

    /**
     * 新增消防设备
     * 
     * @param equipment 消防设备
     * @return 结果
     */
    public int insertEquipment(FireEquipment equipment);

    /**
     * 修改消防设备
     * 
     * @param equipment 消防设备
     * @return 结果
     */
    public int updateEquipment(FireEquipment equipment);

    /**
     * 批量删除消防设备
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteEquipmentByIds(Long[] ids);

    /**
     * 统计设备总数
     * 
     * @return 设备数量
     */
    public int countEquipment();

    /**
     * 按设备类型统计数量
     * 
     * @return 统计结果
     */
    public List<java.util.Map<String, Object>> countEquipmentByType();

    /**
     * 按设备状态统计数量
     * 
     * @return 统计结果
     */
    public List<java.util.Map<String, Object>> countEquipmentByStatus();

    /**
     * 统计正常设备数量
     *
     * @return 正常设备数量
     */
    public int countNormalEquipment();

    /**
     * 统计预警设备数量
     *
     * @return 预警设备数量
     */
    public int countWarningEquipment();

    /**
     * 统计故障设备数量
     *
     * @return 故障设备数量
     */
    public int countFaultEquipment();

    /**
     * 统计过期设备数量
     *
     * @return 过期设备数量
     */
    public int countExpiredEquipment();

    /**
     * 统计即将过期设备数（30天内）
     */
    public int countExpiringSoon();
    
    /**
     * 统计已过期设备数
     */
    public int countExpired();
    
    /**
     * 统计有效期内设备数
     */
    public int countInDate();
    
    /**
     * 查询即将过期设备列表（前5条）
     */
    public List<FireEquipment> selectExpiringSoonList();
    
    /**
     * 查询已过期设备列表（前5条）
     */
    public List<FireEquipment> selectExpiredList();
    
    /**
     * 查询最近添加的设备列表（前5条）
     */
    public List<FireEquipment> selectRecentlyAdded();
}
