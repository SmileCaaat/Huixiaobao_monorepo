package com.ruoyi.web.controller.system;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.config.ServerConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.QrCodeUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysDeptRegisterInviteService;
import com.ruoyi.system.service.ISysDeptService;

/**
 * Department register QR code management.
 */
@Controller
@RequestMapping("/system/dept/registerQrcode")
public class SysDeptRegisterQrcodeController extends BaseController
{
    @Autowired
    private ISysDeptRegisterInviteService inviteService;

    @Autowired
    private ISysDeptService deptService;

    @RequiresPermissions("system:dept:registerQrcode:manage")
    @Log(title = "\u90e8\u95e8\u6ce8\u518c\u4e8c\u7ef4\u7801", businessType = BusinessType.INSERT)
    @PostMapping("/generate")
    @ResponseBody
    public AjaxResult generate(@RequestParam("deptId") Long deptId,
            @RequestParam(value = "registerMode", required = false, defaultValue = "0") String registerMode,
            @RequestParam(value = "useLimit", required = false) Integer useLimit,
            HttpServletRequest request)
    {
        try
        {
            deptService.checkDeptDataScope(deptId);
            Map<String, Object> data = inviteService.generate(deptId, registerMode, useLimit, getLoginName());
            String siteRoot = ServerConfig.getDomain(request);
            String rawToken = (String) data.get("rawToken");
            String registerUrl = inviteService.buildRegisterUrl(siteRoot, rawToken);
            data.put("registerUrl", registerUrl);
            data.put("qrBase64", QrCodeUtils.generateQrCodeBase64(registerUrl));
            return success(data);
        }
        catch (ServiceException e)
        {
            return error(e.getMessage());
        }
    }

    @RequiresPermissions("system:dept:registerQrcode:view")
    @GetMapping("/{deptId}")
    @ResponseBody
    public AjaxResult get(@PathVariable("deptId") Long deptId, HttpServletRequest request)
    {
        try
        {
            deptService.checkDeptDataScope(deptId);
            Map<String, Object> data = inviteService.getActiveMeta(deptId);
            if (data == null)
            {
                return success(null);
            }
            String siteRoot = ServerConfig.getDomain(request);
            String inviteCode = (String) data.get("inviteCode");
            if (StringUtils.isNotEmpty(inviteCode))
            {
                String url = siteRoot + "/register?inviteCode=" + inviteCode;
                data.put("registerUrl", url);
                data.put("qrBase64", QrCodeUtils.generateQrCodeBase64(url));
            }
            return success(data);
        }
        catch (ServiceException e)
        {
            return error(e.getMessage());
        }
    }

    @RequiresPermissions("system:dept:registerQrcode:manage")
    @Log(title = "\u505c\u7528\u90e8\u95e8\u6ce8\u518c\u4e8c\u7ef4\u7801", businessType = BusinessType.UPDATE)
    @PostMapping("/disable")
    @ResponseBody
    public AjaxResult disable(@RequestParam("deptId") Long deptId)
    {
        try
        {
            deptService.checkDeptDataScope(deptId);
            return toAjax(inviteService.disableByDeptId(deptId, getLoginName()));
        }
        catch (ServiceException e)
        {
            return error(e.getMessage());
        }
    }

    @RequiresPermissions("system:dept:registerQrcode:download")
    @GetMapping("/download/{deptId}")
    @ResponseBody
    public AjaxResult download(@PathVariable("deptId") Long deptId, HttpServletRequest request)
    {
        // Do not extend expiry; return current active short-code QR only.
        return get(deptId, request);
    }
}
