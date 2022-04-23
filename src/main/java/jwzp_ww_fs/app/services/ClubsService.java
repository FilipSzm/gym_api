package jwzp_ww_fs.app.services;

import jwzp_ww_fs.app.Exceptions.ClubHasEventsException;
import jwzp_ww_fs.app.Exceptions.EventHoursInClubException;
import jwzp_ww_fs.app.models.*;
import jwzp_ww_fs.app.models.Schedule;
import jwzp_ww_fs.app.repositories.ClubsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public void setFillLevel(int clubId, Map<DayOfWeek, OpeningHours> fillLevel) {
        var converted = new HashMap<DayOfWeek, EventHours>();
        for (var entry : fillLevel.entrySet())
            converted.put(entry.getKey(), new EventHours(entry.getValue().from(), entry.getValue().to()));

        var club = repository.findById(clubId).orElse(null);
        if (club == null) return;

        club.fillLevel(converted);
        repository.save(club);
    }

    public synchronized void addEventToClub(int clubId) {
        var club = repository.getById(clubId);
        club.addEvent();
        repository.save(club);
    }

    public synchronized void subtractEventFromClub(int clubId) {
        var club = repository.getById(clubId);
        club.subEvent();
        repository.save(club);
    }

    public Club addClub(Club club) {
        var out = repository.save(club);
        out.numberOfEvents(0);
        out.fillLevel(new HashMap<>());
        return repository.save(club);
    }

    public Club patchClub(int clubId, Club club) throws EventHoursInClubException {
        var clubToUpdate = repository.findById(clubId).orElse(null);
        if (clubToUpdate == null) return null;
        if (hoursCollision(clubToUpdate, club)) throw  new EventHoursInClubException();

        clubToUpdate.updateData(club);
        return repository.save(clubToUpdate);
    }

    private boolean hoursCollision(Club oldClub, Club newClub) {
        var newHours = newClub.whenOpen();
        var oldHours = oldClub.fillLevel();

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
        Club club = repository.findById(clubId).orElse(null);
        if (club == null) return null;
        if (!club.isEmpty()) throw new ClubHasEventsException();

        repository.deleteById(clubId);
        return club;
    }

    public List<Club> removeAllClubs() throws ClubHasEventsException {
        var clubs = repository.findAll();
        for (var club : clubs)
            if (!club.isEmpty()) throw new ClubHasEventsException();

        repository.deleteAll();
        return clubs;
    }

    public List<Club> getAllClubs() {
        return repository.findAll();
    }

    public Page<Club> getAllClubs(Pageable p) {
        return repository.findAll(p);
    }

    public Club getClub(int clubId) {
        return repository.findById(clubId).orElse(null);
    }



    public boolean isScheduleInClubOpeningHours(Schedule scheduleToAdd) {
        LocalTime beg = scheduleToAdd.time();
        LocalTime end = scheduleToAdd.time().plus(scheduleToAdd.duration());

        if (beg.isBefore(end)) {
            OpeningHours openingHours = getClub(scheduleToAdd.clubId()).whenOpen().get(scheduleToAdd.day());
            if (openingHours.from().equals(openingHours.to())) return true;
            return !openingHours.from().isAfter(beg) && ((!openingHours.to().isBefore(end)) || (openingHours.to().equals(LocalTime.MIDNIGHT)));
        } else {
            OpeningHours firstDay = getClub(scheduleToAdd.clubId()).whenOpen().get(scheduleToAdd.day());
            OpeningHours secondDay = getClub(scheduleToAdd.clubId()).whenOpen().get(scheduleToAdd.day().plus(1));

            boolean firstDayOk, secondDayOk;
            if (firstDay.from().equals(firstDay.to())) firstDayOk = true;
            else firstDayOk = !firstDay.from().isAfter(beg) && firstDay.to().equals(LocalTime.MIDNIGHT);

            if (secondDay.from().equals(secondDay.to())) secondDayOk = true;
            else secondDayOk = secondDay.from().equals(LocalTime.MIDNIGHT) && !secondDay.to().isBefore(end);

            return firstDayOk && secondDayOk;
        }
    }

    public boolean isEventInstanceInClubOpeningHours(EventInstance eventToAdd) {
        LocalTime beg = eventToAdd.time();
        LocalTime end = eventToAdd.time().plus(eventToAdd.duration());

        if (beg.isBefore(end)) {
            OpeningHours openingHours = getClub(eventToAdd.clubId()).whenOpen().get(eventToAdd.date().getDayOfWeek());
            if (openingHours.from().equals(openingHours.to())) return true;
            return !openingHours.from().isAfter(beg) && ((!openingHours.to().isBefore(end)) || (openingHours.to().equals(LocalTime.MIDNIGHT)));
        } else {
            OpeningHours firstDay = getClub(eventToAdd.clubId()).whenOpen().get(eventToAdd.date().getDayOfWeek());
            OpeningHours secondDay = getClub(eventToAdd.clubId()).whenOpen().get(eventToAdd.date().getDayOfWeek().plus(1));

            boolean firstDayOk, secondDayOk;
            if (firstDay.from().equals(firstDay.to())) firstDayOk = true;
            else firstDayOk = !firstDay.from().isAfter(beg) && firstDay.to().equals(LocalTime.MIDNIGHT);

            if (secondDay.from().equals(secondDay.to())) secondDayOk = true;
            else secondDayOk = secondDay.from().equals(LocalTime.MIDNIGHT) && !secondDay.to().isBefore(end);

            return firstDayOk && secondDayOk;
        }
    }

    public Page<Club> getPage(Pageable p) {
        return repository.findAll(p);
    }
}
