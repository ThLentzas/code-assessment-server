package gr.aegean.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import gr.aegean.exception.BadCredentialsException;
import gr.aegean.model.user.User;
import gr.aegean.model.user.UserPrincipal;
import gr.aegean.security.auth.AuthResponse;
import gr.aegean.security.auth.RegisterRequest;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    private AuthService underTest;

    @BeforeEach
    void setup() {
        underTest = new AuthService(userService, passwordEncoder, jwtService);
    }

    @Test
    void shouldRegisterUserAndReturnJwtToken() {
        //Arrange
        RegisterRequest request = new RegisterRequest(
                "Test",
                "Test",
                "TestT",
                "test@gmail.com",
                "CyN549^*o2Cr",
                "I have a real passion for teaching",
                "Cleveland, OH",
                "Code Monkey, LLC"
        );

        User user = new User(
                request.firstname(),
                request.lastname(),
                request.username(),
                request.email(),
                request.password(),
                request.bio(),
                request.location(),
                request.company());

        String jwtToken = "jwtToken";
        Integer generatedID = 1;

        when(passwordEncoder.encode(user.getPassword())).thenReturn("hashedPassword");
        when(userService.registerUser(any(User.class))).thenReturn(generatedID);
        when(jwtService.assignToken(any(UserPrincipal.class))).thenReturn(jwtToken);

        //Act
        AuthResponse authResponse = underTest.register(request);

        //Assert
        assertThat(authResponse.getId()).isEqualTo(generatedID);
        assertThat(authResponse.getToken()).isEqualTo(jwtToken);

        verify(passwordEncoder, times(1)).encode(user.getPassword());
        verify(userService, times(1)).registerUser(any(User.class));
        verify(jwtService, times(1)).assignToken(any(UserPrincipal.class));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void shouldThrowBadCredentialsExceptionWhenRequestFirstnameIsNullOrEmpty(String firstname) {
        //Arrange
        RegisterRequest request = new RegisterRequest(
                firstname,
                "Test",
                "TestT",
                "test@gmail.com",
                "CyN549^*o2Cr",
                "I have a real passion for teaching",
                "Cleveland, OH",
                "Code Monkey, LLC"
        );

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("The First Name field is required.");
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void shouldThrowBadCredentialsExceptionWhenRequestLastnameIsNullOrEmpty(String lastname) {
        //Arrange
        RegisterRequest request = new RegisterRequest(
                "Test",
                lastname,
                "TestT",
                "test@gmail.com",
                "CyN549^*o2Cr",
                "I have a real passion for teaching",
                "Cleveland, OH",
                "Code Monkey, LLC"
        );

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("The Last Name field is required.");
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void shouldThrowBadCredentialsExceptionWhenRequestUsernameIsNullOrEmpty(String username) {
        //Arrange
        RegisterRequest request = new RegisterRequest(
                "Test",
                "Test",
                username,
                "test@gmail.com",
                "CyN549^*o2Cr",
                "I have a real passion for teaching",
                "Cleveland, OH",
                "Code Monkey, LLC"
        );

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("The Username field is required.");
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void shouldThrowBadCredentialsExceptionWhenRequestEmailIsNullOrEmpty(String email) {
        //Arrange
        RegisterRequest request = new RegisterRequest(
                "Test",
                "Test",
                "TestT",
                email,
                "CyN549^*o2Cr",
                "I have a real passion for teaching",
                "Cleveland, OH",
                "Code Monkey, LLC"
        );

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("The Email field is required.");
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void shouldThrowBadCredentialsExceptionWhenRequestPasswordIsNullOrEmpty(String password) {
        //Arrange
        RegisterRequest request = new RegisterRequest(
                "Test",
                "Test",
                "TestT",
                "test@gmail.com",
                password,
                "I have a real passion for teaching",
                "Cleveland, OH",
                "Code Monkey, LLC"
        );

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("The Password field is required.");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRegisterFirstnameExceedsMaxLength() {
        Random random = new Random();
        RegisterRequest request = new RegisterRequest(
                generateRandomString(random.nextInt(31) + 31),
                "Test",
                "TestT",
                "test@gmail.com",
                "CyN549^*o2Cr",
                "I have a real passion for teaching",
                "Cleveland, OH",
                "Code Monkey, LLC"
        );

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid firstname. Too many characters");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRegisterFirstnameContainsNumbers() {
        //Arrange
        RegisterRequest request = new RegisterRequest(
                "T3st",
                "Test",
                "TestT",
                "test@gmail.com",
                "CyN549^*o2Cr",
                "I have a real passion for teaching",
                "Cleveland, OH",
                "Code Monkey, LLC"
        );

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid firstname. Name should contain only characters");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRegisterFirstnameContainsSpecialCharacters() {
        //Arrange
        RegisterRequest request = new RegisterRequest(
                "T^st",
                "Test",
                "TestT",
                "test@gmail.com",
                "CyN549^*o2Cr",
                "I have a real passion for teaching",
                "Cleveland, OH",
                "Code Monkey, LLC"
        );

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid firstname. Name should contain only characters");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRegisterLastnameExceedsMaxLength() {
        Random random = new Random();
        RegisterRequest request = new RegisterRequest(
                "Test",
                generateRandomString(random.nextInt(31) + 31),
                "TestT",
                "test@gmail.com",
                "CyN549^*o2Cr",
                "I have a real passion for teaching",
                "Cleveland, OH",
                "Code Monkey, LLC"
        );

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid lastname. Too many characters");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRegisterLastnameContainsNumbers() {
        //Arrange
        RegisterRequest request = new RegisterRequest(
                "Test",
                "T3st",
                "TestT",
                "test@gmail.com",
                "CyN549^*o2Cr",
                "I have a real passion for teaching",
                "Cleveland, OH",
                "Code Monkey, LLC"
        );

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid lastname. Name should contain only characters");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRegisterLastnameContainsSpecialCharacters() {
        //Arrange
        RegisterRequest request = new RegisterRequest(
                "Test",
                "T^st",
                "TestT",
                "test@gmail.com",
                "CyN549^*o2Cr",
                "I have a real passion for teaching",
                "Cleveland, OH",
                "Code Monkey, LLC"
        );

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid lastname. Name should contain only characters");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRegisterUsernameExceedsMaxLength() {
        Random random = new Random();
        RegisterRequest request = new RegisterRequest(
                "Test",
                "TestT",
                generateRandomString(random.nextInt(31) + 31),
                "test@gmail.com",
                "CyN549^*o2Cr",
                "I have a real passion for teaching",
                "Cleveland, OH",
                "Code Monkey, LLC"
        );

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid username. Too many characters");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRegisterEmailExceedsMaxLength() {
        Random random = new Random();
        RegisterRequest request = new RegisterRequest(
                "Test",
                "Test",
                "TestT",
                generateRandomString(random.nextInt(51) + 51),
                "CyN549^*o2Cr",
                "I have a real passion for teaching",
                "Cleveland, OH",
                "Code Monkey, LLC"
        );

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email. Too many characters");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRegisterEmailDoesNotContainAtSymbol() {
        RegisterRequest request = new RegisterRequest(
                "Test",
                "Test",
                "TestT",
                "testgmail.com",
                "CyN549^*o2Cr",
                "I have a real passion for teaching",
                "Cleveland, OH",
                "Code Monkey, LLC"
        );

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRegisterLocationExceedsMaxLength() {
        Random random = new Random();
        RegisterRequest request = new RegisterRequest(
                "Test",
                "Test",
                "TestT",
                "test@gmail.com",
                "CyN549^*o2Cr",
                "I have a real passion for teaching",
                generateRandomString(random.nextInt(51) + 51),
                "Code Monkey, LLC"
        );

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid location. Too many characters");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRegisterCompanyExceedsMaxLength() {
        Random random = new Random();
        RegisterRequest request = new RegisterRequest(
                "Test",
                "Test",
                "TestT",
                "test@gmail.com",
                "CyN549^*o2Cr",
                "I have a real passion for teaching",
                "Cleveland, OH",
                generateRandomString(random.nextInt(51) + 51)
        );

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid company. Too many characters");
    }

    private String generateRandomString(int length) {
        return RandomStringUtils.randomAlphabetic(length);
    }
}
