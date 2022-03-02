package jwzp_ww_fs.app.controllers;

import jwzp_ww_fs.app.models.Coach;
import jwzp_ww_fs.app.services.CoachesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CoachesController {

    private final CoachesService service;

    @Autowired
    public CoachesController(CoachesService service) {
        this.service = service;
    }

    @GetMapping("/api/coaches")
    public List<Coach> getAllCoaches() {
        return service.getAllCoaches();
    }

    @GetMapping("/api/coaches/{coachId}")
    public Coach getCoach(@PathVariable int coachId) {
        return service.getCoach(coachId);
    }

    @PostMapping("/api/coaches")
    public Coach addCoach(@RequestBody Coach coach) {
        return service.addCoach(coach);
    }

    @DeleteMapping("/api/coaches/{coachId}")
    public Coach removeCoach(@PathVariable int coachId) {
        return service.removeCoach(coachId);
    }

    @PatchMapping("/api/coaches/{coachId}")
    public Coach patchCoach(@PathVariable int coachId, @RequestBody Coach coach) {
        return service.patchCoach(coachId, coach);
    }
}
