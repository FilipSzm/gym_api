package jwzp_ww_fs.app.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import jwzp_ww_fs.app.exceptions.event.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jwzp_ww_fs.app.models.EventInstance;
import jwzp_ww_fs.app.models.EventInstanceData;
import jwzp_ww_fs.app.models.Schedule;
import jwzp_ww_fs.app.repositories.EventsInstancesRepository;

// public List<Event> getAllEvents() {
//     return repository.findAll();
// }

// public List<Event> getEventsByCoach(int coachId) {
//     // return repository.findAll().stream().filter(c -> c.coachId() == coachId).toList();
//     return repository.findEventByCoachId(coachId);
// }

// public List<Event> getEventsByClub(int clubId) {
//     // return repository.findAll().stream().filter(c -> c.clubId() == clubId).toList();
//     return repository.findEventByClubId(clubId);
// }

// public List<Event> getEventsByCoachAndClub(int coachId, int clubId) {
//     // return repository.findAll().stream().filter(c -> c.coachId() == coachId && c.clubId() == clubId).toList();
//     return repository.findEventByClubIdAndCoachId(clubId, coachId);
// }

// public Event getEvent(int id) {
//     Optional<Event> event = repository.findById(id);
//     return event.isPresent() ? event.get() : null;
// }

// public Page<Event> getPage(Pageable p, Optional<Integer> clubId, Optional<Integer> coachId) {
//     if (clubId.isPresent() && coachId.isPresent()) return repository.findEventByClubIdAndCoachId(p, clubId.get(), coachId.get());
//     if (clubId.isPresent() && coachId.isEmpty()) return repository.findEventByClubId(p, clubId.get());
//     if (clubId.isEmpty() && coachId.isPresent()) return repository.findEventByCoachId(p, coachId.get());
//     return repository.findAll(p);
// }
// }

@Service
public class EventsInstancesService {
    private LocalDate lastGeneratedInstancesDate = null;
    private final int GENERATED_DAYS = 30;

    EventsInstancesRepository repository;

    ClubsService clubsService;
    CoachesService coachesService;
    ScheduleService scheduleService;

