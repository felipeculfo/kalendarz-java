import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GlobalEventsDialog extends JDialog {
    private final EventManager eventManager;
    private DefaultTableModel tableModel;
    private JTable eventTable;

    public GlobalEventsDialog(JFrame parent, EventManager eventManager) {
        super(parent, "Wszystkie wydarzenia (Widok globalny)", true);
        this.eventManager = eventManager;

        setSize(650, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // Tabela prezentująca wszystkie wydarzenia z kalendarza
        String[] columnNames = {"Data", "Godzina", "Nazwa wydarzenia", "Notatka"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Blokada edycji bezpośrednio w komórkach
            }
        };

        eventTable = new JTable(tableModel);
        eventTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        eventTable.setRowHeight(22);

        eventTable.getColumnModel().getColumn(0).setPreferredWidth(90);  // Data
        eventTable.getColumnModel().getColumn(1).setPreferredWidth(60);  // Godzina
        eventTable.getColumnModel().getColumn(2).setPreferredWidth(180); // Nazwa
        eventTable.getColumnModel().getColumn(3).setPreferredWidth(220); // Notatka

        refreshTable();

        JScrollPane scrollPane = new JScrollPane(eventTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(scrollPane, BorderLayout.CENTER);

        // Panel przycisków zarządzania globalnego
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton editButton = new JButton("Edytuj zaznaczone");
        editButton.addActionListener(e -> executeEdit());

        JButton deleteButton = new JButton("Usuń zaznaczone");
        deleteButton.addActionListener(e -> executeDelete());

        JButton closeButton = new JButton("Zamknij");
        closeButton.addActionListener(e -> dispose());

        bottomPanel.add(editButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(closeButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<CalendarEvent> events = eventManager.getEvents();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (CalendarEvent ev : events) {
            Object[] row = {
                    ev.getEventDateTime().format(dateFormatter),
                    ev.getEventDateTime().format(timeFormatter),
                    ev.getTitle(),
                    ev.getDescription()
            };
            tableModel.addRow(row);
        }
    }

    private void executeEdit() {
        int selectedRow = eventTable.getSelectedRow();
        List<CalendarEvent> events = eventManager.getEvents();

        if (selectedRow != -1 && selectedRow < events.size()) {
            CalendarEvent selectedEvent = events.get(selectedRow);
            int year = selectedEvent.getEventDateTime().getYear();
            int month = selectedEvent.getEventDateTime().getMonthValue();
            int day = selectedEvent.getEventDateTime().getDayOfMonth();

            // Używamy uniwersalnego EventDialog do edycji
            EventDialog dialog = new EventDialog(this, "Edycja wydarzenia", year, month, day, selectedEvent);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                eventManager.updateEvent(selectedRow, dialog.getEventTitle(), dialog.getEventDescription(), dialog.getEventDateTime());
                refreshTable();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Wybierz wydarzenie z tabeli do edycji.", "Ostrzeżenie", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void executeDelete() {
        int selectedRow = eventTable.getSelectedRow();
        List<CalendarEvent> events = eventManager.getEvents();

        if (selectedRow != -1 && selectedRow < events.size()) {
            eventManager.removeEvent(selectedRow);
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Wybierz wydarzenie do usunięcia.", "Ostrzeżenie", JOptionPane.WARNING_MESSAGE);
        }
    }
}