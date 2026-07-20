package com.ruoyi.fire.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.fire.domain.FireEquipment;
import com.ruoyi.fire.domain.FireCompany;
import com.ruoyi.fire.domain.FireBuilding;
import com.ruoyi.fire.mapper.FireEquipmentMapper;
import com.ruoyi.fire.mapper.FireCompanyMapper;
import com.ruoyi.fire.mapper.FireBuildingMapper;
import com.ruoyi.fire.service.IFireEquipmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * 消防设备 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class FireEquipmentServiceImpl implements IFireEquipmentService {
    private static final Logger log = LoggerFactory.getLogger(FireEquipmentServiceImpl.class);

    @Autowired
    private FireEquipmentMapper equipmentMapper;

    @Autowired
    private FireCompanyMapper companyMapper;

    @Autowired
    private FireBuildingMapper buildingMapper;

    /**
     * 查询消防设备列表
     * 
     * @param equipment 消防设备
     * @return 消防设备集合
     */
    @Override
    public List<FireEquipment> selectEquipmentList(FireEquipment equipment) {
        return equipmentMapper.selectEquipmentList(equipment);
    }

    /**
     * 查询所有消防设备
     * 
     * @return 消防设备集合
     */
    @Override
    public List<FireEquipment> selectEquipmentAll() {
        return equipmentMapper.selectEquipmentAll();
    }

    /**
     * 根据设备ID查询消防设备
     * 
     * @param equipmentId 设备ID
     * @return 消防设备
     */
    @Override
    public FireEquipment selectEquipmentById(Long equipmentId) {
        return equipmentMapper.selectEquipmentById(equipmentId);
    }

    /**
     * 根据设备编码查询消防设备
     * 
     * @param equipmentCode 设备编码
     * @return 消防设备
     */
    @Override
    public FireEquipment selectEquipmentByCode(String equipmentCode) {
        return equipmentMapper.selectEquipmentByCode(equipmentCode);
    }

    /**
     * 校验设备编码是否唯一
     * 
     * @param equipment 消防设备
     * @return 结果
     */
    @Override
    public boolean checkEquipmentCodeUnique(FireEquipment equipment) {
        Long equipmentId = StringUtils.isNull(equipment.getEquipmentId()) ? -1L : equipment.getEquipmentId();
        FireEquipment info = equipmentMapper.checkEquipmentCodeUnique(equipment.getEquipmentCode());
        if (StringUtils.isNotNull(info) && info.getEquipmentId().longValue() != equipmentId.longValue()) {
            return false;
        }
        return true;
    }

    /**
     * 新增消防设备
     * 
     * @param equipment 消防设备
     * @return 结果
     */
    @Override
    public int insertEquipment(FireEquipment equipment) {
        // 如果没有设备编码，自动生成唯一编码
        if (StringUtils.isEmpty(equipment.getEquipmentCode())) {
            equipment.setEquipmentCode(generateEquipmentCode());
        }
        return equipmentMapper.insertEquipment(equipment);
    }

    /**
     * 导入设备数据
     * 
     * @param equipmentList   设备数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName        操作用户
     * @return 结果
     */
    @Override
    public String importEquipment(List<FireEquipment> equipmentList, Boolean isUpdateSupport, String operName) {
        if (StringUtils.isNull(equipmentList) || equipmentList.size() == 0) {
            throw new RuntimeException("导入设备数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();

        for (FireEquipment equipment : equipmentList) {
            try {
                // 验证设备名称必填
                if (StringUtils.isEmpty(equipment.getEquipmentName())) {
                    failureNum++;
                    failureMsg.append("<br/>第" + (successNum + failureNum) + "条导入失败，设备名称不能为空");
                    continue;
                }

                // 翻译客户名称为客户ID
                if (StringUtils.isNotEmpty(equipment.getCompanyName())) {
                    FireCompany sysCompany = new FireCompany();
                    sysCompany.setCompanyName(equipment.getCompanyName());
                    List<FireCompany> companies = companyMapper.selectFireCompanyList(sysCompany);
                    if (companies != null && companies.size() > 0) {
                        equipment.setCompanyId(companies.get(0).getCompanyId());
                    } else {
                        failureNum++;
                        failureMsg.append("<br/>设备 " + equipment.getEquipmentName() + " 导入失败：找不到匹配的客户名称【"
                                + equipment.getCompanyName() + "】");
                        continue;
                    }
                }

                // 翻译建筑名称为建筑ID
                if (StringUtils.isNotEmpty(equipment.getBuildingName()) && equipment.getCompanyId() != null) {
                    FireBuilding sysBuilding = new FireBuilding();
                    sysBuilding.setBuildingName(equipment.getBuildingName());
                    sysBuilding.setCompanyId(equipment.getCompanyId());
                    List<FireBuilding> buildings = buildingMapper.selectBuildingList(sysBuilding);
                    if (buildings != null && buildings.size() > 0) {
                        equipment.setBuildingId(buildings.get(0).getBuildingId());
                    } else {
                        failureNum++;
                        failureMsg.append("<br/>设备 " + equipment.getEquipmentName() + " 导入失败：在指定客户名下找不到对应的建筑名称【"
                                + equipment.getBuildingName() + "】");
                        continue;
                    }
                }

                // 如果设备数量为空默认为1
                if (equipment.getQuantity() == null || equipment.getQuantity() <= 0) {
                    equipment.setQuantity(1);
                }

                // 检查判断依据：如果没有传设备编码，那就生成一个新的
                if (StringUtils.isEmpty(equipment.getEquipmentCode())) {
                    equipment.setEquipmentCode(generateEquipmentCode());
                }

                boolean exists = !checkEquipmentCodeUnique(equipment);

                if (!exists) {
                    equipment.setCreateBy(operName);
                    this.insertEquipment(equipment);
                    successNum++;
                    successMsg.append("<br/>设备 " + equipment.getEquipmentName() + " 导入成功");
                } else if (isUpdateSupport) {
                    FireEquipment updateEq = this.selectEquipmentByCode(equipment.getEquipmentCode());
                    equipment.setEquipmentId(updateEq.getEquipmentId());
                    equipment.setUpdateBy(operName);
                    this.updateEquipment(equipment);
                    successNum++;
                    successMsg.append("<br/>设备 " + equipment.getEquipmentName() + " 更新成功");
                } else {
                    failureNum++;
                    failureMsg.append("<br/>设备编码 " + equipment.getEquipmentCode() + " 已存在");
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>设备 " + equipment.getEquipmentName() + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
                log.error(msg, e);
            }
        }

        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new RuntimeException(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }

    /**
     * 生成唯一的设备编码
     * 格式：EQ + 时间戳后8位 + 4位随机数
     */
    private String generateEquipmentCode() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 10000);
        return String.format("EQ%08d%04d", timestamp % 100000000, random);
    }

    /**
     * 修改消防设备
     * 
     * @param equipment 消防设备
     * @return 结果
     */
    @Override
    public int updateEquipment(FireEquipment equipment) {
        return equipmentMapper.updateEquipment(equipment);
    }

    /**
     * 批量删除消防设备
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteEquipmentByIds(String ids) {
        return equipmentMapper.deleteEquipmentByIds(Convert.toLongArray(ids));
    }

    /**
     * 统计设备总数
     * 
     * @return 设备数量
     */
    @Override
    public int countEquipment() {
        return equipmentMapper.countEquipment();
    }

    /**
     * 按设备类型统计数量
     * 
     * @return 统计结果
     */
    @Override
    public List<Map<String, Object>> countEquipmentByType() {
        return equipmentMapper.countEquipmentByType();
    }

    /**
     * 按设备状态统计数量
     * 
     * @return 统计结果
     */
    @Override
    public List<Map<String, Object>> countEquipmentByStatus() {
        return equipmentMapper.countEquipmentByStatus();
    }

    /**
     * 获取设备状态统计
     * 
     * @return 统计结果
     */
    @Override
    public Map<String, Integer> getEquipmentStatusCount() {
        Map<String, Integer> result = new HashMap<>();
        result.put("total", equipmentMapper.countEquipment());
        result.put("normal", equipmentMapper.countNormalEquipment());
        result.put("warning", equipmentMapper.countWarningEquipment());
        result.put("fault", equipmentMapper.countFaultEquipment());
        result.put("expired", equipmentMapper.countExpiredEquipment());
        return result;
    }

    @Override
    public int countExpiringSoon() {
        return equipmentMapper.countExpiringSoon();
    }

    @Override
    public int countExpired() {
        return equipmentMapper.countExpired();
    }

    @Override
    public int countInDate() {
        return equipmentMapper.countInDate();
    }

    @Override
    public List<FireEquipment> selectExpiringSoonList() {
        return equipmentMapper.selectExpiringSoonList();
    }

    @Override
    public List<FireEquipment> selectExpiredList() {
        return equipmentMapper.selectExpiredList();
    }

    @Override
    public List<FireEquipment> selectRecentlyAdded() {
        return equipmentMapper.selectRecentlyAdded();
    }
}
