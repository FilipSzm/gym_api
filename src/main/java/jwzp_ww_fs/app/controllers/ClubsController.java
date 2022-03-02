package jwzp_ww_fs.app.controllers;

import jwzp_ww_fs.app.models.Club;
import jwzp_ww_fs.app.models.Coach;
import jwzp_ww_fs.app.services.ClubsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ClubsController {

    private final ClubsService service;

    @Autowired
    public ClubsController(ClubsService service) {
        this.service = service;
    }

    @GetMapping("/api/clubs")
    public List<Club> getAllClubs() {
        return service.getAllClubs();
    }

    @GetMapping("/api/clubs/{clubId}")
    public Club getClub(@PathVariable int clubId) {
        return service.getClub(clubId);
    }

    @PostMapping("/api/clubs")
    public Club addClub(@RequestBody Club club) {
        return service.addClub(club);
    }

    @DeleteMapping("/api/clubs/{clubId}")
    public Club removeCoach(@PathVariable int clubId) {
        return service.removeClub(clubId);
    }

    @PatchMapping("/api/clubs/{clubId}")
    public Club patchClub(@PathVariable int clubId, @RequestBody Club club) {
        return service.patchClub(clubId, club);
    }
}
