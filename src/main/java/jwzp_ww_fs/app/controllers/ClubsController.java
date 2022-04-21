package jwzp_ww_fs.app.controllers;

import jwzp_ww_fs.app.Exceptions.GymException;
import jwzp_ww_fs.app.dto.ClubRepresentation;
import jwzp_ww_fs.app.models.Club;
import jwzp_ww_fs.app.models.DefaultValues;
import jwzp_ww_fs.app.models.ExceptionInfo;
import jwzp_ww_fs.app.services.ClubsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping({ "/api/v1/clubs", "/api/v2/clubs", "/api/clubs" })
@Tag(name = "Clubs", description = "Clubs where coaches conduct events")
public class ClubsController {

    private final DefaultValues defaultValues;
    private final ClubsService service;

    @Autowired
    public ClubsController(ClubsService service, DefaultValues defaultValues) {
        this.service = service;
        this.defaultValues = defaultValues;
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Club.class)))
            }, responseCode = "200", description = "Correctly returned clubs")
    })
    @GetMapping("")
    public ResponseEntity<?> getAllClubs(@Parameter(description = "data for paging") Pageable p) {
        if (p.equals(defaultValues.defaultPageable))
            return new ResponseEntity<>(service.getAllClubs(), HttpStatus.OK);

        return new ResponseEntity<>(service.getAllClubs(p), HttpStatus.OK);
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Club.class)))
            }, responseCode = "200", description = "Returned club with specified ID or nothing if there is no club with such ID"),
    })
    @GetMapping("/{clubId}")
    public Club getClub(
            @Parameter(required = true, description = "ID of club to get", in = ParameterIn.PATH) @PathVariable int clubId) {
        return service.getClub(clubId);
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Club.class)))
            }, responseCode = "200", description = "Succesfully added club to database and returned it"),
    })
    @PostMapping("")
    public Club addClub(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Information about club to add", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Club.class))) @org.springframework.web.bind.annotation.RequestBody Club club) {
        return service.addClub(club);
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Club.class)))
            }, responseCode = "200", description = "Correctly returned deleted club"),
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ExceptionInfo.class)))
            }, responseCode = "400", description = "Error occured while trying to remove club (eg. no club with such ID)")
    })
    @DeleteMapping("/{clubId}")
    public ResponseEntity<?> removeCoach(
            @Parameter(required = true, description = "ID of club to delete") @PathVariable int clubId) {
        Club removed;
        try {
            removed = service.removeClub(clubId);
        } catch (GymException e) {
            return ResponseEntity.badRequest().body(e.getErrorInfo());
        }

        if (removed == null)
            return ResponseEntity.badRequest().body("Could not remove club with that ID");
        return ResponseEntity.ok().body(removed);
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Club.class)))
            }, responseCode = "200", description = "Correctly deleted all clubs and returned deleted clubs"),
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ExceptionInfo.class)))
            }, responseCode = "400", description = "Error occured due to club being assigned to some event")
    })
    @DeleteMapping("")
    public ResponseEntity<?> removeAllClubs() {
        try {
            return ResponseEntity.ok().body(service.removeAllClubs());
        } catch (GymException e) {
            return ResponseEntity.badRequest().body(e.getErrorInfo());
        }
    }

    @ApiResponses(value = {
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Club.class)))
            }, responseCode = "200", description = "Correctly updated specified club and returned old version"),
            @ApiResponse(content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ExceptionInfo.class)))
            }, responseCode = "400", description = "Could not perform update becaouse spefified club does not exist")
    })
    @PatchMapping("/{clubId}")
    public ResponseEntity<?> patchClub(
            @Parameter(required = true, description = "ID of coach to update") @PathVariable int clubId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Information about coach to add", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Club.class))) @org.springframework.web.bind.annotation.RequestBody Club club) {
        Club patched;
        try {
            patched = service.patchClub(clubId, club);
        } catch (GymException e) {
            return ResponseEntity.badRequest().body(e.getErrorInfo());
        }

        if (patched == null)
            return ResponseEntity.badRequest().body("Could not update club with that ID");
        return ResponseEntity.ok().body(patched);
    }

    @GetMapping(value = "/hateoas", produces = "application/hal+json")
    public List<ClubRepresentation> getAllHateoas() {
        var people = service.getAllClubs();
        return people.stream().map(this::represent).collect(Collectors.toList());
    }

    private ClubRepresentation represent(Club club) {
        Link selfLink = linkTo(methodOn(ClubsController.class).getClub(club.id())).withSelfRel();
        var representation = ClubRepresentation.fromClub(club);
        representation.add(selfLink);
        return representation;
    }
}
