package com.abcfinancial.api.billing.scheduler

import org.quartz.Calendar
import org.quartz.TriggerBuilder
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

import java.time.*

class PeriodTriggerTests
{
    private static final ZoneId UTC = ZoneId.of("UTC")

    @DataProvider
    Object[][] sampleSchedules()
    {
        [
                [
                        LocalDate.of(2010, 1, 31),
                        Period.ofMonths(1), Period.ofYears(1),
                        [
                                LocalDate.of(2010, 2, 28),
                                LocalDate.of(2010, 3, 31),
                                LocalDate.of(2010, 4, 30),
                                LocalDate.of(2010, 5, 31),
                                LocalDate.of(2010, 6, 30),
                                LocalDate.of(2010, 7, 31),
                                LocalDate.of(2010, 8, 31),
                                LocalDate.of(2010, 9, 30),
                                LocalDate.of(2010, 10, 31),
                                LocalDate.of(2010, 11, 30),
                                LocalDate.of(2010, 12, 31)
                        ]
                ],
                [
                        LocalDate.of(2016, 1, 31),
                        Period.ofMonths(1), Period.ofYears(1),
                        [
                                LocalDate.of(2016, 2, 29),
                                LocalDate.of(2016, 3, 31),
                                LocalDate.of(2016, 4, 30),
                                LocalDate.of(2016, 5, 31),
                                LocalDate.of(2016, 6, 30),
                                LocalDate.of(2016, 7, 31),
                                LocalDate.of(2016, 8, 31),
                                LocalDate.of(2016, 9, 30),
                                LocalDate.of(2016, 10, 31),
                                LocalDate.of(2016, 11, 30),
                                LocalDate.of(2016, 12, 31)
                        ]
                ],
                [
                        LocalDate.of(2010, 7, 31),
                        Period.ofMonths(1), Period.ofYears(3),
                        [
                                LocalDate.of(2010, 8, 31),
                                LocalDate.of(2010, 9, 30),
                                LocalDate.of(2010, 10, 31),
                                LocalDate.of(2010, 11, 30),
                                LocalDate.of(2010, 12, 31),
                                LocalDate.of(2011, 1, 31),
                                LocalDate.of(2011, 2, 28),
                                LocalDate.of(2011, 3, 31),
                                LocalDate.of(2011, 4, 30),
                                LocalDate.of(2011, 5, 31),
                                LocalDate.of(2011, 6, 30),
                                LocalDate.of(2011, 7, 31),
                                LocalDate.of(2011, 8, 31),
                                LocalDate.of(2011, 9, 30),
                                LocalDate.of(2011, 10, 31),
                                LocalDate.of(2011, 11, 30),
                                LocalDate.of(2011, 12, 31),
                                LocalDate.of(2012, 1, 31),
                                LocalDate.of(2012, 2, 29),
                                LocalDate.of(2012, 3, 31),
                                LocalDate.of(2012, 4, 30),
                                LocalDate.of(2012, 5, 31),
                                LocalDate.of(2012, 6, 30),
                                LocalDate.of(2012, 7, 31),
                                LocalDate.of(2012, 8, 31),
                                LocalDate.of(2012, 9, 30),
                                LocalDate.of(2012, 10, 31),
                                LocalDate.of(2012, 11, 30),
                                LocalDate.of(2012, 12, 31),
                                LocalDate.of(2013, 1, 31),
                                LocalDate.of(2013, 2, 28),
                                LocalDate.of(2013, 3, 31),
                                LocalDate.of(2013, 4, 30),
                                LocalDate.of(2013, 5, 31),
                                LocalDate.of(2013, 6, 30),

                        ]
                ]
        ]
    }

    @Test(dataProvider = 'sampleSchedules')
    void testSchedule(LocalDate startDate, Period frequency, Period duration, List<LocalDate> scheduledTriggers)
    {
        final Calendar calendar = null
        Clock clock = Clock.fixed(startDate.atStartOfDay().toInstant(ZoneOffset.UTC), UTC)
        PeriodTrigger trigger = TriggerBuilder.newTrigger()
                .startAt(Date.from(startDate.atStartOfDay().toInstant(ZoneOffset.UTC)))
                .withSchedule(PeriodScheduleBuilder.scheduleBuilder(clock)
                .frequency(frequency)
                .duration(duration))
                .build()

        trigger.computeFirstFireTime(calendar)
        Assert.assertEquals(trigger.getNextFire(), startDate.atStartOfDay())

        scheduledTriggers.each { expectedDate ->
            trigger.triggered(calendar)
            LocalDateTime nextFire = trigger.getNextFire()
            Assert.assertNotNull(nextFire)
            Assert.assertEquals(nextFire, expectedDate.atStartOfDay())
        }

        trigger.triggered(calendar)
        Assert.assertTrue(trigger.isComplete())
        Assert.assertNull(trigger.getNextFire())
    }

