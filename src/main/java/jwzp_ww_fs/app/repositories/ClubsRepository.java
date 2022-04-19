package jwzp_ww_fs.app.repositories;

import jwzp_ww_fs.app.models.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubsRepository extends JpaRepository<Club, Integer> { }

