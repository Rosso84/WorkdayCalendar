import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;


public class WorkdayCalendar {

    private final Set<MonthDay> recurringHolidays = new HashSet<>();
    private final Set<LocalDate> holidays = new HashSet<>();
    private float workdayStartAndStop;


    /*@Sets a holiday with yyy/MM/dd/*/
    public void setHoliday(final LocalDate date) {
        holidays.add(date);
    }

    /*@Sets a recurring holiday with MM/dd and disregards the year*/
    public void setRecurringHoliday(MonthDay monthDay) {
        this.recurringHolidays.add(monthDay);
    }

    /*@Sets amount of hours to workday pr day*/
    public void setWorkdayStartAndStop(LocalDateTime start, LocalDateTime stop) {
        long wholeDays = ChronoUnit.DAYS.between(start, start);
        start = start.plusDays(wholeDays);
        Duration workDay = Duration.between(start, stop);
        this.workdayStartAndStop = (float) workDay.toMinutes() / (float) Duration.ofHours(1).toMinutes();
    }

    /*@returns when a Date ends from a starting Date*/
    public LocalDateTime getWorkdayIncrement(LocalDateTime startDate, float incrementInWorkdays) {
        Holidays holidays = new Holidays();
        CalendarController cc = new CalendarController();
        holidays.setHolidayIfIsSetToRecurring();

        int days = (int) Math.abs(incrementInWorkdays);
        float remaining = incrementInWorkdays - days;
        float fHours = remaining * 8f;
        int hours = (int) fHours;

        remaining = fHours - hours;
        float fMinutes = remaining * 60f;
        int minutes = (int) fMinutes;

        LocalDateTime mDateTime = null;

        for (int i = 0; i <= days; i++) {
            mDateTime = startDate.plusDays(i).plusHours(hours).plusMinutes(minutes);

            LocalDate toLocalDate = mDateTime.toLocalDate();

            //if the incremented day is a holiday, skip to next day
            if (cc.isHoliday(toLocalDate)) {
                days += 1;
            }
        }
        return mDateTime;
    }

    /*@returns registered working hours pr day*/
    public float getWorkdayStartAndStop() {
        return this.workdayStartAndStop;
    }


    class CalendarController {
        /*@Checks if given date is a weekend*/
        public boolean isWeekend(final LocalDate localDate) {
            final DayOfWeek dow = localDate.getDayOfWeek();
            return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
        }

        /*@Checks if given date is a weekend or a registered holiday*/
        public boolean isHoliday(final LocalDate localDate) {
            return isWeekend(localDate) || holidays.contains(localDate);
        }

    }


    class Holidays {
        /*@removes a registered holiday*/
        public void removeHoliday(LocalDate date) {
            holidays.remove(date);
        }

        /*@removes a registered recurring holiday*/
        public void removeRecurringHoliday(MonthDay date) {
            recurringHolidays.remove(date);
        }

        /*@returns registered holidays*/
        public Set<LocalDate> getHolidays() {
            return holidays;
        }

        /*@returns registered recurring holidays*/
        public Set<MonthDay> getRecurringHolidays() {
            return recurringHolidays;
        }

        /*@checks if there are any holidays to set for this year from recurringholidays*/
        public void setHolidayIfIsSetToRecurring() {
            Holidays holidays = new Holidays();
            try {
                for (MonthDay holidayMonthDay : holidays.getRecurringHolidays()) {
                    Year thisYear = Year.now();
                    LocalDate lDate = holidayMonthDay.atYear(thisYear.getValue());
                    setHoliday(lDate);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        WorkdayCalendar workdayCalendar = new WorkdayCalendar();

        workdayCalendar.setWorkdayStartAndStop(
                LocalDateTime.of(2020, 1, 1, 8, 0),
                LocalDateTime.of(2020, 1, 1, 16, 0));

        workdayCalendar.setRecurringHoliday(
                MonthDay.of(5, 17));

        workdayCalendar.setHoliday(LocalDate.of(2020, 5, 27));

        LocalDateTime start = LocalDateTime.of(2020, 5, 24, 8, 5);

        String datePattern = "dd-MM-yyyy HH:mm";
        DateTimeFormatter europeanDateFormatter = DateTimeFormatter.ofPattern(datePattern);

        float increment = 1.5f;

        System.out.println(
                europeanDateFormatter.format(start) +
                        " with the addition of " +
                        increment +
                        " working days is " +
                        europeanDateFormatter.format(workdayCalendar.getWorkdayIncrement(start, increment)));

    }
}
