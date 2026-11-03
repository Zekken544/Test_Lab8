package mn.edu.must.sqat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {
    private static final String ID = "S001";
    private static final String COURSE = "CS201";
    private static final String EMAIL = "student@example.test";

    @Mock StudentRepository repository;
    @Mock EmailSender emailSender;
    CourseRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new CourseRegistrationService(repository, emailSender);
    }

    private void stubEligibleStudent() {
        when(repository.findStudent(ID))
                .thenReturn(new Student(ID, EMAIL, true, List.of("CS101")));
        when(repository.courseExists(COURSE)).thenReturn(true);
        when(repository.prerequisites(COURSE)).thenReturn(List.of("CS101"));
    }

    @Test
    void register_activeEligibleStudent_savesAndEmails() {
        stubEligibleStudent();

        assertEquals("OK", service.register(ID, COURSE));

        verify(repository).saveRegistration(ID, COURSE);
        verify(emailSender).send(eq(EMAIL), anyString(), anyString());
    }

    @Test
    void register_missingStudent_rejectsWithoutSavingOrEmailing() {
        when(repository.findStudent(ID)).thenReturn(null);

        assertEquals("ERROR_NO_STUDENT", service.register(ID, COURSE));

        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void register_inactiveStudent_rejectsWithoutEmailing() {
        when(repository.findStudent(ID))
                .thenReturn(new Student(ID, EMAIL, false, List.of()));

        assertEquals("ERROR_INACTIVE_STUDENT", service.register(ID, COURSE));

        verifyNoInteractions(emailSender);
    }

    @Test
    void register_missingPrerequisite_rejectsWithoutSaving() {
        when(repository.findStudent(ID))
                .thenReturn(new Student(ID, EMAIL, true, List.of()));
        when(repository.courseExists(COURSE)).thenReturn(true);
        when(repository.prerequisites(COURSE)).thenReturn(List.of("CS101"));

        assertEquals("ERROR_PREREQUISITES", service.register(ID, COURSE));

        verify(repository, never()).saveRegistration(anyString(), anyString());
    }

    @Test
    void register_emailThrows_keepsSavedRegistration() {
        stubEligibleStudent();
        doThrow(new RuntimeException("Email unavailable"))
                .when(emailSender).send(eq(EMAIL), anyString(), anyString());

        assertEquals("OK", service.register(ID, COURSE));

        verify(repository).saveRegistration(ID, COURSE);
        verify(emailSender).send(eq(EMAIL), anyString(), anyString());
    }

    @Test
    void register_eitherArgumentNull_throwsWithoutCallingDependencies() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.register(null, COURSE)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.register(ID, null)),
                () -> verifyNoInteractions(repository, emailSender)
        );
    }

    @Test
    void register_confirmationSent_emailBodyContainsCourseCode() {
        stubEligibleStudent();
        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);

        assertEquals("OK", service.register(ID, COURSE));

        verify(emailSender).send(eq(EMAIL), anyString(), body.capture());
        assertTrue(body.getValue().contains(COURSE));
    }

    @ParameterizedTest(name = "{0} returns {1}")
    @CsvSource({
            "missing-student, ERROR_NO_STUDENT",
            "inactive-student, ERROR_INACTIVE_STUDENT",
            "missing-course, ERROR_NO_COURSE"
    })
    void register_rejectedScenario_returnsExpectedError(String scenario, String expected) {
        switch (scenario) {
            case "missing-student" ->
                    when(repository.findStudent(ID)).thenReturn(null);
            case "inactive-student" ->
                    when(repository.findStudent(ID))
                            .thenReturn(new Student(ID, EMAIL, false, List.of()));
            case "missing-course" -> {
                when(repository.findStudent(ID))
                        .thenReturn(new Student(ID, EMAIL, true, List.of()));
                when(repository.courseExists(COURSE)).thenReturn(false);
            }
            default -> throw new AssertionError("Unknown scenario: " + scenario);
        }

        assertEquals(expected, service.register(ID, COURSE));

        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }
}
