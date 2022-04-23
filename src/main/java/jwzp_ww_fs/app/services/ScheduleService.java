package jwzp_ww_fs.app.services;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jwzp_ww_fs.app.Exceptions.EventCoachOverlapException;
import jwzp_ww_fs.app.Exceptions.EventDoesNotExistException;
import jwzp_ww_fs.app.Exceptions.EventNoSuchClubException;
import jwzp_ww_fs.app.Exceptions.EventNoSuchCoachException;
import jwzp_ww_fs.app.Exceptions.EventNotInOpeningHoursException;
import jwzp_ww_fs.app.Exceptions.EventTooLongException;
import jwzp_ww_fs.app.Exceptions.GymException;
import jwzp_ww_fs.app.models.OpeningHours;
import jwzp_ww_fs.app.models.Schedule;
import jwzp_ww_fs.app.repositories.ScheduleRepository;
import jwzp_ww_fs.app.services.ClubsService;
import jwzp_ww_fs.app.services.CoachesService;

@Service
public class ScheduleService {
    ScheduleRepository repository;

    ClubsService clubsService;
    CoachesService coachesService;

    @Autowired
    //TODO schedule repository
    public ScheduleService(ScheduleRepository repository, ClubsService clubsService, CoachesService coachesService) {
        this.repository = repository;
        this.clubsService = clubsService;
        this.coachesService = coachesService;
    }

    public Schedule addSchedule(Schedule schedule) throws GymException {
        if (!existsClubForSchedule(schedule)) throw new EventNoSuchClubException();
        if (!existsCoachForSchedule(schedule)) throw new EventNoSuchCoachException();
        if (existsSimultaniousScheduleWithCoach(schedule, null)) throw new EventCoachOverlapException();
        if (!clubsService.isScheduleInClubOpeningHours(schedule)) throw new EventNotInOpeningHoursException();
        if (!isScheduleCorrectLength(schedule)) throw new EventTooLongException();

        clubsService.addEventToClub(schedule.clubId());
        clubsService.setFillLevel(schedule.clubId(), getMinimalOpeningHoursForClub(schedule));
        coachesService.addEventForCoach(schedule.coachId());

        return repository.save(schedule);
    }

    private Map<DayOfWeek, OpeningHours> getMinimalOpeningHoursForClub(Schedule scheduleToAdd) {
        var clubSchedule = Stream.concat(Stream.of(scheduleToAdd), getSchedulesByClub(scheduleToAdd.clubId()).stream()).toList();

        var result = new HashMap<DayOfWeek, OpeningHours>();
        for (DayOfWeek day : DayOfWeek.values()) {
            OpeningHours openingHoursForDay = getDayMinOpeningHours(day, clubSchedule);
            if (openingHoursForDay != null) result.put(day, openingHoursForDay);
        }
        return result;
    }

    private OpeningHours getDayMinOpeningHours(DayOfWeek day, List<Schedule> schedules) {
        var pervDay = schedules.stream().filter(e -> e.day().equals(day.minus(1)));
        var currDay = schedules.stream().filter(e -> e.day().equals(day)).toList();

        LocalTime minBeg, minEnd;
        if (pervDay.anyMatch(this::isScheduleOverMidnight)) minBeg = LocalTime.MIDNIGHT;
        else {
            if (currDay.size() == 0) return null;
            minBeg = LocalTime.MAX;
            for(Schedule s : currDay) {
                if (minBeg.isAfter(s.time())) minBeg = s.time();
            }
        }

        if (currDay.stream().anyMatch(this::isScheduleOverMidnight)) minEnd = LocalTime.MIDNIGHT;
        else {
            minEnd = LocalTime.MIN;
            for(Schedule s : currDay) {
                if (minEnd.isBefore(s.time().plus(s.duration()))) minEnd = s.time().plus(s.duration());
            }
        }

        return new OpeningHours(minBeg, minEnd);
    }

    private boolean isScheduleCorrectLength(Schedule scheduleToAdd) {
        return scheduleToAdd.duration().compareTo(Duration.ofDays(1)) <= 0;
    }

    private boolean existsClubForSchedule(Schedule scheduleToAdd) {
        return clubsService.getClub(scheduleToAdd.clubId()) != null;
    }

    private boolean existsCoachForSchedule(Schedule scheduleToAdd) {
        return coachesService.getCoach(scheduleToAdd.coachId()) != null;
    }

