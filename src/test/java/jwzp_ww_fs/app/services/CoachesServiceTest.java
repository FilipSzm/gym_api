package jwzp_ww_fs.app.services;

import jwzp_ww_fs.app.exceptions.club.EventAssociatedWithClubException;
import jwzp_ww_fs.app.exceptions.club.ProtrudingEventException;
import jwzp_ww_fs.app.exceptions.coach.EventAssociatedWithCoachException;
import jwzp_ww_fs.app.models.Club;
import jwzp_ww_fs.app.models.Coach;
import jwzp_ww_fs.app.models.OpeningHours;
import jwzp_ww_fs.app.repositories.CoachRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CoachesServiceTest {
    @Mock
    private CoachRepository repository;

    @BeforeEach
    public void initializeMocks() {
        var exampleCoaches = List.of(
                new Coach("N1", "S1", Year.of(2000)),
                new Coach("N1", "S1", Year.of(2000)),
                new Coach("N2", "S1", Year.of(2000)),
                new Coach("N1", "S2", Year.of(2000)),
                new Coach("N1", "S1", Year.of(2001)),
                new Coach("N3", "S3", Year.of(2002))
        );

        exampleCoaches.get(0).addEvent();

        lenient().when(repository.save(Mockito.any())).thenAnswer(i -> i.getArguments()[0]);
        lenient().when(repository.getById(Mockito.any(int.class))).thenAnswer(i -> {
            int index = (Integer) i.getArguments()[0] - 1;
            if (index >= 0 && index < exampleCoaches.size())
                return exampleCoaches.get(index);
            else
                return null;
        });
        lenient().when(repository.findById(Mockito.any(int.class))).thenAnswer(i -> {
            int index = (Integer) i.getArguments()[0] - 1;
            if (index >= 0 && index < exampleCoaches.size())
                return Optional.of(exampleCoaches.get(index));
            else
                return Optional.empty();
        });
        lenient().when(repository.findAll()).thenReturn(exampleCoaches);
    }

//POST

    @ParameterizedTest(name="exceptions POST {0}")
    @MethodSource("incorrectAddCoachesProvider")
    public void addCoachTestException(Coach coachToAdd, Class<?> expectedException) {
        CoachesService service = new CoachesService(repository);

        Throwable uut = catchThrowable(() -> service.addCoach(coachToAdd));

        if (expectedException == null)
            assertNull(uut);
        else
            assertThat(uut).isExactlyInstanceOf(expectedException);
    }

    private static Stream<Arguments> incorrectAddCoachesProvider() {
        return Stream.of(
                Arguments.of(new Coach("N1", "S1", Year.of(2000)), null)
        );
    }

    @ParameterizedTest(name="no exceptions POST {0}")
    @MethodSource("addCoachesProvider")
    public void addCoachTest(Coach coachToAdd) {
        CoachesService uut = new CoachesService(repository);

        assertDoesNotThrow(() -> uut.addCoach(coachToAdd));
    }

    private static Stream<Arguments> addCoachesProvider() {
        return Stream.of(
                Arguments.of(new Coach("N1", "S1", Year.of(2000))),
                Arguments.of(new Coach("N1", "S2", Year.of(2000)))
        );
    }

    //PATCH

    @ParameterizedTest(name="exception PATCH {1}")
    @MethodSource("incorrectUpdateCoachesProvider")
    public void updateCoachTestException(int prevCoachId, Coach updatedCoach, Class<?> expectedException) {
        CoachesService service = new CoachesService(repository);

        Throwable uut = catchThrowable(() -> service.patchCoach(prevCoachId, updatedCoach));

        if (expectedException == null)
            assertNull(uut);
        else
            assertThat(uut).isExactlyInstanceOf(expectedException);
    }

    private static Stream<Arguments> incorrectUpdateCoachesProvider() {
        return Stream.of(
                Arguments.of(1, new Coach("N1", "S2", Year.of(2020)), null)
        );
    }

    @ParameterizedTest(name="no exceptions PATCH {1}")
    @MethodSource("updateCoachesProvider")
    public void updateCoachesTest(int prevCoachId, Coach updatedCoach) {
        CoachesService service = new CoachesService(repository);

        assertDoesNotThrow(() -> service.patchCoach(prevCoachId, updatedCoach));
    }

    private static Stream<Arguments> updateCoachesProvider() {
        return Stream.of(
                Arguments.of(1, new Coach("N1", "S2", Year.of(2020))),
                Arguments.of(1, new Coach("N1", "S4", Year.of(2020))),
                Arguments.of(1, new Coach("N1", "S1", Year.of(2018)))
        );
    }

    //DELETE

    @ParameterizedTest(name="exceptions DELETE {0}")
    @MethodSource("incorrectDeleteCoachesProvider")
    public void deleteCoachesTestException(int coachIdToDelete, Class<?> expectedException) {
        CoachesService service = new CoachesService(repository);

        Throwable uut = catchThrowable(() -> service.removeCoach(coachIdToDelete));

        if (expectedException == null)
            assertNull(uut);
        else
            assertThat(uut).isExactlyInstanceOf(expectedException);
    }

    private static Stream<Arguments> incorrectDeleteCoachesProvider() {
        return Stream.of(
                Arguments.of(1, EventAssociatedWithCoachException.class)
        );
    }

    @ParameterizedTest(name="no exceptions DELETE {0}")
    @MethodSource("deleteCoachesProvider")
    public void deleteCoachesTestNoException(int coachIdToDelete) {
        CoachesService service = new CoachesService(repository);

        Throwable uut = catchThrowable(() -> service.removeCoach(coachIdToDelete));

        assertNull(uut);
    }

    private static Stream<Arguments> deleteCoachesProvider() {
        return Stream.of(
                Arguments.of(-1)
        );
    }

    //GET

    @ParameterizedTest(name="GET coach {0}")
    @MethodSource("getCoachProvider")
    public void getCoachTest(int coachId) {
        CoachesService service = new CoachesService(repository);

        Throwable uut = catchThrowable(() -> service.getCoach(coachId));

        assertNull(uut);
    }

    private static Stream<Arguments> getCoachProvider() {
        return Stream.of(
                Arguments.of(1),
                Arguments.of(2)
        );
    }
}