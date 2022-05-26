package jwzp_ww_fs.app.services;

import jwzp_ww_fs.app.exceptions.event.AlreadyAssignedCoachException;
import jwzp_ww_fs.app.exceptions.event.ExcessivelyLongEventException;
import jwzp_ww_fs.app.exceptions.event.NonExistingEventException;
import jwzp_ww_fs.app.exceptions.event.ProtrudingEventException;
import jwzp_ww_fs.app.models.Club;
import jwzp_ww_fs.app.models.Coach;
import jwzp_ww_fs.app.models.EventInstance;
import jwzp_ww_fs.app.models.EventInstanceData;
import jwzp_ww_fs.app.repositories.EventsInstancesRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class EventsServiceTest {
    
    @Mock
    private EventsInstancesRepository repository;
    @Mock
    private CoachesService coachesService;
    @Mock
    private ClubsService clubsService;
    @Mock
    private ScheduleService scheduleService;

    @BeforeEach
    public void initializeMocks() {
        var e0 = new EventInstance("E0", LocalDate.of(2022, Month.JANUARY, 4), LocalTime.of(14, 30), Duration.ofHours(3), 10, 1, 1);
        var e1 = new EventInstance("E1", LocalDate.of(2022, Month.JANUARY, 4), LocalTime.of(23, 30), Duration.ofHours(3), 10, 1, 1);
        var e2 = new EventInstance("E2", LocalDate.of(2022, Month.JANUARY, 5), LocalTime.of(1, 0), Duration.ofHours(3), 10, 1, 1);
        var e3 = new EventInstance("E3", LocalDate.of(2022, Month.JANUARY, 5), LocalTime.of(17, 0), Duration.ofHours(3), 10, 1, 1);
        var e4 = new EventInstance("E4", LocalDate.of(2022, Month.JANUARY, 5), LocalTime.of(5, 0), Duration.ofHours(3), 10, 1, 1);
        var e5 = new EventInstance("E5", LocalDate.of(2022, Month.JANUARY, 5), LocalTime.of(5, 0), Duration.ofHours(12), 10, 2, 1);
        var e6 = new EventInstance("E6", LocalDate.of(2022, Month.JANUARY, 5), LocalTime.of(17, 0), Duration.ofHours(8), 10, 1, 1);
        var e7 = new EventInstance("E7", LocalDate.of(2022, Month.JANUARY, 7), LocalTime.of(4, 0), Duration.ofHours(3), 10, 1, 1);
        var e8 = new EventInstance("E8", LocalDate.of(2022, Month.JANUARY, 5), LocalTime.of(1, 0), Duration.ofHours(22), 10, 1, 2);

        lenient().when(repository.findAll()).thenReturn(List.of(e0,e1,e2,e3,e4,e5,e6,e7,e8));

        lenient().when(repository.findEventByClubId(1)).thenReturn(List.of(e0,e1,e2,e3,e4,e6,e7,e8));
        lenient().when(repository.findEventByClubId(2)).thenReturn(List.of(e5));

        lenient().when(repository.findEventByCoachId(1)).thenReturn(List.of(e0,e1,e2,e3,e4,e5,e6,e7));
        lenient().when(repository.findEventByCoachId(2)).thenReturn(List.of(e8));

        lenient().when(repository.getById(1L)).thenReturn(e0);
        lenient().when(repository.findById(1L)).thenReturn(Optional.of(e0));
        lenient().when(repository.findById(not(eq(1L)))).thenReturn(Optional.empty());

        Club exampleClub = new Club("name", "address", null);
        lenient().when(clubsService.getClub(1)).thenReturn(exampleClub);
        lenient().when(clubsService.getClub(2)).thenReturn(null);

        Coach exampleCoach = new Coach("name1", "name2", Year.of(1999));
        lenient().when(coachesService.getCoach(1)).thenReturn(exampleCoach);
        lenient().when(coachesService.getCoach(2)).thenReturn(null);
    }

    //POST

    @ParameterizedTest(name="exceptions POST {0}")
    @MethodSource("incorrectEventsProvider")
    public void addEventTestException(EventInstance eventToAdd, boolean inOpeningHours, Class<?> expectedException) {
        lenient().when(clubsService.isEventInstanceInClubOpeningHours(Mockito.any())).thenReturn(inOpeningHours);

        EventsInstancesService serviceToTest = new EventsInstancesService(repository, clubsService, coachesService, scheduleService);
        Throwable uut = catchThrowable(() -> serviceToTest.addEvent(eventToAdd));

        if (expectedException == null)
            assertNull(uut);
        else
            assertThat(uut).isExactlyInstanceOf(expectedException);
    }

    private static Stream<Arguments> incorrectEventsProvider() {
        return Stream.of(
            Arguments.of(new EventInstance("TEST", LocalDate.of(2022, Month.JANUARY, 4), LocalTime.of(15, 30), Duration.ofMinutes(30), 10, 1, 1), true, null)
        );
    }

    @ParameterizedTest(name="no exceptions POST {0}")
    @MethodSource("correctEventsProvider")
    public void addEventTestNoException(EventInstance eventToAdd) {
        lenient().when(clubsService.isEventInstanceInClubOpeningHours(Mockito.any())).thenReturn(true);

        EventsInstancesService serviceToTest = new EventsInstancesService(repository, clubsService, coachesService, scheduleService);

        assertDoesNotThrow(() -> serviceToTest.addEvent(eventToAdd));
    }

    private static Stream<Arguments> correctEventsProvider() {
        return Stream.of(
            Arguments.of(new EventInstance("TEST", LocalDate.of(2022, Month.JANUARY, 4), LocalTime.of(15, 30), Duration.ofMinutes(30), 10, 1, 1))
        );
    }

    //PATCH

    @ParameterizedTest(name="exceptions PATCH {1}")
    @MethodSource("incorrectUpdateEventsProvider")
    public void updateEventTestException(int eventId, EventInstanceData updatedEventData, boolean inOpeningHours, Class<?> expectedException) {
        lenient().when(clubsService.isEventInstanceInClubOpeningHours(Mockito.any())).thenReturn(inOpeningHours);

        EventsInstancesService serviceToTest = new EventsInstancesService(repository, clubsService, coachesService, scheduleService);
        Throwable thrown = catchThrowable(() -> serviceToTest.updateEventInstance(eventId, updatedEventData));

        assertThat(thrown).isExactlyInstanceOf(expectedException);
    }

    private static Stream<Arguments> incorrectUpdateEventsProvider() {
        return Stream.of(
            Arguments.of(-1, new EventInstanceData(10, LocalDate.of(2022, Month.JANUARY, 4), LocalTime.of(14, 30)), true, NonExistingEventException.class)
        );
    }

    @ParameterizedTest(name="no exceptions PATCH {1}")
    @MethodSource("correctUpdateEventsProvider")
    public void updateEventTestNoException(long eventId, EventInstanceData eventToAdd, boolean inOpeningHours) {
        lenient().when(clubsService.isEventInstanceInClubOpeningHours(Mockito.any())).thenReturn(inOpeningHours);

        EventsInstancesService serviceToTest = new EventsInstancesService(repository, clubsService, coachesService, scheduleService);

        EventInstance oldEvent = assertDoesNotThrow(() -> serviceToTest.updateEventInstance(eventId, eventToAdd));
        assertThat(oldEvent).isEqualTo(repository.getById(eventId));
    }

    private static Stream<Arguments> correctUpdateEventsProvider() {
        return Stream.of(
                Arguments.of(1, new EventInstanceData(10, LocalDate.of(2022, Month.JANUARY, 4), LocalTime.of(14, 30)), true)
        );
    }

    //DELETE
    @Test
    public void deleteEventTestNoException() {
        EventsInstancesService serviceToTest = new EventsInstancesService(repository, clubsService, coachesService, scheduleService);

        EventInstance deletedEvent = assertDoesNotThrow(() -> serviceToTest.removeEvent(1));

        assertThat(deletedEvent).isEqualTo(repository.getById(1L));
    }

    @Test
    public void deleteEventTestException() {
        EventsInstancesService serviceToTest = new EventsInstancesService(repository, clubsService, coachesService, scheduleService);

        Throwable thrown = catchThrowable(() -> serviceToTest.removeEvent(2));

        assertThat(thrown).isExactlyInstanceOf(NonExistingEventException.class);
    }

    @Test
    public void deleteAllEventsTest() {
        EventsInstancesService serviceToTest = new EventsInstancesService(repository, clubsService, coachesService, scheduleService);

        List<EventInstance> oldEvent = assertDoesNotThrow(serviceToTest::removeAllEvents);
        assertThat(oldEvent).containsExactlyInAnyOrderElementsOf(repository.findAll());
    }

    //GET

    @ParameterizedTest(name="GET event {0}")
    @MethodSource("getEventProvider")
    public void getEventTest(int eventId) {
        EventsInstancesService service = new EventsInstancesService(repository, clubsService, coachesService, scheduleService);

        Throwable uut = Assertions.catchThrowable(() -> service.getEventInstanceWithId(eventId));

        assertNull(uut);
    }

    private static Stream<Arguments> getEventProvider() {
        return Stream.of(
                Arguments.of(1),
                Arguments.of(2)
        );
    }
}
