package jwzp_ww_fs.app.controllers;

import java.util.List;
import java.util.Optional;

import jwzp_ww_fs.app.exceptions.schedule.ScheduleException;
import jwzp_ww_fs.app.util.DefaultValues;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jwzp_ww_fs.app.models.ExceptionInfo;
import jwzp_ww_fs.app.models.Schedule;
import jwzp_ww_fs.app.services.ScheduleService;

@RestController
@RequestMapping({ "/api/v1/schedule", "api/schedule" })
@Tag(name = "Schedules", description = "schedules that are organized in clubs by coaches")
public class ScheduleController {

    private final ScheduleService service;
    private final DefaultValues defaultValues;

    Logger logger = LoggerFactory.getLogger(ScheduleController.class);

    @Autowired
    public ScheduleController(ScheduleService service, DefaultValues defaultValues) {
        this.service = service;
        this.defaultValues = defaultValues;
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Schedule.class)))
            }, responseCode = "200", description = "Succesfully added schedule to database and returned it"),
    })
    @PostMapping("")
    public ResponseEntity<?> addSchedule(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Information about schedule to add", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Schedule.class))) @org.springframework.web.bind.annotation.RequestBody Schedule schedule) {
        try {
            service.addSchedule(schedule);
            logger.info("Added new item to schedule");
            return ResponseEntity.ok().body(schedule);
        } catch (ScheduleException ex) {
            logger.info("Could not add new item to schedule");
            return ResponseEntity.badRequest().body(ex.getErrorInfo());
        }
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Schedule.class)))
            }) })
    @DeleteMapping("")
    public List<Schedule> removeAllSchedules() {
        logger.info("Deleted all items from schedule");
        return service.removeAllSchedules();
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Schedule.class)))
            }, responseCode = "200", description = "Returned schedule with specified ID or nothing if there is no schedule with such ID"),
    })
    @GetMapping("/{id}")
    public Schedule getSchedule(
            @Parameter(required = true, description = "ID of schedule to get", in = ParameterIn.PATH) @PathVariable int id) {
        logger.info("Returned schedule item with id {}", id);
        return service.getSchedule(id);
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Schedule.class)))
            }, responseCode = "200", description = "Correctly returned deleted schedule"),
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ExceptionInfo.class)))
            }, responseCode = "400", description = "Error occured while trying to remove schedule (eg. no schedule with such ID)")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSchedule(
            @Parameter(required = true, description = "ID of schedule to delete") @PathVariable int id) {
        try {
            var removed = service.removeSchedule(id);
            logger.info("Deleted schedule item with id {}", id);
            return ResponseEntity.ok().body(removed);
        } catch (ScheduleException ex) {
            logger.info("Could not delete schedule item with id {}", id);
            return ResponseEntity.badRequest().body(ex.getErrorInfo());
        }
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Schedule.class)))
            }, responseCode = "200", description = "Correctly updated specified schedule and returned old version"),
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ExceptionInfo.class)))
            }, responseCode = "400", description = "Could not perform update becaouse spefified schedule does not exist")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateSchedule(
            @Parameter(required = true, description = "ID of schedule to update") @PathVariable int id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true, description = "Information about coach to add",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Schedule.class))
            ) @org.springframework.web.bind.annotation.RequestBody Schedule schedule) {
        try {
            var patched = service.updateSchedule(id, schedule);
            logger.info("Updated event with id {}", id);
            return ResponseEntity.ok().body(patched);
        } catch (ScheduleException ex) {
            logger.info("Could not update event with id {}", id);
            return ResponseEntity.badRequest().body(ex.getErrorInfo());
        }
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Page.class)))
            }, responseCode = "200", description = "Correctly returned schedules")
    })
    @GetMapping("")
    public ResponseEntity<?> getSchedulesPaged(
            @Parameter(description = "ID of coach to narrow search") @RequestParam Optional<Integer> coachId,
            @Parameter(description = "ID of club to narrow search") @RequestParam Optional<Integer> clubId,
            @Parameter(description = "data for paging") Pageable p) {
        var out = service.getPage(p, clubId, coachId);

        if (p.equals(defaultValues.defaultPageable)) {
            logger.info("Returned list of all schedule items with clubId {} and coachId {} (no paging)",
                    clubId.orElse(-1), coachId.orElse(-1));
            return new ResponseEntity<>(out.getContent(), HttpStatus.OK);
        }

        logger.info("Returned list of all schedule items with clubId {} and coachId {} (paging)", clubId.orElse(-1),
                coachId.orElse(-1));
        return new ResponseEntity<>(out, HttpStatus.OK);
    }
}