package jwzp_ww_fs.app.models.v2;

import java.time.DayOfWeek;
import java.time.Duration;
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

@Entity
@Table(name="schedules")
@Schema(example = Schedule.exampleSchema)
public class Schedule {
    public static final String exampleSchema = """
        {\"title\": \"string\",
        \"day\": \"MONDAY\",
        \"time\": \"00:00\",
        \"capacity\": 0,
        \"duration\": \"PT10M\",
        \"coachId\": 0,
        \"clubId\": 0}""";

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private int id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("day")
    @Enumerated(EnumType.STRING)
    private DayOfWeek day;

    @JsonProperty("time")
    private LocalTime time;

    @JsonProperty("duration")
    private Duration duration;

    @JsonProperty("capacity")
    private int capacity;

    @JsonProperty("clubId")
    //TODO FOREIGN KEY
    private int clubId;

    @JsonProperty("coachId")
    //TODO FOREIGN KEY
    private int coachId;

    public Schedule() {
        this.title = null;
        this.day = null;
        this.time = null;
        this.duration = null;
        this.capacity = -1;
        this.clubId = -1;
        this.coachId = -1;
    }

    public Schedule(String title, DayOfWeek day, LocalTime time, Duration duration, int capacity, int clubId, int coachId) {
        this.title = title;
        this.day = day;
        this.time = time;
        this.duration = duration;
        this.capacity = capacity;
        this.clubId = clubId;
        this.coachId = coachId;
    }

    public void updateData(Schedule other) {
        this.title = other.title;
        this.day = other.day;
        this.time = other.time;
        this.duration = other.duration;
        this.capacity = other.capacity;
        this.clubId = other.clubId;
        this.coachId = other.coachId;
    }

    public String title() {
        return title;
    }

    public DayOfWeek day() {
        return day;
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

    public int coachId() {
        return coachId;
    }

    public int clubId() {
        return clubId;
    }
}