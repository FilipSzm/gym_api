 package jwzp_ww_fs.app.services;

 import jwzp_ww_fs.app.models.Coach;
 import jwzp_ww_fs.app.repositories.CoachRepository;
 import org.junit.jupiter.api.BeforeEach;
 import org.junit.jupiter.api.extension.ExtendWith;
 import org.junit.jupiter.params.ParameterizedTest;
 import org.junit.jupiter.params.provider.Arguments;
 import org.junit.jupiter.params.provider.MethodSource;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.junit.jupiter.MockitoExtension;

 import java.time.Year;
 import java.util.List;
 import java.util.stream.Stream;

 import static org.assertj.core.api.Assertions.assertThat;
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

         lenient().when(repository.save(Mockito.any())).thenAnswer(i -> i.getArguments()[0]);

         lenient().when(repository.findAll()).thenReturn(exampleCoaches);
     }

     //POST

     @ParameterizedTest(name="POST {0}")
     @MethodSource("coachesProvider")
     public void addCoachTest(Coach coachToAdd) {
         CoachesService service = new CoachesService(repository);

         var uut = service.addCoach(coachToAdd);

         assertThat(uut).isEqualTo(coachToAdd);
         verify(repository).save(coachToAdd);
     }

     private static Stream<Arguments> coachesProvider() {
         return Stream.of(
                 Arguments.of(new Coach("N1", "S1", Year.of(2000))),
                 Arguments.of(new Coach("N4", "S4", Year.of(2007)))
         );
     }
 }
