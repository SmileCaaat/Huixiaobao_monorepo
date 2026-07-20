package com.ruoyi.fire.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 签到图片对象 fire_check_in_image
 * 
 * @author ruoyi
 */
public class FireCheckInImage {
    private static final long serialVersionUID = 1L;

    /** 图片ID */
    private Long imageId;

    /** 签到ID */
    private Long checkInId;

    /** 图片地址 */
    private String imageUrl;

    /** 图片名称 */
    private String imageName;

    /** 排序 */
    private Integer sortOrder;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public Long getCheckInId() {
        return checkInId;
    }

    public void setCheckInId(Long checkInId) {
        this.checkInId = checkInId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("imageId", getImageId())
                .append("checkInId", getCheckInId())
                .append("imageUrl", getImageUrl())
                .append("imageName", getImageName())
                .append("sortOrder", getSortOrder())
                .append("createTime", getCreateTime())
                .toString();
    }
}
