package com.ruoyi.web.controller.fire;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.fire.domain.FireCheckIn;
import com.ruoyi.fire.domain.FireCheckInImage;
import com.ruoyi.fire.domain.FireMaintenanceTask;
import com.ruoyi.fire.service.IFireCheckInService;
import com.ruoyi.fire.service.IFireMaintenanceTaskService;

/**
 * 维保签到Controller
 *
 * @author ruoyi
 */
@Controller
@RequestMapping("/fire/checkIn")
public class FireCheckInController extends BaseController {
    private String prefix = "fire/checkIn";

    @Autowired
    private IFireCheckInService fireCheckInService;

    @Autowired
    private IFireMaintenanceTaskService fireMaintenanceTaskService;

    @RequiresPermissions("fire:checkIn:list")
    @GetMapping()
    public String checkIn() {
        return prefix + "/checkIn";
    }

    @RequiresPermissions("fire:checkIn:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(FireCheckIn fireCheckIn) {
        startPage();
        List<FireCheckIn> list = fireCheckInService.selectFireCheckInList(fireCheckIn);
        return getDataTable(list);
    }

    @RequiresPermissions("fire:checkIn:export")
    @Log(title = "维保签到", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(FireCheckIn fireCheckIn) {
        List<FireCheckIn> list = fireCheckInService.selectFireCheckInList(fireCheckIn);
        ExcelUtil<FireCheckIn> util = new ExcelUtil<FireCheckIn>(FireCheckIn.class);
        return util.exportExcel(list, "维保签到数据");
    }

    @GetMapping("/add")
    public String add(ModelMap mmap) {
        return prefix + "/add";
    }

    @PostMapping("/listTasksByCompany")
    @ResponseBody
    public AjaxResult listTasksByCompany(Long companyId) {
        FireMaintenanceTask query = new FireMaintenanceTask();
        if (companyId != null) {
            query.setCompanyId(companyId);
        }
        List<FireMaintenanceTask> taskList = fireMaintenanceTaskService.selectFireMaintenanceTaskList(query);
        return AjaxResult.success(taskList);
    }

    @RequiresPermissions("fire:checkIn:add")
    @Log(title = "维保签到", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(FireCheckIn fireCheckIn, String imageUrls,
            @RequestParam(value = "addressMode", required = false) String addressMode) {
        try {
            if (StringUtils.isNotEmpty(imageUrls)) {
                fireCheckIn.setImages(parseImageUrls(imageUrls));
            }
            fireCheckInService.prepareAdminInsert(fireCheckIn, addressMode);
            return toAjax(fireCheckInService.insertFireCheckIn(fireCheckIn));
        } catch (ServiceException e) {
            return error(e.getMessage());
        }
    }

    @GetMapping("/edit/{checkInId}")
    public String edit(@PathVariable("checkInId") Long checkInId, ModelMap mmap) {
        FireCheckIn checkIn = fireCheckInService.selectFireCheckInById(checkInId);
        mmap.put("checkIn", checkIn);
        return prefix + "/edit";
    }

    @RequiresPermissions("fire:checkIn:edit")
    @Log(title = "维保签到", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(FireCheckIn fireCheckIn, String imageUrls,
            @RequestParam(value = "addressMode", required = false) String addressMode) {
        try {
            if (StringUtils.isNotEmpty(imageUrls)) {
                fireCheckIn.setImages(parseImageUrls(imageUrls));
            } else {
                fireCheckIn.setImages(new ArrayList<>());
            }
            // 编辑页未接入地址模式切换时，默认按已有 address 视为手动
            if (StringUtils.isEmpty(addressMode)) {
                addressMode = "manual";
                fireCheckIn.setManualAddress(fireCheckIn.getAddress());
            }
            fireCheckInService.prepareAdminUpdate(fireCheckIn, addressMode);
            return toAjax(fireCheckInService.updateFireCheckIn(fireCheckIn));
        } catch (ServiceException e) {
            return error(e.getMessage());
        }
    }

    @RequiresPermissions("fire:checkIn:detail")
    @GetMapping("/detail/{checkInId}")
    public String detail(@PathVariable("checkInId") Long checkInId, ModelMap mmap) {
        FireCheckIn checkIn = fireCheckInService.selectFireCheckInById(checkInId);
        mmap.put("checkIn", checkIn);
        Map<String, FireCheckIn> pair = fireCheckInService.resolvePairRecords(checkIn);
        mmap.put("checkInRecord", pair.get("checkInRecord"));
        mmap.put("checkOutRecord", pair.get("checkOutRecord"));
        mmap.put("historyUnlinkedTask", checkIn != null && checkIn.getTaskId() == null);
        return prefix + "/detail";
    }

    @RequiresPermissions("fire:checkIn:remove")
    @Log(title = "维保签到", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(fireCheckInService.deleteFireCheckInByIds(convertStrToLongArray(ids)));
    }

    // =========== 移动端API接口 ===========

    @PostMapping("/api/add")
    @ResponseBody
    public AjaxResult apiAdd(@RequestBody FireCheckIn fireCheckIn) {
        try {
            fireCheckInService.prepareMobileInsert(fireCheckIn, true);
            int result = fireCheckInService.insertFireCheckIn(fireCheckIn);
            if (result > 0) {
                AjaxResult ajax = AjaxResult.success("签到成功");
                ajax.put("checkInId", fireCheckIn.getCheckInId());
                return ajax;
            }
            return AjaxResult.error("签到失败");
        } catch (ServiceException e) {
            return AjaxResult.error(e.getMessage());
        } catch (Exception e) {
            return AjaxResult.error("签到失败：" + e.getMessage());
        }
    }

    @PostMapping("/api/edit")
    @ResponseBody
    public AjaxResult apiEdit(@RequestBody FireCheckIn fireCheckIn) {
        try {
            if (fireCheckIn.getCheckInId() == null) {
                return AjaxResult.error("签到ID不能为空");
            }
            // 移动端不信任请求体 userId/userName
            fireCheckIn.setUserId(ShiroUtils.getUserId());
            fireCheckIn.setUserName(null);
            fireCheckIn.setManualAddress(fireCheckIn.getAddress());
            fireCheckInService.prepareAdminUpdate(fireCheckIn, "manual");
            return toAjax(fireCheckInService.updateFireCheckIn(fireCheckIn));
        } catch (ServiceException e) {
            return AjaxResult.error(e.getMessage());
        } catch (Exception e) {
            return AjaxResult.error("修改失败：" + e.getMessage());
        }
    }

    @GetMapping("/api/detail/{checkInId}")
    @ResponseBody
    public AjaxResult apiDetail(@PathVariable("checkInId") Long checkInId) {
        FireCheckIn checkIn = fireCheckInService.selectFireCheckInById(checkInId);
        if (checkIn == null) {
            return AjaxResult.error("签到记录不存在");
        }
        AjaxResult ajax = AjaxResult.success(checkIn);
        Map<String, FireCheckIn> pair = fireCheckInService.resolvePairRecords(checkIn);
        ajax.put("checkInRecord", pair.get("checkInRecord"));
        ajax.put("checkOutRecord", pair.get("checkOutRecord"));
        ajax.put("historyUnlinkedTask", checkIn.getTaskId() == null);
        return ajax;
    }

    @PostMapping("/api/list")
    @ResponseBody
    public AjaxResult apiList(@RequestBody FireCheckIn fireCheckIn) {
        List<FireCheckIn> list = fireCheckInService.selectFireCheckInList(fireCheckIn);
        return AjaxResult.success(list);
    }

    private List<FireCheckInImage> parseImageUrls(String imageUrls) {
        List<FireCheckInImage> images = new ArrayList<>();
        if (StringUtils.isEmpty(imageUrls)) {
            return images;
        }
        String[] urls = imageUrls.split(",");
        for (int i = 0; i < urls.length; i++) {
            String url = urls[i].trim();
            if (StringUtils.isNotEmpty(url)) {
                FireCheckInImage image = new FireCheckInImage();
                image.setImageUrl(url);
                image.setImageName("签到图片" + (i + 1));
                image.setSortOrder(i);
                images.add(image);
            }
        }
        return images;
    }

    private Long[] convertStrToLongArray(String ids) {
        String[] strIds = ids.split(",");
        Long[] longIds = new Long[strIds.length];
        for (int i = 0; i < strIds.length; i++) {
            longIds[i] = Long.parseLong(strIds[i]);
        }
        return longIds;
    }
}
