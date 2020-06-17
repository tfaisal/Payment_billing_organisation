package com.abcfinancial.api.billing.scheduler.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor

public class JobDetailsID implements Serializable
{
    private static final long serialVersionUID = 1L;
    @Column( name = "SCHED_NAME" )
    private String scheduleName;
    @Column( name = "JOB_NAME" )
    private String jobName;
    @Column( name = "JOB_GROUP" )
    private String jobGroup;
}
