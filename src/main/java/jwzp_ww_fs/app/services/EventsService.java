package jwzp_ww_fs.app.services;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import jwzp_ww_fs.app.models.Club;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jwzp_ww_fs.app.Exceptions.EventCoachOverlapException;
import jwzp_ww_fs.app.Exceptions.EventDoesNotExistException;
import jwzp_ww_fs.app.Exceptions.EventNoSuchClubException;
import jwzp_ww_fs.app.Exceptions.EventNoSuchCoachException;
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
        if (!existsCoachForEvent(event)) throw new EventNoSuchCoachException();
        if (existsSimultaniousEventWithCoach(event, null)) throw new EventCoachOverlapException();
        if (!clubsService.isEventInClubOpeningHours(event)) throw new EventNotInOpeningHoursException();
        if (!isEventCorrectLength(event)) throw new EventTooLongException();

        clubsService.addEventToClub(event.clubId());
        clubsService.setFillLevel(event.clubId(), getMinimalOpeningHoursForClub(event));
        coachesService.addEventForCoach(event.coachId());

        return repository.save(event);
    }

    private Map<DayOfWeek, OpeningHours> getMinimalOpeningHoursForClub(Event eventToAdd) {
        var clubEvents = Stream.concat(Stream.of(eventToAdd), getEventsByClub(eventToAdd.clubId()).stream()).toList();

        var result = new HashMap<DayOfWeek, OpeningHours>();
        for (DayOfWeek day : DayOfWeek.values()) {
            OpeningHours openingHoursForDay = getDayMinOpeningHours(day, clubEvents);
            if (openingHoursForDay != null) result.put(day, openingHoursForDay);
        }
        return result;
    }

    private OpeningHours getDayMinOpeningHours(DayOfWeek day, List<Event> events) {
        var pervDay = events.stream().filter(e -> e.day().equals(day.minus(1)));
        var currDay = events.stream().filter(e -> e.day().equals(day)).toList();

        LocalTime minBeg, minEnd;
        if (pervDay.anyMatch(this::isEventOverMidnight)) minBeg = LocalTime.MIDNIGHT;
        else {
            if (currDay.size() == 0) return null;
            minBeg = LocalTime.MAX;
            for(Event e : currDay) {
                if (minBeg.isAfter(e.time())) minBeg = e.time();
            }
        }

        if (currDay.stream().anyMatch(this::isEventOverMidnight)) minEnd = LocalTime.MIDNIGHT;
        else {
            minEnd = LocalTime.MIN;
            for(Event e : currDay) {
                if (minEnd.isBefore(e.time().plus(e.duration()))) minEnd = e.time().plus(e.duration());
            }
        }

        return new OpeningHours(minBeg, minEnd);
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
            var fromCurr = eventsSameDay.filter(e -> e.time().plus(e.duration()).isAfter(beg) || e.time().isAfter(beg)).findAny().isPresent();
            var fromCurr2 = eventsSameDayOvernight.toList().size() > 0;
            return fromPrev || fromNext || fromCurr || fromCurr2;
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
        // Event removedEvent = repository.removeEventWithId(eventId);

        // if (removedEvent == null)
        //     throw new EventDoesNotExistException();
        // return removedEvent;
        Optional<Event> eventToRemove = repository.findById(eventId);

        if (eventToRemove.isEmpty()) throw new EventDoesNotExistException();

        Event removedEvent = eventToRemove.get();

        repository.deleteById(eventId);
        return removedEvent;
    }

    public List<Event> removeAllEvents() {
        var allEvents = getAllEvents();

        for (Event e : allEvents) {
            clubsService.subtractEventFromClub(e.clubId());
            clubsService.setFillLevel(e.clubId(), new HashMap<DayOfWeek, OpeningHours>());
            coachesService.subtractEventFromCoach(e.coachId());
        }

        var removedEvents = repository.findAll();

        repository.deleteAll();

        return removedEvents;
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

        Event eventToUpdate = repository.getById(eventId);
        eventToUpdate.updateData(event);
        repository.save(eventToUpdate);

        return currentEventWithId;
    }

    public List<Event> getAllEvents() {
        return repository.findAll();
    }

    public List<Event> getEventsByCoach(int coachId) {
        // return repository.findAll().stream().filter(c -> c.coachId() == coachId).toList();
        return repository.findEventByCoachId(coachId);
    }

    public List<Event> getEventsByClub(int clubId) {
        // return repository.findAll().stream().filter(c -> c.clubId() == clubId).toList();
        return repository.findEventByClubId(clubId);
    }

    public List<Event> getEventsByCoachAndClub(int coachId, int clubId) {
        // return repository.findAll().stream().filter(c -> c.coachId() == coachId && c.clubId() == clubId).toList();
        return repository.findEventByClubIdAndCoachId(clubId, coachId);
    }

    public Event getEvent(int id) {
        Optional<Event> event = repository.findById(id);
        return event.isPresent() ? event.get() : null;
    }

    public Page<Event> getPage(Pageable p, Optional<Integer> clubId, Optional<Integer> coachId) {
        if (clubId.isPresent() && coachId.isPresent()) return repository.findEventByClubIdAndCoachId(p, clubId.get(), coachId.get());
        if (clubId.isPresent() && coachId.isEmpty()) return repository.findEventByClubId(p, clubId.get());
        if (clubId.isEmpty() && coachId.isPresent()) return repository.findEventByCoachId(p, coachId.get());
        return repository.findAll(p);
    }
}