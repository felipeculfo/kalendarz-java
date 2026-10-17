public class DaysInWeek {

    /**
     * Contains the names for the days in a week and a way to assign them to a given number
     *
     * <p>It also contains a reference date object for a Monday.</p>
     *
     */
    public static Date refMonday = new Date(2026, 04, 20);

    public static String[] names = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };

    /**
     * Assigns a name of the week to a number representing number of days (0-6) after the reference date.
     *
     * @param n integer 0-6
     * @return string representing the day in a week
     */
    public static String getName(int n) {
        return names[n];
    }

}