    @DataProvider
    Object[][] leapYears()
    {
        [
                [2000, true], [2001, false], [2002, false], [2003, false], [2004, true], [2005, false], [2006, false], [2007, false], [2008, true], [2009, false],
                [2010, false], [2011, false], [2012, true], [2013, false], [2014, false], [2015, false], [2016, true], [2017, false], [2018, false], [2019, false],
                [2020, true], [2021, false], [2022, false], [2023, false], [2024, true], [2025, false], [2026, false], [2027, false], [2028, true], [2029, false],
                [2100, false], [2200, false], [2300, false], [2400, true]
        ]
    }

    @Test(dataProvider = 'leapYears')
    void testLeapYear(int year, boolean isLeapYear)
    {
        final Calendar calendar = null
        final LocalDateTime start = LocalDate.of(year, 1, 31).atStartOfDay()
        final Period frequency = Period.ofMonths(1)
        Clock clock = Clock.fixed(start.toInstant(ZoneOffset.UTC), UTC)
        PeriodTrigger trigger = TriggerBuilder.newTrigger()
                .startAt(Date.from(start.toInstant(ZoneOffset.UTC)))
                .withSchedule(PeriodScheduleBuilder.scheduleBuilder(clock)
                .frequency(frequency)
                .duration(Period.ofYears(1)))
                .build()

        trigger.computeFirstFireTime(calendar)
        Assert.assertEquals(trigger.getNextFire(), LocalDate.of(year, 1, 31).atStartOfDay())

        trigger.triggered(calendar)
        LocalDateTime nextFire = trigger.getNextFire()
        Assert.assertNotNull(nextFire)
        Assert.assertEquals(nextFire, LocalDate.of(year, 2, isLeapYear ? 29 : 28).atStartOfDay())
    }

    @DataProvider
    Object[][] lastBillDate()
    {
        [
                [LocalDate.of(2010, 1, 1), Period.ofMonths(1), Period.ofYears(1), LocalDate.of(2010, 12, 1)],
                [LocalDate.of(2010, 1, 1), Period.ofMonths(1), Period.ofYears(3), LocalDate.of(2012, 12, 1)],

                [LocalDate.of(2010, 1, 1), Period.ofWeeks(1), Period.ofYears(1), LocalDate.of(2010, 12, 31)],
                [LocalDate.of(2010, 1, 1), Period.ofWeeks(1), Period.ofMonths(6), LocalDate.of(2010, 6, 25)], //i'm thinking we divide duration by frequency, then round down...then either giving the "change" away un-billed, or ending your amenity usage early???
                [LocalDate.of(2010, 1, 1), Period.ofWeeks(1), Period.ofYears(3), LocalDate.of(2012, 12, 28)]  //or we accept the fact that you will be billed towards the end, stretching your usage of the amenity past the duration
                //prorating sucks, I don't even want to think about it
        ]
    }

    @Test(dataProvider = 'lastBillDate')
    void testLastBillDate(LocalDate startDate, Period frequency, Period duration, LocalDate lastBillDate)
    {
        final Calendar calendar = null
        final LocalDateTime start = startDate.atStartOfDay()
        Clock clock = Clock.fixed(start.toInstant(ZoneOffset.UTC), UTC)
        PeriodTrigger trigger = TriggerBuilder.newTrigger()
                .startAt(Date.from(start.toInstant(ZoneOffset.UTC)))
                .withSchedule(PeriodScheduleBuilder.scheduleBuilder(clock)
                .frequency(frequency)
                .duration(duration))
                .build()

        trigger.computeFirstFireTime(calendar)
        Assert.assertEquals(trigger.getNextFire(), startDate.atStartOfDay())

        Optional<LocalDateTime> finalFire = trigger.getFinalFire()
        Assert.assertTrue(finalFire.isPresent())
        Assert.assertEquals(finalFire.get(), lastBillDate.atStartOfDay())
    }
}
