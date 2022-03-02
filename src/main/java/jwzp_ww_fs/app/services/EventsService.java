package jwzp_ww_fs.app.services;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jwzp_ww_fs.app.models.Event;
import jwzp_ww_fs.app.repositories.EventsRepository;

@Service
public class EventsService {
    EventsRepository repository;

    @Autowired
    public EventsService(EventsRepository repository) {
        this.repository = repository;
    }

    public Event addEvent(Event event) {
        return repository.addEvent(event);
    }

    public Event removeEvent(int eventId) {
        return repository.removeEventWithId(eventId);
    }

    public Event updateEvent(int eventId, Event event) {
        return repository.updateEvent(eventId, event);
    }

    public Map<DayOfWeek, List<Event>> getAllEvents() {
        return eventsToSchedule(repository.getAllEvents());
    }

    public Map<DayOfWeek, List<Event>> getEventsByCoach(int coachId) {
        return eventsToSchedule(repository.getAllEvents().stream().filter(c -> c.coachId() == coachId).toList());
    }

    public Map<DayOfWeek, List<Event>> getEventsByClub(int clubId) {
        return eventsToSchedule(repository.getAllEvents().stream().filter(c -> c.clubId() == clubId).toList());
    }

    public Map<DayOfWeek, List<Event>> getEventsByCoachAndClub(int coachId, int clubId) {
        return eventsToSchedule(repository.getAllEvents().stream().filter(c -> c.coachId() == coachId && c.clubId() == clubId).toList());
    }

    private Map<DayOfWeek, List<Event>> eventsToSchedule(List<Event> events) {
        Map<DayOfWeek, List<Event>> result = new TreeMap<>(); 
        
        for (var dayOfWeek : DayOfWeek.values()) {
            List<Event> eventsForDay = events.stream().filter(e -> e.day().equals(dayOfWeek)).toList();
            result.put(dayOfWeek, eventsForDay);
        }

        return result;
    }

    public Event getEvent(int id) {
        return repository.getEvent(id);
    }
}