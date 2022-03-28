package jwzp_ww_fs.app.services;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.eq;

import jwzp_ww_fs.app.Exceptions.EventCoachOverlapException;
import jwzp_ww_fs.app.Exceptions.EventDoesNotExistException;
import jwzp_ww_fs.app.Exceptions.EventNoSuchClubException;
import jwzp_ww_fs.app.Exceptions.EventNoSuchCoachException;
import jwzp_ww_fs.app.Exceptions.EventNotInOpeningHoursException;
import jwzp_ww_fs.app.Exceptions.EventTooLongException;
import jwzp_ww_fs.app.models.Club;
import jwzp_ww_fs.app.models.Coach;
import jwzp_ww_fs.app.models.Event;
import jwzp_ww_fs.app.repositories.EventsRepository;

@ExtendWith(MockitoExtension.class)
public class EventsServiceTest {
    
    @Mock
    private EventsRepository repository;
    @Mock
    private CoachesService coachesService;
    @Mock
    private ClubsService clubsService;

    @BeforeEach
    public void initializeMocks() {
        Event e0 = new Event("E0", DayOfWeek.MONDAY, LocalTime.of(14, 30), Duration.ofHours(3), 1, 1);
        Event e1 = new Event("E1", DayOfWeek.MONDAY, LocalTime.of(23, 30), Duration.ofHours(3), 1, 1);
        Event e2 = new Event("E2", DayOfWeek.TUESDAY, LocalTime.of(1, 0), Duration.ofHours(3), 1, 1);
        Event e3 = new Event("E3", DayOfWeek.TUESDAY, LocalTime.of(17, 0), Duration.ofHours(3), 1, 1);
        Event e4 = new Event("E4", DayOfWeek.TUESDAY, LocalTime.of(5, 0), Duration.ofHours(3), 1, 1);
        Event e5 = new Event("E5", DayOfWeek.TUESDAY, LocalTime.of(5, 0), Duration.ofHours(12), 2, 1);
        Event e6 = new Event("E6", DayOfWeek.TUESDAY, LocalTime.of(17, 0), Duration.ofHours(8), 1, 1);
        Event e7 = new Event("E7", DayOfWeek.WEDNESDAY, LocalTime.of(4, 0), Duration.ofHours(3), 1, 1);
        Event e8 = new Event("E8", DayOfWeek.TUESDAY, LocalTime.of(1, 0), Duration.ofHours(22), 1, 2);

        lenient().when(repository.findAll()).thenReturn(List.of(e0,e1,e2,e3,e4,e5,e6,e7,e8));

        lenient().when(repository.findEventByClubId(1)).thenReturn(List.of(e0,e1,e2,e3,e4,e6,e7,e8));
        lenient().when(repository.findEventByClubId(2)).thenReturn(List.of(e5));

        lenient().when(repository.findEventByCoachId(1)).thenReturn(List.of(e0,e1,e2,e3,e4,e5,e6,e7));
        lenient().when(repository.findEventByCoachId(2)).thenReturn(List.of(e8));

        lenient().when(repository.findEventByClubIdAndCoachId(1,1)).thenReturn(List.of(e0,e1,e2,e3,e4,e6,e7));
        lenient().when(repository.findEventByClubIdAndCoachId(1,2)).thenReturn(List.of(e8));
        lenient().when(repository.findEventByClubIdAndCoachId(2,1)).thenReturn(List.of(e5));
        lenient().when(repository.findEventByClubIdAndCoachId(2,2)).thenReturn(List.of());

        lenient().when(repository.getById(1)).thenReturn(e0);
        lenient().when(repository.findById(1)).thenReturn(Optional.of(e0));
        lenient().when(repository.findById(not(eq(1)))).thenReturn(Optional.empty());
                        
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
    public void addEventTestException(Event eventToAdd, boolean inOpeningHours, Class<?> expectedException) {
        lenient().when(clubsService.isEventInClubOpeningHours(Mockito.any())).thenReturn(inOpeningHours);

        EventsService serviceToTest = new EventsService(repository, clubsService, coachesService);
        Throwable thrown = catchThrowable(() -> serviceToTest.addEvent(eventToAdd));

        assertThat(thrown).isExactlyInstanceOf(expectedException);
    }

    private static Stream<Arguments> incorrectEventsProvider() {
        return Stream.of(
            Arguments.of(new Event("TEST", DayOfWeek.MONDAY, LocalTime.of(15, 30), Duration.ofMinutes(30), 1, 1), true, EventCoachOverlapException.class),
            Arguments.of(new Event("TEST", DayOfWeek.MONDAY, LocalTime.of(0, 0), Duration.ofHours(1), 1, 2), true, EventNoSuchCoachException.class),
            Arguments.of(new Event("TEST", DayOfWeek.MONDAY, LocalTime.of(0, 0), Duration.ofHours(1), 2, 1), true, EventNoSuchClubException.class),
            Arguments.of(new Event("TEST", DayOfWeek.SUNDAY, LocalTime.of(23, 0), Duration.ofHours(2), 1, 1), false, EventNotInOpeningHoursException.class),
            Arguments.of(new Event("TEST", DayOfWeek.FRIDAY, LocalTime.of(7, 0), Duration.ofHours(24).plus(Duration.ofSeconds(1)), 1, 1), true, EventTooLongException.class)
        );
    }

    @ParameterizedTest(name="no exceptions POST {0}")
    @MethodSource("correctEventsProvider")
    public void addEventTestNoException(Event eventToAdd) {
        when(clubsService.isEventInClubOpeningHours(Mockito.any())).thenReturn(true);

        EventsService serviceToTest = new EventsService(repository, clubsService, coachesService);
        
        assertDoesNotThrow(() -> serviceToTest.addEvent(eventToAdd));
    }

    private static Stream<Arguments> correctEventsProvider() {
        return Stream.of(
            Arguments.of(new Event("Exacly Between", DayOfWeek.MONDAY, LocalTime.of(17, 30), Duration.ofHours(6), 1, 1)),
            Arguments.of(new Event("Over Midnight", DayOfWeek.FRIDAY, LocalTime.of(6, 0), Duration.ofHours(23), 1, 1)),
            Arguments.of(new Event("One Minute", DayOfWeek.FRIDAY, LocalTime.of(6, 0), Duration.ofMinutes(1), 1, 1)),
            Arguments.of(new Event("Full Day", DayOfWeek.FRIDAY, LocalTime.of(7, 0), Duration.ofHours(24), 1, 1))
        );
    }

    //PATCH

    @ParameterizedTest(name="exceptions PATCH {1}")
    @MethodSource("incorrectUpdateEventsProvider")
    public void updateEventTestException(int eventId, Event updatedEvent, boolean inOpeningHours, Class<?> expectedException) {
        lenient().when(clubsService.isEventInClubOpeningHours(Mockito.any())).thenReturn(inOpeningHours);

        EventsService serviceToTest = new EventsService(repository, clubsService, coachesService);
        Throwable thrown = catchThrowable(() -> serviceToTest.updateEvent(eventId, updatedEvent));

        assertThat(thrown).isExactlyInstanceOf(expectedException);
    }

    private static Stream<Arguments> incorrectUpdateEventsProvider() {
        return Stream.of(
            Arguments.of(2, new Event("TEST", DayOfWeek.MONDAY, LocalTime.of(15, 30), Duration.ofMinutes(30), 1, 1), true, EventDoesNotExistException.class),
            Arguments.of(1, new Event("TEST", DayOfWeek.MONDAY, LocalTime.of(14, 30), Duration.ofHours(10), 1, 1), true, EventCoachOverlapException.class),
            Arguments.of(1, new Event("TEST", DayOfWeek.MONDAY, LocalTime.of(14, 30), Duration.ofHours(2), 1, 2), true, EventNoSuchCoachException.class),
            Arguments.of(1, new Event("TEST", DayOfWeek.MONDAY, LocalTime.of(14, 30), Duration.ofHours(2), 2, 1), true, EventNoSuchClubException.class),
            Arguments.of(1, new Event("TEST", DayOfWeek.SUNDAY, LocalTime.of(23, 0), Duration.ofHours(2), 1, 1), false, EventNotInOpeningHoursException.class),
            Arguments.of(1, new Event("TEST", DayOfWeek.FRIDAY, LocalTime.of(7, 0), Duration.ofHours(24).plus(Duration.ofSeconds(1)), 1, 1), true, EventTooLongException.class)
        );
    }

    @ParameterizedTest(name="no exceptions PATCH {1}")
    @MethodSource("correctUpdateEventsProvider")
    public void updateEventTestNoException(int eventId, Event eventToAdd) {
        when(clubsService.isEventInClubOpeningHours(Mockito.any())).thenReturn(true);

        EventsService serviceToTest = new EventsService(repository, clubsService, coachesService);
        
        Event oldEvent = assertDoesNotThrow(() -> serviceToTest.updateEvent(eventId, eventToAdd));
        assertThat(oldEvent).isEqualTo(repository.getById(eventId));
    }

    private static Stream<Arguments> correctUpdateEventsProvider() {
        return Stream.of(
            Arguments.of(1, new Event("Exacly Between", DayOfWeek.MONDAY, LocalTime.of(17, 30), Duration.ofHours(6), 1, 1)),
            Arguments.of(1, new Event("Over Midnight", DayOfWeek.FRIDAY, LocalTime.of(6, 0), Duration.ofHours(23), 1, 1)),
            Arguments.of(1, new Event("One Minute", DayOfWeek.FRIDAY, LocalTime.of(6, 0), Duration.ofMinutes(1), 1, 1)),
            Arguments.of(1, new Event("Full Day", DayOfWeek.FRIDAY, LocalTime.of(7, 0), Duration.ofHours(24), 1, 1))
        );
    }

    //DELETE
    @Test
    public void deleteEventTestNoException() {
        EventsService serviceToTest = new EventsService(repository, clubsService, coachesService);
        
        Event deletedEvent = assertDoesNotThrow(() -> serviceToTest.removeEvent(1));
        assertThat(deletedEvent).isEqualTo(repository.getById(1));
    }

    @Test
    public void deleteEventTestException() {
        EventsService serviceToTest = new EventsService(repository, clubsService, coachesService);
        
        Throwable thrown = catchThrowable(() -> serviceToTest.removeEvent(2));
        assertThat(thrown).isExactlyInstanceOf(EventDoesNotExistException.class);
    }

    @Test
    public void deleteAllEventsTest() {
        EventsService serviceToTest = new EventsService(repository, clubsService, coachesService);
     
        List<Event> oldEvent = assertDoesNotThrow(serviceToTest::removeAllEvents);
        assertThat(oldEvent).containsExactlyInAnyOrderElementsOf(repository.findAll());
    }

    //GET

    @Test
    public void getAllEventsTest() {
        EventsService serviceToTest = new EventsService(repository, clubsService, coachesService);

        List<Event> events = assertDoesNotThrow(serviceToTest::getAllEvents);
        assertThat(events).containsExactlyInAnyOrderElementsOf(repository.findAll());
    }

    @ParameterizedTest(name="GET coach {0}")
    @MethodSource("getEventByCoachProvider")
    public void getEventByCoachTest(int coachId) {
        List<Event> expectedEvents = repository.findAll().stream().filter(e -> e.coachId() == coachId).toList();

        EventsService serviceToTest = new EventsService(repository, clubsService, coachesService);
        
        List<Event> events = assertDoesNotThrow(() -> serviceToTest.getEventsByCoach(coachId));
        assertThat(events).containsExactlyInAnyOrderElementsOf(expectedEvents);
    }

    private static Stream<Arguments> getEventByCoachProvider() {
        return Stream.of(
            Arguments.of(1), Arguments.of(2), Arguments.of(3)
        );
    }

    @ParameterizedTest(name="GET club {0}")
    @MethodSource("getEventByClubProvider")
    public void getEventByClubTest(int clubId) {
        List<Event> expectedEvents = repository.findAll().stream().filter(e -> e.clubId() == clubId).toList();

        EventsService serviceToTest = new EventsService(repository, clubsService, coachesService);
        
        List<Event> events = assertDoesNotThrow(() -> serviceToTest.getEventsByClub(clubId));
        assertThat(events).containsExactlyInAnyOrderElementsOf(expectedEvents);
    }

    private static Stream<Arguments> getEventByClubProvider() {
        return Stream.of(
            Arguments.of(1), Arguments.of(2), Arguments.of(3)
        );
    }

    @ParameterizedTest(name="GET club {0} coach {1}")
    @MethodSource("getEventByClubAndCoachProvider")
    public void getEventByClubAndCoachTest(int clubId, int coachId) {
        List<Event> expectedEvents = repository.findAll().stream().filter(e -> e.clubId() == clubId && e.coachId() == coachId).toList();

        EventsService serviceToTest = new EventsService(repository, clubsService, coachesService);
        
        List<Event> events = assertDoesNotThrow(() -> serviceToTest.getEventsByCoachAndClub(coachId, clubId));
        assertThat(events).containsExactlyInAnyOrderElementsOf(expectedEvents);
    }

    private static Stream<Arguments> getEventByClubAndCoachProvider() {
        return Stream.of(
            Arguments.of(1,1), Arguments.of(2,1), Arguments.of(1,2), Arguments.of(2,2), Arguments.of(3,1)
        );
    }
}
