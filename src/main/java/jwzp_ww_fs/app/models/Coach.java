package jwzp_ww_fs.app.models;

import java.time.Year;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import static javax.persistence.GenerationType.AUTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

// public record Coach(String firstName, String lastName, Year yearOfBirth) {
// }

@Entity
@Table(name="coaches")
public class Coach {
    @Id
    @GeneratedValue(strategy = AUTO)
    private int id;

    @JsonProperty("firstName")
    private /*final*/ String firstName;

    @JsonProperty("lastName")
    private /*final*/ String lastName;

    @JsonProperty("yearOfBirth")
    private /*final*/ Year yearOfBirth;

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

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Coach() {
        firstName = null;
        lastName = null;
        yearOfBirth = null;
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Coach(String firstName, String lastName, Year yearOfBirth) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.yearOfBirth = yearOfBirth;
    }

    public void updateData(Coach other) {
        this.firstName = other.firstName();
        this.lastName = other.lastName();
        this.yearOfBirth = other.yearOfBirth();
    }
}