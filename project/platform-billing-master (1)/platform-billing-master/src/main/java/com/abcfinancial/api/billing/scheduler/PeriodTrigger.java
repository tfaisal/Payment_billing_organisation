package com.abcfinancial.api.billing.scheduler;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.triggers.AbstractTrigger;
import org.quartz.impl.triggers.CoreTrigger;

import java.time.*;
import java.util.Date;
import java.util.Optional;

@Data
@Slf4j
@Builder
@EqualsAndHashCode( callSuper = true )

public class PeriodTrigger extends AbstractTrigger<PeriodTrigger> implements CoreTrigger, Trigger
{
    private static final long serialVersionUID = -2635982274232850343L;
    /**
     *<p>
     * Instructs the<code>{@link Scheduler}</code> that upon a mis-fire
     * situation, the<code>{@link CalendarIntervalTrigger}</code> wants to be
     * fired now by<code>Scheduler</code>.
     *</p>
     */

    public static final int MISFIRE_INSTRUCTION_FIRE_ONCE_NOW = 1;
    /**
     *<p>
     * Instructs the<code>{@link Scheduler}</code> that upon a mis-fire
     * situation, the<code>{@link CalendarIntervalTrigger}</code> wants to have it's
     * next-fire-time updated to the next time in the schedule after the
     * current time ( taking into account any associated<code>{@link java.util.Calendar}</code>, 
     * but it does not want to be fired now.
     *</p>
     */

    public static final int MISFIRE_INSTRUCTION_DO_NOTHING = 2;
    private Clock clock;
    private LocalDateTime start;
    private LocalDateTime end;
    private Period frequency;
    private Period duration;
    private LocalDateTime nextFire;
    private LocalDateTime previousFire;
    private int timesTriggered;

    @Override
    public ScheduleBuilder<PeriodTrigger> getScheduleBuilder( )
    {
        return PeriodScheduleBuilder.scheduleBuilder( getClock( ) )
                                    .frequency( getFrequency( ) )
                                    .duration( getDuration( ) );
    }

    @Override
    @SneakyThrows
    public void validate( )
    {
        super.validate( );
        if( getFrequency( ) == null )
        {
            throw new SchedulerException( "Billing trigger requires a valid frequency" );
        }
    }

    public Optional<LocalDateTime> computeFirstFire( Calendar calendar )
    {
        setNextFire( getStart( ) );
        while( getNextFire( ) != null && calendar != null && !calendar.isTimeIncluded( getNextFireTime( ).getTime( ) ) )
        {
            setNextFire( getFireTimeAfter( getNextFire( ) ).orElse( null ) );
            if( getNextFire( ) == null )
            {
                break;
            }
            //avoid infinite loop
            if( getNextFire( ).isAfter( LocalDateTime.now( getClock( ) ).plusYears( 100 ) ) )
            {
                setNextFire( null );
            }
        }
        return Optional.ofNullable( getNextFire( ) );
    }

    @Override
    public void triggered( Calendar calendar )
    {
        timesTriggered++;
        setPreviousFire( getNextFire( ) );
        setNextFire( getFireTimeAfter( getNextFire( ) ).orElse( null ) );
        while( getNextFire( ) != null && calendar != null && !calendar.isTimeIncluded( getNextFire( ).toInstant( ZoneOffset.UTC ).toEpochMilli( ) ) )
        {
            setNextFire( getFireTimeAfter( getNextFire( ) ).orElse( null ) );
            if( getNextFire( ) == null )
            {
                break;
            }
            //avoid infinite loop
            if( getNextFire( ).isAfter( LocalDateTime.now( getClock( ) ).plusYears( 100 ) ) )
            {
                setNextFire( null );
            }
        }
    }

    public Optional<LocalDateTime> getFireTimeAfter( LocalDateTime afterTime )
    {
        Optional<LocalDateTime> fireTimeAfter;
        if( isInfiniteRepeat( ) || isBeforeEnd( afterTime ) )
        {
            LocalDateTime current = getStart( );
            log.trace( "Billing trigger FIRST fire {}", current );
            for( int i = 0; ( current.isEqual( afterTime ) || current.isBefore( afterTime ) ) && isBeforeEnd( current ); i++ )
            {
                current = getStart( ).plus( getFrequency( ).multipliedBy( i ) );
                log.trace( "Billing trigger next fire {}", current );
            }
            if( isAfterEnd( current ) )
            {
                fireTimeAfter = Optional.empty( ); //we don't fire after this time
            }
            else
            {
                fireTimeAfter = Optional.of( current );
            }
        }
        else //trigger ends before the after time
        {
            fireTimeAfter = Optional.empty( );
        }
        return fireTimeAfter;
    }

