package jwzp_ww_fs.app.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import jwzp_ww_fs.app.models.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    List<Schedule> findScheduleByCoachId(int coachId);
    List<Schedule> findScheduleByClubId(int clubId);
    List<Schedule> findScheduleByClubIdAndCoachId(int clubId, int coachId);
    Page<Schedule> findScheduleByCoachId(Pageable p, int coachId);
    Page<Schedule> findScheduleByClubId(Pageable p, int coachId);
    Page<Schedule> findScheduleByClubIdAndCoachId(Pageable p, int clubId, int coachId);
}