package com.abcfinancial.api.billing.scheduler;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.ScheduleBuilder;
import org.quartz.spi.MutableTrigger;

import java.time.Clock;
import java.time.Period;

@Slf4j
@Getter
@Setter

public class PeriodScheduleBuilder extends ScheduleBuilder<PeriodTrigger>
{
    private Clock clock;
    private Period frequency;
    private Period duration;

    protected PeriodScheduleBuilder( Clock clock )
    {
        setClock( clock );
    }

    @Override
    public MutableTrigger build( )
    {
        return PeriodTrigger.builder( )
                            .clock( getClock( ) )
                            .frequency( getFrequency( ) )
                            .duration( getDuration( ) )
                            .build( );
    }

    public static PeriodScheduleBuilder scheduleBuilder( Clock clock )
    {
        return new PeriodScheduleBuilder( clock );
    }

    public PeriodScheduleBuilder frequency( Period frequency )
    {
        setFrequency( frequency );
        return this;
    }

    public PeriodScheduleBuilder duration( Period duration )
    {
        setDuration( duration );
        return this;
    }
}
