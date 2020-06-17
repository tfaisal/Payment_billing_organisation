package com.abcfinancial.api.billing.scheduler.domain;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Blob;

@Entity
@Table( name = "QRTZ_JOB_DETAILS" )
@Data
public class JobDetails
{
    @EmbeddedId
    private JobDetailsID jobDetailsID;
    @Column( name = "DESCRIPTION" )
    private String description;
    @Column( name = "JOB_CLASS_NAME" )
    private String jobClassName;
    @Column( name = "IS_DURABLE" )
    private Integer isDurable;
    @Column( name = "IS_NONCONCURRENT" )
    private Integer isNonconcurrent;
    @Column( name = "IS_UPDATE_DATA" )
    private Integer isUpdate_data;
    @Column( name = "REQUESTS_RECOVERY" )
    private Integer RequestsRecovery;
    @Column( name = "JOB_DATA" )
    private Blob jobData;
}
