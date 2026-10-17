import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Main {
    public static void main(String[] args) {


        Date d1 = new Date("2026", "5", "21");
//        Date d2 = new Date(2024, 7, 29);
        Date d2 = Date.parse("29-07-2024");
        Date d3 = new Date(2026, 4, 30);



        SwingUtilities.invokeLater(() -> {
            CalendarFrame frame = new CalendarFrame();
            frame.setVisible(true);
        });
    }
}