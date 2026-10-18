import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventSelectionDialog extends JDialog {
    private int selectedIndex = -1;

    public EventSelectionDialog(JFrame parent, EventManager eventManager, String actionTitle) {
        super(parent, actionTitle, true);
        setSize(450, 320);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        List<CalendarEvent> events = eventManager.getEvents();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (CalendarEvent event : events) {
            listModel.addElement(event.getEventDateTime().format(formatter) + " - " + event.getTitle());
        }

        JList<String> eventJList = new JList<>(listModel);
        eventJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        eventJList.setFont(new Font("Arial", Font.PLAIN, 13));

        // Umieszczenie listy w JScrollPane dodaje suwak automatycznie, gdy elementów przybędzie
        JScrollPane scrollPane = new JScrollPane(eventJList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(scrollPane, BorderLayout.CENTER);

        JButton actionButton = new JButton(actionTitle);
        actionButton.addActionListener(e -> {
            selectedIndex = eventJList.getSelectedIndex();
            if (selectedIndex != -1) {
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Wybierz wydarzenie z listy.", "Błąd", JOptionPane.WARNING_MESSAGE);
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        bottomPanel.add(actionButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }
}