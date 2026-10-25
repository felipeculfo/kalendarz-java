import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class DayEventsDialog extends JDialog {
    private final EventManager eventManager;
    private final int year;
    private final int month;
    private final int day;
    private DefaultListModel<CalendarEvent> listModel;
    private JList<CalendarEvent> eventJList;

    public DayEventsDialog(JFrame parent, EventManager eventManager, int year, int month, int day) {
        super(parent, "Wydarzenia z dnia: " + year + "-" + String.format("%02d", month) + "-" + String.format("%02d", day), true);
        this.eventManager = eventManager;
        this.year = year;
        this.month = month;
        this.day = day;

        setSize(520, 340);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        refreshEventList();

        eventJList = new JList<>(listModel);
        eventJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        eventJList.setFont(new Font("Arial", Font.PLAIN, 13));

        // Niestandardowy renderer wyświetlający godzinę i tytuł wydarzenia
        eventJList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof CalendarEvent) {
                    CalendarEvent ev = (CalendarEvent) value;
                    setText(ev.getEventDateTime().format(DateTimeFormatter.ofPattern("HH:mm")) + " - " + ev.getTitle());
                }
                return this;
            }
        });

        // Suwak dla listy wydarzeń z danego dnia
        JScrollPane scrollPane = new JScrollPane(eventJList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));

        JButton addButton = new JButton("Dodaj");
        addButton.addActionListener(e -> openAddOrEditDialog(null));

        // Nowy przycisk Edytuj
        JButton editButton = new JButton("Edytuj zaznaczone");
        editButton.addActionListener(e -> {
            CalendarEvent selectedEvent = eventJList.getSelectedValue();
            if (selectedEvent != null) {
                openAddOrEditDialog(selectedEvent);
            } else {
                JOptionPane.showMessageDialog(this, "Wybierz wydarzenie z listy do edycji.", "Błąd", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton deleteButton = new JButton("Usuń zaznaczone");
        deleteButton.addActionListener(e -> {
            CalendarEvent selectedEvent = eventJList.getSelectedValue();
            if (selectedEvent != null) {
                int actualIndex = eventManager.getEvents().indexOf(selectedEvent);
                if (actualIndex != -1) {
                    eventManager.removeEvent(actualIndex);
                    refreshEventList();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Wybierz wydarzenie do usunięcia.", "Błąd", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton closeButton = new JButton("Zamknij");
        closeButton.addActionListener(e -> dispose());

        bottomPanel.add(addButton);
        bottomPanel.add(editButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(closeButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void refreshEventList() {
        if (listModel == null) {
            listModel = new DefaultListModel<>();
        } else {
            listModel.clear();
        }

        List<CalendarEvent> dayEvents = eventManager.getEvents().stream()
                .filter(ev -> {
                    LocalDate d = ev.getEventDateTime().toLocalDate();
                    return d.getYear() == year && d.getMonthValue() == month && d.getDayOfMonth() == day;
                })
                .collect(Collectors.toList());

        for (CalendarEvent ev : dayEvents) {
            listModel.addElement(ev);
        }
        if (eventJList != null) {
            eventJList.repaint();
        }
    }

    private void openAddOrEditDialog(CalendarEvent existingEvent) {
        String dialogTitle = (existingEvent == null) ? "Nowe wydarzenie" : "Edycja wydarzenia";

        EventDialog dialog = new EventDialog(this, dialogTitle, year, month, day, existingEvent);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            if (existingEvent == null) {
                eventManager.addEvent(dialog.getEventTitle(), dialog.getEventDateTime());
            } else {
                int actualIndex = eventManager.getEvents().indexOf(existingEvent);
                if (actualIndex != -1) {
                    eventManager.updateEvent(actualIndex, dialog.getEventTitle(), dialog.getEventDateTime());
                }
            }
            refreshEventList();
        }
    }
}