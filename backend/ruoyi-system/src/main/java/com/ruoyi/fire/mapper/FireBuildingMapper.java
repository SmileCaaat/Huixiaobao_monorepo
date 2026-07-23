package com.ruoyi.fire.mapper;

import java.util.List;
import com.ruoyi.fire.domain.FireBuilding;

/**
 * 建筑信息 数据层
 * 
 * @author ruoyi
 */
public interface FireBuildingMapper {
    /**
     * 查询建筑信息列表
     * 
     * @param building 建筑信息
     * @return 建筑信息集合
     */
    public List<FireBuilding> selectBuildingList(FireBuilding building);

    /**
     * 查询所有建筑信息
     * 
     * @return 建筑信息集合
     */
    public List<FireBuilding> selectBuildingAll();

    /**
     * 根据建筑ID查询建筑信息
     * 
     * @param buildingId 建筑ID
     * @return 建筑信息
     */
    public FireBuilding selectBuildingById(Long buildingId);

    /**
     * 校验建筑编码是否唯一
     * 
     * @param buildingCode 建筑编码
     * @return 建筑信息
     */
    public FireBuilding checkBuildingCodeUnique(String buildingCode);

    /**
     * 校验建筑名称是否在同一客户下唯一
     * 
     * @param building 建筑信息（需含 companyId、buildingName，编辑时含 buildingId）
     * @return 建筑信息
     */
    public FireBuilding checkBuildingNameUnique(FireBuilding building);

    /**
     * 新增建筑信息
     * 
     * @param building 建筑信息
     * @return 结果
     */
    public int insertBuilding(FireBuilding building);

    /**
     * 批量新增建筑信息
     *
     * @param list 建筑信息列表
     * @return 结果
     */
    public int batchInsertBuildings(List<FireBuilding> list);

    /**
     * 修改建筑信息
     * 
     * @param building 建筑信息
     * @return 结果
     */
    public int updateBuilding(FireBuilding building);

    /**
     * 批量删除建筑信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteBuildingByIds(Long[] ids);

    /**
     * 统计建筑数量
     * 
     * @return 建筑数量
     */
    public int countBuilding();

    /**
     * 根据所属客户ID删除建筑信息（逻辑删除）
     *
     * @param companyId 客户ID
     * @return 结果
     */
    public int deleteBuildingByCompanyId(Long companyId);

    /**
     * 按建筑类型统计数量
     * 
     * @return 统计结果
     */
    public List<java.util.Map<String, Object>> countBuildingByType();
}