    public Optional<LocalDateTime> getFinalFire( )
    {
        Optional<LocalDateTime> finalFireTime;
        if( getEnd( ).isPresent( ) )
        {
            LocalDateTime current = getStart( );
            log.trace( "Billing trigger FIRST fire {}", current );
            for( int i = 0; isBeforeEnd( current ); i++ )
            {
                current = getStart( ).plus( getFrequency( ).multipliedBy( i ) );
                log.trace( "Billing trigger next fire {}", current );
            }
            finalFireTime = Optional.of( current );
        }
        else
        {
            log.trace( "Billing trigger is complete or has no end {}", this );
            finalFireTime = Optional.empty( );
        }
        return finalFireTime;
    }

    public boolean isComplete( )
    {
        return getNextFire( ) == null;
    }

    public LocalDateTime getStart( )
    {
        if( start == null )
        {
            start = LocalDateTime.now( getClock( ) );
        }
        return start;
    }

    public void setStart( LocalDateTime startDate )
    {
        if( startDate != null )
        {
            if( isInfiniteRepeat( ) || isBeforeEnd( startDate ) )
            {
                this.start = startDate;
            }
            else
            {
                throw new IllegalArgumentException( "End time cannot be before start time" );
            }
        }
        else
        {
            throw new IllegalArgumentException( "Start time cannot be null" );
        }
    }
    /**
     * Used to override the end time, regardless of the configured duration
     */

    public void setEnd( LocalDateTime endTime )
    {
        if( endTime == null || getStart( ) == null || getStart( ).isBefore( endTime ) )
        {
            this.end = endTime;
        }
        else
        {
            throw new IllegalArgumentException( "End time cannot be before start time" );
        }
    }

    @Override
    protected boolean validateMisfireInstruction( int misfireInstruction )
    {
        if( misfireInstruction < MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY )
        {
            return false;
        }
        return misfireInstruction <= MISFIRE_INSTRUCTION_DO_NOTHING;
    }

    @Override
    public void updateAfterMisfire( Calendar cal )
    {
        int instr = getMisfireInstruction( );
        if( instr == Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY )
        {
            return;
        }
        if( instr == MISFIRE_INSTRUCTION_SMART_POLICY )
        {
            instr = MISFIRE_INSTRUCTION_FIRE_ONCE_NOW;
        }
        if( instr == MISFIRE_INSTRUCTION_DO_NOTHING )
        {
            Date newFireTime = getFireTimeAfter( new Date( ) );
            while( newFireTime != null && cal != null
                   && !cal.isTimeIncluded( newFireTime.getTime( ) ) )
            {
                newFireTime = getFireTimeAfter( newFireTime );
            }
            setNextFireTime( newFireTime );
        }
        else if( instr == MISFIRE_INSTRUCTION_FIRE_ONCE_NOW )
        {
            // fire once now...
            setNextFire( LocalDateTime.now( getClock( ) ) );
            // the new fire time afterward will magically preserve the original
            // time of day for firing for day/week/month interval triggers, 
            // because of the way getFireTimeAfter( ) works - in its always restarting
            // computation from the start time.
        }
    }

    @Override
    public void updateWithNewCalendar( Calendar calendar, long misfireThresholdMillis )
    {
        setNextFire( getFireTimeAfter( getPreviousFire( ) ).orElse( null ) );
        if( getNextFire( ) == null || calendar == null )
        {
            return;
        }
        LocalDateTime now = LocalDateTime.now( getClock( ) );
        while( getNextFire( ) != null && !calendar.isTimeIncluded( getNextFire( ).toInstant( ZoneOffset.UTC ).toEpochMilli( ) ) )
        {
            setNextFire( getFireTimeAfter( getNextFire( ) ).orElse( null ) );
            if( getNextFire( ) != null )
            {
                //avoid infinite loop
                if( getNextFire( ).isAfter( now.plusYears( 100 ) ) )
                {
                    setNextFire( null );
                }
                if( getNextFire( ) != null && Duration.between( getNextFire( ), now ).toMillis( ) >= misfireThresholdMillis )
                {
                    setNextFireTime( getFireTimeAfter( getNextFireTime( ) ) );
                }
            }
        }
    }

