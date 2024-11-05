package com.example.backend.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 *
 * @TableName organizations
 */
@TableName(value ="organizations")
@Data
public class Org implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Integer organizationId;

    /**
     *
     */
    private String name;

    /**
     *
     */
    private String link;

    /**
     *
     */
    private String category1;

    /**
     *
     */
    private String category2;

    /**
     *
     */
    private String category3;

    /**
     *
     */
    private String category4;

    /**
     *
     */
    private Date startDate;

    /**
     *
     */
    private Date endDate;

    /**
     *
     */
    private String timeRange;

    /**
     *
     */
    private String contactEmail;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Org other = (Org) that;
        return (this.getOrganizationId() == null ? other.getOrganizationId() == null : this.getOrganizationId().equals(other.getOrganizationId()))
                && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
                && (this.getLink() == null ? other.getLink() == null : this.getLink().equals(other.getLink()))
                && (this.getCategory1() == null ? other.getCategory1() == null : this.getCategory1().equals(other.getCategory1()))
                && (this.getCategory2() == null ? other.getCategory2() == null : this.getCategory2().equals(other.getCategory2()))
                && (this.getCategory3() == null ? other.getCategory3() == null : this.getCategory3().equals(other.getCategory3()))
                && (this.getCategory4() == null ? other.getCategory4() == null : this.getCategory4().equals(other.getCategory4()))
                && (this.getStartDate() == null ? other.getStartDate() == null : this.getStartDate().equals(other.getStartDate()))
                && (this.getEndDate() == null ? other.getEndDate() == null : this.getEndDate().equals(other.getEndDate()))
                && (this.getTimeRange() == null ? other.getTimeRange() == null : this.getTimeRange().equals(other.getTimeRange()))
                && (this.getContactEmail() == null ? other.getContactEmail() == null : this.getContactEmail().equals(other.getContactEmail()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getOrganizationId() == null) ? 0 : getOrganizationId().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getLink() == null) ? 0 : getLink().hashCode());
        result = prime * result + ((getCategory1() == null) ? 0 : getCategory1().hashCode());
        result = prime * result + ((getCategory2() == null) ? 0 : getCategory2().hashCode());
        result = prime * result + ((getCategory3() == null) ? 0 : getCategory3().hashCode());
        result = prime * result + ((getCategory4() == null) ? 0 : getCategory4().hashCode());
        result = prime * result + ((getStartDate() == null) ? 0 : getStartDate().hashCode());
        result = prime * result + ((getEndDate() == null) ? 0 : getEndDate().hashCode());
        result = prime * result + ((getTimeRange() == null) ? 0 : getTimeRange().hashCode());
        result = prime * result + ((getContactEmail() == null) ? 0 : getContactEmail().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", organizationId=").append(organizationId);
        sb.append(", name=").append(name);
        sb.append(", link=").append(link);
        sb.append(", category1=").append(category1);
        sb.append(", category2=").append(category2);
        sb.append(", category3=").append(category3);
        sb.append(", category4=").append(category4);
        sb.append(", startDate=").append(startDate);
        sb.append(", endDate=").append(endDate);
        sb.append(", timeRange=").append(timeRange);
        sb.append(", contactEmail=").append(contactEmail);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}