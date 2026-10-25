import java.io.Serializable;
import java.time.LocalDateTime;

public class CalendarEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title;
    private String description; // Pole na notatkę
    private LocalDateTime eventDateTime;

    private boolean reminded7Days = false;
    private boolean reminded3Days = false;
    private boolean reminded1Day = false;
    private boolean reminded12Hours = false;
    private boolean reminded1Hour = false;

    public CalendarEvent(String title, String description, LocalDateTime eventDateTime) {
        this.title = title;
        this.description = description != null ? description : "";
        this.eventDateTime = eventDateTime;
    }

    public CalendarEvent(String title, LocalDateTime eventDateTime) {
        this(title, "", eventDateTime);
    }

    // Gettery
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDateTime getEventDateTime() { return eventDateTime; }

    // Settery (niezbędne do aktualizacji danych przy edycji!)
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description != null ? description : ""; }
    public void setEventDateTime(LocalDateTime eventDateTime) { this.eventDateTime = eventDateTime; }

    // Obsługa przypomnień
    public boolean isReminded7Days() { return reminded7Days; }
    public void setReminded7Days(boolean reminded) { this.reminded7Days = reminded; }

    public boolean isReminded3Days() { return reminded3Days; }
    public void setReminded3Days(boolean reminded) { this.reminded3Days = reminded; }

    public boolean isReminded1Day() { return reminded1Day; }
    public void setReminded1Day(boolean reminded) { this.reminded1Day = reminded; }

    public boolean isReminded12Hours() { return reminded12Hours; }
    public void setReminded12Hours(boolean reminded) { this.reminded12Hours = reminded; }

    public boolean isReminded1Hour() { return reminded1Hour; }
    public void setReminded1Hour(boolean reminded) { this.reminded1Hour = reminded; }
}