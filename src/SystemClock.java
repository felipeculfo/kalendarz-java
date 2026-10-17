import java.time.ZoneId;
import java.time.ZonedDateTime;

public class SystemClock {

    private final ZoneId systemZone;

    public SystemClock() {
        this.systemZone = ZoneId.systemDefault();
    }

    public ZonedDateTime getCurrentTime() {
        return ZonedDateTime.now(systemZone);
    }

    public String getZoneName() {
        return systemZone.toString();
    }

    public int getCurrentHour(ZonedDateTime time) {
        return time.getHour();
    }

    public int getCurrentMinute(ZonedDateTime time) {
        return time.getMinute();
    }

    public int getCurrentSecond(ZonedDateTime time) {
        return time.getSecond();
    }
}