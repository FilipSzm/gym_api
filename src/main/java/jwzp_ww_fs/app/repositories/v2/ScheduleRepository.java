package jwzp_ww_fs.app.repositories.v2;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import jwzp_ww_fs.app.models.v2.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    public List<Schedule> findScheduleByCoachId(int coachId);
    public List<Schedule> findScheduleByClubId(int clubId);
    public List<Schedule> findScheduleByClubIdAndCoachId(int clubId, int coachId);
    public Page<Schedule> findScheduleByCoachId(Pageable p, int coachId);
    public Page<Schedule> findScheduleByClubId(Pageable p, int coachId);
    public Page<Schedule> findScheduleByClubIdAndCoachId(Pageable p, int clubId, int coachId);
}