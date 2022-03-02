package jwzp_ww_fs.app.models;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

public record Event(String title, DayOfWeek day, LocalTime time, Duration duration, int clubId, int coachId) implements Comparable<Event>{

    @Override
    public int compareTo(Event other) {
        return this.time.compareTo(other.time);
    }
}
