package com.ruoyi.web.controller.fire;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StreamUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.fire.domain.FireCompany;
import com.ruoyi.fire.domain.FireContract;
import com.ruoyi.fire.service.IFireCompanyService;
import com.ruoyi.fire.service.IFireContractService;
import com.ruoyi.common.core.domain.entity.SysUser;

/**
 * 维保合同管理 Controller
 */
@Controller
@RequestMapping("/fire/contract")
public class FireContractController extends BaseController {

    private String prefix = "fire/contract";
    private static final String DEFAULT_ENTRY_UNIT = "Admin";
    private static final String CONTRACT_TEMPLATE_NAME = "建筑消防设施维修保养合同（样例）.docx";
    private static final String CONTRACT_TEMPLATE_CLASSPATH = "template/" + CONTRACT_TEMPLATE_NAME;

    @Autowired
    private IFireContractService fireContractService;

    @Autowired
    private IFireCompanyService companyService;

    @RequiresPermissions("fire:contract:view")
    @GetMapping()
    public String contract() {
        return prefix + "/contract";
    }

    /**
     * 查询合同列表
     */
    @RequiresPermissions("fire:contract:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(FireContract contract) {
        startPage();
        List<FireContract> list = fireContractService.selectFireContractList(contract);
        return getDataTable(list);
    }

    /**
     * 查询对象池统计
     */
    @RequiresPermissions("fire:contract:list")
    @GetMapping("/poolStats")
    @ResponseBody
    public AjaxResult poolStats() {
        Map<String, Integer> stats = fireContractService.selectContractPoolStats();
        return AjaxResult.success(stats);
    }

    /**
     * 新增页面
     */
    @RequiresPermissions("fire:contract:add")
    @GetMapping("/add")
    public String add(ModelMap mmap) {
        mmap.put("entryUnit", getCurrentDeptName());
        return prefix + "/add";
    }

    /**
     * 新增保存
     */
    @RequiresPermissions("fire:contract:add")
    @Log(title = "维保合同", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated FireContract contract) {
        if (StringUtils.isEmpty(contract.getEntryUnit())) {
            contract.setEntryUnit(getCurrentDeptName());
        }
        contract.setCreateBy(getLoginName());
        return toAjax(fireContractService.insertFireContract(contract));
    }

    /**
     * 编辑页面
     */
    @RequiresPermissions("fire:contract:edit")
    @GetMapping("/edit/{contractId}")
    public String edit(@PathVariable("contractId") Long contractId, ModelMap mmap) {
        FireContract contract = fireContractService.selectFireContractById(contractId);
        mmap.put("contract", contract);
        return prefix + "/edit";
    }

    /**
     * 编辑保存
     */
    @RequiresPermissions("fire:contract:edit")
    @Log(title = "维保合同", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated FireContract contract) {
        contract.setUpdateBy(getLoginName());
        return toAjax(fireContractService.updateFireContract(contract));
    }

    /**
     * 续签页面
     */
    @RequiresPermissions("fire:contract:renew")
    @GetMapping("/renew/{contractId}")
    public String renew(@PathVariable("contractId") Long contractId, ModelMap mmap) {
        FireContract contract = fireContractService.selectFireContractById(contractId);
        mmap.put("contract", contract);
        return prefix + "/renew";
    }

    /**
     * 续签保存
     */
    @RequiresPermissions("fire:contract:renew")
    @Log(title = "维保合同续签", businessType = BusinessType.UPDATE)
    @PostMapping("/renew")
    @ResponseBody
    public AjaxResult renewSave(@Validated FireContract contract) {
        contract.setUpdateBy(getLoginName());
        return toAjax(fireContractService.updateFireContract(contract));
    }

    /**
     * 详情页
     */
    @RequiresPermissions("fire:contract:list")
    @GetMapping("/detail/{contractId}")
    public String detail(@PathVariable("contractId") Long contractId, ModelMap mmap) {
        FireContract contract = fireContractService.selectFireContractById(contractId);
        mmap.put("contract", contract);
        return prefix + "/detail";
    }

    /**
     * 删除
     */
    @RequiresPermissions("fire:contract:remove")
    @Log(title = "维保合同", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(fireContractService.deleteFireContractByIds(ids));
    }

    /**
     * 终止合同（移入未生效对象池）
     */
    @RequiresPermissions("fire:contract:terminate")
    @Log(title = "维保合同终止", businessType = BusinessType.UPDATE)
    @PostMapping("/terminate")
    @ResponseBody
    public AjaxResult terminate(Long contractId) {
        if (contractId == null) {
            return AjaxResult.error("合同ID不能为空");
        }
        return toAjax(fireContractService.terminateContract(contractId, getLoginName()));
    }

    /**
     * 导出勾选数据
     */
    @RequiresPermissions("fire:contract:export")
    @Log(title = "维保合同", businessType = BusinessType.EXPORT)
    @PostMapping("/exportSelected")
    @ResponseBody
    public AjaxResult exportSelected(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return AjaxResult.warn("请先勾选需要导出的合同");
        }
        List<FireContract> list = fireContractService.selectFireContractByIds(ids);
        ExcelUtil<FireContract> util = new ExcelUtil<FireContract>(FireContract.class);
        return util.exportExcel(list, "维保合同数据");
    }

    /**
     * 选择客户页
     */
    @GetMapping("/selectCompany")
    public String selectCompany() {
        return prefix + "/selectCompany";
    }

    /**
     * 选择客户列表（每页5条）
     */
    @PostMapping("/selectCompanyList")
    @ResponseBody
    public TableDataInfo selectCompanyList(FireCompany company) {
        company.setStatus("0");
        startPage();
        List<FireCompany> list = companyService.selectFireCompanyList(company);
        return getDataTable(list);
    }

    /**
     * 合同模板预览（浏览器支持时可在线预览，不支持时会直接下载）
     */
    @RequiresPermissions("fire:contract:view")
    @GetMapping("/template/preview")
    public void previewTemplate(HttpServletResponse response) {
        writeTemplate(response, false);
    }

    /**
     * 合同模板下载
     */
    @RequiresPermissions("fire:contract:view")
    @GetMapping("/template/download")
    public void downloadTemplate(HttpServletResponse response) {
        writeTemplate(response, true);
    }

    private void writeTemplate(HttpServletResponse response, boolean attachment) {
        ClassPathResource resource = new ClassPathResource(CONTRACT_TEMPLATE_CLASSPATH);
        if (!resource.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try (InputStream in = resource.getInputStream(); OutputStream out = response.getOutputStream()) {
            String encodedName = URLEncoder.encode(CONTRACT_TEMPLATE_NAME, "UTF-8").replaceAll("\\+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader("Content-Disposition",
                    (attachment ? "attachment" : "inline") + "; filename=\"" + encodedName + "\"; filename*=UTF-8''"
                            + encodedName);
            response.setContentLengthLong(resource.contentLength());
            StreamUtils.copy(in, out);
            out.flush();
        } catch (Exception e) {
            logger.error("处理合同模板失败", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        }
    }

    private String getCurrentDeptName() {
        SysUser user = ShiroUtils.getSysUser();
        if (user != null && user.getDept() != null && StringUtils.isNotEmpty(user.getDept().getDeptName())) {
            return DEFAULT_ENTRY_UNIT;
        }
        return DEFAULT_ENTRY_UNIT;
    }
}
