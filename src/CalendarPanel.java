import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class CalendarPanel extends JPanel {

    private final JLabel titleLabel;
    private final JPanel gridPanel;
    private static final String[] DAYS_OF_WEEK = {
            "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
    };

    public CalendarPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Nagłówek z nazwą miesiąca i roku
        titleLabel = new JLabel("", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);

        // Siatka dni (7 kolumn na dni tygodnia + dni miesiąca)
        gridPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        add(gridPanel, BorderLayout.CENTER);
    }

    public void updateCalendar(int year, int monthNum) {
        gridPanel.removeAll();

        // 1. Dodanie nagłówków dni tygodnia
        for (String dayName : DAYS_OF_WEEK) {
            JLabel dayHeader = new JLabel(dayName, SwingConstants.CENTER);
            dayHeader.setFont(new Font("Arial", Font.BOLD, 12));
            dayHeader.setForeground(Color.GRAY);
            gridPanel.add(dayHeader);
        }

        // 2. Pobranie danych z Twoich klas (Month i Months)
        Month monthObj = Months.getMonth(monthNum);
        titleLabel.setText(monthObj.getName().toUpperCase() + " " + year);

        Date firstDayOfMonth = new Date(year, monthNum, 1);
        String dayOfWeekStr = firstDayOfMonth.getDayOfWeek2();

        int startDayIndex = getDayIndex(dayOfWeekStr);
        int daysInMonth = monthObj.getNumberOfDays();
        if (monthNum == 2 && Date.isLeapYear(year)) {
            daysInMonth++;
        }

        // 3. Puste pola przed pierwszym dniem miesiąca
        for (int i = 0; i < startDayIndex; i++) {
            gridPanel.add(new JLabel(""));
        }

        // 4. Wypełnienie dni miesiąca
        LocalDate today = LocalDate.now();
        for (int day = 1; day <= daysInMonth; day++) {
            JLabel dayLabel = new JLabel(String.valueOf(day), SwingConstants.CENTER);
            dayLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            dayLabel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

            // Podświetlenie dzisiejszego dnia (jeśli to bieżący miesiąc i rok)
            if (today.getYear() == year && today.getMonthValue() == monthNum && today.getDayOfMonth() == day) {
                dayLabel.setOpaque(true);
                dayLabel.setBackground(new Color(173, 216, 230)); // jasnoniebieski
            }

            gridPanel.add(dayLabel);
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private int getDayIndex(String dayName) {
        for (int i = 0; i < DAYS_OF_WEEK.length; i++) {
            if (DAYS_OF_WEEK[i].equalsIgnoreCase(dayName) || dayName.toLowerCase().startsWith(DAYS_OF_WEEK[i].toLowerCase())) {
                return i;
            }
        }
        return 0;
    }
}