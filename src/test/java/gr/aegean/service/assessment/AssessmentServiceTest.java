package gr.aegean.service.assessment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gr.aegean.entity.AnalysisReport;
import gr.aegean.entity.Constraint;
import gr.aegean.entity.Preference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import static org.mockito.Mockito.verifyNoInteractions;


@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {
    @Mock
    private RankingService rankingService;
    @Mock
    private FilteringService filteringService;
    private final ObjectMapper mapper = new ObjectMapper();
    private AssessmentService underTest;

    @BeforeEach
    void setup() {
        underTest = new AssessmentService(rankingService, filteringService);
    }

    @Test
    void shouldNotFilterReportsWhenNoConstraintsAreProvided() throws IOException {
        //Arrange
        List<Constraint> constraints = new ArrayList<>();
        List<Preference> preferences = new ArrayList<>();

        String analysisReportPath = "src/test/resources/reports/analysis-reports.json";
        CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, AnalysisReport.class);
        List<AnalysisReport> reports = mapper.readValue(new File(analysisReportPath), type);

        //Act
        underTest.assessAnalysisResult(reports, constraints, preferences);

        //Assert
        verifyNoInteractions(filteringService);
    }
}
