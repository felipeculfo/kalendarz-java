import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DayEventsDialog extends JDialog {
    private final EventManager eventManager;
    private final int year;
    private final int month;
    private final int day;
    private DefaultTableModel tableModel;
    private JTable eventTable;
    private List<CalendarEvent> currentDayEvents = new ArrayList<>();

    public DayEventsDialog(JFrame parent, EventManager eventManager, int year, int month, int day) {
        super(parent, "Wydarzenia z dnia: " + year + "-" + String.format("%02d", month) + "-" + String.format("%02d", day), true);
        this.eventManager = eventManager;
        this.year = year;
        this.month = month;
        this.day = day;

        setSize(550, 350);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // Definicja kolumn tabeli
        String[] columnNames = {"Godzina", "Nazwa wydarzenia", "Notatka"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Wyłączenie edycji wewnątrz komórek
            }
        };

        eventTable = new JTable(tableModel);
        eventTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        eventTable.setFont(new Font("Arial", Font.PLAIN, 12));
        eventTable.setRowHeight(22);

        // Dostosowanie szerokości kolumn
        eventTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        eventTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        eventTable.getColumnModel().getColumn(2).setPreferredWidth(250);

        refreshEventList();

        JScrollPane scrollPane = new JScrollPane(eventTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));

        JButton addButton = new JButton("Dodaj");
        addButton.addActionListener(e -> openAddOrEditDialog(null));

        JButton editButton = new JButton("Edytuj zaznaczone");
        editButton.addActionListener(e -> {
            int selectedRow = eventTable.getSelectedRow();
            if (selectedRow != -1 && selectedRow < currentDayEvents.size()) {
                openAddOrEditDialog(currentDayEvents.get(selectedRow));
            } else {
                JOptionPane.showMessageDialog(this, "Wybierz wydarzenie z tabeli.", "Błąd", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton deleteButton = new JButton("Usuń zaznaczone");
        deleteButton.addActionListener(e -> {
            int selectedRow = eventTable.getSelectedRow();
            if (selectedRow != -1 && selectedRow < currentDayEvents.size()) {
                CalendarEvent eventToRemove = currentDayEvents.get(selectedRow);
                int actualIndex = eventManager.getEvents().indexOf(eventToRemove);
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
        tableModel.setRowCount(0);

        currentDayEvents = eventManager.getEvents().stream()
                .filter(ev -> {
                    LocalDate d = ev.getEventDateTime().toLocalDate();
                    return d.getYear() == year && d.getMonthValue() == month && d.getDayOfMonth() == day;
                })
                .collect(Collectors.toList());

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (CalendarEvent ev : currentDayEvents) {
            Object[] rowData = {
                    ev.getEventDateTime().format(timeFormatter),
                    ev.getTitle(),
                    ev.getDescription()
            };
            tableModel.addRow(rowData);
        }
    }

    private void openAddOrEditDialog(CalendarEvent existingEvent) {
        String dialogTitle = (existingEvent == null) ? "Nowe wydarzenie" : "Edycja wydarzenia";

        EventDialog dialog = new EventDialog(this, dialogTitle, year, month, day, existingEvent);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            if (existingEvent == null) {
                eventManager.addEvent(dialog.getEventTitle(), dialog.getEventDescription(), dialog.getEventDateTime());
            } else {
                int actualIndex = eventManager.getEvents().indexOf(existingEvent);
                if (actualIndex != -1) {
                    eventManager.updateEvent(actualIndex, dialog.getEventTitle(), dialog.getEventDescription(), dialog.getEventDateTime());
                }
            }
            refreshEventList();
        }
    }
}