package jwzp_ww_fs.app.repositories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Repository;

import jwzp_ww_fs.app.models.Event;

@Repository
public class EventsRepository {
    AtomicInteger nextId = new AtomicInteger();
    private Map<Integer, Event> allEvents = new HashMap<>();

    public Event addEvent(Event event) {
        if (event == null) return null;

        return allEvents.put(nextId.incrementAndGet(), event);
    }

    public Event removeEventWithId(int eventId) {
        return allEvents.remove(eventId);
    }

    public List<Event> getAllEvents() {
        return allEvents.values().stream().toList();
    }

    public Event getEvent(int id) {
        return allEvents.get(id);
    }
}
