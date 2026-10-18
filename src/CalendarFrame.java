import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

public class CalendarFrame extends JFrame implements ClockTicker.TimeListener {

    private final JLabel clockLabel;
    private final CalendarPanel calendarPanel;
    private final ClockTicker ticker;

    private final JTextField yearField;
    private final JComboBox<String> monthComboBox;
    private final JButton prevMonthButton;
    private final JButton nextMonthButton;
    private final JButton todayButton;

    private final Timer typingCooldownTimer;
    private static final int COOLDOWN_MS = 1000;

    private TrayIcon trayIcon;
    private SystemTray systemTray;

    private final EventManager eventManager = new EventManager();

    public CalendarFrame() {
        setTitle("Kalendarz i Zegar Czasu Rzeczywistego");
        setSize(480, 540);
        setResizable(false);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                minimizeToTray();
            }
        });

        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Inicjalizacja górnego paska menu
        initMenuBar();

        // Inicjalizacja zasobnika systemowego
        initSystemTray();

        typingCooldownTimer = new Timer(COOLDOWN_MS, e -> handleDateChange());
        typingCooldownTimer.setRepeats(false);

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));

        clockLabel = new JLabel("Synchronizacja czasu...", SwingConstants.CENTER);
        clockLabel.setFont(new Font("Arial", Font.BOLD, 16));
        clockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        clockLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        topContainer.add(clockLabel);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

        prevMonthButton = new JButton("<");
        prevMonthButton.addActionListener(e -> navigateMonth(-1));
        controlPanel.add(prevMonthButton);

        controlPanel.add(new JLabel("Miesiąc:"));

        String[] monthNames = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        monthComboBox = new JComboBox<>(monthNames);
        monthComboBox.addActionListener(e -> handleDateChange());
        controlPanel.add(monthComboBox);

        controlPanel.add(new JLabel("Rok:"));
        yearField = new JTextField(4);

        ((AbstractDocument) yearField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) return;
                if (isNumeric(string) && (fb.getDocument().getLength() + string.length()) <= 4) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) return;
                int newLength = fb.getDocument().getLength() - length + text.length();
                if (isNumeric(text) && newLength <= 4) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }

            private boolean isNumeric(String text) {
                return text.chars().allMatch(Character::isDigit);
            }
        });

        yearField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { restartCooldown(); }
            @Override
            public void removeUpdate(DocumentEvent e) { restartCooldown(); }
            @Override
            public void changedUpdate(DocumentEvent e) { restartCooldown(); }

            private void restartCooldown() { typingCooldownTimer.restart(); }
        });

        controlPanel.add(yearField);

        nextMonthButton = new JButton(">");
        nextMonthButton.addActionListener(e -> navigateMonth(1));
        controlPanel.add(nextMonthButton);

        todayButton = new JButton("Dzisiaj");
        todayButton.addActionListener(e -> loadCurrentSystemDate());
        controlPanel.add(todayButton);

        topContainer.add(controlPanel);
        add(topContainer, BorderLayout.NORTH);

        // Obsługa kliknięcia w kafelek dnia -> Otwiera dedykowany widok dnia
        calendarPanel = new CalendarPanel();
        calendarPanel.setOnDateClickListener((year, day) -> {
            int month = monthComboBox.getSelectedIndex() + 1;
            DayEventsDialog dayDialog = new DayEventsDialog(this, eventManager, year, month, day);
            dayDialog.setVisible(true);
        });
        add(calendarPanel, BorderLayout.CENTER);

        loadCurrentSystemDate();
        ticker = new ClockTicker(this);
        ticker.start();
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu eventMenu = new JMenu("Wydarzenia");

        JMenuItem addItem = new JMenuItem("Dodaj event");
        addItem.addActionListener(e -> {
            int year = Integer.parseInt(yearField.getText());
            int month = monthComboBox.getSelectedIndex() + 1;
            openAddEventDialog(year, month, ZonedDateTime.now().getDayOfMonth(), -1, null);
        });
        eventMenu.add(addItem);

        JMenuItem editItem = new JMenuItem("Edytuj event");
        editItem.addActionListener(e -> {
            if (eventManager.getEvents().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Brak wydarzeń do edycji.", "Informacja", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            EventSelectionDialog editDialog = new EventSelectionDialog(this, eventManager, "Wybierz event do edycji");
            editDialog.setVisible(true);
            int editIndex = editDialog.getSelectedIndex();
            if (editIndex != -1) {
                CalendarEvent eventToEdit = eventManager.getEvents().get(editIndex);
                openAddEventDialog(
                        eventToEdit.getEventDateTime().getYear(),
                        eventToEdit.getEventDateTime().getMonthValue(),
                        eventToEdit.getEventDateTime().getDayOfMonth(),
                        editIndex,
                        eventToEdit
                );
            }
        });
        eventMenu.add(editItem);

        JMenuItem deleteItem = new JMenuItem("Usuń event");
        deleteItem.addActionListener(e -> {
            if (eventManager.getEvents().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Brak wydarzeń do usunięcia.", "Informacja", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            EventSelectionDialog delDialog = new EventSelectionDialog(this, eventManager, "Wybierz event do usunięcia");
            delDialog.setVisible(true);
            int delIndex = delDialog.getSelectedIndex();
            if (delIndex != -1) {
                eventManager.removeEvent(delIndex);
                JOptionPane.showMessageDialog(this, "Wydarzenie zostało usunięte.", "Sukces", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        eventMenu.add(deleteItem);

        menuBar.add(eventMenu);
        setJMenuBar(menuBar);
    }

    private void initSystemTray() {
        if (!SystemTray.isSupported()) return;

        systemTray = SystemTray.getSystemTray();
        Image image = createImageForTray();

        PopupMenu popup = new PopupMenu();

        MenuItem openItem = new MenuItem("Pokaż kalendarz");
        openItem.addActionListener(e -> SwingUtilities.invokeLater(this::restoreFromTray));
        popup.add(openItem);

        popup.addSeparator();

        MenuItem addEventItem = new MenuItem("Dodaj wydarzenie");
        addEventItem.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            openAddEventDialog(Integer.parseInt(yearField.getText()), monthComboBox.getSelectedIndex() + 1, 1, -1, null);
        }));
        popup.add(addEventItem);

        MenuItem editEventItem = new MenuItem("Edytuj wydarzenie...");
        editEventItem.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            if (eventManager.getEvents().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Brak wydarzeń do edycji.", "Informacja", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            EventSelectionDialog editDialog = new EventSelectionDialog(null, eventManager, "Wybierz event do edycji");
            editDialog.setVisible(true);
            int editIndex = editDialog.getSelectedIndex();
            if (editIndex != -1) {
                CalendarEvent eventToEdit = eventManager.getEvents().get(editIndex);
                openAddEventDialog(
                        eventToEdit.getEventDateTime().getYear(),
                        eventToEdit.getEventDateTime().getMonthValue(),
                        eventToEdit.getEventDateTime().getDayOfMonth(),
                        editIndex,
                        eventToEdit
                );
            }
        }));
        popup.add(editEventItem);

        MenuItem deleteEventItem = new MenuItem("Usuń wydarzenie...");
        deleteEventItem.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            if (eventManager.getEvents().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Brak wydarzeń do usunięcia.", "Informacja", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            EventSelectionDialog delDialog = new EventSelectionDialog(null, eventManager, "Wybierz event do usunięcia");
            delDialog.setVisible(true);
            int delIndex = delDialog.getSelectedIndex();
            if (delIndex != -1) {
                eventManager.removeEvent(delIndex);
                JOptionPane.showMessageDialog(null, "Wydarzenie zostało usunięte.", "Sukces", JOptionPane.INFORMATION_MESSAGE);
            }
        }));
        popup.add(deleteEventItem);

        popup.addSeparator();

        MenuItem exitItem = new MenuItem("Zamknij");
        exitItem.addActionListener(e -> {
            if (systemTray != null && trayIcon != null) systemTray.remove(trayIcon);
            System.exit(0);
        });
        popup.add(exitItem);

        trayIcon = new TrayIcon(image, "Kalendarz i Zegar", popup);
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(e -> SwingUtilities.invokeLater(this::restoreFromTray));

        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private void openAddEventDialog(int year, int month, int day, int editIndex, CalendarEvent existingEvent) {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));

        JTextField titleField = new JTextField(existingEvent != null ? existingEvent.getTitle() : "");

        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner dateTimeSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateTimeSpinner, "yyyy-MM-dd HH:mm");
        dateTimeSpinner.setEditor(editor);

        if (existingEvent != null) {
            java.util.Date date = java.util.Date.from(existingEvent.getEventDateTime().atZone(java.time.ZoneId.systemDefault()).toInstant());
            dateTimeSpinner.setValue(date);
        } else {
            LocalDateTime initialDT = LocalDateTime.of(year, month, day, 12, 0);
            java.util.Date date = java.util.Date.from(initialDT.atZone(java.time.ZoneId.systemDefault()).toInstant());
            dateTimeSpinner.setValue(date);
        }

        panel.add(new JLabel("Nazwa wydarzenia:"));
        panel.add(titleField);
        panel.add(new JLabel("Data i godzina:"));
        panel.add(dateTimeSpinner);

        String dialogTitle = (existingEvent == null) ? "Nowe wydarzenie" : "Edycja wydarzenia";
        int result = JOptionPane.showConfirmDialog(this, panel, dialogTitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String title = titleField.getText().trim();
                if (title.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Nazwa wydarzenia nie może być pusta.", "Błąd", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                java.util.Date selectedDate = (java.util.Date) dateTimeSpinner.getValue();
                LocalDateTime eventTime = selectedDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();

                if (editIndex == -1) {
                    eventManager.addEvent(title, eventTime);
                    JOptionPane.showMessageDialog(this, "Wydarzenie zostało dodane pomyślnie!", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    eventManager.updateEvent(editIndex, title, eventTime);
                    JOptionPane.showMessageDialog(this, "Wydarzenie zostało zaktualizowane!", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Wprowadzono niepoprawne dane.", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void minimizeToTray() { setVisible(false); }
    private void restoreFromTray() { setVisible(true); setExtendedState(JFrame.NORMAL); toFront(); }

    private Image createImageForTray() {
        int width = 16, height = 16;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(new Color(51, 102, 204));
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        g2d.drawString("C", 4, 12);
        g2d.dispose();
        return img;
    }

    @Override
    public void onTimeChanged(String timeString, ZonedDateTime currentTime) {
        SwingUtilities.invokeLater(() -> {
            clockLabel.setText("Godzina: " + timeString);

            List<String> alerts = eventManager.checkReminders(currentTime.toLocalDateTime());
            for (String alert : alerts) {
                if (trayIcon != null) {
                    trayIcon.displayMessage("Przypomnienie o wydarzeniu", alert, TrayIcon.MessageType.INFO);
                }
                // Wyświetlanie okna powiadomienia, które NIE kradnie fokusu systemu (nie przerywa gry)
                createNonFocusedAlertWindow(alert);
            }
        });
    }

    private void createNonFocusedAlertWindow(String message) {
        // Używamy JWindow zamiast JDialog – nie tworzy standardowej ramki systemowej Windows,
        // dzięki czemu nie wchodzi w konflikt z priorytetem okna w grach pełnoekranowych.
        JWindow alertWindow = new JWindow();
        alertWindow.setAlwaysOnTop(true);
        alertWindow.setSize(380, 140);
        alertWindow.setLocationRelativeTo(null); // Wyśrodkowanie na ekranie

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("Przypomnienie o wydarzeniu", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(new Color(50, 50, 50));
        panel.add(titleLabel, BorderLayout.NORTH);

        JLabel msgLabel = new JLabel("<html><center>" + message.replace("\n", "<br>") + "</center></html>", SwingConstants.CENTER);
        msgLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(msgLabel, BorderLayout.CENTER);

        JButton okButton = new JButton("OK (Zamknij)");
        okButton.setFont(new Font("Arial", Font.BOLD, 12));
        // Użycie MouseListener zamiast standardowego ActionListener gwarantuje
        // natychmiastową reakcję na pierwsze kliknięcie, bez żadnych opóźnień fokusowych.
        okButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                alertWindow.dispose();
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(okButton);
        panel.add(btnPanel, BorderLayout.SOUTH);

        alertWindow.add(panel);
        alertWindow.setVisible(true);
    }

    private void loadCurrentSystemDate() {
        typingCooldownTimer.stop();
        ZonedDateTime now = ZonedDateTime.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        ActionListener[] listeners = monthComboBox.getActionListeners();
        for (ActionListener l : listeners) monthComboBox.removeActionListener(l);

        yearField.setText(String.valueOf(currentYear));
        monthComboBox.setSelectedIndex(currentMonth - 1);

        for (ActionListener l : listeners) monthComboBox.addActionListener(l);

        calendarPanel.updateCalendar(currentYear, currentMonth);
    }

    private void navigateMonth(int delta) {
        typingCooldownTimer.stop();
        try {
            String textYear = yearField.getText().trim();
            if (textYear.isEmpty()) return;

            int year = Integer.parseInt(textYear);
            int monthIndex = monthComboBox.getSelectedIndex() + delta;

            if (monthIndex < 0) { monthIndex = 11; year--; }
            else if (monthIndex > 11) { monthIndex = 0; year++; }

            if (year >= 1 && year <= 9999) {
                yearField.setText(String.valueOf(year));
                monthComboBox.setSelectedIndex(monthIndex);
                calendarPanel.updateCalendar(year, monthIndex + 1);
            }
        } catch (NumberFormatException ignored) {}
    }

    private void handleDateChange() {
        try {
            String textYear = yearField.getText().trim();
            if (textYear.length() < 1) return;

            int year = Integer.parseInt(textYear);
            int monthNum = monthComboBox.getSelectedIndex() + 1;

            if (year >= 1 && year <= 9999) {
                calendarPanel.updateCalendar(year, monthNum);
            }
        } catch (NumberFormatException ignored) {}
    }
}