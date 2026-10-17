import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
* Represents a calendar date.
*
* <p> The class provides a date validation and date comparison.
* Shifting a date a week forward and backward is possible, as is
* calculation of a day in a week. </p>
*
* <p> The class provides multiple string formats. </p>
*/

public class Date implements Comparable<Date> {
    private int year;
    private Month month;
    private int day;
    boolean leapYear;

    /**
     * Creates new Date object and passes given values to the initializer function.
     *
     * @param y value for a year
     * @param m number of a month in range 1-12
     * @param d number of a day (valid for a given month)
     */
    public Date(int y, int m, int d) {
        initialize(y, m, d);
    }

    /**
     * Creates new Date object, converts given string values to ints and posses them to the initializer function.
     *
     * @param y value for a year (as string)
     * @param m number of a month in range 1-12 (as string)
     * @param d number of a day (valid for a given month) (as string)
     */
    public Date(String y, String m, String d) {
        initialize(
                Integer.parseInt(y),
                Integer.parseInt(m),
                Integer.parseInt(d)
        );
    }

    /**
    * Initializes values in a newly created date object.
    *
    * @param y value for a year
    * @param m number of a month in range 1-12
    * @param d number of a day (valid for a given month)
    *
    * @throws RuntimeException if the provided day is invalid
    *                          for the given month
    */
    private void initialize(int y, int m, int d) {
        year = y;
        month = Months.getMonth(m);

        if ((year % 400 == 0) || ((year % 4 == 0) && (year % 100 != 0))) {
            leapYear = true;
        }

        int numberOfDays = month.getNumberOfDays();
        if (getMonth() == 2 && leapYear) {
            numberOfDays++;
        }

        if (d > 0 && d <= numberOfDays) {
            day = d;
        } else {
            throw new RuntimeException("Invalid day " + day + " for month " + month.getName());
        }
    }


    /**
     * Creates a copy of another date object.
     *
     * @param d date object to copy
     */
    public Date(Date d) {
        this(d.getYear(), d.getMonth(), d.getDay());
    }


    /**
     * Creates a new Date object from a "DD-MM-YYYY" string.
     * Divides a "DD-MM-YYYY" string to three string values and passes them to a constructor.
     *
     * @param text string in "DD-MM-YYYY format"
     * @return Date object
     */
    public static Date parse(String text) {
        String[] parts = text.split("-");

        if (parts.length != 3) {
            throw new RuntimeException("Invalid date format: " + text);
        }

        return new Date(parts[2], parts[1], parts[0]);
    }

    private int getDay() {
        return day;
    }

    private String getDayString() {
        if (day < 10) {
            return "0" + day;
        } else {
            return String.valueOf(day);
        }
    }

    private int getMonth() {
        return Integer.parseInt(month.getNumber());
    }
    private int getYear() {
        return year;
    }

    /**
     * Checks if the year is a leap year and updates the leapYear parameter accordingly.
     */
    private void checkLeapYear() {
        leapYear = (year % 400 == 0) || ((year % 4 == 0) && (year % 100 != 0));
    }

    public static boolean isLeapYear(int year) {
        return ((year % 400 == 0) || ((year % 4 == 0) && (year % 100 != 0)));
    }

    /**
     * Returns the string representation of this date in yyyy-mm-dd format.
     *
     * @return date string
     */
    @Override
    public String toString() {
        if (day < 10) {
            return year + "-" + month.getNumber() + "-" + "0"+day;
        }
        return year + "-" + month.getNumber() + "-" + day;
    }

    /**
     * Moves this date forward by one week.
     *
     * <p>Method handles crossing months and years </p>.
     */
    public void weekForward() {
        int daysInMonth = month.getNumberOfDays();
        if (getMonth() == 2 && leapYear) {
            daysInMonth++;
        }

        day += 7;
        if (day > daysInMonth) {
            day -= daysInMonth;

            if (this.getMonth() < 12) {
                month = Months.getMonth(this.getMonth() + 1);
            } else {
                year += 1;
                checkLeapYear();
                month = Months.getMonth(1);
            }
        }
    }

    /**
     * Moves this date backward by one week.
     *
     * <p>Method handles crossing months and years </p>.
     */
    public void weekBackward() {
        day -= 7;
        if (day <= 0) {
            if (getMonth() > 1) {
                month = Months.getMonth(this.getMonth() - 1);

                int daysInMonth = month.getNumberOfDays();
                if (getMonth() == 2 && leapYear) {
                    daysInMonth++;
                }

                day = daysInMonth + day;

            } else {
                year--;
                checkLeapYear();
                month = Months.getMonth(12);
                day = month.getNumberOfDays() + day;
            }
        }
    }


