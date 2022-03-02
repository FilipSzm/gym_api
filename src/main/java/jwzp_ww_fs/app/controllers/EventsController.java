package jwzp_ww_fs.app.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jwzp_ww_fs.app.models.Event;
import jwzp_ww_fs.app.services.EventsService;

@RestController
public class EventsController {

    private EventsService service;

    @Autowired
    public EventsController(EventsService service) {
        this.service = service;
    }

    @GetMapping("/api/events")
    public List<Event> getAllEvents() {
        return service.getAllEvents();
    }

    @PostMapping("/api/events")
    public Event addEvent(@RequestBody Event event) {
        return service.addEvent(event);
    }

}