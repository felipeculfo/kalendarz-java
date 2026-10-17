import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ClockTicker {

    public interface TimeListener {
        void onTimeChanged(String timeString, ZonedDateTime currentTime);
    }

    private final ZoneId systemZone;
    private final TimeListener listener;
    private volatile boolean running = true;

    public ClockTicker(TimeListener listener) {
        this.systemZone = ZoneId.systemDefault();
        this.listener = listener;
    }

    public void start() {
        Thread thread = new Thread(() -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            while (running) {
                ZonedDateTime now = ZonedDateTime.now(systemZone);
                String timeStr = now.format(formatter) + " (" + systemZone.getId() + ")";

                if (listener != null) {
                    listener.onTimeChanged(timeStr, now);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        running = false;
    }
}