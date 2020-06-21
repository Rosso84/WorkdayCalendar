import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class WorkdayCalendarTest {

    private WorkdayCalendar wCalendar = new WorkdayCalendar();
    private WorkdayCalendar.CalendarController cController = wCalendar.new CalendarController();
    private WorkdayCalendar.Holidays holidays = wCalendar.new Holidays();

    private String europeanDatePattern = "dd-MM-yyyy HH:mm";
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(europeanDatePattern);

    @After
    public void cleanUp() {
        for (LocalDate day: this.getDefaultHolidaysAsLocalDate()){
            holidays.removeHoliday(day);
        }

        for (MonthDay day: this.getDefaultHolidaysAsMonthdays()){
            holidays.removeRecurringHoliday(day);
        }
    }


    private List<MonthDay> getDefaultHolidaysAsMonthdays() {
        List<MonthDay> defaultMonthDays = new ArrayList<>();

        MonthDay xmas = MonthDay.of( Month.DECEMBER, 25 ) ;
        MonthDay easterMonday = MonthDay.of( Month.APRIL, 3 ) ;
        MonthDay easterSunday = MonthDay.of( Month.APRIL, 12 ) ;

        defaultMonthDays.add(xmas);
        defaultMonthDays.add(easterMonday);
        defaultMonthDays.add(easterSunday);

        return defaultMonthDays;
    }

    private List<LocalDate> getDefaultHolidaysAsLocalDate() {
        List<LocalDate> localDates = new ArrayList<>();

        ZoneId zoneId = ZoneId.of( "Europe/Paris" ) ;
        Year thisYear = Year.now( zoneId );

        LocalDate xmasThisYear = thisYear.atMonthDay( getDefaultHolidaysAsMonthdays().get(0) ) ;

        LocalDate easterMondayThisYear = thisYear.atMonthDay( getDefaultHolidaysAsMonthdays().get(1)) ;

        LocalDate easterSundayThisYear = thisYear.atMonthDay( getDefaultHolidaysAsMonthdays().get(2)) ;

        localDates.add(xmasThisYear);
        localDates.add(easterMondayThisYear);
        localDates.add(easterSundayThisYear);

        return localDates;
    }

    private void setDefaultHolidays(){
        for (LocalDate date : getDefaultHolidaysAsLocalDate()) {
            if (!cController.isWeekend(date)) {
                wCalendar.setHoliday(date);
            }
        }
    }

    private void setDefaultRecurringHolidays(){
        for (MonthDay date : getDefaultHolidaysAsMonthdays()) {
                wCalendar.setRecurringHoliday(date);
        }
    }

    private void setWorkdayStartAndStop(){
        LocalDateTime start = LocalDateTime.of(2020, 2, 3, 8, 0);
        LocalDateTime stop = LocalDateTime.of(2020, 2, 3, 16, 0);

        wCalendar.setWorkdayStartAndStop(start, stop);
    }

    @Test
    public void testIsWeekend() {
        //Verify if chosen date is a weekend
        assertFalse( cController.isWeekend( getDefaultHolidaysAsLocalDate().get( 0 ) ) );  //xmas friday this year
        assertFalse( cController.isWeekend( getDefaultHolidaysAsLocalDate().get( 1 ) ) ); //easterMonday this year

        //True if on a sunday
        assertTrue( cController.isWeekend(getDefaultHolidaysAsLocalDate().get( 2 ) ) );// easterSunday this year
    }

    @Test
    public void testIsHoliday(){

        //set a holiday
        LocalDate xmas = getDefaultHolidaysAsLocalDate().get(0); //on a friday
        wCalendar.setHoliday(xmas);

        //Create a date on a sunday
        LocalDate sunday = getDefaultHolidaysAsLocalDate().get(2); //easterSunday

        assertTrue(cController.isHoliday(xmas));
        assertTrue(cController.isHoliday(sunday));

    }

    @Test
    public void testSetHoliday() {
        int previousSizeOfHolidays = holidays.getHolidays().size();

        setDefaultHolidays();

        Set<LocalDate> newRegisteredHolidays = holidays.getHolidays();

        assertEquals( previousSizeOfHolidays + 2 , newRegisteredHolidays.size() );

        //Verify same date is stored
        LocalDate xmas2020 = getDefaultHolidaysAsLocalDate().get(0);

        assertTrue( newRegisteredHolidays.contains( xmas2020 ) );

        //Add same dates again and Verify duplicates is removed
        setDefaultHolidays();
        assertEquals(previousSizeOfHolidays + 2, holidays.getHolidays().size() );

    }


    //TODO: not finished
    @Test
    public void testSetRecurringHoliday() {
        setDefaultRecurringHolidays();

        //Verify  3 dates added
        assertEquals(3, holidays.getRecurringHolidays().size() );

        //Verify same date is stored
        MonthDay xmas = getDefaultHolidaysAsMonthdays().get(0);
        assertTrue( holidays.getRecurringHolidays().contains( xmas ) );

        //Add same dates again and Verify duplicates removed
        setDefaultRecurringHolidays();
        assertEquals(3, holidays.getRecurringHolidays().size() );

    }


    @Test
    public void testSetHolidayIfIsRecurring(){
        int previousSizeHoliday = holidays.getHolidays().size();
        int previousSizeRecurring = holidays.getRecurringHolidays().size();

        //sets xmas, easterMonday and eastersunday from 2020
        setDefaultRecurringHolidays();

        int newSizeRecurringHolidaysList = holidays.getRecurringHolidays().size();

        //Verify previous contains 3 more
        assertEquals(previousSizeRecurring + 3, newSizeRecurringHolidaysList);

        holidays.setHolidayIfIsSetToRecurring();

        //should ha have been transferred from  recurring and contain 3 more now
        int newSizeHolidays = holidays.getHolidays().size();

        //verify holidays contains 3
        assertEquals(previousSizeHoliday + 3, newSizeHolidays);
    }

    @Test
    public void testSetWorkdayStartAndStop() {
        //is by default set to 8 working hours a day
        setWorkdayStartAndStop();
        float workingHoursPrDay = wCalendar.getWorkdayStartAndStop();

        //verify from 08-16 o'clock is '8' hours and '0' minutes with no differentials between 'expected' and 'actual'
        assertEquals(8.0f, workingHoursPrDay, 0.0);

    }

    @Test
    public void testIfGetWorkdayIncrementExcludesWeekends() {
        //set to 8 hours workday by default
        setWorkdayStartAndStop();

        //Verify 8 hours workday registered with no differentials between expected and acutal
        assertEquals(8, wCalendar.getWorkdayStartAndStop(), 0.0);

        //Create from a Wednesday
        LocalDateTime startDate = LocalDateTime.of(2020, 1, 23, 8, 0);

        //days to increment 2.75days = 2 * 8 workingHours + 6 hours on the third day (75% of a full workday)
        float incrementDays =  3.75f;

        //should skip weekends and end on the sixth workingday (wednesday week after)
        LocalDateTime endDateSkipedWeekends = wCalendar.getWorkdayIncrement( startDate, incrementDays );

        assertEquals("28-01-2020 14:00",  formatter.format( endDateSkipedWeekends ) );
    }


    @Test
    public void testIfGetWorkdayIncrementCalculatesNegativeValuesOfDays(){
        //set to 8 hours workday by default
        setWorkdayStartAndStop();

        //Verify 8 hours workday registered with no differentials between expected and acutal
        assertEquals(8, wCalendar.getWorkdayStartAndStop(), 0.0);

        //Create from a Wednesday
        LocalDateTime startDate = LocalDateTime.of(2020, 1, 23, 8, 0);

        //days to increment 2.75days = 2 * 8 workingHours + 6 hours on the third day (75% of a full workday)
        float incrementDays = -3.75f;

        LocalDateTime endDateSkipedWeekends = wCalendar.getWorkdayIncrement( startDate, incrementDays );

        //Verify the date
        assertEquals("24-01-2020 02:00", formatter.format(endDateSkipedWeekends) );

    }

    @Test
    public void testIfGetWorkdayIncrementExcludesHolidays() {

        //sets xmas 25th 2020 (on a friday), easterMonday 3rd april, and easterSunday 12th april
        setDefaultHolidays();

        //set to 8 hours workday by default
        setWorkdayStartAndStop();

        //Verify 8 hours workday registered with no differentials between expected and acutal
        assertEquals(8, wCalendar.getWorkdayStartAndStop(), 0.0);

        //Create from 23/12/2020 (Wednesday)
        LocalDateTime startDate = LocalDateTime.of(2020, 12, 23, 8, 0);

        //days to increment 2.75days = 2 * 8 workingHours + 6 hours on the third day (75% of a full workday)
        float incrementDays =  2.75f;

        //should skip christmasday (friday)and weekends and end on the weekday after)
        LocalDateTime endDateSkipedWeekends = wCalendar.getWorkdayIncrement( startDate, incrementDays );

        //Verify the date
        assertEquals("28-12-2020 14:00", formatter.format(endDateSkipedWeekends));
    }

    @Test
    public void testGetWorkdayIncrementWithLargeNumberOfDays() {
        //set to 8 hours workday by default
        setWorkdayStartAndStop();

        //Verify 8 hours workday registered with no differentials between expected and acutal
        assertEquals(8, wCalendar.getWorkdayStartAndStop(), 0.0);

        //Create from a Wednesday
        LocalDateTime startDate = LocalDateTime.of(2020, 1, 23, 8, 0);

        //days to increment
        float incrementDays =  3333.7545f;

        LocalDateTime dateSkippedWeekends = wCalendar.getWorkdayIncrement( startDate, incrementDays );

        assertEquals("02-11-2032 14:02", formatter.format( dateSkippedWeekends ) );
    }


}
