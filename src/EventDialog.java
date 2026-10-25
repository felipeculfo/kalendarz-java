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
    private LocalDateTime eventDateTime;

    public EventDialog(Window owner, String dialogTitle, int year, int month, int day, CalendarEvent existingEvent) {
        super(owner, dialogTitle, ModalityType.APPLICATION_MODAL);
        setSize(380, 220);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField titleField = new JTextField(existingEvent != null ? existingEvent.getTitle() : "");

        // 1. Osobny JSpinner dla DATY
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);

        // 2. Osobny JSpinner dla GODZINY
        SpinnerDateModel timeModel = new SpinnerDateModel();
        JSpinner timeSpinner = new JSpinner(timeModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);

        // Ustawienie wartości początkowych
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

        panel.add(new JLabel("Nazwa wydarzenia:"));
        panel.add(titleField);
        panel.add(new JLabel("Data (YYYY-MM-DD):"));
        panel.add(dateSpinner);
        panel.add(new JLabel("Godzina (HH:MM):"));
        panel.add(timeSpinner);

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
            this.eventDateTime = LocalDateTime.of(localDate, localTime);
            this.confirmed = true;
            dispose();
        });

        cancelButton.addActionListener(e -> dispose());

        btnPanel.add(okButton);
        btnPanel.add(cancelButton);
        add(btnPanel, BorderLayout.SOUTH);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public LocalDateTime getEventDateTime() {
        return eventDateTime;
    }
}