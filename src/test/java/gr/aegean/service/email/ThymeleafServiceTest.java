package gr.aegean.service.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticApplicationContext;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class ThymeleafServiceTest {
    private TemplateEngine templateEngine;
    private ThymeleafService underTest;

    /*
        Since we don't have an autoconfigured @Bean of TemplateEngine by Spring, we have to set up one.
     */
    @BeforeEach
    void setup() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(new StaticApplicationContext());
        templateResolver.setPrefix("classpath:/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        underTest = new ThymeleafService(templateEngine);
    }

    @Test
    void shouldSetContextForPasswordResetSuccessHtmlEmailTemplateCorrectly() throws Exception {
        //Arrange
        String path = "src/test/resources/templates/password_reset_success_test.html";
        String actual = new String(Files.readAllBytes(Paths.get(path)));

        //Act
        String expected = underTest.setPasswordResetSuccessEmailContext(
                "Test",
                "http://test@example",
                "http://someurl/password_reset"
        );

        //Assert
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldSetContextForPasswordResetRequestHtmlEmailTemplateCorrectly() throws Exception {
        //Arrange
        String path = "src/test/resources/templates/password_reset_request_test.html";
        String actual = new String(Files.readAllBytes(Paths.get(path)));

        //Act
        String expected = underTest.setPasswordResetEmailContext(
                "http://someurl/confirm?token=token",
                "http://someurl/password_reset"
        );

        //Assert
        assertThat(actual).isEqualTo(expected);
    }


    @Test
    void shouldSetContextForEmailVerificationHtmlEmailTemplateCorrectly() throws Exception {
        //Arrange
        String path = "src/test/resources/templates/email_verification_test.html";
        String actual = new String(Files.readAllBytes(Paths.get(path)));

        //Act
        String expected = underTest.setEmailVerificationContext(
                "Test",
                "http://someurl/email?token=token",
                "http://someurl/email"
        );

        //Assert
        assertThat(actual).isEqualTo(expected);
    }
}
