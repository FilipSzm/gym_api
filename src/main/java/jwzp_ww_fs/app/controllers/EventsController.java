package jwzp_ww_fs.app.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jwzp_ww_fs.app.models.Event;
import jwzp_ww_fs.app.services.EventsService;

@RestController
@RequestMapping("/api/events")
public class EventsController {

    private EventsService service;

    @Autowired
    public EventsController(EventsService service) {
        this.service = service;
    }

    @GetMapping("")
    public List<Event> getEventWithCoachAndClub(@RequestParam Optional<Integer> coachId, @RequestParam Optional<Integer> clubId) {

        if (coachId.isEmpty() && clubId.isEmpty()) return service.getAllEvents();
        else if (coachId.isEmpty() && clubId.isPresent()) return service.getEventsByClub(clubId.get());
        else if (coachId.isPresent() && clubId.isEmpty()) return service.getEventsByCoach(coachId.get());
        return service.getEventsByCoachAndClub(coachId.get(), clubId.get());
    }

    @PostMapping("")
    public Event addEvent(@RequestBody Event event) {
        return service.addEvent(event);
    }

    @DeleteMapping("")
    public List<Event> removeAllEvents() {
        return service.removeAllEvents();
    }

    @GetMapping("/{id}")
    public Event getEvent(@PathVariable int id) {
        return service.getEvent(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable int id) {
        Event removed = service.removeEvent(id);
        
        if (removed == null) return ResponseEntity.badRequest().body("Could not remove event with that ID");
        return ResponseEntity.ok().body(removed);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable int id, @RequestBody Event event) {
        Event changed = service.updateEvent(id, event);
        
        if (changed == null) return ResponseEntity.badRequest().body("Could not update event with that ID");
        return ResponseEntity.ok().body(changed);
    }
}