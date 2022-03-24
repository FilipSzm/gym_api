package jwzp_ww_fs.app.services;

import jwzp_ww_fs.app.Exceptions.*;
import jwzp_ww_fs.app.models.Club;
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
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
                }}, 0, new HashMap<>())
        );

        lenient().when(repository.save(Mockito.any())).thenAnswer(i -> i.getArguments()[0]);

        lenient().when(repository.getById(Mockito.any(int.class))).thenAnswer(i -> exampleClubs.get((Integer) i.getArguments()[0] - 1));

        lenient().when(repository.findAll()).thenReturn(exampleClubs);
    }

    //POST

    @ParameterizedTest(name="POST {0}")
    @MethodSource("clubsProvider")
    public void addClubTest(Club clubToAdd) {
        ClubsService service = new ClubsService(repository);

        var uut = service.addClub(clubToAdd);

        assertThat(uut).isEqualTo(clubToAdd);
        verify(repository, times(2)).save(clubToAdd);
    }

    private static Stream<Arguments> clubsProvider() {
        return Stream.of(
                Arguments.of(new Club("C1", "A1", new HashMap<>() {{
                    put(DayOfWeek.MONDAY, new OpeningHours(LocalTime.of(1, 30), LocalTime.of(23, 30)));
                }}, 0, new HashMap<>()))
        );
    }

    //PATCH

    @ParameterizedTest(name="exception PATCH {1}")
    @MethodSource("incorrectUpdateClubsProvider")
    public void updateEventTestException(Club prevClub, Club updatedClub, Class<?> expectedException) {
        ClubsService service = new ClubsService(repository);
        service.addClub(prevClub);
        service.addEventToClub(1);
        service.setFillLevel(1, new HashMap<>() {{put(DayOfWeek.TUESDAY, new OpeningHours(LocalTime.of(1, 30), LocalTime.of(1, 30)));}});

        Throwable thrown = catchThrowable(() -> service.patchClub(1, updatedClub));

        assertThat(thrown).doesNotThrowAnyException();
    }

    private static Stream<Arguments> incorrectUpdateClubsProvider() {
        var prev = new Club("C1", "A1", new HashMap<>() {{
            put(DayOfWeek.MONDAY, new OpeningHours(LocalTime.of(1, 30), LocalTime.of(23, 30)));
            put(DayOfWeek.SUNDAY, new OpeningHours(LocalTime.of(1, 30), LocalTime.of(1, 30)));
        }}, 0, new HashMap<>());
        return Stream.of(
                Arguments.of(prev, new Club("C1", "A1", new HashMap<>() {{
                    put(DayOfWeek.MONDAY, new OpeningHours(LocalTime.of(1, 30), LocalTime.of(23, 30)));
                }}, 0, new HashMap<>()), EventHoursInClubException.class)
        );
    }

    @ParameterizedTest(name="no exceptions PATCH {1}")
    @MethodSource("correctUpdateClubsProvider")
    public void updateClubsTestNoException(Club prevClub, Club updatedClub) {
        ClubsService service = new ClubsService(repository);
        service.addClub(prevClub);

        assertDoesNotThrow(() -> service.patchClub(1, updatedClub));
    }

    private static Stream<Arguments> correctUpdateClubsProvider() {
        var prev = new Club("C1", "A1", new HashMap<>() {{
            put(DayOfWeek.MONDAY, new OpeningHours(LocalTime.of(1, 30), LocalTime.of(23, 30)));
            put(DayOfWeek.SUNDAY, new OpeningHours(LocalTime.of(1, 30), LocalTime.of(1, 30)));
        }}, 0, new HashMap<>());
        return Stream.of(
                Arguments.of(prev, new Club("C1", "A1", new HashMap<>() {{
                    put(DayOfWeek.MONDAY, new OpeningHours(LocalTime.of(1, 30), LocalTime.of(23, 30)));
                }}, 0, new HashMap<>()))
        );
    }

    //DELETE

    @ParameterizedTest(name="exceptions DELETE {0}")
    @MethodSource("deleteClubsProvider")
    public void deleteClubsTestException(int clubIdToDelete, Club clubToAdd) {
        ClubsService service = new ClubsService(repository);
        service.addClub(clubToAdd);
        service.addEventToClub(clubIdToDelete);

        Throwable thrown = catchThrowable(() -> service.removeClub(clubIdToDelete));

        assertThat(thrown).doesNotThrowAnyException();
    }

    @ParameterizedTest(name="no exceptions DELETE {0}")
    @MethodSource("deleteClubsProvider")
    public void deleteClubsTestNoException(int clubIdToDelete, Club clubToAdd) {
        ClubsService service = new ClubsService(repository);
        service.addClub(clubToAdd);

        assertDoesNotThrow(() -> service.removeClub(clubIdToDelete));
    }

    private static Stream<Arguments> deleteClubsProvider() {
        return Stream.of(
                Arguments.of(1, new Club("C1", "A1", new HashMap<>() {{
                    put(DayOfWeek.MONDAY, new OpeningHours(LocalTime.of(1, 30), LocalTime.of(23, 30)));
                }}, 0, new HashMap<>()))
        );
    }

    //GET

    @ParameterizedTest(name="GET club {0}")
    @MethodSource("getClubProvider")
    public void getClubTest(int clubId, Club club) {
        ClubsService service = new ClubsService(repository);

        var uut = service.getClub(clubId);

        assertThat(uut).isNull();
    }

    private static Stream<Arguments> getClubProvider() {
        return Stream.of(
                Arguments.of(1, new Club("C1", "A1", new HashMap<>() {{
                    put(DayOfWeek.MONDAY, new OpeningHours(LocalTime.of(1, 30), LocalTime.of(23, 30)));
                    put(DayOfWeek.SUNDAY, new OpeningHours(LocalTime.of(1, 30), LocalTime.of(1, 30)));
                }}, 0, new HashMap<>()))
        );
    }
}
