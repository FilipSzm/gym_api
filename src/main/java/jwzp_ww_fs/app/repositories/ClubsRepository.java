package jwzp_ww_fs.app.repositories;

import jwzp_ww_fs.app.models.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ClubsRepository extends JpaRepository<Club, Integer> {}

//@Repository
//public class ClubsRepository {
//    private final AtomicInteger nextId = new AtomicInteger();
//    private final Map<Integer, Club> allClubs = new HashMap<>();
//
//    public int getNextId() {
//        return nextId.get() + 1;
//    }
//
//    public Club addClub(Club club) {
//        if (club == null) return null;
//
//        return allClubs.put(nextId.incrementAndGet(), club);
//    }
//
//    public Club patchClubWithId(int clubId, Club club) {
//        return allClubs.replace(clubId, club);
//    }
//
//    public Club removeClubWithId(int clubId) {
//        return allClubs.remove(clubId);
//    }
//
//    public List<Club> removeAllClubs() {
//        List<Club> removed = allClubs.values().stream().toList();
//
//        allClubs.clear();
//
//        return removed;
//    }
//
//    public List<Club> getAllClubs() {
//        return allClubs.values().stream().toList();
//    }
//
//    public Club getClub(int clubId) {
//        return allClubs.get(clubId);
//    }
//}
