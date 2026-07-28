package com.ruoyi.system.service;

/**
 * 短信验证码服务（可插拔）
 */
public interface ISmsCodeService
{
    /**
     * 发送注册验证码
     *
     * @param phone 手机号
     * @param clientIp 客户端 IP（限流）
     * @return 提示信息；失败抛 ServiceException
     */
    String sendRegisterCode(String phone, String clientIp);

    /**
     * 校验并消费验证码
     *
     * @return true 校验通过
     */
    boolean verifyAndConsume(String phone, String code);
}
