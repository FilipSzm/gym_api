package jwzp_ww_fs.app.controllers;

import jwzp_ww_fs.app.Exceptions.ClubHasEventsException;
import jwzp_ww_fs.app.Exceptions.GymException;
import jwzp_ww_fs.app.models.Coach;
import jwzp_ww_fs.app.util.DefaultValues;
import jwzp_ww_fs.app.models.ExceptionInfo;
import jwzp_ww_fs.app.services.CoachesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping({"/api/v1/coaches", "/api/v2/coaches", "api/coaches"})
@Tag(name = "Coaches", description = "Coaches responsible for conducting events in clubs")
public class CoachesController {

    private final CoachesService service;
    private final DefaultValues defaultValues;

    @Autowired
    public CoachesController(CoachesService service, DefaultValues defaultValues) {
        this.service = service;
        this.defaultValues = defaultValues;
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Coach.class)))
            }, responseCode = "200", description = "Correctly returned coaches")
    })
    @GetMapping("")
    public ResponseEntity<?> getAllCoaches(@Parameter(description = "data for paging") Pageable p) {
        if (p.equals(defaultValues.defaultPageable))
            return new ResponseEntity<>(service.getAllCoaches(), HttpStatus.OK);

        return new ResponseEntity<>(service.getAllCoaches(p), HttpStatus.OK);
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Coach.class))),
            }, responseCode = "200", description = "Returned coach with specified ID or nothing if there is no coach with such ID"),
    })
    @GetMapping("/{coachId}")
    public Coach getCoach(
            @Parameter(required = true, description = "ID of coach to get", in = ParameterIn.PATH) @PathVariable int coachId) {
        return service.getCoach(coachId);
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Coach.class)))
            }, responseCode = "200", description = "Succesfully added coach to database and returned it"),
    })
    @PostMapping("")
    public Coach addCoach(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Information about coach to add", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Coach.class))) @org.springframework.web.bind.annotation.RequestBody Coach coach) {
        return service.addCoach(coach);
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Coach.class)))
            }, responseCode = "200", description = "Correctly returned deleted coach"),
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ExceptionInfo.class)))
            }, responseCode = "400", description = "Error occured while trying to remove coach (eg. no coach with such ID)")
    })
    @DeleteMapping("/{coachId}")
    public ResponseEntity<?> removeCoach(
            @Parameter(required = true, description = "ID of coach to delete") @PathVariable int coachId) {
        Coach deleted;
        try {
            deleted = service.removeCoach(coachId);
        } catch (GymException e) {
            return ResponseEntity.badRequest().body(e.getErrorInfo());
        }

        if (deleted == null)
            return ResponseEntity.badRequest().body("Could not remove coach with that ID");
        return ResponseEntity.ok().body(deleted);
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Coach.class)))
            }, responseCode = "200", description = "Correctly deleted all coaches and returned deleted coaches"),
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ExceptionInfo.class)))
            }, responseCode = "400", description = "Error occured due to coach being assigned to some event")
    })
    @DeleteMapping("")
    public ResponseEntity<?> removeAllCoaches() {
        try {
            return ResponseEntity.ok().body(service.removeAllCoaches());
        } catch (ClubHasEventsException e) {
            return ResponseEntity.badRequest().body(e.getErrorInfo());
        }
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Coach.class)))
            }, responseCode = "200", description = "Correctly updated specified coach and returned old version"),
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ExceptionInfo.class)))
            }, responseCode = "400", description = "Could not perform update becaouse spefified coach does not exist")
    })
    @PatchMapping("/{coachId}")
    public ResponseEntity<?> patchCoach(
            @Parameter(required = true, description = "ID of coach to update") @PathVariable int coachId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Information about coach to add", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Coach.class))) @org.springframework.web.bind.annotation.RequestBody Coach coach) {
        Coach pached = service.patchCoach(coachId, coach);

        if (pached == null)
            return ResponseEntity.badRequest().body("Could not update coach with that ID");
        return ResponseEntity.ok().body(pached);
    }
}