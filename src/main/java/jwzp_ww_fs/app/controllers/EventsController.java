package jwzp_ww_fs.app.controllers;

import java.util.List;
import java.util.Optional;

import jwzp_ww_fs.app.models.Club;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jwzp_ww_fs.app.Exceptions.EventDoesNotExistException;
import jwzp_ww_fs.app.Exceptions.GymException;
import jwzp_ww_fs.app.models.Event;
import jwzp_ww_fs.app.models.ExceptionInfo;
import jwzp_ww_fs.app.services.EventsService;

@RestController
@RequestMapping({"/api/v1/events", "api/events"})
@Tag(name = "Events", description = "events that are organized in clubs by coaches")
public class EventsController {

    private EventsService service;

    @Autowired
    public EventsController(EventsService service) {
        this.service = service;
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Event.class)))
            }, responseCode = "200", description = "Correctly returned all events")
    })
    @GetMapping("")
    public List<Event> getEventWithCoachAndClub(@RequestParam Optional<Integer> coachId,
            @RequestParam Optional<Integer> clubId) {

        if (coachId.isEmpty() && clubId.isEmpty())
            return service.getAllEvents();
        else if (coachId.isEmpty() && clubId.isPresent())
            return service.getEventsByClub(clubId.get());
        else if (coachId.isPresent() && clubId.isEmpty())
            return service.getEventsByCoach(coachId.get());
        return service.getEventsByCoachAndClub(coachId.get(), clubId.get());
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Event.class)))
            }, responseCode = "200", description = "Succesfully added event to database and returned it"),
    })
    @PostMapping("")
    public ResponseEntity<?> addEvent(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Information about event to add", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Event.class))) @org.springframework.web.bind.annotation.RequestBody Event event) {
        try {
            service.addEvent(event);
            return ResponseEntity.ok().body(event);
        } catch (GymException ex) {
            return ResponseEntity.badRequest().body(ex.getErrorInfo());
        }
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Event.class)))
            }) })
    @DeleteMapping("")
    public List<Event> removeAllEvents() {
        return service.removeAllEvents();
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Event.class)))
            }, responseCode = "200", description = "Returned event with specified ID or nothing if there is no event with such ID"),
    })
    @GetMapping("/{id}")
    public Event getEvent(
            @Parameter(required = true, description = "ID of event to get", in = ParameterIn.PATH) @PathVariable int id) {
        return service.getEvent(id);
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Event.class)))
            }, responseCode = "200", description = "Correctly returned deleted event"),
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ExceptionInfo.class)))
            }, responseCode = "400", description = "Error occured while trying to remove event (eg. no event with such ID)")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(
            @Parameter(required = true, description = "ID of event to delete") @PathVariable int id) {
        try {
            return ResponseEntity.ok().body(service.removeEvent(id));
        } catch (EventDoesNotExistException ex) {
            return ResponseEntity.badRequest().body(ex.getErrorInfo());
        }
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Event.class)))
            }, responseCode = "200", description = "Correctly updated specified event and returned old version"),
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ExceptionInfo.class)))
            }, responseCode = "400", description = "Could not perform update becaouse spefified event does not exist")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateEvent(
            @Parameter(required = true, description = "ID of event to update") @PathVariable int id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Information about coach to add", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Event.class))) @org.springframework.web.bind.annotation.RequestBody Event event) {
        try {
            return ResponseEntity.ok().body(service.updateEvent(id, event));
        } catch (GymException ex) {
            return ResponseEntity.badRequest().body(ex.getErrorInfo());
        }
    }

    @GetMapping("/page")
    public Page<Event> getAll(Pageable p) {
        return service.getPage(p);
    }
}