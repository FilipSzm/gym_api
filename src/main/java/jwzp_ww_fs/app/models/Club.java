package jwzp_ww_fs.app.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.util.Map;

import static javax.persistence.GenerationType.AUTO;

@Entity
@Table(name = "clubs")
public class Club {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = AUTO)
    @Column(name = "id")
    private int id;

    @JsonProperty("name")
    @Column(name = "name")
    private String name;

    @JsonProperty("address")
    @Column(name = "address")
    private String address;

    @JsonProperty("whenOpen")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "when_open",
            joinColumns = {@JoinColumn(name = "club_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "opening_hours_id", referencedColumnName = "id")})
    @MapKeyEnumerated
    private Map<DayOfWeek, OpeningHours> whenOpen;

    @JsonIgnore
    @Column(name = "number_of_events")
    private int numberOfEvents;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "fill_level",
            joinColumns = {@JoinColumn(name = "club_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "fill_level_id", referencedColumnName = "id")})
    @MapKeyEnumerated
    private Map<DayOfWeek, EventHours> fillLevel;

    public Club() {
        this.name = null;
        this.address = null;
        this.whenOpen = null;
        this.numberOfEvents = -1;
        this.fillLevel = null;
    }

    public Club(String name, String address, Map<DayOfWeek, OpeningHours> whenOpen) {
        this.name = name;
        this.address = address;
        this.whenOpen = whenOpen;
    }

    public Club(String name, String address, Map<DayOfWeek, OpeningHours> whenOpen, int numberOfEvents, Map<DayOfWeek, EventHours> fillLevel) {
        this.name = name;
        this.address = address;
        this.whenOpen = whenOpen;
        this.numberOfEvents = numberOfEvents;
        this.fillLevel = fillLevel;
    }

    public void whenOpen(Map<DayOfWeek, OpeningHours> whenOpen) {
        this.whenOpen = whenOpen;
    }

    public Map<DayOfWeek, OpeningHours> whenOpen() {
        return whenOpen;
    }

    public void fillLevel(Map<DayOfWeek, EventHours> fillLevel) {
        this.fillLevel.clear();
        this.fillLevel.putAll(fillLevel);
    }

    public Map<DayOfWeek, EventHours> fillLevel() {
        return fillLevel;
    }

    public void numberOfEvents(int numberOfEvents) {
        this.numberOfEvents = numberOfEvents;
    }

    public int numberOfEvents() {
        return numberOfEvents;
    }

    public void updateData(Club club) {
        this.name = club.name;
        this.address = club.address;
        this.whenOpen.clear();
        this.whenOpen.putAll(club.whenOpen);
    }

    public void addEvent() {
        numberOfEvents++;
    }

    public void subEvent() {
        numberOfEvents--;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return numberOfEvents <= 0;
    }

    public void deleteEvents() {
        numberOfEvents = 0;
    }
}