    @Override
    public boolean mayFireAgain( )
    {
        return getNextFire( ) != null;
    }

    public boolean isInfiniteRepeat( )
    {
        return getDuration( ) == null;
    }

    public boolean isBeforeEnd( LocalDateTime time )
    {
        return getEnd( ).map( time::isBefore ).orElse( true );
    }

    public boolean isAfterEnd( LocalDateTime time )
    {
        return getEnd( ).map( time::isAfter ).orElse( false );
    }

    public Optional<LocalDateTime> getEnd( )
    {
        Optional<LocalDateTime> end;
        if( this.end != null )
        {
            end = Optional.of( this.end );
        }
        else if( getDuration( ) != null )
        {
            end = Optional.of( getStart( ).plus( getDuration( ) ).minus( getFrequency( ) ) );
        }
        else
        {
            end = Optional.empty( );
        }
        return end;
    }
    /*
     * Legacy methods using old java time library...required by interface, but prefer jdk 8 versions
     */

    @Override
    public Date computeFirstFireTime( Calendar calendar )
    {
        return computeFirstFire( calendar ).map( localDateTime -> Date.from( localDateTime.toInstant( ZoneOffset.UTC ) ) ).orElse( null );
    }

    @Override
    public Date getFireTimeAfter( Date afterTime )
    {
        return getFireTimeAfter( LocalDateTime.ofInstant( afterTime.toInstant( ), ZoneOffset.UTC ) )
            .map( fireTimeAfter -> Date.from( fireTimeAfter.toInstant( ZoneOffset.UTC ) ) )
            .orElse( null );
    }

    @Override
    public Date getFinalFireTime( )
    {
        return getFinalFire( )
            .map( finalFire -> Date.from( finalFire.toInstant( ZoneOffset.UTC ) ) )
            .orElse( null );
    }

    @Override
    public Date getStartTime( )
    {
        return Optional.ofNullable( getStart( ) ).map( localDateTime -> Date.from( localDateTime.toInstant( ZoneOffset.UTC ) ) ).orElse( null );
    }

    @Override
    public void setStartTime( Date startDate )
    {
        if( startDate != null )
        {
            setStart( LocalDateTime.ofInstant( startDate.toInstant( ), ZoneOffset.UTC ) );
        }
        else
        {
            setStart( null );
        }
    }

    @Override
    public Date getEndTime( )
    {
        return getEnd( ).map( localDateTime -> Date.from( localDateTime.toInstant( ZoneOffset.UTC ) ) ).orElse( null );
    }

    @Override
    public void setEndTime( Date endTime )
    {
        if( endTime != null )
        {
            setEnd( LocalDateTime.ofInstant( endTime.toInstant( ), ZoneOffset.UTC ) );
        }
        else
        {
            setEnd( null );
        }
    }

    @Override
    public Date getNextFireTime( )
    {
        return Optional.ofNullable( getNextFire( ) ).map( localDateTime -> Date.from( localDateTime.toInstant( ZoneOffset.UTC ) ) ).orElse( null );
    }

    @Override
    public void setNextFireTime( Date nextFireTime )
    {
        if( nextFireTime != null )
        {
            setNextFire( LocalDateTime.ofInstant( nextFireTime.toInstant( ), ZoneOffset.UTC ) );
        }
        else
        {
            setNextFire( null );
        }
    }

    @Override
    public Date getPreviousFireTime( )
    {
        return Optional.ofNullable( getPreviousFire( ) ).map( localDateTime -> Date.from( localDateTime.toInstant( ZoneOffset.UTC ) ) ).orElse( null );
    }

    @Override
    public void setPreviousFireTime( Date previousFireTime )
    {
        if( previousFireTime != null )
        {
            setPreviousFire( LocalDateTime.ofInstant( previousFireTime.toInstant( ), ZoneOffset.UTC ) );
        }
        else
        {
            setPreviousFire( null );
        }
    }

    public boolean hasAdditionalProperties( )
    {
        return false;
    }
}
