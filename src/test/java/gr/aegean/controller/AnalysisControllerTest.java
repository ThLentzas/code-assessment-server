package gr.aegean.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gr.aegean.config.security.AuthConfig;
import gr.aegean.config.security.SecurityConfig;
import gr.aegean.config.security.JwtConfig;
import gr.aegean.repository.UserRepository;
import gr.aegean.service.analysis.AnalysisService;
import gr.aegean.service.analysis.ProcessProjectService;
import gr.aegean.config.DeserializerConfig;
import gr.aegean.service.auth.AppUserDetailsService;


/*
    The @Valid annotation is tested in the POST request for creating the analysis, by validating constraints and
    preferences. We don't have to test for the refresh request. Same process.
 */
@WebMvcTest(AnalysisController.class)
@Import({
        SecurityConfig.class,
        AuthConfig.class,
        JwtConfig.class,
        AppUserDetailsService.class,
        DeserializerConfig.class})
class AnalysisControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ProcessProjectService processProjectService;
    @MockBean
    private AnalysisService analysisService;
    @MockBean
    private UserRepository userRepository;
    private static final String ANALYSIS_PATH = "/api/v1/analysis";

    @Test
    void shouldReturnHTTP401WhenAnalyzeIsCalledByUnauthenticatedUser() throws Exception {
        String requestBody = """
                {
                    "projectUrls": [
                        "https://github.com/repo1",
                        "https://github.com/repo2"
                    ],
                    "constraints": [
                        {
                            "qualityMetric": "CYCLOMATIC_COMPLEXITY",
                            "operator": ">",
                            "threshold": 0.85
                        }
                    ]
                }
                """;

        mockMvc.perform(post(ANALYSIS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(processProjectService);
    }

    /*
        @NullAndEmptySource only works for Strings, not for a List<String>
     */
    @Test
    @WithMockUser(username = "test")
    void shouldReturnHTTP400WhenListOfProjectUrlsIsEmpty() throws Exception {
        String requestBody = """
                {
                    "projectUrls": []
                }
                """;
        String responseBody = """
                {
                    "message": "Provide at least one GitHub url repository",
                    "statusCode": 400
                }
                """;

        mockMvc.perform(post(ANALYSIS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().json(responseBody)
                );

        verifyNoInteractions(processProjectService);
    }

    /*
        @NullAndEmptySource only works for Strings, not for a List<String>
     */
    @Test
    @WithMockUser(username = "test")
    void shouldReturnHTTP400WhenListOfProjectUrlsIsNull() throws Exception {
        String requestBody = """
                {
                    "projectUrls": null
                }
                """;
        String responseBody = """
                {
                    "message": "Provide at least one GitHub url repository",
                    "statusCode": 400
                }
                """;

        mockMvc.perform(post(ANALYSIS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().json(responseBody)
                );

        verifyNoInteractions(processProjectService);
    }

    /*
        The empty case will be handled by the deserializer as an invalid value and is tested in the deserializer's tests
        Also no need for parameterized test because we only check for null
     */
    @Test
    @WithMockUser(username = "test")
    void shouldReturnHTTP400WhenConstraintQualityMetricIsNull() throws Exception {
        String requestBody = """ 
                {
                    "projectUrls": [
                        "https://github.com/user/test"
                    ],
                    "constraints": [{
                            "qualityMetric": null,
                            "qualityMetricOperator": "<=",
                            "threshold": 1.0
                        }, {
                            "qualityMetric": "HOTSPOT_PRIORITY",
                            "qualityMetricOperator": "<=",
                            "threshold": 1.0
                        }
                    ],
                    "preferences": [{
                        "qualityAttribute": "QUALITY",
                        "weight": 0.4
                        }, {
                        "qualityAttribute": "SECURITY",
                        "weight": 0.6
                         }
                    ]
                }
                """;
        String responseBody = """
                {
                    "message": "Quality metric is required",
                    "statusCode": 400
                }
                """;

        mockMvc.perform(post(ANALYSIS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().json(responseBody)
                );

        verifyNoInteractions(processProjectService);
    }

    /*
        The empty case will be handled by the deserializer as an invalid value and is tested in the deserializer's tests
        Also no need for parameterized test because we only check for null
     */
    @Test
    @WithMockUser(username = "test")
    void shouldReturnHTTP400WhenConstraintQualityMetricOperatorIsNull() throws Exception {
        String requestBody = """ 
                {
                    "projectUrls": [
                        "https://github.com/user/test"
                    ],
                    "constraints": [{
                            "qualityMetric": "BUG_SEVERITY",
                            "qualityMetricOperator": null,
                            "threshold": 1.0
                        }
                    ],
                    "preferences": [{
                        "qualityAttribute": "QUALITY",
                        "weight": 0.4
                        }
                    ]
                }
                """;
        String responseBody = """
                {
                    "message": "Quality metric operator is required",
                    "statusCode": 400
                }
                """;

        mockMvc.perform(post(ANALYSIS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().json(responseBody)
                );

        verifyNoInteractions(processProjectService);
    }

    /*
        For threshold, we can not combine the two @NullSource and @EmptySource because we have different error messages
     */
    @Test
    @WithMockUser(username = "test")
    void shouldReturnHTTP400WhenConstraintThresholdIsNull() throws Exception {
        String requestBody = """ 
                {
                    "projectUrls": [
                        "https://github.com/user/test"
                    ],
                    "constraints": [{
                            "qualityMetric": "BUG_SEVERITY",
                            "qualityMetricOperator": "<=",
                            "threshold": null
                        }
                    ],
                    "preferences": [{
                        "qualityAttribute": "QUALITY",
                        "weight": 0.4
                        }
                    ]
                }
                """;
        String responseBody = """
                {
                    "message": "Threshold is required",
                    "statusCode": 400
                }
                """;

        mockMvc.perform(post(ANALYSIS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().json(responseBody)
                );

        verifyNoInteractions(processProjectService);
    }

    /*
        For threshold, we can not combine the two @NullSource and @EmptySource because we have different error messages
     */
    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 1.1})
    @WithMockUser(username = "test")
    void shouldReturnHTTP400WhenConstraintThresholdIsInvalid(Double thresholdValue) throws Exception {
        String threshold = thresholdValue.toString();
        String requestBody = String.format("""
                {
                    "projectUrls": [
                        "https://github.com/user/test"
                    ],
                    "constraints": [{
                            "qualityMetric": "BUG_SEVERITY",
                            "qualityMetricOperator": "<=",
                            "threshold": %s
                        }
                    ],
                    "preferences": [{
                        "qualityAttribute": "QUALITY",
                        "weight": 0.4
                        }
                    ]
                }
                """, threshold);
        String responseBody = """
                {
                    "message": "Threshold value must be in the range of [0.0 - 1.0]",
                    "statusCode": 400
                }
                """;

        mockMvc.perform(post("/api/v1/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().json(responseBody)
                );

        verifyNoInteractions(processProjectService);
    }

    /*
        The empty case will be handled by the deserializer as an invalid value and is tested in the deserializer's tests
        Also no need for parameterized test because we only check for null
     */
    @Test
    @WithMockUser(username = "test")
    void shouldReturnHTTP400WhenPreferenceQualityAttributeIsNull() throws Exception {
        String requestBody = """ 
                {
                    "projectUrls": [
                        "https://github.com/user/test"
                    ],
                    "constraints": [{
                            "qualityMetric": "BUG_SEVERITY",
                            "qualityMetricOperator": "<=",
                            "threshold": 1.0
                        }
                    ],
                    "preferences": [{
                        "qualityAttribute": null,
                        "weight": 0.4
                        }
                    ]
                }
                """;
        String responseBody = """
                {
                    "message": "Quality attribute is required",
                    "statusCode": 400
                }
                """;

        mockMvc.perform(post(ANALYSIS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().json(responseBody)
                );

        verifyNoInteractions(processProjectService);
    }

    /*
        For weight, we can not combine the two @NullSource and @EmptySource because we have different error messages
     */
    @Test
    @WithMockUser(username = "test")
    void shouldReturnHTTP400WhenPreferenceWeightIsNull() throws Exception {
        String requestBody = """ 
                {
                    "projectUrls": [
                        "https://github.com/user/test"
                    ],
                    "constraints": [{
                            "qualityMetric": "BUG_SEVERITY",
                            "qualityMetricOperator": "<=",
                            "threshold": 1.0
                        }
                    ],
                    "preferences": [{
                        "qualityAttribute": "QUALITY",
                        "weight": null
                        }
                    ]
                }
                """;
        String responseBody = """
                {
                    "message": "Weight is required",
                    "statusCode": 400
                }
                """;

        mockMvc.perform(post(ANALYSIS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().json(responseBody)
                );

        verifyNoInteractions(processProjectService);
    }

    /*
        For weight, we can not combine the two @NullSource and @EmptySource because we have different error messages
     */
    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 1.1})
    @WithMockUser(username = "test")
    void shouldReturnHTTP400WhenPreferenceWeightIsInvalid(Double weightValue) throws Exception {
        String weight = weightValue.toString();
        String requestBody = String.format("""
                {
                    "projectUrls": [
                        "https://github.com/user/test"
                    ],
                    "constraints": [{
                            "qualityMetric": "BUG_SEVERITY",
                            "qualityMetricOperator": "<=",
                            "threshold": 1.0
                        }
                    ],
                    "preferences": [{
                        "qualityAttribute": "QUALITY",
                        "weight": %s
                        }
                    ]
                }
                """, weight);
        String responseBody = """
                {
                    "message": "Weight value must be in the range of [0.0 - 1.0]",
                    "statusCode": 400
                }
                """;

        mockMvc.perform(post("/api/v1/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().json(responseBody)
                );

        verifyNoInteractions(processProjectService);
    }

    @Test
    void shouldReturnHTTP401WhenGetAnalysisResultIsCalledByUnauthenticatedUser() throws Exception {
        mockMvc.perform(get(ANALYSIS_PATH + "/{analysisId}", 1))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(processProjectService);
    }

    /*
        The refresh request validation for constraints and preferences is the same with the analysis request,
        which is already tested
     */
    @Test
    void shouldReturnHTTP401WhenRefreshAnalysisResultIsCalledByUnauthenticatedUser() throws Exception {
        String requestBody = """
                {
                    "constraints": [
                        {
                            "qualityMetric": "CYCLOMATIC_COMPLEXITY",
                            "operator": ">",
                            "threshold": 0.85
                        }
                    ]
                }
                """;

        mockMvc.perform(put(ANALYSIS_PATH + "/{analysisId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(processProjectService);
    }

    @Test
    void shouldReturnHTTP401WhenDeleteAnalysisIsCalledByUnauthenticatedUser() throws Exception {
        mockMvc.perform(delete(ANALYSIS_PATH + "/{analysisId}", 1))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(processProjectService);
    }

    @Test
    void shouldReturnHTTP401WhenGetAnalysisRequestIsCalledByUnauthenticatedUser() throws Exception {
        mockMvc.perform(get(ANALYSIS_PATH + "/{analysisId}/request", 1))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(processProjectService);
    }
}
