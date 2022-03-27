package jwzp_ww_fs.app.dto;

import jwzp_ww_fs.app.models.Club;
import jwzp_ww_fs.app.models.OpeningHours;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import java.time.DayOfWeek;
import java.util.Map;

@Data
@AllArgsConstructor
public class ClubRepresentation extends RepresentationModel<ClubRepresentation> {
    private final int id;
    private final String name;
    private final String address;
    private final Map<DayOfWeek, OpeningHours> whenOpen;

    public static ClubRepresentation fromClub(Club club) {
        return new ClubRepresentation(club.id(), club.name(), club.address(), club.whenOpen());
    }
}

