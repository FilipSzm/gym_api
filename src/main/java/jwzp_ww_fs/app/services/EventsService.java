package jwzp_ww_fs.app.services;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jwzp_ww_fs.app.Exceptions.EventCoachOverlapException;
import jwzp_ww_fs.app.Exceptions.EventDoesNotExistException;
import jwzp_ww_fs.app.Exceptions.EventNoSuchClubException;
import jwzp_ww_fs.app.Exceptions.EventNoSuchCoachException;
import jwzp_ww_fs.app.Exceptions.EventNotInOpeningHoursException;
import jwzp_ww_fs.app.Exceptions.EventTooLongException;
import jwzp_ww_fs.app.Exceptions.GymException;
import jwzp_ww_fs.app.models.Event;
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
        if (!existsCoachForEvent(event)) throw new EventNoSuchCoachException();
        if (existsSimultaniousEventWithCoach(event, null)) throw new EventCoachOverlapException();
        if (!clubsService.isEventInClubOpeningHours(event)) throw new EventNotInOpeningHoursException();
        if (!isEventCorrectLength(event)) throw new EventTooLongException();

        return repository.addEvent(event);
    }

    private boolean isEventCorrectLength(Event eventToAdd) {
        return eventToAdd.duration().compareTo(Duration.ofDays(1)) <= 0;
    }

    private boolean existsClubForEvent(Event eventToAdd) {
        return clubsService.getClub(eventToAdd.clubId()) != null;
    }

    private boolean existsCoachForEvent(Event eventToAdd) {
        return coachesService.getCoach(eventToAdd.coachId()) != null;
    }

    private boolean existsSimultaniousEventWithCoach(Event eventToAdd, Event eventToIgnore) {
        var otherEventsWithCoach = getEventsByCoach(eventToAdd.coachId());
        if (eventToIgnore != null) {
            otherEventsWithCoach = otherEventsWithCoach.stream().filter(e -> !e.equals(eventToIgnore)).toList();
        }

        LocalTime beg = eventToAdd.time();
        LocalTime end = eventToAdd.time().plus(eventToAdd.duration());

        var eventsPrevDay = otherEventsWithCoach.stream().filter(e -> e.day().equals(eventToAdd.day().minus(1)))
                .filter(this::isEventOverMidnight);
        var eventsSameDayOvernight = otherEventsWithCoach.stream().filter(e -> e.day().equals(eventToAdd.day()))
                .filter(this::isEventOverMidnight);
        var eventsSameDay = otherEventsWithCoach.stream().filter(e -> e.day().equals(eventToAdd.day()))
                .filter(this::isEventNotOverMidnight);
        var eventsNextDay = otherEventsWithCoach.stream().filter(e -> e.day().equals(eventToAdd.day().plus(1)));
        if (beg.isBefore(end)) {
            var fromPrev = eventsPrevDay.filter(e -> e.time().plus(e.duration()).isAfter(beg)).findAny().isPresent();
            var fromCurrOvernight = eventsSameDayOvernight.filter(e -> e.time().isBefore(end)).findAny().isPresent();
            var fromCurr = eventsSameDay
                    .filter(e -> isDuringEvent(eventToAdd, e.time())
                            || isDuringEvent(eventToAdd, e.time().plus(e.duration()))
                            || (e.time().isBefore(beg) && e.time().plus(e.duration()).isAfter(end)))
                    .findAny().isPresent();

            return fromPrev || fromCurrOvernight || fromCurr;
        } else {
            var fromPrev = eventsPrevDay.filter(e -> e.time().plus(e.duration()).isAfter(beg)).findAny().isPresent();
            var fromNext = eventsNextDay.filter(e -> e.time().isBefore(end)).findAny().isPresent();
            var fromCurr = eventsSameDay.filter(e -> e.time().plus(e.duration()).isAfter(beg) || e.time().isAfter(beg))
                    .findAny().isPresent();
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

    public Event removeEvent(int eventId) throws EventDoesNotExistException {
        Event removedEvent = repository.removeEventWithId(eventId);

        if (removedEvent == null)
            throw new EventDoesNotExistException();
        return removedEvent;
    }

    public List<Event> removeAllEvents() {
        return repository.removeAllEvents();
    }

    public Event updateEvent(int eventId, Event event) throws GymException {
        Event currentEventWithId = getEvent(eventId);
        
        if (currentEventWithId == null) throw new EventDoesNotExistException();

        if (!existsClubForEvent(event))
            throw new EventNoSuchClubException();
        if (!existsCoachForEvent(event))
            throw new EventNoSuchCoachException();
        if (existsSimultaniousEventWithCoach(event, currentEventWithId))
            throw new EventCoachOverlapException();
        if (!clubsService.isEventInClubOpeningHours(event))
            throw new EventNotInOpeningHoursException();
        if (!isEventCorrectLength(event))
            throw new EventTooLongException();

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