package com.ruoyi.web.controller.system;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.shiro.service.SysRegisterService;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 注册验证
 * 
 * @author ruoyi
 */
@Controller
public class SysRegisterController extends BaseController
{
    @Autowired
    private SysRegisterService registerService;

    @Autowired
    private ISysConfigService configService;

    @GetMapping("/register")
    public String register()
    {
        return "register";
    }

    @PostMapping("/register")
    @ResponseBody
    public AjaxResult ajaxRegister(SysUser user)
    {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser"))))
        {
            return error("当前系统没有开启注册功能！");
        }
        String msg = registerService.register(user);
        return StringUtils.isEmpty(msg) ? success() : error(msg);
    }

    /**
     * 小程序用户注册接口（公开接口，支持JSON）
     * 使用 Map 接收参数，避免 @JsonIgnore 导致密码无法获取
     */
    @PostMapping("/api/register")
    @ResponseBody
    public AjaxResult apiRegister(@RequestBody Map<String, String> params)
    {
        String loginName = params.get("loginName");
        String userName = params.get("userName");
        String phonenumber = params.get("phonenumber");
        String password = params.get("password");
        
        // 参数校验
        if (StringUtils.isEmpty(loginName))
        {
            return error("登录账号不能为空");
        }
        if (StringUtils.isEmpty(password))
        {
            return error("密码不能为空");
        }
        
        // 构建用户对象
        SysUser user = new SysUser();
        user.setLoginName(loginName);
        user.setUserName(StringUtils.isNotEmpty(userName) ? userName : loginName);
        user.setPhonenumber(phonenumber);
        user.setPassword(password);
        
        String msg = registerService.register(user);
        return StringUtils.isEmpty(msg) ? success("注册成功") : error(msg);
    }
}
