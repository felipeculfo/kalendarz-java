/**
 * Provides access to predefined month objects.
 *
 * <p>The class stores objects representing the 12 months of a year
 * and allows access to them</p>
 */

public class Months {
    private static Month[] months = {
            new Month("01", "January", 31),
            new Month("02", "February", 28),
            new Month("03", "March", 31),
            new Month("04", "April", 30),
            new Month("05", "May", 31),
            new Month("06", "June", 30),
            new Month("07", "July", 31),
            new Month("08", "August", 31),
            new Month("09", "September", 30),
            new Month("10", "October", 31),
            new Month("11", "November", 30),
            new Month("12", "December", 31)};


    /**
     * Allows access to the month corresponding to the given number.
     *
     * <p>The method decreases given number by one to account for a position in an array.</p>
     * @param n number in a year
     * @return Month object
     */
    public static Month getMonth(int n) {
        return months[n-1];
    }

}
