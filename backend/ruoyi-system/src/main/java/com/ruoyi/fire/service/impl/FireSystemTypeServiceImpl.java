package com.ruoyi.fire.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.fire.domain.FireSystemType;
import com.ruoyi.fire.mapper.FireSystemTypeMapper;
import com.ruoyi.fire.service.IFireSystemTypeService;

/**
 * 消防系统类型配置Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class FireSystemTypeServiceImpl implements IFireSystemTypeService {

    @Autowired
    private FireSystemTypeMapper fireSystemTypeMapper;

    /**
     * 查询消防系统类型
     * 
     * @param typeId 类型ID
     * @return 消防系统类型
     */
    @Override
    public FireSystemType selectFireSystemTypeById(Long typeId) {
        return fireSystemTypeMapper.selectFireSystemTypeById(typeId);
    }

    /**
     * 查询消防系统类型列表
     * 
     * @param fireSystemType 消防系统类型
     * @return 消防系统类型
     */
    @Override
    public List<FireSystemType> selectFireSystemTypeList(FireSystemType fireSystemType) {
        return fireSystemTypeMapper.selectFireSystemTypeList(fireSystemType);
    }

    /**
     * 查询所有正常状态的系统类型
     * 
     * @return 消防系统类型集合
     */
    @Override
    public List<FireSystemType> selectFireSystemTypeAll() {
        return fireSystemTypeMapper.selectFireSystemTypeAll();
    }

    /**
     * 新增消防系统类型
     * 
     * @param fireSystemType 消防系统类型
     * @return 结果
     */
    @Override
    public int insertFireSystemType(FireSystemType fireSystemType) {
        return fireSystemTypeMapper.insertFireSystemType(fireSystemType);
    }

    /**
     * 修改消防系统类型
     * 
     * @param fireSystemType 消防系统类型
     * @return 结果
     */
    @Override
    public int updateFireSystemType(FireSystemType fireSystemType) {
        return fireSystemTypeMapper.updateFireSystemType(fireSystemType);
    }

    /**
     * 删除消防系统类型对象
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteFireSystemTypeByIds(String ids) {
        String[] strIds = ids.split(",");
        Long[] longIds = new Long[strIds.length];
        for (int i = 0; i < strIds.length; i++) {
            longIds[i] = Long.parseLong(strIds[i].trim());
        }
        return fireSystemTypeMapper.deleteFireSystemTypeByIds(longIds);
    }

    /**
     * 删除消防系统类型信息
     * 
     * @param typeId 类型ID
     * @return 结果
     */
    @Override
    public int deleteFireSystemTypeById(Long typeId) {
        return fireSystemTypeMapper.deleteFireSystemTypeById(typeId);
    }

    /**
     * 校验类型编码是否唯一
     */
    @Override
    public boolean checkTypeCodeUnique(FireSystemType fireSystemType) {
        Long typeId = fireSystemType.getTypeId() == null ? -1L : fireSystemType.getTypeId();
        FireSystemType info = fireSystemTypeMapper.checkTypeCodeUnique(fireSystemType.getTypeCode());
        if (info != null && info.getTypeId().longValue() != typeId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验类型名称是否唯一
     */
    @Override
    public boolean checkTypeNameUnique(FireSystemType fireSystemType) {
        Long typeId = fireSystemType.getTypeId() == null ? -1L : fireSystemType.getTypeId();
        FireSystemType info = fireSystemTypeMapper.checkTypeNameUnique(fireSystemType.getTypeName());
        if (info != null && info.getTypeId().longValue() != typeId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }
}
