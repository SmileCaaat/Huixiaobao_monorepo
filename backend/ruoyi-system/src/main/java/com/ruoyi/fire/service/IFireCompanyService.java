package com.ruoyi.fire.service;

import java.util.List;
import com.ruoyi.fire.domain.FireCompany;
import com.ruoyi.fire.domain.FireUserCompany;

/**
 * 巡检公司Service接口
 * 
 * @author ruoyi
 */
public interface IFireCompanyService {

    /**
     * 查询公司列表
     */
    List<FireCompany> selectFireCompanyList(FireCompany fireCompany);

    /**
     * 客户管理列表（聚合合同与建筑信息）
     */
    List<FireCompany> selectCustomerManageList(FireCompany fireCompany);

    /**
     * 按客户ID查询客户管理导出数据
     */
    List<FireCompany> selectCustomerManageByIds(Long[] companyIds);

    /**
     * 查询所有公司
     */
    List<FireCompany> selectCompanyAll();

    /**
     * 根据ID查询公司
     */
    FireCompany selectFireCompanyById(Long companyId);

    /**
     * 新增公司
     */
    int insertFireCompany(FireCompany fireCompany);

    /**
     * 修改公司
     */
    int updateFireCompany(FireCompany fireCompany);

    /**
     * 删除公司
     */
    int deleteFireCompanyById(Long companyId);

    /**
     * 批量删除公司
     */
    int deleteFireCompanyByIds(Long[] companyIds);

    /**
     * 检查公司编码是否唯一
     */
    boolean checkCompanyCodeUnique(FireCompany fireCompany);

    /**
     * 检查公司名称是否唯一
     */
    boolean checkCompanyNameUnique(FireCompany fireCompany);

    /**
     * 查询用户关联的公司列表
     */
    List<FireCompany> selectCompanyListByUserId(Long userId);

    /**
     * 查询公司关联的用户列表
     */
    List<FireUserCompany> selectUserListByCompanyId(Long companyId);

    /**
     * 查询公司下账号正常、未删除的已注册用户
     */
    List<FireUserCompany> selectActiveUserListByCompanyId(Long companyId);

    /**
     * 分配用户到公司
     */
    int assignUsers(Long companyId, Long[] userIds, String roleType, String createBy);

    /**
     * 取消用户与公司的关联
     */
    int cancelUserCompany(Long companyId, Long userId);

    /**
     * 统计公司数量
     */
    int countCompany();
}
