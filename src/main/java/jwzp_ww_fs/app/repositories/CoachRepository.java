package jwzp_ww_fs.app.repositories;

import jwzp_ww_fs.app.models.Coach;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CoachRepository extends JpaRepository<Coach, Integer> {}