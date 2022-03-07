package jwzp_ww_fs.app.services;

import jwzp_ww_fs.app.models.Club;
import jwzp_ww_fs.app.repositories.ClubsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClubsService {
    ClubsRepository repository;

    @Autowired
    public ClubsService(ClubsRepository repository) {
        this.repository = repository;
    }

    public Club addClub(Club club) {
        return repository.addClub(club);
    }

    public Club patchClub(int clubId, Club club) {
        return repository.patchClubWithId(clubId, club);
    }

    public Club removeClub(int clubId) {
        return repository.removeClubWithId(clubId);
    }

    public List<Club> removeAllClubs() {
        return repository.removeAllClubs();
    }

    public List<Club> getAllClubs() {
        return repository.getAllClubs();
    }

    public Club getClub(int clubId) {
        return repository.getClub(clubId);
    }
}
