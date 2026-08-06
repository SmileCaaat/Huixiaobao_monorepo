package com.ruoyi.fire.mapper;

import java.util.List;
import com.ruoyi.fire.domain.FireFaultRepairLog;

/**
 * ���ϱ��޹�����־ Mapper
 */
public interface FireFaultRepairLogMapper {
    int insertFireFaultRepairLog(FireFaultRepairLog log);

    List<FireFaultRepairLog> selectLogsByRepairId(Long repairId);
}
