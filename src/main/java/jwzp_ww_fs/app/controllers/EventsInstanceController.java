package jwzp_ww_fs.app.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jwzp_ww_fs.app.util.DefaultValues;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jwzp_ww_fs.app.Exceptions.GymException;
import jwzp_ww_fs.app.models.EventInstance;
import jwzp_ww_fs.app.models.EventInstanceData;
import jwzp_ww_fs.app.services.EventsInstancesService;

@RestController
@RequestMapping({ "/api/v1/events", "/api/events" })
@Tag(name = "Events", description = "events that are organized in clubs by coaches")
public class EventsInstanceController {

    private final EventsInstancesService service;
    private final DefaultValues defaultValues;

    Logger logger = LoggerFactory.getLogger(EventsInstanceController.class);

    @Autowired
    public EventsInstanceController(EventsInstancesService service, DefaultValues defaultValues) {
        this.service = service;
        this.defaultValues = defaultValues;
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = EventInstance.class)))
            }, responseCode = "200", description = "Correctly returned all events")
    })
    @GetMapping("")
    public ResponseEntity<?> getAllEventInstances(
            @Parameter(description = "How to divide return data into pages") Pageable p,
            @Parameter(description = "Date in the format yyyy-mm-dd to search by") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Optional<LocalDate> date,
            @Parameter(description = "ID of club to narrow search") @RequestParam Optional<Integer> clubId) {
        var out = service.getEventsByParams(p, date, clubId);

        if (p.equals(defaultValues.defaultPageable)) {
            logger.info("Returned list of all events with clubId {} and date {} (no paging)",
                    clubId.orElse(-1), date.orElse(LocalDate.MIN));
            return new ResponseEntity<>(out.getContent(), HttpStatus.OK);
        }

        logger.info("Returned list of all events with clubId {} and date {} (paging)",
                clubId.orElse(-1), date.orElse(LocalDate.MIN));
        return new ResponseEntity<>(out, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public EventInstance getAllEventInstances(@PathVariable long id) {
        logger.info("Returned event with id {}", id);
        return service.getEventInstanceWithId(id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateEventInstance(@PathVariable long id,
            @org.springframework.web.bind.annotation.RequestBody EventInstanceData newData) {
        try {
            var updated = service.updateEventInstance(id, newData);
            logger.info("Updated event with id {}", id);
            return ResponseEntity.ok().body(updated);
        } catch (GymException ex) {
            logger.info("Could not update event with id {}", id);
            return ResponseEntity.badRequest().body(ex.getErrorInfo());
        }
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> updateEventInstance(@PathVariable long id) {
        try {
            var signed = service.signUpForEvent(id, LocalDate.now());
            logger.info("Signed person up for event with id {}", id);
            return ResponseEntity.ok().body(signed);
        } catch (GymException ex) {
            logger.info("Could not sign person up for event with id {}", id);
            return ResponseEntity.badRequest().body(ex.getErrorInfo());
        }
    }

    @DeleteMapping("")
    public List<EventInstance> deleteAllEventInstances() {
        logger.info("Removed all events");
        return service.removeAllEvents();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEventInstance(@PathVariable long id) {
        try {
            var removed = service.removeEvent(id);
            logger.info("Removed event with id {}", id);
            return ResponseEntity.ok().body(removed);
        } catch (GymException ex) {
            logger.info("Could not remove event with id {}", id);
            return ResponseEntity.badRequest().body(ex.getErrorInfo());
        }
    }

    // @ApiResponses(value = {
    // @ApiResponse(content = {
    // @Content(mediaType = "application/json", array = @ArraySchema(schema =
    // @Schema(implementation = Event.class)))
    // }, responseCode = "200", description = "Correctly returned all events")
    // })
    // @GetMapping("")
    // public List<Event> getEventWithCoachAndClub(
    // @Parameter(required = false, description = "ID of coach to narrow search")
    // @RequestParam Optional<Integer> coachId,
    // @Parameter(required = false, description = "ID of club to narrow search")
    // @RequestParam Optional<Integer> clubId) {

    // if (coachId.isEmpty() && clubId.isEmpty())
    // return service.getAllEvents();
    // else if (coachId.isEmpty() && clubId.isPresent())
    // return service.getEventsByClub(clubId.get());
    // else if (coachId.isPresent() && clubId.isEmpty())
    // return service.getEventsByCoach(coachId.get());
    // return service.getEventsByCoachAndClub(coachId.get(), clubId.get());
    // }

    // @ApiResponses(value = {
    // @ApiResponse(content = {
    // @Content(mediaType = "application/json", array = @ArraySchema(schema =
    // @Schema(implementation = Event.class)))
    // }, responseCode = "200", description = "Succesfully added event to database
    // and returned it"),
    // })
    // @PostMapping("")
    // public ResponseEntity<?> addEvent(
    // @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
    // description = "Information about event to add", content = @Content(mediaType
    // = "application/json", schema = @Schema(implementation = Event.class)))
    // @org.springframework.web.bind.annotation.RequestBody Event event) {
    // try {
    // service.addEvent(event);
    // return ResponseEntity.ok().body(event);
    // } catch (GymException ex) {
    // return ResponseEntity.badRequest().body(ex.getErrorInfo());
    // }
    // }

    // @ApiResponses(value = {
    // @ApiResponse(content = {
    // @Content(mediaType = "application/json", array = @ArraySchema(schema =
    // @Schema(implementation = Event.class)))
    // }) })
    // @DeleteMapping("")
    // public List<Event> removeAllEvents() {
    // return service.removeAllEvents();
    // }

    // @ApiResponses(value = {
    // @ApiResponse(content = {
    // @Content(mediaType = "application/json", array = @ArraySchema(schema =
    // @Schema(implementation = Event.class)))
    // }, responseCode = "200", description = "Returned event with specified ID or
    // nothing if there is no event with such ID"),
    // })
    // @GetMapping("/{id}")
    // public Event getEvent(
    // @Parameter(required = true, description = "ID of event to get", in =
    // ParameterIn.PATH) @PathVariable int id) {
    // return service.getEvent(id);
    // }

    // @ApiResponses(value = {
    // @ApiResponse(content = {
    // @Content(mediaType = "application/json", array = @ArraySchema(schema =
    // @Schema(implementation = Event.class)))
    // }, responseCode = "200", description = "Correctly returned deleted event"),
    // @ApiResponse(content = {
    // @Content(mediaType = "application/json", array = @ArraySchema(schema =
    // @Schema(implementation = ExceptionInfo.class)))
    // }, responseCode = "400", description = "Error occured while trying to remove
    // event (eg. no event with such ID)")
    // })
    // @DeleteMapping("/{id}")
    // public ResponseEntity<?> deleteEvent(
    // @Parameter(required = true, description = "ID of event to delete")
    // @PathVariable int id) {
    // try {
    // return ResponseEntity.ok().body(service.removeEvent(id));
    // } catch (EventDoesNotExistException ex) {
    // return ResponseEntity.badRequest().body(ex.getErrorInfo());
    // }
    // }

    // @ApiResponses(value = {
    // @ApiResponse(content = {
    // @Content(mediaType = "application/json", array = @ArraySchema(schema =
    // @Schema(implementation = Event.class)))
    // }, responseCode = "200", description = "Correctly updated specified event and
    // returned old version"),
    // @ApiResponse(content = {
    // @Content(mediaType = "application/json", array = @ArraySchema(schema =
    // @Schema(implementation = ExceptionInfo.class)))
    // }, responseCode = "400", description = "Could not perform update becaouse
    // spefified event does not exist")
    // })
    // @PatchMapping("/{id}")
    // public ResponseEntity<?> updateEvent(
    // @Parameter(required = true, description = "ID of event to update")
    // @PathVariable int id,
    // @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
    // description = "Information about coach to add", content = @Content(mediaType
    // = "application/json", schema = @Schema(implementation = Event.class)))
    // @org.springframework.web.bind.annotation.RequestBody Event event) {
    // try {
    // return ResponseEntity.ok().body(service.updateEvent(id, event));
    // } catch (GymException ex) {
    // return ResponseEntity.badRequest().body(ex.getErrorInfo());
    // }
    // }

    // @ApiResponses(value = {
    // @ApiResponse(content = {
    // @Content(mediaType = "application/json", array = @ArraySchema(schema =
    // @Schema(implementation = Page.class)))
    // }, responseCode = "200", description = "Correctly return page of coaches")
    // })
    // @GetMapping("/page")
    // public Page<Event> getEventsPaged(
    // @Parameter(required = false, description = "ID of coach to narrow search")
    // @RequestParam Optional<Integer> coachId,
    // @Parameter(required = false, description = "ID of club to narrow search")
    // @RequestParam Optional<Integer> clubId,
    // @Parameter(required = false, description = "data for paging") Pageable p) {
    // return service.getPage(p, clubId, coachId);
    // }
}