    private boolean existsSimultaniousScheduleWithCoach(Schedule scheduleToAdd, Schedule scheduleToIgnore) {
        var otherSchedulesWithCoach = getSchedulesByCoach(scheduleToAdd.coachId());
        if (scheduleToIgnore != null) {
            otherSchedulesWithCoach = otherSchedulesWithCoach.stream().filter(e -> !e.equals(scheduleToIgnore)).toList();
        }

        LocalTime beg = scheduleToAdd.time();
        LocalTime end = scheduleToAdd.time().plus(scheduleToAdd.duration());

        var schedulesPrevDay = otherSchedulesWithCoach.stream().filter(s -> s.day().equals(scheduleToAdd.day().minus(1)))
                .filter(this::isScheduleOverMidnight);
        var schedulesSameDayOvernight = otherSchedulesWithCoach.stream().filter(s -> s.day().equals(scheduleToAdd.day()))
                .filter(this::isScheduleOverMidnight);
        var schedulesSameDay = otherSchedulesWithCoach.stream().filter(s -> s.day().equals(scheduleToAdd.day()))
                .filter(this::isScheduleNotOverMidnight);
        var schedulesNextDay = otherSchedulesWithCoach.stream().filter(s -> s.day().equals(scheduleToAdd.day().plus(1)));
        if (beg.isBefore(end)) {
            var fromPrev = schedulesPrevDay.filter(s -> s.time().plus(s.duration()).isAfter(beg)).findAny().isPresent();
            var fromCurrOvernight = schedulesSameDayOvernight.filter(s -> s.time().isBefore(end)).findAny().isPresent();
            var fromCurr = schedulesSameDay
                    .filter(s -> isDuringSchedule(scheduleToAdd, s.time())
                            || isDuringSchedule(scheduleToAdd, s.time().plus(s.duration()))
                            || (s.time().isBefore(beg) && s.time().plus(s.duration()).isAfter(end)))
                    .findAny().isPresent();

            return fromPrev || fromCurrOvernight || fromCurr;
        } else {
            var fromPrev = schedulesPrevDay.filter(s -> s.time().plus(s.duration()).isAfter(beg)).findAny().isPresent();
            var fromNext = schedulesNextDay.filter(s -> s.time().isBefore(end)).findAny().isPresent();
            var fromCurr = schedulesSameDay.filter(s -> s.time().plus(s.duration()).isAfter(beg) || s.time().isAfter(beg)).findAny().isPresent();
            var fromCurr2 = schedulesSameDayOvernight.toList().size() > 0;
            return fromPrev || fromNext || fromCurr || fromCurr2;
        }
    }

    private boolean isScheduleOverMidnight(Schedule s) {
        return !s.time().plus(s.duration()).isAfter(s.time());
    }

    private boolean isScheduleNotOverMidnight(Schedule s) {
        return s.time().plus(s.duration()).isAfter(s.time());
    }

    private boolean isDuringSchedule(Schedule s, LocalTime t) {
        return t.isAfter(s.time()) && t.isBefore(s.time().plus(s.duration()));
    }

    public Schedule removeSchedule(int scheduleId) throws EventDoesNotExistException {
        // Event removedEvent = repository.removeScheduleWithId(eventId);

        // if (removedEvent == null)
        //     throw new EventDoesNotExistException();
        // return removedEvent;
        Optional<Schedule> scheduleToRemove = repository.findById(scheduleId);

        if (scheduleToRemove.isEmpty()) throw new EventDoesNotExistException();

        Schedule removedSchedule = scheduleToRemove.get();

        repository.deleteById(scheduleId);
        return removedSchedule;
    }

    public List<Schedule> removeAllSchedules() {
        var allSchedules = getAllSchedules();

        for (Schedule s : allSchedules) {
            clubsService.subtractEventFromClub(s.clubId());
            clubsService.setFillLevel(s.clubId(), new HashMap<DayOfWeek, OpeningHours>());
            coachesService.subtractEventFromCoach(s.coachId());
        }

        var removedSchedules = repository.findAll();

        repository.deleteAll();

        return removedSchedules;
    }

    public Schedule updateSchedule(int scheduleId, Schedule schedule) throws GymException {
        Schedule currentScheduleWithId = getSchedule(scheduleId);
        
        if (currentScheduleWithId == null) throw new EventDoesNotExistException();

        if (!existsClubForSchedule(schedule))
            throw new EventNoSuchClubException();
        if (!existsCoachForSchedule(schedule))
            throw new EventNoSuchCoachException();
        if (existsSimultaniousScheduleWithCoach(schedule, currentScheduleWithId))
            throw new EventCoachOverlapException();
        if (!clubsService.isScheduleInClubOpeningHours(schedule))
            throw new EventNotInOpeningHoursException();
        if (!isScheduleCorrectLength(schedule))
            throw new EventTooLongException();

        Schedule scheduleToUpdate = repository.getById(scheduleId);
        scheduleToUpdate.updateData(schedule);
        repository.save(scheduleToUpdate);

        return currentScheduleWithId;
    }

    public List<Schedule> getAllSchedules() {
        return repository.findAll();
    }

    public List<Schedule> getSchedulesByCoach(int coachId) {
        // return repository.findAll().stream().filter(c -> c.coachId() == coachId).toList();
        return repository.findScheduleByCoachId(coachId);
    }

    public List<Schedule> getSchedulesByClub(int clubId) {
        // return repository.findAll().stream().filter(c -> c.clubId() == clubId).toList();
        return repository.findScheduleByClubId(clubId);
    }

    public List<Schedule> getSchedulesByCoachAndClub(int coachId, int clubId) {
        // return repository.findAll().stream().filter(c -> c.coachId() == coachId && c.clubId() == clubId).toList();
        return repository.findScheduleByClubIdAndCoachId(clubId, coachId);
    }

    public Schedule getSchedule(int id) {
        Optional<Schedule> schedule = repository.findById(id);
        return schedule.isPresent() ? schedule.get() : null;
    }

    public Page<Schedule> getPage(Pageable p, Optional<Integer> clubId, Optional<Integer> coachId) {
        if (clubId.isPresent() && coachId.isPresent()) return repository.findScheduleByClubIdAndCoachId(p, clubId.get(), coachId.get());
        if (clubId.isPresent() && coachId.isEmpty()) return repository.findScheduleByClubId(p, clubId.get());
        if (clubId.isEmpty() && coachId.isPresent()) return repository.findScheduleByCoachId(p, coachId.get());
        return repository.findAll(p);
    }
}