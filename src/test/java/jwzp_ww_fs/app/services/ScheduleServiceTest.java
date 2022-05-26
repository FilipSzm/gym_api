package jwzp_ww_fs.app.services;

import jwzp_ww_fs.app.exceptions.club.EventAssociatedWithClubException;
import jwzp_ww_fs.app.exceptions.club.ProtrudingEventException;
import jwzp_ww_fs.app.exceptions.schedule.AlreadyAssignedCoachException;
import jwzp_ww_fs.app.exceptions.schedule.ProtrudingScheduleException;
import jwzp_ww_fs.app.models.*;
import jwzp_ww_fs.app.repositories.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceTest {
    @Mock
    ScheduleRepository repository;
    @Mock
    ClubsService clubsService;
    @Mock
    CoachesService coachesService;

    @BeforeEach
    public void initializeMocks() {
        var exampleSchedules = List.of(
                new Schedule("S1", DayOfWeek.FRIDAY, LocalTime.of(1, 30), Duration.ofHours(2), 10, 1, 1),
                new Schedule("S2", DayOfWeek.FRIDAY, LocalTime.of(17, 30), Duration.ofHours(3), 10, 1, 1)
        );

        lenient().when(repository.save(Mockito.any())).thenAnswer(i -> i.getArguments()[0]);
        lenient().when(repository.getById(Mockito.any(int.class))).thenAnswer(i -> {
            int index = (Integer) i.getArguments()[0] - 1;
            if (index >= 0 && index < exampleSchedules.size())
                return exampleSchedules.get(index);
            else
                return null;
        });
        lenient().when(repository.findById(Mockito.any(int.class))).thenAnswer(i -> {
            int index = (Integer) i.getArguments()[0] - 1;
            if (index >= 0 && index < exampleSchedules.size())
                return Optional.of(exampleSchedules.get(index));
            else
                return Optional.empty();
        });
        lenient().when(repository.findAll()).thenReturn(exampleSchedules);

        Club exampleClub = new Club("name", "address", new HashMap<>() {{
            put(DayOfWeek.FRIDAY, new OpeningHours(LocalTime.of(0, 30), LocalTime.of(23, 30)));
            put(DayOfWeek.SUNDAY, new OpeningHours(LocalTime.of(1, 30), LocalTime.of(23, 45)));
        }});
        lenient().when(clubsService.getClub(1)).thenReturn(exampleClub);
        lenient().when(clubsService.getClub(2)).thenReturn(null);

        Coach exampleCoach = new Coach("name1", "name2", Year.of(1999));
        lenient().when(coachesService.getCoach(1)).thenReturn(exampleCoach);
        lenient().when(coachesService.getCoach(2)).thenReturn(null);
    }

    //POST

    @ParameterizedTest(name="exceptions POST {0}")
    @MethodSource("incorrectAddSchedulesProvider")
    public void addScheduleTestException(Schedule scheduleToAdd, Class<?> expectedException) {
        ScheduleService service = new ScheduleService(repository, clubsService, coachesService);

        Throwable uut = catchThrowable(() -> service.addSchedule(scheduleToAdd));

        if (expectedException == null)
            assertNull(uut);
        else
            assertThat(uut).isExactlyInstanceOf(expectedException);
    }

    private static Stream<Arguments> incorrectAddSchedulesProvider() {
        return Stream.of(
                Arguments.of(
                        new Schedule("S3", DayOfWeek.FRIDAY, LocalTime.of(17, 30), Duration.ofHours(2), 10, 1, 1),
                        ProtrudingScheduleException.class
                )
        );
    }


    @ParameterizedTest(name="no exceptions POST {0}")
    @MethodSource("addSchedulesProvider")
    public void addScheduleTest(Schedule scheduleToAdd) {
        when(clubsService.isScheduleInClubOpeningHours(Mockito.any())).thenReturn(true);

        ScheduleService uut = new ScheduleService(repository, clubsService, coachesService);

        assertDoesNotThrow(() -> uut.addSchedule(scheduleToAdd));
    }

    private static Stream<Arguments> addSchedulesProvider() {
        return Stream.of(
                Arguments.of(
                        new Schedule("S3", DayOfWeek.SUNDAY, LocalTime.of(17, 30), Duration.ofHours(1), 10, 1, 1)
                )
        );
    }

    //PATCH

    @ParameterizedTest(name="exception PATCH {1}")
    @MethodSource("incorrectUpdateSchedulesProvider")
    public void updateScheduleTestException(int prevScheduleId, Schedule updatedSchedule, Class<?> expectedException) {
        ScheduleService service = new ScheduleService(repository, clubsService, coachesService);

        Throwable uut = catchThrowable(() -> service.updateSchedule(prevScheduleId, updatedSchedule));

        if (expectedException == null)
            assertNull(uut);
        else
            assertThat(uut).isExactlyInstanceOf(expectedException);
    }

    private static Stream<Arguments> incorrectUpdateSchedulesProvider() {
        return Stream.of(
                Arguments.of(
                        1,
                        new Schedule("S3", DayOfWeek.SUNDAY, LocalTime.of(17, 30), Duration.ofHours(1), 10, 1, 1),
                        ProtrudingScheduleException.class
                )
        );
    }

    @ParameterizedTest(name="no exceptions PATCH {1}")
    @MethodSource("updateSchedulesProvider")
    public void updateSchedulesTest(int prevScheduleId, Schedule updatedSchedule) {
        when(clubsService.isScheduleInClubOpeningHours(Mockito.any())).thenReturn(true);

        ScheduleService service = new ScheduleService(repository, clubsService, coachesService);

        assertDoesNotThrow(() -> service.updateSchedule(prevScheduleId, updatedSchedule));
    }

    private static Stream<Arguments> updateSchedulesProvider() {
        return Stream.of(
                Arguments.of(
                        1,
                        new Schedule("S3", DayOfWeek.SUNDAY, LocalTime.of(17, 30), Duration.ofHours(1), 10, 1, 1)
                )
        );
    }

    //DELETE

    @ParameterizedTest(name="exceptions DELETE {0}")
    @MethodSource("incorrectDeleteSchedulesProvider")
    public void deleteSchedulesTestException(int scheduleIdToDelete, Class<?> expectedException) {
        ScheduleService service = new ScheduleService(repository, clubsService, coachesService);

        Throwable uut = catchThrowable(() -> service.removeSchedule(scheduleIdToDelete));

        if (expectedException == null)
            assertNull(uut);
        else
            assertThat(uut).isExactlyInstanceOf(expectedException);
    }

    private static Stream<Arguments> incorrectDeleteSchedulesProvider() {
        return Stream.of(
                Arguments.of(1, null)
        );
    }

    @ParameterizedTest(name="no exceptions DELETE {0}")
    @MethodSource("deleteSchedulesProvider")
    public void deleteSchedulesTestNoException(int scheduleIdToDelete) {
        ScheduleService service = new ScheduleService(repository, clubsService, coachesService);

        Throwable uut = catchThrowable(() -> service.removeSchedule(scheduleIdToDelete));

        assertNull(uut);
    }

    private static Stream<Arguments> deleteSchedulesProvider() {
        return Stream.of(
                Arguments.of(1)
        );
    }

    //GET

    @ParameterizedTest(name="GET schedule {0}")
    @MethodSource("getSchedulesProvider")
    public void getScheduleTest(int scheduleId) {
        ScheduleService service = new ScheduleService(repository, clubsService, coachesService);

        Throwable uut = catchThrowable(() -> service.getSchedule(scheduleId));

        assertNull(uut);
    }

    private static Stream<Arguments> getSchedulesProvider() {
        return Stream.of(
                Arguments.of(1)
        );
    }
}
