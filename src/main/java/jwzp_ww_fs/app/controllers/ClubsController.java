package jwzp_ww_fs.app.controllers;


import jwzp_ww_fs.app.Exceptions.GymException;
import jwzp_ww_fs.app.models.Club;

import jwzp_ww_fs.app.services.ClubsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/clubs")
@Tag(name = "Clubs", description = "Clubs where coaches conduct events")
public class ClubsController {

    private final ClubsService service;

    @Autowired
    public ClubsController(ClubsService service) {
        this.service = service;
    }

    @GetMapping("")
    public List<Club> getAllClubs() {
        return service.getAllClubs();
    }

    @GetMapping("/{clubId}")
    public Club getClub(@PathVariable int clubId) {
        return service.getClub(clubId);
    }

    @PostMapping("")
    public Club addClub(@RequestBody Club club) {
        return service.addClub(club);
    }

    @DeleteMapping("/{clubId}")
    public ResponseEntity<?> removeCoach(@PathVariable int clubId) {
        Club removed;
        try {
            removed = service.removeClub(clubId);
        } catch (GymException e) {
            return ResponseEntity.badRequest().body(e.getErrorInfo());
        }

        if (removed == null) return ResponseEntity.badRequest().body("Could not remove club with that ID");
        return ResponseEntity.ok().body(removed);
    }

    @DeleteMapping("")
    public ResponseEntity<?> removeAllClubs() {
        try {
            return ResponseEntity.ok().body(service.removeAllClubs());
        } catch (GymException e) {
            return ResponseEntity.badRequest().body(e.getErrorInfo());
        }
    }

    @PatchMapping("/{clubId}")
    public ResponseEntity<?> patchClub(@PathVariable int clubId, @RequestBody Club club) {
        Club patched;
        try {
            patched = service.patchClub(clubId, club);
        } catch (GymException e) {
            return ResponseEntity.badRequest().body(e.getErrorInfo());
        }

        if (patched == null) return ResponseEntity.badRequest().body("Could not update club with that ID");
        return ResponseEntity.ok().body(patched);
    }
}
