package com.ruoyi.fire.service.support;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.fire.domain.FireMaintenanceRecord;

/** 巡查测试页面分组键：一级按名称合并，二级按记录类型隔离。 */
public final class FireInspectionTestGroupKeys
{
    private FireInspectionTestGroupKeys()
    {
    }

    public static String categoryBusinessKey(FireMaintenanceRecord record)
    {
        if (record == null)
        {
            return "";
        }
        String name = FireInspectionTestKeys.normalizeText(record.getItemName());
        if (StringUtils.isNotEmpty(name))
        {
            return "n:" + name.toLowerCase();
        }
        String key = FireInspectionTestKeys.businessKey(record);
        if (StringUtils.isEmpty(key) || "n:".equals(key))
        {
            return "id:" + record.getRecordId();
        }
        return key;
    }

    public static String equipmentBusinessKey(FireMaintenanceRecord record)
    {
        if (record == null)
        {
            return "";
        }
        String key = FireInspectionTestKeys.businessKey(record);
        if (StringUtils.isEmpty(key) || "n:".equals(key))
        {
            key = "id:" + record.getRecordId();
        }
        String type = record.getRecordType();
        if (!"1".equals(type) && !"2".equals(type))
        {
            type = "0";
        }
        return "t:" + type + "|" + key;
    }
}
