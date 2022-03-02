package jwzp_ww_fs.app.models;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

public record Event(String title, DayOfWeek day, LocalTime time, Duration duration, int clubId, int coachId) {
    // @Override
    // public boolean equals(Object other) {
    //     if (other == null) return false;

    //     if (other instanceof Event otherEvent) {
    //         return otherEvent.eventId == this.eventId;
    //     }

    //     return false;
    // }

    // @Override
    // public int hashCode() {
    //     return eventId;
    // }
}
