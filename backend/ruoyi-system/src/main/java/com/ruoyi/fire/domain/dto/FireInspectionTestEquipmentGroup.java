package com.ruoyi.fire.domain.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.ruoyi.fire.domain.FireMaintenanceRecord;

/**
 * Ѳ����Զ����豸���飨ͬҵ����ϲ� type0/type1 ��Դ����
 */
public class FireInspectionTestEquipmentGroup
{
    private String equipmentKey;
    private String equipmentName;
    private Integer sortOrder;
    private Long maintenanceRecordId;
    private Long fireTestRecordId;
    private List<FireMaintenanceRecord> sourceRecords = new ArrayList<>();
    private List<FireMaintenanceRecord> checkItems = new ArrayList<>();
    private Integer totalItems = 0;
    private Integer completedItems = 0;
    private Integer uncompletedItems = 0;
    private String status;
    /** 0=巡查，1=测试，2=保养。 */
    private String recordType;
    /** 由后端统一生成，前端只负责展示。 */
    private String recordTypeLabel;

    /** ��������ά���ֶΣ�ȡ fireTest ���¼�� */
    private String deviceLocation;
    private String testSituation;
    private Date testTime;
    private String testResult;
    private String sitePhotos;
    private boolean hasCheckItems;
    private boolean hasMaintenance;

    public String getEquipmentKey()
    {
        return equipmentKey;
    }

    public void setEquipmentKey(String equipmentKey)
    {
        this.equipmentKey = equipmentKey;
    }

    public String getEquipmentName()
    {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName)
    {
        this.equipmentName = equipmentName;
    }

    public Integer getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder)
    {
        this.sortOrder = sortOrder;
    }

    public Long getMaintenanceRecordId()
    {
        return maintenanceRecordId;
    }

    public void setMaintenanceRecordId(Long maintenanceRecordId)
    {
        this.maintenanceRecordId = maintenanceRecordId;
    }

    public Long getFireTestRecordId()
    {
        return fireTestRecordId;
    }

    public void setFireTestRecordId(Long fireTestRecordId)
    {
        this.fireTestRecordId = fireTestRecordId;
    }

    public List<FireMaintenanceRecord> getSourceRecords()
    {
        return sourceRecords;
    }

    public void setSourceRecords(List<FireMaintenanceRecord> sourceRecords)
    {
        this.sourceRecords = sourceRecords;
    }

    public List<FireMaintenanceRecord> getCheckItems()
    {
        return checkItems;
    }

    public void setCheckItems(List<FireMaintenanceRecord> checkItems)
    {
        this.checkItems = checkItems;
    }

    public Integer getTotalItems()
    {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems)
    {
        this.totalItems = totalItems;
    }

    public Integer getCompletedItems()
    {
        return completedItems;
    }

    public void setCompletedItems(Integer completedItems)
    {
        this.completedItems = completedItems;
    }

    public Integer getUncompletedItems()
    {
        return uncompletedItems;
    }

    public void setUncompletedItems(Integer uncompletedItems)
    {
        this.uncompletedItems = uncompletedItems;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getRecordType()
    {
        return recordType;
    }

    public void setRecordType(String recordType)
    {
        this.recordType = recordType;
    }

    public String getRecordTypeLabel()
    {
        return recordTypeLabel;
    }

    public void setRecordTypeLabel(String recordTypeLabel)
    {
        this.recordTypeLabel = recordTypeLabel;
    }

    public String getDeviceLocation()
    {
        return deviceLocation;
    }

    public void setDeviceLocation(String deviceLocation)
    {
        this.deviceLocation = deviceLocation;
    }

    public String getTestSituation()
    {
        return testSituation;
    }

    public void setTestSituation(String testSituation)
    {
        this.testSituation = testSituation;
    }

    public Date getTestTime()
    {
        return testTime;
    }

    public void setTestTime(Date testTime)
    {
        this.testTime = testTime;
    }

    public String getTestResult()
    {
        return testResult;
    }

    public void setTestResult(String testResult)
    {
        this.testResult = testResult;
    }

    public String getSitePhotos()
    {
        return sitePhotos;
    }

    public void setSitePhotos(String sitePhotos)
    {
        this.sitePhotos = sitePhotos;
    }

    public boolean isHasCheckItems()
    {
        return hasCheckItems;
    }

    public void setHasCheckItems(boolean hasCheckItems)
    {
        this.hasCheckItems = hasCheckItems;
    }

    public boolean isHasMaintenance()
    {
        return hasMaintenance;
    }

    public void setHasMaintenance(boolean hasMaintenance)
    {
        this.hasMaintenance = hasMaintenance;
    }
}
