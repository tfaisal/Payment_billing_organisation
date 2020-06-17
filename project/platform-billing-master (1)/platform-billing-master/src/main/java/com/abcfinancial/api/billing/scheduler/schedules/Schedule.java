package com.abcfinancial.api.billing.scheduler.schedules;

import com.abcfinancial.api.billing.utility.common.ApplicationConfiguration;
import com.abcfinancial.api.billing.utility.common.MessageUtils;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Data
@Builder

public class Schedule<T>
{
    @Autowired
    private ApplicationConfiguration applicationConfiguration;
    private LocalDateTime start;
    private boolean repeating;
    private Period frequency;
    private Period duration;
    private T properties;
    private LocalDate cancel;
    private LocalDate expire;
    private LocalDate renewStart;

    @SneakyThrows
    public void validate( )
    {
        if( getStart( ) == null )
        {
            throw new SchedulerException( applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SCHEDULED_EVENT ) );
        }
        if( isRepeating( ) && getFrequency( ) == null )
        {
            throw new SchedulerException( applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SCHEDULED_REPEAT ) );
        }
        if( getProperties( ) == null )
        {
            throw new SchedulerException( applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SCHEDULED_EVENT1 ) );
        }
    }
}
