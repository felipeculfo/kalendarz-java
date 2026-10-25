import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class EventManager {
    private List<CalendarEvent> events;
    // Pamiętaj, aby tutaj dopisać swoją logikę wczytywania/zapisu do pliku (np. saveEvents / loadEvents)

    public EventManager() {
        this.events = new ArrayList<>();
        // loadEvents(); // Odkomentuj jeśli tak ładujesz dane
    }

    public List<CalendarEvent> getEvents() {
        return events;
    }

    // Nowa metoda dodawania z notatką
    public void addEvent(String title, String description, LocalDateTime eventDateTime) {
        events.add(new CalendarEvent(title, description, eventDateTime));
        saveEvents();
    }

    // Stara metoda dla kompatybilności wstecznej
    public void addEvent(String title, LocalDateTime eventDateTime) {
        addEvent(title, "", eventDateTime);
    }

    // Nowa metoda aktualizacji z notatką
    public void updateEvent(int index, String title, String description, LocalDateTime eventDateTime) {
        if (index >= 0 && index < events.size()) {
            CalendarEvent event = events.get(index);
            event.setTitle(title);
            event.setDescription(description);
            event.setEventDateTime(eventDateTime);
            saveEvents();
        }
    }

    public void removeEvent(int index) {
        if (index >= 0 && index < events.size()) {
            events.remove(index);
            saveEvents();
        }
    }

    public void saveEvents() {
        // Twoja dotychczasowa logika zapisu do pliku .dat
    }

    public List<String> checkReminders(LocalDateTime now) {
        List<String> triggeredAlerts = new ArrayList<>();

        for (CalendarEvent event : events) {
            long minutesUntil = ChronoUnit.MINUTES.between(now, event.getEventDateTime());
            long hoursUntil = ChronoUnit.HOURS.between(now, event.getEventDateTime());
            long daysUntil = ChronoUnit.DAYS.between(now.toLocalDate(), event.getEventDateTime().toLocalDate());

            String noteText = event.getDescription().isEmpty() ? "" : "\nNotatka: " + event.getDescription();

            if (daysUntil == 7 && !event.isReminded7Days() && minutesUntil >= 0) {
                triggeredAlerts.add("Przypomnienie (za 7 dni): \"" + event.getTitle() + "\"" + noteText);
                event.setReminded7Days(true);
            } else if (daysUntil == 3 && !event.isReminded3Days() && minutesUntil >= 0) {
                triggeredAlerts.add("Przypomnienie (za 3 dni): \"" + event.getTitle() + "\"" + noteText);
                event.setReminded3Days(true);
            } else if (daysUntil == 1 && !event.isReminded1Day() && minutesUntil >= 0) {
                triggeredAlerts.add("Nadchodzi jutro: \"" + event.getTitle() + "\"" + noteText);
                event.setReminded1Day(true);
            } else if (hoursUntil <= 12 && hoursUntil > 1 && !event.isReminded12Hours() && minutesUntil >= 0) {
                triggeredAlerts.add("Za 12 godzin: \"" + event.getTitle() + "\"" + noteText);
                event.setReminded12Hours(true);
            } else if (hoursUntil <= 1 && hoursUntil >= 0 && !event.isReminded1Hour() && minutesUntil >= 0) {
                triggeredAlerts.add("Pilne! Za godzinę: \"" + event.getTitle() + "\"" + noteText);
                event.setReminded1Hour(true);
            }
        }

        return triggeredAlerts;
    }
}