package jwzp_ww_fs.app.models;

import java.time.Year;

import javax.persistence.*;

import static javax.persistence.GenerationType.AUTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name="coaches")
public class Coach {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = AUTO)
    private int id;

    @JsonProperty("firstName")
    private /*final*/ String firstName;

    @JsonProperty("lastName")
    private /*final*/ String lastName;

    @JsonProperty("yearOfBirth")
    private /*final*/ Year yearOfBirth;

    @JsonIgnore
    private int numberOfEvents;


    public int id() {
        return id;
    }

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

    public Year yearOfBirth() {
        return yearOfBirth;
    }

    public Coach() {
        firstName = null;
        lastName = null;
        yearOfBirth = null;
        numberOfEvents = 0;
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Coach(String firstName, String lastName, Year yearOfBirth) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.yearOfBirth = yearOfBirth;
        numberOfEvents = 0;
    }

    public void updateData(Coach other) {
        this.firstName = other.firstName();
        this.lastName = other.lastName();
        this.yearOfBirth = other.yearOfBirth();
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