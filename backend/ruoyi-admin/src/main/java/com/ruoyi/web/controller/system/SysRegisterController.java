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
import com.ruoyi.common.constant.ShiroConstants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.shiro.service.EmployeeRegisterService;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 注册验证（PC + 小程序），统一走员工邀请注册流程（图片验证码，无短信）。
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
            @RequestParam(value = "inviteCode", required = false) String inviteCode)
    {
        return doRegister(user, inviteToken, inviteCode);
    }

    /**
     * 小程序用户注册接口（公开接口，支持JSON）
     */
    @PostMapping("/api/register")
    @ResponseBody
    public AjaxResult apiRegister(@RequestBody Map<String, String> params)
    {
        SysUser user = new SysUser();
        String userName = params.get("userName");
        String phonenumber = params.get("phonenumber");
        if (StringUtils.isEmpty(phonenumber))
        {
            phonenumber = params.get("phone");
        }
        // loginName 统一由后端使用手机号
        user.setLoginName(phonenumber);
        user.setUserName(userName);
        user.setPhonenumber(phonenumber);
        user.setPassword(params.get("password"));
        return doRegister(user, params.get("inviteToken"), params.get("inviteCode"));
    }

    private AjaxResult doRegister(SysUser user, String inviteToken, String inviteCode)
    {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser"))))
        {
            return error("当前系统没有开启注册功能！");
        }
        if (ShiroConstants.CAPTCHA_ERROR
                .equals(ServletUtils.getRequest().getAttribute(ShiroConstants.CURRENT_CAPTCHA)))
        {
            return error("验证码错误");
        }
        Map<String, Object> extra = new HashMap<>();
        String msg = employeeRegisterService.register(user, inviteToken, inviteCode, extra);
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
