package jwzp_ww_fs.app.models.v2;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import static javax.persistence.GenerationType.IDENTITY;

// public record Event(String title, DayOfWeek day, LocalTime time, Duration duration, int clubId, int coachId) {
// }

@Entity
@Table(name="events")
@Schema(example = Event.exampleSchema)
public class Event {
    public static final String exampleSchema = """
        {\"title\": \"string\",
        \"day\": \"MONDAY\",
        \"time\": \"00:00\",
        \"duration\": \"PT10M\",
        \"coachId\": 0,
        \"clubId\": 0}""";

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private long id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("date")
    @Enumerated(EnumType.STRING)
    private LocalDate date;

    @JsonProperty("time")
    private LocalTime time;

    @JsonProperty("duration")
    private Duration duration;

    @JsonProperty("capacity")
    private int capacity;

    private int participants;

    @JsonProperty("clubId")
    //TODO FOREIGN KEY
    private int clubId;

    @JsonProperty("coachId")
    //TODO FOREIGN KEY
    private int coachId;

    public Event() {
        this.title = null;
        this.date = null;
        this.time = null;
        this.duration = null;
        this.capacity = -1;
        this.clubId = -1;
        this.coachId = -1;
        this.participants = 0;
    }

    public Event(String title, LocalDate date, LocalTime time, Duration duration, int capacity, int clubId, int coachId) {
        this.title = title;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.capacity = capacity;
        this.participants = 0;
        this.clubId = clubId;
        this.coachId = coachId;
    }

    public void updateData(Event other) {
        this.title = other.title;
        this.date = other.date;
        this.time = other.time;
        this.duration = other.duration;
        this.capacity = other.capacity;
        this.participants = other.participants;
        this.clubId = other.clubId;
        this.coachId = other.coachId;
    }

    public String title() {
        return title;
    }

    public LocalDate date() {
        return date;
    }

    public LocalTime time() {
        return time;
    }

    public Duration duration() {
        return duration;
    }

    public int capacity() {
        return capacity;
    }

    public int participants() {
        return participants;
    }

    public int coachId() {
        return coachId;
    }

    public int clubId() {
        return clubId;
    }
}