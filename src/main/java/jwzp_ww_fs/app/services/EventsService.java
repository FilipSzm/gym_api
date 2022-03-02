package jwzp_ww_fs.app.services;

import java.util.List;

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

    public List<Event> getAllEvents() {
        return repository.getAllEvents();
    }
}