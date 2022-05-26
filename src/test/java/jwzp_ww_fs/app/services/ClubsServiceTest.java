package jwzp_ww_fs.app.services;

import jwzp_ww_fs.app.exceptions.*;
import jwzp_ww_fs.app.exceptions.club.EventAssociatedWithClubException;
import jwzp_ww_fs.app.exceptions.club.ProtrudingEventException;
import jwzp_ww_fs.app.models.Club;
import jwzp_ww_fs.app.models.EventHours;
import jwzp_ww_fs.app.models.OpeningHours;
import jwzp_ww_fs.app.repositories.ClubsRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClubsServiceTest {
    @Mock
    private ClubsRepository repository;

    @BeforeEach
    public void initializeMocks() {
        var exampleClubs = List.of(
                new Club("C1", "A1", new HashMap<>() {{
                    put(DayOfWeek.MONDAY, new OpeningHours(LocalTime.of(1, 30), LocalTime.of(23, 30)));
                    put(DayOfWeek.SUNDAY, new OpeningHours(LocalTime.of(1, 30), LocalTime.of(1, 30)));
                }}, 1, new HashMap<>() {{
                    put(DayOfWeek.MONDAY, new EventHours(LocalTime.of(1, 30), LocalTime.of(23, 30)));
                }})
        );

        lenient().when(repository.save(Mockito.any())).thenAnswer(i -> i.getArguments()[0]);
        lenient().when(repository.getById(Mockito.any(int.class))).thenAnswer(i -> {
            int index = (Integer) i.getArguments()[0] - 1;
            if (index >= 0 && index < exampleClubs.size())
                return exampleClubs.get(index);
            else
                return null;
        });
        lenient().when(repository.findById(Mockito.any(int.class))).thenAnswer(i -> {
            int index = (Integer) i.getArguments()[0] - 1;
            if (index >= 0 && index < exampleClubs.size())
                return Optional.of(exampleClubs.get(index));
            else
                return Optional.empty();
        });
        lenient().when(repository.findAll()).thenReturn(exampleClubs);
    }

    //POST

    @ParameterizedTest(name="exceptions POST {0}")
    @MethodSource("incorrectAddClubsProvider")
    public void addClubTestException(Club clubToAdd, Class<?> expectedException) {
        ClubsService service = new ClubsService(repository);

        Throwable uut = catchThrowable(() -> service.addClub(clubToAdd));

        if (expectedException == null)
            assertNull(uut);
        else
            assertThat(uut).isExactlyInstanceOf(expectedException);
    }

    private static Stream<Arguments> incorrectAddClubsProvider() {
        return Stream.of(
                Arguments.of(new Club("C1", "A1", new HashMap<>() {{
                    put(DayOfWeek.MONDAY, new OpeningHours(LocalTime.of(2, 30), LocalTime.of(23, 30)));
                }}, 0, new HashMap<>()), null)
        );
    }


    @ParameterizedTest(name="no exceptions POST {0}")
    @MethodSource("addClubsProvider")
    public void addClubTest(Club clubToAdd) {
        ClubsService uut = new ClubsService(repository);

        assertDoesNotThrow(() -> uut.addClub(clubToAdd));
    }

    private static Stream<Arguments> addClubsProvider() {
        return Stream.of(
                Arguments.of(new Club("C1", "A1", new HashMap<>() {{
                    put(DayOfWeek.MONDAY, new OpeningHours(LocalTime.of(1, 30), LocalTime.of(23, 30)));
                }}, 0, new HashMap<>())),
                Arguments.of(new Club("C2", "A1", new HashMap<>() {{
                    put(DayOfWeek.MONDAY, new OpeningHours(LocalTime.of(2, 30), LocalTime.of(23, 30)));
                }}, 0, new HashMap<>()))
        );
    }

    //PATCH

    @ParameterizedTest(name="exception PATCH {1}")
    @MethodSource("incorrectUpdateClubsProvider")
    public void updateClubTestException(int prevClubId, Club updatedClub, Class<?> expectedException) {
        ClubsService service = new ClubsService(repository);

        Throwable uut = catchThrowable(() -> service.patchClub(prevClubId, updatedClub));

        if (expectedException == null)
            assertNull(uut);
        else
            assertThat(uut).isExactlyInstanceOf(expectedException);
    }

    private static Stream<Arguments> incorrectUpdateClubsProvider() {
        return Stream.of(
                Arguments.of(1, new Club("C1", "A1", new HashMap<>() {{
                    put(DayOfWeek.MONDAY, new OpeningHours(LocalTime.of(1, 30), LocalTime.of(23, 20)));
                }}, 0, new HashMap<>()), ProtrudingEventException.class)
        );
    }

    @ParameterizedTest(name="no exceptions PATCH {1}")
    @MethodSource("updateClubsProvider")
    public void updateClubsTest(int prevClubId, Club updatedClub) {
        ClubsService service = new ClubsService(repository);

        assertDoesNotThrow(() -> service.patchClub(prevClubId, updatedClub));
    }

    private static Stream<Arguments> updateClubsProvider() {
        return Stream.of(
                Arguments.of(1, new Club("C1", "A1", new HashMap<>() {{
                    put(DayOfWeek.MONDAY, new OpeningHours(LocalTime.of(1, 30), LocalTime.of(23, 30)));
                }}, 0, new HashMap<>())),
                Arguments.of(1, new Club("C1", "A1", new HashMap<>() {{
                    put(DayOfWeek.MONDAY, new OpeningHours(LocalTime.of(1, 30), LocalTime.of(23, 40)));
                }}, 0, new HashMap<>()))
        );
    }

    //DELETE

    @ParameterizedTest(name="exceptions DELETE {0}")
    @MethodSource("incorrectDeleteClubsProvider")
    public void deleteClubsTestException(int clubIdToDelete, Class<?> expectedException) {
        ClubsService service = new ClubsService(repository);

        Throwable uut = catchThrowable(() -> service.removeClub(clubIdToDelete));

        if (expectedException == null)
            assertNull(uut);
        else
            assertThat(uut).isExactlyInstanceOf(expectedException);
    }

    private static Stream<Arguments> incorrectDeleteClubsProvider() {
        return Stream.of(
                Arguments.of(1, EventAssociatedWithClubException.class)
        );
    }

    @ParameterizedTest(name="no exceptions DELETE {0}")
    @MethodSource("deleteClubsProvider")
    public void deleteClubsTestNoException(int clubIdToDelete) {
        ClubsService service = new ClubsService(repository);

        Throwable uut = catchThrowable(() -> service.removeClub(clubIdToDelete));

        assertNull(uut);
    }

    private static Stream<Arguments> deleteClubsProvider() {
        return Stream.of(
                Arguments.of(-1)
        );
    }

    //GET

    @ParameterizedTest(name="GET club {0}")
    @MethodSource("getClubProvider")
    public void getClubTest(int clubId) {
        ClubsService service = new ClubsService(repository);

        Throwable uut = catchThrowable(() -> service.getClub(clubId));

        assertNull(uut);
    }

    private static Stream<Arguments> getClubProvider() {
        return Stream.of(
                Arguments.of(1)
        );
    }
}