    /**
     * Returns the name of the day of the week
     * using a reference date.
     *
     * @return name of the day in a week
     */
    public String getDayOfWeek() {

        int daysAfterMonday = 0;
        Date d_ = new Date(DaysInWeek.refMonday);
        if (compareTo(d_) > 0) {
            while (compareTo(d_) > 0) {
                d_.weekForward();
            }
            daysAfterMonday = 7 - daysTo(d_);
        } else if (compareTo(d_) < 0) {
            while (compareTo(d_) < 0) {
                d_.weekBackward();
            }
            daysAfterMonday = -daysTo(d_);
        }
        return DaysInWeek.getName(daysAfterMonday);
    }

    /**
     * Returns the name of the day of the week
     * using Zeller's congruence algorithm.
     *
     * @return name of the day in a week
     */
    public String getDayOfWeek2() {
        int y = year;
        int m;
        if (month.getNumberInt() >= 3) {
            m = month.getNumberInt();
        } else {
            m = 12 + month.getNumberInt();
            y--;
        }

        int h = (day + 13*(m +1)/5 + y%100 + (y%100)/4 + (y/100)/4 - 2*(y/100)) % 7;

        // Zabezpieczenie przed ujemną resztą i pewny indeks 0-6
        int dayIndex = (h + 5) % 7;
        if (dayIndex < 0) {
            dayIndex += 7;
        }

        return DaysInWeek.getName(dayIndex);
    }

    /**
     * Calculates the number of days to another date.
     *
     * <p>If given date is earlier, returned number will be negative.</p>
     * @param d2 another date object
     * @return difference in days between given object and this one
     */
    public int daysTo(Date d2) {
        int n1 = this.year*365 + this.month.getNumberInt() * 12 + this.day;
        int n2 = d2.year*365 + d2.month.getNumberInt() * 12 + d2.day;

        return n2 - n1;
    }


    /**
     * Gives the number of days since day 0 (day before 01-01-0001) for easier calculations
     *
     * @param d Date object
     * @return integer - number of days since the day before 01-01-0001
     */
    public static int daysNumber(Date d) {
        int days = 0;

        for (int y = 0; y < d.year; y++) {
            days += isLeapYear(y) ? 366 : 365;
        }

        for (int m = 1; m < d.month.getNumberInt(); m++) {
            Month month = Months.getMonth(m);
            days += month.getNumberOfDays();

            if (m == 2 && isLeapYear(d.year)) {
                days += 1;
            }
        }

        days += d.day;

        return days;
    }

    public static int differenceInDays(Date d1, Date d2) {
        return daysNumber(d2) - daysNumber(d1);
    }


    /**
     * Gives the difference in time between two dates.
     *
     * @param d1 first date
     * @param d2 second date
     * @param units string - "days", "weeks" or "years"
     * @return difference in time; units depend on given argument
     */
    public static int difference(Date d1, Date d2, String units) {
        switch (units) {
            case "days":
                return differenceInDays(d1, d2);
            case "weeks":
                return differenceInDays(d1, d2) / 7;
            case "years":
                return differenceInDays(d1, d2) / 365;
            default:
                throw new IllegalArgumentException("Unknown units: " + units);
        }
    }

    /**
     * Compares this date chronologically with another date.
     *
     * @param d date to compare with
     * @return 0 if equal,
     *         1 if this date is later,
     *        -1 if this date is earlier
     */
    @Override
    public int compareTo(Date d) {
        if (this.year == d.year && this.month.getNumberInt() == d.month.getNumberInt() && this.day == d.day) {
            return 0;
        }
        if (this.year > d.getYear()) {
            return 1;
        } else if (this.year == d.year & this.month.getNumberInt() > d.month.getNumberInt()) {
            return 1;
        } else if (this.year == d.year & this.month.getNumberInt() == d.month.getNumberInt() & this.day > d.day) {
            return 1;
        }
        return -1;
    }

    private static final List<String> formats = new ArrayList<>(Arrays.asList("format1", "format2", "format3"));

    /**
     * Returns a formatted string representation of this date.
     *
     * <p>Supported formats:
     * <ul>
     *     <li>"format1" - Monday, April 20, 2026</li>
     *     <li>"format2" - 20-04-2026, Monday</li>
     *     <li>"format3" - 2026-04-20 (Monday)</li>
     * </ul>
     *
     * @param format formatting style identifier
     * @return formatted date string
     *
     * @throws InvalidDateFormat if the format identifier isn't supported
     *
     */
    public String getDateString(String format) {
        if (!(formats.contains(format))) {
            throw new InvalidDateFormat();
        } else if (format.equals("format1")) {
            return getDayOfWeek2() + ", " + month.getName() + " " + day + ", " + year;
        } else if (format.equals("format2")) {
            return day + "-" + month.getNumber() + "-" + year + ", " + getDayOfWeek2();
        } else {
            return year + "-" + month.getNumber() + "-" + getDayString() + " (" + getDayOfWeek2() + ")";
        }
    }
}
