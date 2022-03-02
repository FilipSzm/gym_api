package jwzp_ww_fs.app.repositories;

import jwzp_ww_fs.app.models.Club;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class ClubsRepository {
    private final AtomicInteger nextId = new AtomicInteger();
    private final Map<Integer, Club> allClubs = new HashMap<>();

    public Club addClub(Club club) {
        if (club == null) return null;

        return allClubs.put(nextId.incrementAndGet(), club);
    }

    public Club patchClubWithId(int clubId, Club club) {
        return allClubs.put(clubId, club);
    }

    public Club removeClubWithId(int clubId) {
        return allClubs.remove(clubId);
    }

    public List<Club> getAllClubs() {
        return allClubs.values().stream().toList();
    }

    public Club getClub(int clubId) {
        return allClubs.get(clubId);
    }
}
