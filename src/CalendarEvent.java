import java.io.Serializable;
import java.time.LocalDateTime;

public class CalendarEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title;
    private LocalDateTime eventDateTime;

    // Flagi powiadomień
    private boolean reminded7Days = false;
    private boolean reminded3Days = false;
    private boolean reminded1Day = false;
    private boolean reminded12Hours = false;
    private boolean reminded1Hour = false;

    public CalendarEvent(String title, LocalDateTime eventDateTime) {
        this.title = title;
        this.eventDateTime = eventDateTime;
    }

    // Gettery i settery (oraz Twoje metody isReminded...)
    public String getTitle() { return title; }
    public LocalDateTime getEventDateTime() { return eventDateTime; }

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