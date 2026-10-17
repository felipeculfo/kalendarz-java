/**
 * Represents a calendar month.
 *
 * <p>It contains its number, name and number of days. </p>
 */

public class Month {

    private String number;
    private String name;
    private int numberOfDays;

    /**
     * Defines a new month.
     *
     * @param number number in the year
     * @param name name of the month that's being created
     * @param numberOfDays number of days in the month
     */
    public Month(String number, String name, int numberOfDays) {
        this.number = number;
        this.name = name;
        this.numberOfDays = numberOfDays;
    }

    /**
     * Returns the number of the month as a string.
     *
     *  @return string representing month number
     */
    public String getNumber() {
        return number;
    }

    /**
     * Returns the number of the month as an integer.
     *
     *  @return month number in int format
     */
    public int getNumberInt() {
        return Integer.parseInt(number);
    }

    public String getName() {
        return name;
    }

    public int getNumberOfDays() {
        return numberOfDays;
    }

}
