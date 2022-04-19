package jwzp_ww_fs.app.controllers.v2;

import java.util.List;
import java.util.Optional;

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
import jwzp_ww_fs.app.models.ExceptionInfo;
import jwzp_ww_fs.app.models.v2.Schedule;
import jwzp_ww_fs.app.services.v2.ScheduleService;

@RestController
@RequestMapping({ "/api/v2/schedule", "api/schedule" })
@Tag(name = "Schedules", description = "schedules that are organized in clubs by coaches")
public class ScheduleControler {

    private ScheduleService service;

    @Autowired
    public ScheduleControler(ScheduleService service) {
        this.service = service;
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Schedule.class)))
            }, responseCode = "200", description = "Correctly returned all schedules")
    })
    @GetMapping("")
    public List<Schedule> getScheduleWithCoachAndClub(
            @Parameter(required = false, description = "ID of coach to narrow search") @RequestParam Optional<Integer> coachId,
            @Parameter(required = false, description = "ID of club to narrow search") @RequestParam Optional<Integer> clubId) {

        if (coachId.isEmpty() && clubId.isEmpty())
            return service.getAllSchedules();
        else if (coachId.isEmpty() && clubId.isPresent())
            return service.getSchedulesByClub(clubId.get());
        else if (coachId.isPresent() && clubId.isEmpty())
            return service.getSchedulesByCoach(coachId.get());
        return service.getSchedulesByCoachAndClub(coachId.get(), clubId.get());
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
            return ResponseEntity.ok().body(schedule);
        } catch (GymException ex) {
            return ResponseEntity.badRequest().body(ex.getErrorInfo());
        }
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Schedule.class)))
            }) })
    @DeleteMapping("")
    public List<Schedule> removeAllSchedules() {
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
            return ResponseEntity.ok().body(service.removeSchedule(id));
        } catch (EventDoesNotExistException ex) {
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
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Information about coach to add", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Schedule.class))) @org.springframework.web.bind.annotation.RequestBody Schedule schedule) {
        try {
            return ResponseEntity.ok().body(service.updateSchedule(id, schedule));
        } catch (GymException ex) {
            return ResponseEntity.badRequest().body(ex.getErrorInfo());
        }
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Page.class)))
            }, responseCode = "200", description = "Correctly return page of coaches")
    })
    @GetMapping("/page")
    public Page<Schedule> getSchedulesPaged(
            @Parameter(required = false, description = "ID of coach to narrow search") @RequestParam Optional<Integer> coachId,
            @Parameter(required = false, description = "ID of club to narrow search") @RequestParam Optional<Integer> clubId,
            @Parameter(required = false, description = "data for paging") Pageable p) {
        return service.getPage(p, clubId, coachId);
    }
}