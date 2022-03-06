package jwzp_ww_fs.app.repositories;

import jwzp_ww_fs.app.models.Coach;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class CoachesRepository {
    private final AtomicInteger nextId = new AtomicInteger();
    private final Map<Integer, Coach> allCoaches = new HashMap<>();

    public Coach addCoach(Coach coach) {
        if (coach == null) return null;

        return allCoaches.put(nextId.incrementAndGet(), coach);
    }

    public Coach patchCoachWithId(int coachId, Coach coach) {
        return allCoaches.replace(coachId, coach);
    }

    public Coach removeCoachWithId(int coachId) {
        return allCoaches.remove(coachId);
    }

    public List<Coach> getAllCoaches() {
        return allCoaches.values().stream().toList();
    }

    public Coach getCoach(int coachId) {
        return allCoaches.get(coachId);
    }
}
