package com.ruoyi.web.controller.fire;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.fire.domain.FireCheckIn;
import com.ruoyi.fire.domain.FireCheckInImage;
import com.ruoyi.fire.domain.FireMaintenanceTask;
import com.ruoyi.fire.service.IFireCheckInService;
import com.ruoyi.fire.service.IFireMaintenanceTaskService;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.service.ISysUserService;

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

    /**
     * 查询签到列表
     */
    @RequiresPermissions("fire:checkIn:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(FireCheckIn fireCheckIn) {
        startPage();
        List<FireCheckIn> list = fireCheckInService.selectFireCheckInList(fireCheckIn);
        return getDataTable(list);
    }

    /**
     * 导出签到列表
     */
    @RequiresPermissions("fire:checkIn:export")
    @Log(title = "维保签到", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(FireCheckIn fireCheckIn) {
        List<FireCheckIn> list = fireCheckInService.selectFireCheckInList(fireCheckIn);
        ExcelUtil<FireCheckIn> util = new ExcelUtil<FireCheckIn>(FireCheckIn.class);
        return util.exportExcel(list, "维保签到数据");
    }

    /**
     * 新增签到页面
     */
    @GetMapping("/add")
    public String add(ModelMap mmap) {
        return prefix + "/add";
    }

    /**
     * 根据公司ID获取维保任务列表（AJAX）
     */
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

    /**
     * 新增签到
     */
    @RequiresPermissions("fire:checkIn:add")
    @Log(title = "维保签到", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(FireCheckIn fireCheckIn, String imageUrls) {
        // 设置创建信息
        fireCheckIn.setCreateBy(ShiroUtils.getLoginName());
        if (fireCheckIn.getCheckInTime() == null) {
            fireCheckIn.setCheckInTime(new Date());
        }
        // 处理多图片上传
        if (StringUtils.isNotEmpty(imageUrls)) {
            List<FireCheckInImage> images = parseImageUrls(imageUrls);
            fireCheckIn.setImages(images);
        }
        return toAjax(fireCheckInService.insertFireCheckIn(fireCheckIn));
    }

    /**
     * 修改签到页面
     */
    @GetMapping("/edit/{checkInId}")
    public String edit(@PathVariable("checkInId") Long checkInId, ModelMap mmap) {
        FireCheckIn checkIn = fireCheckInService.selectFireCheckInById(checkInId);
        mmap.put("checkIn", checkIn);
        return prefix + "/edit";
    }

    /**
     * 修改签到
     */
    @RequiresPermissions("fire:checkIn:edit")
    @Log(title = "维保签到", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(FireCheckIn fireCheckIn, String imageUrls) {
        // 设置更新信息
        fireCheckIn.setUpdateBy(ShiroUtils.getLoginName());
        // 处理多图片上传
        if (StringUtils.isNotEmpty(imageUrls)) {
            List<FireCheckInImage> images = parseImageUrls(imageUrls);
            fireCheckIn.setImages(images);
        } else {
            fireCheckIn.setImages(new ArrayList<>());
        }
        return toAjax(fireCheckInService.updateFireCheckIn(fireCheckIn));
    }

    /**
     * 签到详情 - 同时展示同一人同一天的签到和签退记录
     */
    @RequiresPermissions("fire:checkIn:detail")
    @GetMapping("/detail/{checkInId}")
    public String detail(@PathVariable("checkInId") Long checkInId, ModelMap mmap) {
        FireCheckIn checkIn = fireCheckInService.selectFireCheckInById(checkInId);
        mmap.put("checkIn", checkIn);

        // 查询同一用户同一天的配对记录
        if (checkIn != null && checkIn.getUserId() != null && checkIn.getCheckInTime() != null) {
            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(checkIn.getCheckInTime());
            List<FireCheckIn> pairList = fireCheckInService.selectPairCheckIns(
                    checkIn.getUserId(), dateStr, checkIn.getCheckInId());

            // 根据当前记录类型，分别设置签到和签退对象
            FireCheckIn checkInRecord = null;
            FireCheckIn checkOutRecord = null;

            if ("0".equals(checkIn.getCheckInType())) {
                checkInRecord = checkIn;
            } else {
                checkOutRecord = checkIn;
            }

            for (FireCheckIn pair : pairList) {
                if ("0".equals(pair.getCheckInType()) && checkInRecord == null) {
                    checkInRecord = pair;
                } else if ("1".equals(pair.getCheckInType()) && checkOutRecord == null) {
                    checkOutRecord = pair;
                }
            }

            mmap.put("checkInRecord", checkInRecord);
            mmap.put("checkOutRecord", checkOutRecord);
        }

        return prefix + "/detail";
    }

    /**
     * 删除签到
     */
    @RequiresPermissions("fire:checkIn:remove")
    @Log(title = "维保签到", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(fireCheckInService.deleteFireCheckInByIds(convertStrToLongArray(ids)));
    }

    // =========== 移动端API接口 ===========

    /**
     * 移动端新增签到 (API接口)
     * 接收JSON格式数据，支持多图片
     */
    @PostMapping("/api/add")
    @ResponseBody
    public AjaxResult apiAdd(@RequestBody FireCheckIn fireCheckIn) {
        try {
            // 设置创建信息
            if (fireCheckIn.getUserId() == null) {
                fireCheckIn.setUserId(ShiroUtils.getUserId());
            }
            // 如果没有传userName，根据userId查询用户姓名
            if (StringUtils.isEmpty(fireCheckIn.getUserName())) {
                SysUser user = SpringUtils.getBean(ISysUserService.class)
                        .selectUserById(fireCheckIn.getUserId());
                if (user != null) {
                    fireCheckIn.setUserName(user.getUserName());
                }
            }
            fireCheckIn.setCreateBy(fireCheckIn.getUserName());
            if (fireCheckIn.getCheckInTime() == null) {
                fireCheckIn.setCheckInTime(new Date());
            }
            int result = fireCheckInService.insertFireCheckIn(fireCheckIn);
            if (result > 0) {
                AjaxResult ajax = AjaxResult.success("签到成功");
                ajax.put("checkInId", fireCheckIn.getCheckInId());
                return ajax;
            }
            return AjaxResult.error("签到失败");
        } catch (Exception e) {
            return AjaxResult.error("签到失败：" + e.getMessage());
        }
    }

    /**
     * 移动端修改签到 (API接口)
     * 接收JSON格式数据，支持多图片
     */
    @PostMapping("/api/edit")
    @ResponseBody
    public AjaxResult apiEdit(@RequestBody FireCheckIn fireCheckIn) {
        try {
            if (fireCheckIn.getCheckInId() == null) {
                return AjaxResult.error("签到ID不能为空");
            }
            // 设置更新信息
            fireCheckIn.setUpdateBy(fireCheckIn.getUserName());
            int result = fireCheckInService.updateFireCheckIn(fireCheckIn);
            return toAjax(result);
        } catch (Exception e) {
            return AjaxResult.error("修改失败：" + e.getMessage());
        }
    }

    /**
     * 移动端获取签到详情 (API接口) - 同时返回签到和签退配对信息
     */
    @GetMapping("/api/detail/{checkInId}")
    @ResponseBody
    public AjaxResult apiDetail(@PathVariable("checkInId") Long checkInId) {
        FireCheckIn checkIn = fireCheckInService.selectFireCheckInById(checkInId);
        if (checkIn == null) {
            return AjaxResult.error("签到记录不存在");
        }
        AjaxResult ajax = AjaxResult.success(checkIn);

        // 查询配对记录
        if (checkIn.getUserId() != null && checkIn.getCheckInTime() != null) {
            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(checkIn.getCheckInTime());
            List<FireCheckIn> pairList = fireCheckInService.selectPairCheckIns(
                    checkIn.getUserId(), dateStr, checkIn.getCheckInId());

            FireCheckIn checkInRecord = null;
            FireCheckIn checkOutRecord = null;

            if ("0".equals(checkIn.getCheckInType())) {
                checkInRecord = checkIn;
            } else {
                checkOutRecord = checkIn;
            }

            for (FireCheckIn pair : pairList) {
                if ("0".equals(pair.getCheckInType()) && checkInRecord == null) {
                    checkInRecord = pair;
                } else if ("1".equals(pair.getCheckInType()) && checkOutRecord == null) {
                    checkOutRecord = pair;
                }
            }

            ajax.put("checkInRecord", checkInRecord);
            ajax.put("checkOutRecord", checkOutRecord);
        }

        return ajax;
    }

    /**
     * 移动端查询签到列表 (API接口)
     */
    @PostMapping("/api/list")
    @ResponseBody
    public AjaxResult apiList(@RequestBody FireCheckIn fireCheckIn) {
        List<FireCheckIn> list = fireCheckInService.selectFireCheckInList(fireCheckIn);
        return AjaxResult.success(list);
    }

    /**
     * 解析图片URL字符串为图片对象列表
     * 
     * @param imageUrls 图片URL，多个用逗号分隔
     * @return 图片对象列表
     */
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

    /**
     * 转换字符串为Long数组
     */
    private Long[] convertStrToLongArray(String ids) {
        String[] strIds = ids.split(",");
        Long[] longIds = new Long[strIds.length];
        for (int i = 0; i < strIds.length; i++) {
            longIds[i] = Long.parseLong(strIds[i]);
        }
        return longIds;
    }
}
