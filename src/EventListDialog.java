import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventListDialog extends JDialog {

    private final EventManager eventManager;
    private DefaultListModel<String> listModel;
    private JList<String> eventJList;

    public EventListDialog(JFrame parent, EventManager eventManager) {
        super(parent, "Zarządzanie wydarzeniami", true);
        this.eventManager = eventManager;

        setSize(400, 350);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // Model listy
        listModel = new DefaultListModel<>();
        refreshList();

        eventJList = new JList<>(listModel);
        eventJList.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(eventJList);
        add(scrollPane, BorderLayout.CENTER);

        // Panel przycisków na dole
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton deleteButton = new JButton("Usuń zaznaczone");
        deleteButton.addActionListener(e -> deleteSelectedEvent());
        buttonPanel.add(deleteButton);

        JButton closeButton = new JButton("Zamknij");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void refreshList() {
        listModel.clear();
        List<CalendarEvent> events = eventManager.getEvents();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (CalendarEvent event : events) {
            String itemText = event.getEventDateTime().format(formatter) + " - " + event.getTitle();
            listModel.addElement(itemText);
        }
    }

    private void deleteSelectedEvent() {
        int selectedIndex = eventJList.getSelectedIndex();
        if (selectedIndex != -1) {
            eventManager.removeEvent(selectedIndex);
            refreshList();
        } else {
            JOptionPane.showMessageDialog(this, "Wybierz wydarzenie z listy do usunięcia.", "Informacja", JOptionPane.WARNING_MESSAGE);
        }
    }
}