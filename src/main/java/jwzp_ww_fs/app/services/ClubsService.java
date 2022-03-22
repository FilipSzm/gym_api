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

    @Autowired
    public ClubsService(ClubsRepository repository) {
        this.repository = repository;
    }

    Map<DayOfWeek, OpeningHours> getFillLevel(int clubId) {
//        return clubsFillLevel.get(clubId);
        return null;
    }

    void setFillLevel(int clubId, Map<DayOfWeek, OpeningHours> fillLevel) {
//        clubsFillLevel.put(clubId, fillLevel); hmmm
    }

    synchronized boolean addEventToClub(int clubId) {
//        if (!numberOfEventsInClubs.containsKey(clubId)) return false;
//
//        numberOfEventsInClubs.replace(clubId, numberOfEventsInClubs.get(clubId) + 1);
        return true;
    }

    synchronized boolean subtractEventFromClub(int clubId) {
//        if (!numberOfEventsInClubs.containsKey(clubId)) return false;
//        if (numberOfEventsInClubs.get(clubId) <= 0) return false;
//
//        numberOfEventsInClubs.replace(clubId, numberOfEventsInClubs.get(clubId) - 1);
        return true;
    }



    public Club addClub(Club club) {
        var out = repository.save(club);
        out.numberOfEvents(0);
        out.fillLevel(new HashMap<>());
        return repository.save(club);
    }

    public Club patchClub(int clubId, Club club) throws EventHoursInClubException {
        var clubToUpdate = repository.findById(clubId).orElse(null);
        if (clubToUpdate == null) throw  new EventHoursInClubException();  //// chyba nie dziala

        clubToUpdate.updateData(club);
        return repository.save(clubToUpdate);
    }

    private boolean hoursCollision(int clubId, Club club) {
        var newHours = club.whenOpen();
        var oldHours = repository.getById(clubId).fillLevel();

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
//        if (numberOfEventsInClubs.get(clubId) > 0) throw new ClubHasEventsException();
//
//        numberOfEventsInClubs.remove(clubId);
//        clubsFillLevel.remove(clubId);
//        return repository.removeClubWithId(clubId);


        return null;
    }

    public List<Club> removeAllClubs() throws ClubHasEventsException {
//        for (var key : numberOfEventsInClubs.keySet())
//            if (numberOfEventsInClubs.get(key) > 0) throw new ClubHasEventsException();
//
//        numberOfEventsInClubs = new HashMap<>();
//        clubsFillLevel = new HashMap<>();
//        return repository.removeAllClubs();

        return null;

    }

    public List<Club> getAllClubs() {
        return repository.findAll();
    }

    public Club getClub(int clubId) {
        return repository.findById(clubId).orElse(null);
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
}
