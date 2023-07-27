package gr.aegean.service;

import gr.aegean.service.analysis.GitHubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;


class GitHubServiceTest {
    private GitHubService underTest;

    @BeforeEach
    void setup() {
        underTest = new GitHubService();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "https://gitlab.com/user/repo", // different host
            "https://github.com@user/repo",
            "https://github.com.malicious.com"//malicious url
    })
    void shouldReturnFalseForInvalidHostInTheURI(String url) {
        boolean isValid = underTest.isValidGitHubUrl(url);

        assertThat(isValid).isFalse();
    }
}
