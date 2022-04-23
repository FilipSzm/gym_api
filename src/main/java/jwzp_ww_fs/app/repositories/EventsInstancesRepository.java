package jwzp_ww_fs.app.repositories;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jwzp_ww_fs.app.models.EventInstance;

public interface EventsInstancesRepository extends JpaRepository<EventInstance, Long> {
    public List<EventInstance> findEventByClubId(int clubId);

    public List<EventInstance> findEventByCoachId(int coachId);

    public List<EventInstance> findEventByDate(LocalDate date);

    public List<EventInstance> findEventByClubIdAndDate(int clubId, LocalDate date);

    public Page<EventInstance> findEventByClubId(Pageable p, int coachId);

    public Page<EventInstance> findEventByDate(Pageable p, LocalDate date);

    public Page<EventInstance> findEventByClubIdAndDate(Pageable p, int clubId, LocalDate date);

    @Modifying
    @Query(value = "update EventInstance e set e.participants = e.participants + 1 where e.id = ?1")
    public int incrementParticipantsForEvent(long eventId);

    @Modifying
    @Query(value = "update EventInstance e set e.capacity = ?2 where e.id = ?1")
    public int setCapacityForEvent(long eventId, int capacity);

    @Modifying
    @Query(value = "update EventInstance e set e.date = ?2, e.time = ?3 where e.id = ?1")
    public int setDateAndTimeOfEvent(long eventId, LocalDate date, LocalTime time);

    @Modifying
    public void deleteEventByDateBefore(LocalDate date);
}