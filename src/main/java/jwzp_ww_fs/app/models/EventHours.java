package jwzp_ww_fs.app.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.time.LocalTime;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "event_hours")
public class EventHours {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    private int id;

    @JsonProperty("from")
    @Column(name = "_from")
    private LocalTime from;

    @JsonProperty("to")
    @Column(name = "_to")
    private LocalTime to;

    public EventHours() {
        from = null;
        to = null;
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public EventHours(LocalTime from, LocalTime to) {
        this.from = from;
        this.to = to;
    }

    public void updateData(EventHours eventHours) {
        this.from = eventHours.from;
        this.to = eventHours.to;
    }

    public void from(LocalTime from) {
        this.from = from;
    }

    public LocalTime from() {
        return from;
    }

    public void to(LocalTime to) {
        this.to = to;
    }

    public LocalTime to() {
        return to;
    }
}
