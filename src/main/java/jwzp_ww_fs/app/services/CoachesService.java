package jwzp_ww_fs.app.services;

import jwzp_ww_fs.app.Exceptions.ClubHasEventsException;
import jwzp_ww_fs.app.Exceptions.CoachHasEventsException;
import jwzp_ww_fs.app.models.Coach;
import jwzp_ww_fs.app.repositories.CoachesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CoachesService {
    CoachesRepository repository;

    Map<Integer, Integer> numberOfEventsForCoaches;

    @Autowired
    public CoachesService(CoachesRepository repository) {
        this.repository = repository;
        numberOfEventsForCoaches = new HashMap<>();
    }

    synchronized boolean addEventForCoach(int coachId) {
        if (!numberOfEventsForCoaches.containsKey(coachId)) return false;

        numberOfEventsForCoaches.replace(coachId, numberOfEventsForCoaches.get(coachId) + 1);
        return true;
    }

    synchronized boolean subtractEventFromCoach(int coachId) {
        if (!numberOfEventsForCoaches.containsKey(coachId)) return false;
        if (numberOfEventsForCoaches.get(coachId) <= 0) return false;

        numberOfEventsForCoaches.replace(coachId, numberOfEventsForCoaches.get(coachId) - 1);
        return true;
    }

    public Coach addCoach(Coach coach) {
        // var id = repository.getNextId();
        // var out = repository.addCoach(coach);

        // numberOfEventsForCoaches.put(id, 0);

        // return out;
        return repository.save(coach);
        // return repository.save(new Coach(coach.firstName(), coach.lastName(), coach.yearOfBirth()));
    }

    public Coach patchCoach(int coachId, Coach coach) {
        // return repository.patchCoachWithId(coachId, coach);
        Coach coachToUpdate = repository.getById(coachId);
        
        coachToUpdate.updateData(coach);
        
        return repository.save(coachToUpdate);
    }

    public Coach removeCoach(int coachId) /*throws CoachHasEventsException*/ {
        // if (numberOfEventsForCoaches.get(coachId) > 0) throw new CoachHasEventsException();
        // numberOfEventsForCoaches.remove(coachId);

        // return repository.removeCoachWithId(coachId);
        repository.deleteById(coachId);
        return null;
    }

    public List<Coach> removeAllCoaches() throws ClubHasEventsException {
        // for (var key : numberOfEventsForCoaches.keySet())
        //     if (numberOfEventsForCoaches.get(key) > 0) throw new ClubHasEventsException();

        // numberOfEventsForCoaches = new HashMap<>();

        // return repository.removeAllCoaches();
        repository.deleteAll();
        return null;
    }

    public List<Coach> getAllCoaches() {
        return repository.findAll();
        // return repository.getAllCoaches();
    }

    public Coach getCoach(int coachId) {
        // return repository.getCoach(coachId);
        return repository.findById(coachId).isPresent() ? repository.findById(coachId).get() : null;
    }
}
