package com.ruoyi.web.controller.fire;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.fire.domain.FireEquipment;
import com.ruoyi.fire.domain.FireMaintenanceTask;
import com.ruoyi.fire.service.IFireBuildingService;
import com.ruoyi.fire.service.IFireCompanyService;
import com.ruoyi.fire.service.IFireDataPermissionService;
import com.ruoyi.fire.service.IFireEquipmentService;
import com.ruoyi.fire.service.IFireMaintenanceTaskService;
import com.ruoyi.fire.service.IFireSystemTypeService;

/**
 * 消防首页控制器
 */
@Controller
@RequestMapping("/fire")
public class FireHomeController extends BaseController {
    @Autowired
    private IFireBuildingService buildingService;

    @Autowired
    private IFireEquipmentService equipmentService;

    @Autowired
    private IFireCompanyService companyService;

    @Autowired
    private IFireSystemTypeService systemTypeService;

    @Autowired
    private IFireMaintenanceTaskService taskService;

    @Autowired
    private IFireDataPermissionService fireDataPermissionService;

    @GetMapping("/home")
    public String home(ModelMap mmap) {
        SysUser user = ShiroUtils.getSysUser();
        mmap.put("displayName", resolveDisplayName(user));
        mmap.put("homeScope", isGlobalHomeUser(user) ? "global" : "personal");
        return "fire_main";
    }

    /**
     * 首页统计：管理员/维保部管理员看全局设备统计；普通维保人员看本人任务统计。
     */
    @GetMapping("/stats/home")
    @ResponseBody
    public AjaxResult getHomeStats() {
        SysUser user = ShiroUtils.getSysUser();
        if (isGlobalHomeUser(user)) {
            return buildGlobalStats();
        }
        return buildPersonalTaskStats(user);
    }

    private boolean isGlobalHomeUser(SysUser user) {
        return fireDataPermissionService.hasGlobalBizDataScope(user);
    }

    private AjaxResult buildGlobalStats() {
        AjaxResult result = AjaxResult.success();
        result.put("scope", "global");
        int companyCount = companyService.countCompany();
        result.put("companyCount", companyCount);
        int buildingCount = buildingService.countBuilding();
        result.put("buildingCount", buildingCount);
        int equipmentCount = equipmentService.countEquipment();
        result.put("equipmentCount", equipmentCount);
        int expiringSoonCount = equipmentService.countExpiringSoon();
        result.put("expiringSoonCount", expiringSoonCount);
        int inDateCount = equipmentService.countInDate();
        result.put("inDateCount", inDateCount);
        int expiredCount = equipmentService.countExpired();
        result.put("expiredCount", expiredCount);
        if (equipmentCount > 0) {
            result.put("inDatePercent", String.format("%.1f", inDateCount * 100.0 / equipmentCount));
            result.put("expiringSoonPercent", String.format("%.1f", expiringSoonCount * 100.0 / equipmentCount));
            result.put("expiredPercent", String.format("%.1f", expiredCount * 100.0 / equipmentCount));
        } else {
            result.put("inDatePercent", "0");
            result.put("expiringSoonPercent", "0");
            result.put("expiredPercent", "0");
        }
        List<FireEquipment> expiringSoonList = equipmentService.selectExpiringSoonList();
        result.put("expiringSoonList", expiringSoonList);
        List<FireEquipment> expiredList = equipmentService.selectExpiredList();
        result.put("expiredList", expiredList);
        return result;
    }

    private AjaxResult buildPersonalTaskStats(SysUser user) {
        AjaxResult result = AjaxResult.success();
        result.put("scope", "personal");
        FireMaintenanceTask query = new FireMaintenanceTask();
        fireDataPermissionService.applyTaskListScope(query, user);
        if (query.getManagerId() == null && user != null) {
            query.setManagerId(user.getUserId());
        }
        List<FireMaintenanceTask> list = taskService.selectFireMaintenanceTaskList(query);
        int pending = 0;
        int doing = 0;
        int done = 0;
        int today = 0;
        int overdue = 0;
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH);
        int d = cal.get(Calendar.DAY_OF_MONTH);
        if (list != null) {
            for (FireMaintenanceTask t : list) {
                String status = t.getTaskStatus();
                if ("0".equals(status)) {
                    pending++;
                } else if ("1".equals(status)) {
                    doing++;
                } else if ("2".equals(status)) {
                    done++;
                }
                Date planStart = t.getPlanStartTime();
                if (planStart != null) {
                    Calendar pc = Calendar.getInstance();
                    pc.setTime(planStart);
                    if (pc.get(Calendar.YEAR) == y && pc.get(Calendar.MONTH) == m
                            && pc.get(Calendar.DAY_OF_MONTH) == d) {
                        today++;
                    }
                }
                Date planEnd = t.getPlanEndTime();
                if (planEnd != null && planEnd.before(now) && !"2".equals(status) && !"3".equals(status)) {
                    overdue++;
                }
            }
        }
        result.put("pendingCount", pending);
        result.put("doingCount", doing);
        result.put("todayCount", today);
        result.put("doneCount", done);
        result.put("overdueCount", overdue);
        result.put("totalCount", list == null ? 0 : list.size());
        result.put("taskListUrl", "/fire/task");
        return result;
    }

    /**
     * 后台展示名：优先真实姓名；若姓名为空或与登录账号相同则用登录账号。
     * 不使用微信昵称覆盖后的展示（历史昵称已由迁移脚本移出 user_name）。
     */
    public static String resolveDisplayName(SysUser user) {
        if (user == null) {
            return "";
        }
        String name = user.getUserName();
        if (StringUtils.isNotEmpty(name) && !name.equals(user.getLoginName())
                && !name.equals(user.getPhonenumber())) {
            return name;
        }
        if (StringUtils.isNotEmpty(user.getLoginName())) {
            return user.getLoginName();
        }
        return StringUtils.nvl(name, "");
    }
}
