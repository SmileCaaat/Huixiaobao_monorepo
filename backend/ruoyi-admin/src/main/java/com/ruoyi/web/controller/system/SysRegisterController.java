package com.ruoyi.web.controller.system;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.shiro.service.EmployeeRegisterService;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 注册验证（PC + 小程序），统一走员工邀请/短信注册流程
 */
@Controller
public class SysRegisterController extends BaseController
{
    @Autowired
    private EmployeeRegisterService employeeRegisterService;

    @Autowired
    private ISysConfigService configService;

    @GetMapping("/register")
    public String register()
    {
        return "register";
    }

    @PostMapping("/register")
    @ResponseBody
    public AjaxResult ajaxRegister(SysUser user,
            @RequestParam(value = "inviteToken", required = false) String inviteToken,
            @RequestParam(value = "inviteCode", required = false) String inviteCode,
            @RequestParam(value = "smsCode", required = false) String smsCode)
    {
        return doRegister(user, inviteToken, inviteCode, smsCode);
    }

    /**
     * 小程序用户注册接口（公开接口，支持JSON）
     */
    @PostMapping("/api/register")
    @ResponseBody
    public AjaxResult apiRegister(@RequestBody Map<String, String> params)
    {
        SysUser user = new SysUser();
        String loginName = params.get("loginName");
        String userName = params.get("userName");
        String phonenumber = params.get("phonenumber");
        if (StringUtils.isEmpty(phonenumber))
        {
            phonenumber = params.get("phone");
        }
        user.setLoginName(StringUtils.isNotEmpty(loginName) ? loginName : phonenumber);
        user.setUserName(userName);
        user.setPhonenumber(phonenumber);
        user.setPassword(params.get("password"));
        // 忽略客户端 deptId，防篡改
        return doRegister(user, params.get("inviteToken"), params.get("inviteCode"), params.get("smsCode"));
    }

    private AjaxResult doRegister(SysUser user, String inviteToken, String inviteCode, String smsCode)
    {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser"))))
        {
            return error("当前系统没有开启注册功能！");
        }
        Map<String, Object> extra = new HashMap<>();
        String msg = employeeRegisterService.register(user, inviteToken, inviteCode, smsCode, extra);
        if (StringUtils.isNotEmpty(msg))
        {
            return error(msg);
        }
        Boolean auto = (Boolean) extra.get("autoPassed");
        AjaxResult ajax = success(Boolean.TRUE.equals(auto) ? "注册成功" : "注册成功，请等待管理员审核");
        ajax.put("autoPassed", auto);
        ajax.put("auditStatus", extra.get("auditStatus"));
        return ajax;
    }
}
