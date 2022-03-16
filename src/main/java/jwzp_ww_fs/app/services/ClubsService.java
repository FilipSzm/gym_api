package jwzp_ww_fs.app.services;

import jwzp_ww_fs.app.Exceptions.ClubHasEventsException;
import jwzp_ww_fs.app.Exceptions.EventHoursInClubException;
import jwzp_ww_fs.app.models.Club;
import jwzp_ww_fs.app.models.Event;
import jwzp_ww_fs.app.models.OpeningHours;
import jwzp_ww_fs.app.repositories.ClubsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClubsService {
    ClubsRepository repository;

    Map<Integer, Integer> numberOfEventsInClubs;
    Map<Integer, Map<DayOfWeek, OpeningHours>> clubsFillLevel;

    @Autowired
    public ClubsService(ClubsRepository repository) {
        this.repository = repository;
        numberOfEventsInClubs = new HashMap<>();
        clubsFillLevel = new HashMap<>();
    }

    Map<DayOfWeek, OpeningHours> getFillLevel(int clubId) {
        return clubsFillLevel.get(clubId);
    }

    void setFillLevel(int clubId, Map<DayOfWeek, OpeningHours> fillLevel) {
        clubsFillLevel.put(clubId, fillLevel);
    }

    synchronized boolean addEventToClub(int clubId) {
        if (!numberOfEventsInClubs.containsKey(clubId)) return false;

        numberOfEventsInClubs.replace(clubId, numberOfEventsInClubs.get(clubId) + 1);
        return true;
    }

    synchronized boolean subtractEventFromClub(int clubId) {
        if (!numberOfEventsInClubs.containsKey(clubId)) return false;
        if (numberOfEventsInClubs.get(clubId) <= 0) return false;

        numberOfEventsInClubs.replace(clubId, numberOfEventsInClubs.get(clubId) - 1);
        return true;
    }

    boolean isEventInClubOpeningHours(Event eventToAdd) {
        LocalTime beg = eventToAdd.time();
        LocalTime end = eventToAdd.time().plus(eventToAdd.duration());

        if (beg.isBefore(end)) {
            OpeningHours openingHours = getClub(eventToAdd.clubId()).whenOpen().get(eventToAdd.day());
            if (openingHours.from().equals(openingHours.to())) return true;
            return !openingHours.from().isAfter(beg) && ((!openingHours.to().isBefore(end)) || (openingHours.to().equals(LocalTime.MIDNIGHT)));
        } else {
            OpeningHours firstDay = getClub(eventToAdd.clubId()).whenOpen().get(eventToAdd.day());
            OpeningHours secondDay = getClub(eventToAdd.clubId()).whenOpen().get(eventToAdd.day().plus(1));

            boolean firstDayOk, secondDayOk;
            if (firstDay.from().equals(firstDay.to())) firstDayOk = true;
            else firstDayOk = !firstDay.from().isAfter(beg) && firstDay.to().equals(LocalTime.MIDNIGHT);

            if (secondDay.from().equals(secondDay.to())) secondDayOk = true;
            else secondDayOk = secondDay.from().equals(LocalTime.MIDNIGHT) && !secondDay.to().isBefore(end);

            return firstDayOk && secondDayOk;
        }
    }

    public Club addClub(Club club) {
        var id = repository.getNextId();
        var out = repository.addClub(club);

        numberOfEventsInClubs.put(id, 0);
        clubsFillLevel.put(id, new HashMap<>());

        return out;
    }

    public Club patchClub(int clubId, Club club) throws EventHoursInClubException {
        if (hoursCollision(clubId, club)) throw new EventHoursInClubException();

        return repository.patchClubWithId(clubId, club);
    }

    private boolean hoursCollision(int clubId, Club club) {
        var newHours = club.whenOpen();
        var oldHours = clubsFillLevel.get(clubId);

        for (var key : oldHours.keySet()) {
            if (!newHours.containsKey(key)) return true;
            if (oldHours.get(key) == null) continue;
            if (newHours.get(key) == null) return true;
            if (newHours.get(key).from().equals(newHours.get(key).to())) continue;
            if (oldHours.get(key).from().equals(oldHours.get(key).to()) && newHours.get(key).from().equals(newHours.get(key).to())) continue;

            if (!(newHours.get(key).from().compareTo(oldHours.get(key).from()) <= 0 && newHours.get(key).to().compareTo(oldHours.get(key).to()) >= 0)) return true;
        }

        return false;
    }

    public Club removeClub(int clubId) throws ClubHasEventsException {
        if (numberOfEventsInClubs.get(clubId) > 0) throw new ClubHasEventsException();

        numberOfEventsInClubs.remove(clubId);
        clubsFillLevel.remove(clubId);
        return repository.removeClubWithId(clubId);
    }

    public List<Club> removeAllClubs() throws ClubHasEventsException {
        for (var key : numberOfEventsInClubs.keySet())
            if (numberOfEventsInClubs.get(key) > 0) throw new ClubHasEventsException();

        numberOfEventsInClubs = new HashMap<>();
        clubsFillLevel = new HashMap<>();
        return repository.removeAllClubs();
    }

    public List<Club> getAllClubs() {
        return repository.getAllClubs();
    }

    public Club getClub(int clubId) {
        return repository.getClub(clubId);
    }
}
