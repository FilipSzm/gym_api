package jwzp_ww_fs.app.services;

import jwzp_ww_fs.app.models.Coach;
import jwzp_ww_fs.app.repositories.CoachesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoachesService {
    CoachesRepository repository;

    @Autowired
    public CoachesService(CoachesRepository repository) {
        this.repository = repository;
    }

    public Coach addCoach(Coach coach) {
        return repository.addCoach(coach);
    }

    public Coach patchCoach(int coachId, Coach coach) {
        return repository.patchCoachWithId(coachId, coach);
    }

    public Coach removeCoach(int coachId) {
        return repository.removeCoachWithId(coachId);
    }

    public List<Coach> getAllCoaches() {
        return repository.getAllCoaches();
    }

    public Coach getCoach(int coachId) {
        return repository.getCoach(coachId);
    }
}
