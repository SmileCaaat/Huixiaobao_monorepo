package com.ruoyi.fire.service;

import java.util.List;
import com.ruoyi.fire.domain.FireSystemType;

/**
 * 消防系统类型配置Service接口
 * 
 * @author ruoyi
 */
public interface IFireSystemTypeService {

    /**
     * 查询消防系统类型
     * 
     * @param typeId 类型ID
     * @return 消防系统类型
     */
    public FireSystemType selectFireSystemTypeById(Long typeId);

    /**
     * 查询消防系统类型列表
     * 
     * @param fireSystemType 消防系统类型
     * @return 消防系统类型集合
     */
    public List<FireSystemType> selectFireSystemTypeList(FireSystemType fireSystemType);

    /**
     * 查询所有正常状态的系统类型
     * 
     * @return 消防系统类型集合
     */
    public List<FireSystemType> selectFireSystemTypeAll();

    /**
     * 查询正常未删除的一级类目
     */
    public List<FireSystemType> selectTopLevelSystemTypes();

    /**
     * 查询指定一级类目下的子类目
     */
    public List<FireSystemType> selectChildSystemTypes(Long parentId);

    /**
     * 新增消防系统类型
     * 
     * @param fireSystemType 消防系统类型
     * @return 结果
     */
    public int insertFireSystemType(FireSystemType fireSystemType);

    /**
     * 修改消防系统类型
     * 
     * @param fireSystemType 消防系统类型
     * @return 结果
     */
    public int updateFireSystemType(FireSystemType fireSystemType);

    /**
     * 批量删除消防系统类型
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteFireSystemTypeByIds(String ids);

    /**
     * 删除消防系统类型信息
     * 
     * @param typeId 类型ID
     * @return 结果
     */
    public int deleteFireSystemTypeById(Long typeId);

    /**
     * 校验类型编码是否唯一
     * 
     * @param fireSystemType 系统类型
     * @return 结果
     */
    public boolean checkTypeCodeUnique(FireSystemType fireSystemType);

    /**
     * 校验类型名称是否唯一
     * 
     * @param fireSystemType 系统类型
     * @return 结果
     */
    public boolean checkTypeNameUnique(FireSystemType fireSystemType);
}
