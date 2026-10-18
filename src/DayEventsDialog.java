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

        setSize(450, 320);
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

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton addButton = new JButton("Dodaj wydarzenie");
        addButton.addActionListener(e -> openAddDialogForThisDay());

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

    private void openAddDialogForThisDay() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField titleField = new JTextField();

        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner dateTimeSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateTimeSpinner, "yyyy-MM-dd HH:mm");
        dateTimeSpinner.setEditor(editor);

        LocalDateTime initialDT = LocalDateTime.of(year, month, day, 12, 0);
        java.util.Date date = java.util.Date.from(initialDT.atZone(java.time.ZoneId.systemDefault()).toInstant());
        dateTimeSpinner.setValue(date);

        panel.add(new JLabel("Nazwa wydarzenia:"));
        panel.add(titleField);
        panel.add(new JLabel("Data i godzina:"));
        panel.add(dateTimeSpinner);

        int result = JOptionPane.showConfirmDialog(this, panel, "Nowe wydarzenie (" + year + "-" + month + "-" + day + ")", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String title = titleField.getText().trim();
                if (title.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Nazwa wydarzenia nie może być pusta.", "Błąd", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                java.util.Date selectedDate = (java.util.Date) dateTimeSpinner.getValue();
                LocalDateTime eventTime = selectedDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();

                eventManager.addEvent(title, eventTime);
                refreshEventList();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Wprowadzono niepoprawne dane.", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}