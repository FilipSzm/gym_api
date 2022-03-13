package jwzp_ww_fs.app.services;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jwzp_ww_fs.app.Exceptions.EventCoachOverlapException;
import jwzp_ww_fs.app.Exceptions.EventNoSuchClubException;
import jwzp_ww_fs.app.Exceptions.EventNotInOpeningHoursException;
import jwzp_ww_fs.app.Exceptions.EventTooLongException;
import jwzp_ww_fs.app.Exceptions.GymException;
import jwzp_ww_fs.app.models.Event;
import jwzp_ww_fs.app.models.OpeningHours;
import jwzp_ww_fs.app.repositories.EventsRepository;

@Service
public class EventsService {
    EventsRepository repository;

    ClubsService clubsService;
    CoachesService coachesService;

    @Autowired
    public EventsService(EventsRepository repository, ClubsService clubsService, CoachesService coachesService) {
        this.repository = repository;
        this.clubsService = clubsService;
        this.coachesService = coachesService;
    }

    public Event addEvent(Event event) throws GymException {
        if (!existsClubForEvent(event)) throw new EventNoSuchClubException();
        if (!existsCoachForEvent(event)) throw new EventNoSuchClubException();
        if (existsSimultaniousEventWithCoach(event)) throw new EventCoachOverlapException();
        if (!isEventInClubOpeningHours(event)) throw new EventNotInOpeningHoursException();
        if (!isEventCorrectLength(event)) throw new EventTooLongException();

        return repository.addEvent(event);
    }

    private boolean isEventCorrectLength(Event eventToAdd) {
        return eventToAdd.duration().compareTo(Duration.ofDays(1)) < 0;
    }

    private boolean isEventInClubOpeningHours(Event eventToAdd) {
        LocalTime beg = eventToAdd.time();
        LocalTime end = eventToAdd.time().plus(eventToAdd.duration());

        if (beg.isBefore(end)) {
            OpeningHours openingHours = clubsService.getClub(eventToAdd.clubId()).whenOpen().get(eventToAdd.day());
            if (openingHours.from().equals(openingHours.to())) return true;
            return !openingHours.from().isAfter(beg) && !openingHours.to().isBefore(end);
        } else {
            OpeningHours firstDay = clubsService.getClub(eventToAdd.clubId()).whenOpen().get(eventToAdd.day());
            OpeningHours secondDay = clubsService.getClub(eventToAdd.clubId()).whenOpen().get(eventToAdd.day().plus(1));

            boolean firstDayOk, secondDayOk;
            if (firstDay.from().equals(firstDay.to())) firstDayOk = true;
            else firstDayOk = !firstDay.from().isAfter(beg) && firstDay.to().equals(LocalTime.MIDNIGHT);

            if (secondDay.from().equals(secondDay.to())) secondDayOk = true;
            else secondDayOk = secondDay.from().equals(LocalTime.MIDNIGHT) && !secondDay.to().isBefore(end);

            return firstDayOk && secondDayOk;
        }
    }

    private boolean existsClubForEvent(Event eventToAdd) {
        return clubsService.getClub(eventToAdd.clubId()) != null;
    }

    private boolean existsCoachForEvent(Event eventToAdd) {
        return coachesService.getCoach(eventToAdd.coachId()) != null;
    }

    private boolean existsSimultaniousEventWithCoach(Event eventToAdd) {
        var otherEventsWithCoach = getEventsByCoach(eventToAdd.coachId());

        LocalTime beg = eventToAdd.time();
        LocalTime end = eventToAdd.time().plus(eventToAdd.duration());

        
        var eventsPrevDay = otherEventsWithCoach.stream().filter(e -> e.day().equals(eventToAdd.day().minus(1))).filter(this::isEventOverMidnight);
        var eventsSameDayOvernight = otherEventsWithCoach.stream().filter(e -> e.day().equals(eventToAdd.day())).filter(this::isEventOverMidnight);
        var eventsSameDay = otherEventsWithCoach.stream().filter(e -> e.day().equals(eventToAdd.day())).filter(this::isEventNotOverMidnight);
        var eventsNextDay = otherEventsWithCoach.stream().filter(e -> e.day().equals(eventToAdd.day().plus(1)));
        if (beg.isBefore(end)) {
            var fromPrev = eventsPrevDay.filter(e -> e.time().plus(e.duration()).isAfter(beg)).findAny().isPresent();
            var fromCurrOvernight = eventsSameDayOvernight.filter(e -> e.time().isBefore(end)).findAny().isPresent();
            var fromCurr = eventsSameDay.filter(e -> isDuringEvent(eventToAdd, e.time()) || isDuringEvent(eventToAdd, e.time().plus(e.duration())) || (e.time().isBefore(beg) && e.time().plus(e.duration()).isAfter(end))).findAny().isPresent();
            
            return fromPrev || fromCurrOvernight || fromCurr;
        } else {
            var fromPrev = eventsPrevDay.filter(e -> e.time().plus(e.duration()).isAfter(beg)).findAny().isPresent();
            var fromNext = eventsNextDay.filter(e -> e.time().isBefore(end)).findAny().isPresent();
            var fromCurr = eventsSameDay.filter(e -> e.time().plus(e.duration()).isAfter(beg) || e.time().isAfter(beg)).findAny().isPresent();
            return fromPrev || fromNext || fromCurr;
        }
    }

    private boolean isEventOverMidnight(Event e) {
        return !e.time().plus(e.duration()).isAfter(e.time());
    }
    
    private boolean isEventNotOverMidnight(Event e) {
        return e.time().plus(e.duration()).isAfter(e.time());
    }

    private boolean isDuringEvent(Event e, LocalTime t) {
        return t.isAfter(e.time()) && t.isBefore(e.time().plus(e.duration()));
    }

    public Event removeEvent(int eventId) {
        return repository.removeEventWithId(eventId);
    }

    public List<Event> removeAllEvents() {
        return repository.removeAllEvents();
    }

    public Event updateEvent(int eventId, Event event) {
        return repository.updateEvent(eventId, event);
    }

    public List<Event> getAllEvents() {
        return repository.getAllEvents();
    }

    public List<Event> getEventsByCoach(int coachId) {
        return repository.getAllEvents().stream().filter(c -> c.coachId() == coachId).toList();
    }

    public List<Event> getEventsByClub(int clubId) {
        return repository.getAllEvents().stream().filter(c -> c.clubId() == clubId).toList();
    }

    public List<Event> getEventsByCoachAndClub(int coachId, int clubId) {
        return repository.getAllEvents().stream().filter(c -> c.coachId() == coachId && c.clubId() == clubId).toList();
    }

    public Event getEvent(int id) {
        return repository.getEvent(id);
    }
}