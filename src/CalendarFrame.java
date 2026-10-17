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
import java.time.ZonedDateTime;

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

    // Elementy zasobnika systemowego (System Tray)
    private TrayIcon trayIcon;
    private SystemTray systemTray;

    public CalendarFrame() {
        setTitle("Kalendarz i Zegar Czasu Rzeczywistego");
        setSize(480, 520);

        // Zamiast zamykać aplikację, kliknięcie w "X" ukrywa okno do zasobnika systemowego
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                minimizeToTray();
            }
        });

        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Inicjalizacja ikonki w zasobniku systemowym (tray)
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
        prevMonthButton.setToolTipText("Poprzedni miesiąc");
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

            private void restartCooldown() {
                typingCooldownTimer.restart();
            }
        });

        controlPanel.add(yearField);

        nextMonthButton = new JButton(">");
        nextMonthButton.setToolTipText("Następny miesiąc");
        nextMonthButton.addActionListener(e -> navigateMonth(1));
        controlPanel.add(nextMonthButton);

        todayButton = new JButton("Dzisiaj");
        todayButton.addActionListener(e -> loadCurrentSystemDate());
        controlPanel.add(todayButton);

        topContainer.add(controlPanel);
        add(topContainer, BorderLayout.NORTH);

        calendarPanel = new CalendarPanel();
        add(calendarPanel, BorderLayout.CENTER);

        loadCurrentSystemDate();

        ticker = new ClockTicker(this);
        ticker.start();
    }

    /**
     * Konfiguruje ikonę i menu kontekstowe (PPM) w zasobniku systemowym Windowsa.
     */
    private void initSystemTray() {
        if (!SystemTray.isSupported()) {
            return; // Jeśli system nie wspiera tray'a, pomijamy
        }

        systemTray = SystemTray.getSystemTray();

        // Tworzymy prostą zastępczą ikonę (mały kwadrat 16x16 z literą C)
        Image image = createImageForTray();

        PopupMenu popup = new PopupMenu();

        // Opcja przywrócenia okna
        MenuItem openItem = new MenuItem("Pokaż kalendarz");
        openItem.addActionListener(e -> SwingUtilities.invokeLater(this::restoreFromTray));
        popup.add(openItem);

        popup.addSeparator();

        // Opcja wyłączenia programu z poziomu PPM
        MenuItem exitItem = new MenuItem("Zamknij");
        exitItem.addActionListener(e -> {
            if (systemTray != null && trayIcon != null) {
                systemTray.remove(trayIcon);
            }
            System.exit(0);
        });
        popup.add(exitItem);

        trayIcon = new TrayIcon(image, "Kalendarz i Zegar", popup);
        trayIcon.setImageAutoSize(true);

        // Dwukrotne kliknięcie lewym przyciskiem myszy również przywraca okno
        trayIcon.addActionListener(e -> SwingUtilities.invokeLater(this::restoreFromTray));

        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ukrywa okno aplikacji i zostawia ikonę w zasobniku.
     */
    private void minimizeToTray() {
        setVisible(false);
    }

    /**
     * Przywraca okno z zasobnika systemowego.
     */
    private void restoreFromTray() {
        setVisible(true);
        setExtendedState(JFrame.NORMAL);
        toFront();
    }

    /**
     * Tworzy prostą grafikę zastępczą na potrzeby ikonki w zasobniku.
     */
    private Image createImageForTray() {
        int width = 16;
        int height = 16;
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
        });
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

            if (monthIndex < 0) {
                monthIndex = 11;
                year--;
            } else if (monthIndex > 11) {
                monthIndex = 0;
                year++;
            }

            if (year >= 1 && year <= 9999) {
                yearField.setText(String.valueOf(year));
                monthComboBox.setSelectedIndex(monthIndex);
                calendarPanel.updateCalendar(year, monthIndex + 1);
            }
        } catch (NumberFormatException ignored) {
        }
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
        } catch (NumberFormatException ignored) {
        }
    }
}