    @Autowired
    public EventsInstancesService(
            EventsInstancesRepository repository,
            ClubsService clubsService,
            CoachesService coachesService,
            ScheduleService scheduleService
    ) {
        this.repository = repository;
        this.clubsService = clubsService;
        this.coachesService = coachesService;
        this.scheduleService = scheduleService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void generateEventInstances() {
        generateEvents(LocalDate.now(), GENERATED_DAYS);
        deleteOldEvents(LocalDate.now(), GENERATED_DAYS);
    }

    private void generateEvents(LocalDate today, int daysAhead) {
        List<Schedule> allSchedules = scheduleService.getAllSchedules();

        for (int i = 0; i <= daysAhead; i++) {
            LocalDate date = today.plusDays(i);

            if (lastGeneratedInstancesDate != null && !lastGeneratedInstancesDate.isBefore(date)) {
                continue;
            }

            lastGeneratedInstancesDate = date;

            DayOfWeek dow = date.getDayOfWeek();
            allSchedules.stream().filter(s -> s.day().equals(dow)).forEach(
                    s -> addEvent(new EventInstance(s, date)));
        }
    }

    private void deleteOldEvents(LocalDate today, int daysBackTreshold) {
        repository.deleteEventByDateBefore(today.minusDays(daysBackTreshold));
    }

    public EventInstance addEvent(EventInstance event) {
        return repository.save(event);
    }

    public EventInstance removeEvent(long eventId) throws NonExistingEventException {
        Optional<EventInstance> eventToRemove = repository.findById(eventId);

        if (eventToRemove.isEmpty())
            throw new NonExistingEventException();

        EventInstance removedEvent = eventToRemove.get();

        repository.deleteById(eventId);
        return removedEvent;
    }

    public List<EventInstance> removeAllEvents() {
        var removedEvents = repository.findAll();

        repository.deleteAll();

        return removedEvents;
    }

    @Transactional
    public EventInstance signUpForEvent(long eventId, LocalDate today) throws EventException {
        Optional<EventInstance> eventToUpdate = repository.findById(eventId);

        if (eventToUpdate.isEmpty())
            throw new NonExistingEventException();

        EventInstance updatedEvent = eventToUpdate.get();

        if (updatedEvent.participants() >= updatedEvent.capacity()) {
            throw new FilledEventException();
        } else if (updatedEvent.date().isBefore(today)) {
            throw new ConcludedEventException();
        }

        repository.incrementParticipantsForEvent(eventId);

        return updatedEvent;
    }

    @Transactional
    public EventInstance updateEventInstance(long eventId, EventInstanceData data) throws EventException {
        Optional<EventInstance> eventToUpdate = repository.findById(eventId);

        if (eventToUpdate.isEmpty())
            throw new NonExistingEventException();

        EventInstance updatedEvent = eventToUpdate.get();

        if (data.capacity() < updatedEvent.participants()) {
            throw new NonSufficientCapacityException();
        }

        EventInstance tempEvent = new EventInstance("", data.date(), data.time(), updatedEvent.duration(), -1,
                updatedEvent.clubId(),
                updatedEvent.coachId());

        if (existsSimultaneousEventWithCoach(tempEvent, updatedEvent))
            throw new AlreadyAssignedCoachException();
        if (!clubsService.isEventInstanceInClubOpeningHours(tempEvent))
            throw new ProtrudingEventException();

        repository.setDateAndTimeOfEvent(eventId, data.date(), data.time());

        repository.setCapacityForEvent(eventId, data.capacity());

        return updatedEvent;
    }

    public EventInstance getEventInstanceWithId(long id) {
        return repository.findById(id).orElse(null);
    }

    public Page<EventInstance> getEventsByParams(Pageable p, Optional<LocalDate> date, Optional<Integer> clubId) {
        if (date.isEmpty() && clubId.isEmpty())
            return repository.findAll(p);
        else if (date.isEmpty())
            return repository.findEventByClubId(p, clubId.get());
        else if (clubId.isEmpty())
            return repository.findEventByDate(p, date.get());
        else
            return repository.findEventByClubIdAndDate(p, clubId.get(), date.get());
    }

    private boolean existsSimultaneousEventWithCoach(EventInstance eventToAdd, EventInstance eventToIgnore) {
        var otherEventsWithCoach = repository.findEventByCoachId(eventToAdd.coachId());
        if (eventToIgnore != null) {
            otherEventsWithCoach = otherEventsWithCoach.stream().filter(e -> !e.equals(eventToIgnore)).toList();
        }

        LocalTime beg = eventToAdd.time();
        LocalTime end = eventToAdd.time().plus(eventToAdd.duration());

        var eventsPrevDay = otherEventsWithCoach.stream().filter(e -> e.date().equals(eventToAdd.date().minusDays(1)))
                .filter(this::isEventOverMidnight);
        var eventsSameDayOvernight = otherEventsWithCoach.stream()
                .filter(e -> e.date().equals(eventToAdd.date().minusDays(1)))
                .filter(this::isEventOverMidnight);
        var eventsSameDay = otherEventsWithCoach.stream().filter(e -> e.date().equals(eventToAdd.date()))
                .filter(this::isEventNotOverMidnight);
        var eventsNextDay = otherEventsWithCoach.stream().filter(e -> e.date().equals(eventToAdd.date().plusDays(1)));
        if (beg.isBefore(end)) {
            var fromPrev = eventsPrevDay.anyMatch(e -> e.time().plus(e.duration()).isAfter(beg));
            var fromCurrOvernight = eventsSameDayOvernight.anyMatch(e -> e.time().isBefore(end));
            var fromCurr = eventsSameDay
                    .anyMatch(e -> isDuringEvent(eventToAdd, e.time())
                            || isDuringEvent(eventToAdd, e.time().plus(e.duration()))
                            || (e.time().isBefore(beg) && e.time().plus(e.duration()).isAfter(end)));

            return fromPrev || fromCurrOvernight || fromCurr;
        } else {
            var fromPrev = eventsPrevDay.anyMatch(e -> e.time().plus(e.duration()).isAfter(beg));
            var fromNext = eventsNextDay.anyMatch(e -> e.time().isBefore(end));
            var fromCurr = eventsSameDay.anyMatch(e -> e.time().plus(e.duration()).isAfter(beg) || e.time().isAfter(beg));
            var fromCurr2 = eventsSameDayOvernight.toList().size() > 0;
            return fromPrev || fromNext || fromCurr || fromCurr2;
        }
    }

    private boolean isEventOverMidnight(EventInstance e) {
        return !e.time().plus(e.duration()).isAfter(e.time());
    }

    private boolean isEventNotOverMidnight(EventInstance e) {
        return !isEventOverMidnight(e);
    }

    private boolean isDuringEvent(EventInstance e, LocalTime t) {
        return t.isAfter(e.time()) && t.isBefore(e.time().plus(e.duration()));
    }

    // private Map<DayOfWeek, OpeningHours> getMinimalOpeningHoursForClub(Event
    // eventToAdd) {
    // var clubEvents = Stream
    // .concat(Stream.of(eventToAdd),
    // repository.findEventByClubId(eventToAdd.clubId()).stream()).toList();

    // var result = new HashMap<DayOfWeek, OpeningHours>();
    // for (DayOfWeek day : DayOfWeek.values()) {
    // OpeningHours openingHoursForDay = getDayMinOpeningHours(day, clubEvents);
    // if (openingHoursForDay != null)
    // result.put(day, openingHoursForDay);
    // }
    // return result;
    // }

    // private OpeningHours getDayMinOpeningHours(DayOfWeek day, List<Event> events)
    // {
    // var pervDay = events.stream().filter(e ->
    // e.date().getDayOfWeek().equals(day.minus(1)));
    // var currDay = events.stream().filter(e ->
    // e.date().getDayOfWeek().equals(day)).toList();

    // LocalTime minBeg, minEnd;
    // if (pervDay.anyMatch(this::isEventOverMidnight))
    // minBeg = LocalTime.MIDNIGHT;
    // else {
    // if (currDay.size() == 0)
    // return null;
    // minBeg = LocalTime.MAX;
    // for (Event e : currDay) {
    // if (minBeg.isAfter(e.time()))
    // minBeg = e.time();
    // }
    // }

    // if (currDay.stream().anyMatch(this::isEventOverMidnight))
    // minEnd = LocalTime.MIDNIGHT;
    // else {
    // minEnd = LocalTime.MIN;
    // for (Event e : currDay) {
    // if (minEnd.isBefore(e.time().plus(e.duration())))
    // minEnd = e.time().plus(e.duration());
    // }
    // }

    // return new OpeningHours(minBeg, minEnd);
    // }
}