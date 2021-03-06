package jwzp_ww_fs.app.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;
import java.time.LocalTime;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "opening_hours")
@Schema(example = OpeningHours.exampleSchema)
public class OpeningHours {
    public static final String exampleSchema = """
    {
        \"from\": \"00:00\",
        \"to\": \"00:00\"
    }""";

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

    public OpeningHours() {
        from = null;
        to = null;
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public OpeningHours(LocalTime from, LocalTime to) {
        this.from = from;
        this.to = to;
    }

    public void updateData(OpeningHours openingHours) {
        this.from = openingHours.from;
        this.to = openingHours.to;
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
