package com.ruoyi.fire.service.support;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.fire.domain.FireMaintenanceRecord;

/**
 * ?????????/?豸?????淶???? URL ????????
 */
public final class FireInspectionTestKeys
{
    private FireInspectionTestKeys()
    {
    }

    public static String normalizeText(String text)
    {
        if (text == null)
        {
            return "";
        }
        return text.trim().replaceAll("\\s+", " ");
    }

    public static String businessKey(FireMaintenanceRecord record)
    {
        if (record == null)
        {
            return "";
        }
        return businessKey(record.getItemCode(), record.getItemName());
    }

    /** 按 itemCode 优先、否则 itemName 生成类目业务键（不含 URL 编码）。 */
    public static String businessKey(String itemCode, String itemName)
    {
        String code = normalizeText(itemCode);
        if (StringUtils.isNotEmpty(code))
        {
            return "c:" + code.toLowerCase();
        }
        return "n:" + normalizeText(itemName).toLowerCase();
    }

    public static String encodeKey(String businessKey)
    {
        if (StringUtils.isEmpty(businessKey))
        {
            throw new IllegalArgumentException("businessKey empty");
        }
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(businessKey.getBytes(StandardCharsets.UTF_8));
    }

    public static String decodeKey(String encoded)
    {
        if (StringUtils.isEmpty(encoded))
        {
            throw new IllegalArgumentException("encoded key empty");
        }
        try
        {
            byte[] bytes = Base64.getUrlDecoder().decode(encoded);
            return new String(bytes, StandardCharsets.UTF_8);
        }
        catch (IllegalArgumentException ex)
        {
            throw new IllegalArgumentException("invalid key encoding", ex);
        }
    }
}
