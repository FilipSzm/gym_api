package jwzp_ww_fs.app.controllers;

import jwzp_ww_fs.app.models.Coach;
import jwzp_ww_fs.app.services.CoachesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coaches")
public class CoachesController {

    private final CoachesService service;

    @Autowired
    public CoachesController(CoachesService service) {
        this.service = service;
    }

    @GetMapping("")
    public List<Coach> getAllCoaches() {
        return service.getAllCoaches();
    }

    @GetMapping("/{coachId}")
    public Coach getCoach(@PathVariable int coachId) {
        return service.getCoach(coachId);
    }

    @PostMapping("")
    public Coach addCoach(@RequestBody Coach coach) {
        return service.addCoach(coach);
    }

    @DeleteMapping("/{coachId}")
    public ResponseEntity<?> removeCoach(@PathVariable int coachId) {
        Coach deleted = service.removeCoach(coachId);

        if (deleted == null) return ResponseEntity.badRequest().body("Could not remove coach with that ID");
        return ResponseEntity.ok().body(deleted);
    }

    @DeleteMapping("")
    public List<Coach> removeAllChaches() {
        return service.removeAllCoaches();
    }

    @PatchMapping("/{coachId}")
    public ResponseEntity<?> patchCoach(@PathVariable int coachId, @RequestBody Coach coach) {
        Coach pached = service.patchCoach(coachId, coach);

        if (pached == null) return ResponseEntity.badRequest().body("Could not update coach with that ID");
        return ResponseEntity.ok().body(pached);
    }
}
