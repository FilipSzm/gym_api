package jwzp_ww_fs.app.models;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

public record Event(String title, DayOfWeek day, LocalTime time, Duration duration, int clubId, int coachId) implements Comparable<Event>{

    @Override
    public int compareTo(Event other) {
        int daysComparasion = this.day.compareTo(other.day);
        return (daysComparasion == 0) ? this.time.compareTo(other.time) : daysComparasion;
    }
}
