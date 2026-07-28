package com.ruoyi.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysDeptRegisterInviteService;

/**
 * Public employee register helpers: invite resolve only (SMS removed).
 * Actual register POST is handled by {@link SysRegisterController}.
 */
@Controller
public class EmployeeRegisterController extends BaseController
{
    @Autowired
    private ISysDeptRegisterInviteService inviteService;

    @GetMapping("/register/invite/resolve")
    @ResponseBody
    public AjaxResult resolve(@RequestParam(value = "token", required = false) String token,
            @RequestParam(value = "inviteToken", required = false) String inviteToken,
            @RequestParam(value = "inviteCode", required = false) String inviteCode)
    {
        try
        {
            String t = StringUtils.isNotEmpty(inviteToken) ? inviteToken : token;
            return success(inviteService.resolve(t, inviteCode));
        }
        catch (ServiceException e)
        {
            return error(e.getMessage());
        }
    }

    @GetMapping("/api/register/invite/resolve")
    @ResponseBody
    public AjaxResult apiResolve(@RequestParam(value = "token", required = false) String token,
            @RequestParam(value = "inviteToken", required = false) String inviteToken,
            @RequestParam(value = "inviteCode", required = false) String inviteCode)
    {
        return resolve(token, inviteToken, inviteCode);
    }
}
