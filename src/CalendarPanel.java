import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.function.BiConsumer;

public class CalendarPanel extends JPanel {

    private final JLabel titleLabel;
    private final JPanel gridPanel;
    private static final String[] DAYS_OF_WEEK = {
            "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
    };

    private BiConsumer<Integer, Integer> onDateClickListener;

    public CalendarPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        titleLabel = new JLabel("", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);

        gridPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        add(gridPanel, BorderLayout.CENTER);
    }

    public void setOnDateClickListener(BiConsumer<Integer, Integer> listener) {
        this.onDateClickListener = listener;
    }

    public void updateCalendar(int year, int monthNum) {
        gridPanel.removeAll();

        for (String dayName : DAYS_OF_WEEK) {
            JLabel dayHeader = new JLabel(dayName, SwingConstants.CENTER);
            dayHeader.setFont(new Font("Arial", Font.BOLD, 12));
            dayHeader.setForeground(Color.GRAY);
            gridPanel.add(dayHeader);
        }

        Month monthObj = Months.getMonth(monthNum);
        titleLabel.setText(monthObj.getName().toUpperCase() + " " + year);

        Date firstDayOfMonth = new Date(year, monthNum, 1);
        String dayOfWeekStr = firstDayOfMonth.getDayOfWeek2();

        int startDayIndex = getDayIndex(dayOfWeekStr);
        int daysInMonth = monthObj.getNumberOfDays();
        if (monthNum == 2 && Date.isLeapYear(year)) {
            daysInMonth++;
        }

        for (int i = 0; i < startDayIndex; i++) {
            gridPanel.add(new JLabel(""));
        }

        LocalDate today = LocalDate.now();
        for (int day = 1; day <= daysInMonth; day++) {
            final int selectedDay = day;
            JLabel dayLabel = new JLabel(String.valueOf(day), SwingConstants.CENTER);
            dayLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            dayLabel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
            dayLabel.setOpaque(true); // Kluczowe: cały kafelek łapie kliknięcia
            dayLabel.setBackground(Color.WHITE);
            dayLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Podświetlenie dzisiejszego dnia
            if (today.getYear() == year && today.getMonthValue() == monthNum && today.getDayOfMonth() == day) {
                dayLabel.setBackground(new Color(173, 216, 230)); // jasnoniebieski
            }

            // Używamy mousePressed zamiast mouseClicked - eliminuje opóźnienia i "puste" kliknięcia
            dayLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (onDateClickListener != null) {
                        onDateClickListener.accept(year, selectedDay);
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!(today.getYear() == year && today.getMonthValue() == monthNum && today.getDayOfMonth() == selectedDay)) {
                        dayLabel.setBackground(new Color(240, 240, 240));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!(today.getYear() == year && today.getMonthValue() == monthNum && today.getDayOfMonth() == selectedDay)) {
                        dayLabel.setBackground(Color.WHITE);
                    }
                }
            });

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