package jwzp_ww_fs.app.services;

import jwzp_ww_fs.app.Exceptions.ClubHasEventsException;
import jwzp_ww_fs.app.Exceptions.CoachHasEventsException;
import jwzp_ww_fs.app.models.Coach;
import jwzp_ww_fs.app.repositories.CoachRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoachesService {
    CoachRepository repository;
    @Autowired
    public CoachesService(CoachRepository repository) {
        this.repository = repository;
    }

    synchronized void addEventForCoach(int coachId) {
        Coach coach = repository.getById(coachId);
        coach.addEvent();
        repository.save(coach);
    }

    synchronized void subtractEventFromCoach(int coachId) {
        Coach coach = repository.getById(coachId);
        coach.subEvent();
        repository.save(coach);
    }

    synchronized void deleteAllEvents() {
        List<Coach> coaches = repository.findAll();
        for (var coach : coaches) {
            coach.deleteEvents();
            repository.save(coach);
        }
    }

    synchronized public Coach addCoach(Coach coach) {
        return repository.save(coach);
    }

    public Coach patchCoach(int coachId, Coach coach) {
        var coachToUpdate = repository.findById(coachId).orElse(null);
        if (coachToUpdate == null) return null;

        coachToUpdate.updateData(coach);
        return repository.save(coachToUpdate);
    }

    synchronized public Coach removeCoach(int coachId) throws CoachHasEventsException {
        Coach coach = repository.getById(coachId);
        if (!coach.isEmpty()) throw new CoachHasEventsException();

        repository.deleteById(coachId);
        return coach;
    }

    public List<Coach> removeAllCoaches() throws ClubHasEventsException {
        List<Coach> coaches = repository.findAll();
        for (var coach : coaches)
            if (!coach.isEmpty()) throw new ClubHasEventsException();

        repository.deleteAll();
        return null;
    }

    public List<Coach> getAllCoaches() {
        return repository.findAll();
    }

    public Coach getCoach(int coachId) {
        return repository.findById(coachId).orElse(null);
    }
}
