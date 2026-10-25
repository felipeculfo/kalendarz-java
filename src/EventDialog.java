import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

public class EventDialog extends JDialog {

    private boolean confirmed = false;
    private String eventTitle;
    private String eventDescription;
    private LocalDateTime eventDateTime;

    public EventDialog(Window owner, String dialogTitle, int year, int month, int day, CalendarEvent existingEvent) {
        super(owner, dialogTitle, ModalityType.APPLICATION_MODAL);
        setSize(420, 320);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleField = new JTextField(existingEvent != null ? existingEvent.getTitle() : "");
        JTextArea descArea = new JTextArea(existingEvent != null ? existingEvent.getDescription() : "", 3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);

        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner dateSpinner = new JSpinner(dateModel);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));

        SpinnerDateModel timeModel = new SpinnerDateModel();
        JSpinner timeSpinner = new JSpinner(timeModel);
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm"));

        if (existingEvent != null) {
            Date date = Date.from(existingEvent.getEventDateTime().atZone(ZoneId.systemDefault()).toInstant());
            dateSpinner.setValue(date);
            timeSpinner.setValue(date);
        } else {
            LocalDateTime initialDT = LocalDateTime.of(year, month, day, 12, 0);
            Date date = Date.from(initialDT.atZone(ZoneId.systemDefault()).toInstant());
            dateSpinner.setValue(date);
            timeSpinner.setValue(date);
        }

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Nazwa:"), gbc);
        gbc.gridx = 1;
        panel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Data (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        panel.add(dateSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Godzina (HH:MM):"), gbc);
        gbc.gridx = 1;
        panel.add(timeSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Notatka:"), gbc);
        gbc.gridx = 1;
        panel.add(descScroll, gbc);

        add(panel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton okButton = new JButton("Zapisz");
        JButton cancelButton = new JButton("Anuluj");

        okButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nazwa wydarzenia nie może być pusta.", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Date selectedDatePart = (Date) dateSpinner.getValue();
            Date selectedTimePart = (Date) timeSpinner.getValue();

            LocalDate localDate = selectedDatePart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalTime localTime = selectedTimePart.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

            this.eventTitle = title;
            this.eventDescription = descArea.getText().trim();
            this.eventDateTime = LocalDateTime.of(localDate, localTime);
            this.confirmed = true;
            dispose();
        });

        cancelButton.addActionListener(e -> dispose());

        btnPanel.add(okButton);
        btnPanel.add(cancelButton);
        add(btnPanel, BorderLayout.SOUTH);
    }

    public boolean isConfirmed() { return confirmed; }
    public String getEventTitle() { return eventTitle; }
    public String getEventDescription() { return eventDescription; }
    public LocalDateTime getEventDateTime() { return eventDateTime; }
}