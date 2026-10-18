import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class EventManager {
    private List<CalendarEvent> events = new ArrayList<>();

    // Ścieżka do pliku w folderze domowym użytkownika
    private static final Path STORAGE_PATH = Paths.get(
            System.getProperty("user.home"), ".calendar_app", "events.dat"
    );

    public EventManager() {
        loadEvents();
    }

    public void addEvent(String title, LocalDateTime eventDateTime) {
        events.add(new CalendarEvent(title, eventDateTime));
        saveEvents();
    }

    public void removeEvent(int index) {
        if (index >= 0 && index < events.size()) {
            events.remove(index);
            saveEvents();
        }
    }

    public void updateEvent(int index, String newTitle, LocalDateTime newDateTime) {
        if (index >= 0 && index < events.size()) {
            events.set(index, new CalendarEvent(newTitle, newDateTime));
            saveEvents();
        }
    }

    public List<CalendarEvent> getEvents() {
        return events;
    }

    /**
     * Zapisuje listę wydarzeń do pliku w folderze domowym użytkownika
     */
    private void saveEvents() {
        try {
            // Upewnij się, że katalog .calendar_app istnieje
            if (Files.notExists(STORAGE_PATH.getParent())) {
                Files.createDirectories(STORAGE_PATH.getParent());
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(STORAGE_PATH.toFile()))) {
                oos.writeObject(events);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Wczytuje listę wydarzeń z pliku podczas startu aplikacji
     */
    @SuppressWarnings("unchecked")
    private void loadEvents() {
        if (Files.exists(STORAGE_PATH)) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(STORAGE_PATH.toFile()))) {
                Object obj = ois.readObject();
                if (obj instanceof List) {
                    events = (List<CalendarEvent>) obj;
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                events = new ArrayList<>();
            }
        }
    }

    public List<String> checkReminders(LocalDateTime now) {
        List<String> triggeredAlerts = new ArrayList<>();

        for (CalendarEvent event : events) {
            long minutesUntil = ChronoUnit.MINUTES.between(now, event.getEventDateTime());
            long hoursUntil = ChronoUnit.HOURS.between(now, event.getEventDateTime());
            long daysUntil = ChronoUnit.DAYS.between(now.toLocalDate(), event.getEventDateTime().toLocalDate());

            if (daysUntil == 7 && !event.isReminded7Days() && minutesUntil >= 0) {
                triggeredAlerts.add("Przypomnienie (za 7 dni): \"" + event.getTitle() + "\"");
                event.setReminded7Days(true);
            } else if (daysUntil == 3 && !event.isReminded3Days() && minutesUntil >= 0) {
                triggeredAlerts.add("Przypomnienie (za 3 dni): \"" + event.getTitle() + "\"");
                event.setReminded3Days(true);
            } else if (daysUntil == 1 && !event.isReminded1Day() && minutesUntil >= 0) {
                triggeredAlerts.add("Nadchodzi jutro: \"" + event.getTitle() + "\"");
                event.setReminded1Day(true);
            } else if (hoursUntil <= 12 && hoursUntil > 1 && !event.isReminded12Hours() && minutesUntil >= 0) {
                triggeredAlerts.add("Za 12 godzin: \"" + event.getTitle() + "\"");
                event.setReminded12Hours(true);
            } else if (hoursUntil <= 1 && hoursUntil >= 0 && !event.isReminded1Hour() && minutesUntil >= 0) {
                triggeredAlerts.add("Pilne! Za godzinę: \"" + event.getTitle() + "\"");
                event.setReminded1Hour(true);
            }
        }

        return triggeredAlerts;
    }
}