package gr.aegean.service.assessment;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import gr.aegean.entity.Preference;
import gr.aegean.model.analysis.quality.QualityAttribute;
import gr.aegean.model.analysis.quality.QualityMetric;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/*
    The values in these tests are actual values from reports.
 */
class RankingServiceTest {
    private RankingService underTest;

    @BeforeEach
    void setup() {
        TreeService treeService = new TreeService();
        underTest = new RankingService(treeService);
    }

    @Test
    void shouldRankReportsWhenNoPreferencesAreProvided() {
        //Arrange
        List<Preference> preferences = new ArrayList<>();
        Map<QualityMetric, Double> metricsReport = new EnumMap<>(QualityMetric.class);
        double expected = 0.775794217930906;

        metricsReport.put(QualityMetric.COMMENT_RATE, 0.6662906694752284);
        metricsReport.put(QualityMetric.METHOD_SIZE, 1.0);
        metricsReport.put(QualityMetric.DUPLICATION, 1.0);
        metricsReport.put(QualityMetric.BUG_SEVERITY, 1.0);
        metricsReport.put(QualityMetric.TECHNICAL_DEBT_RATIO, 0.991);
        metricsReport.put(QualityMetric.RELIABILITY_REMEDIATION_EFFORT, 1.0);
        metricsReport.put(QualityMetric.CYCLOMATIC_COMPLEXITY, 0.7755834829443446);
        metricsReport.put(QualityMetric.COGNITIVE_COMPLEXITY, 0.6122082585278277);
        metricsReport.put(QualityMetric.VULNERABILITY_SEVERITY, 1.0);
        metricsReport.put(QualityMetric.HOTSPOT_PRIORITY, 0.041353383458646614);
        metricsReport.put(QualityMetric.SECURITY_REMEDIATION_EFFORT, 1.0);

        //Act
        double actual = underTest.rankTree(metricsReport, preferences);

        //Assert
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldRankReportsWhenPreferencesAreProvided() {
        //Arrange
        List<Preference> preferences = new ArrayList<>();
        Map<QualityMetric, Double> metricsReport = new EnumMap<>(QualityMetric.class);
        double expected = 0.7670978989659297;

        preferences.add(new Preference(QualityAttribute.SIMPLICITY, 0.34));
        preferences.add(new Preference(QualityAttribute.SECURITY_REMEDIATION_EFFORT, 0.25));

        metricsReport.put(QualityMetric.COMMENT_RATE, 0.6662906694752284);
        metricsReport.put(QualityMetric.METHOD_SIZE, 1.0);
        metricsReport.put(QualityMetric.DUPLICATION, 1.0);
        metricsReport.put(QualityMetric.BUG_SEVERITY, 1.0);
        metricsReport.put(QualityMetric.TECHNICAL_DEBT_RATIO, 0.991);
        metricsReport.put(QualityMetric.RELIABILITY_REMEDIATION_EFFORT, 1.0);
        metricsReport.put(QualityMetric.CYCLOMATIC_COMPLEXITY, 0.7755834829443446);
        metricsReport.put(QualityMetric.COGNITIVE_COMPLEXITY, 0.6122082585278277);
        metricsReport.put(QualityMetric.VULNERABILITY_SEVERITY, 1.0);
        metricsReport.put(QualityMetric.HOTSPOT_PRIORITY, 0.041353383458646614);
        metricsReport.put(QualityMetric.SECURITY_REMEDIATION_EFFORT, 1.0);

        //Act
        double actual = underTest.rankTree(metricsReport, preferences);

        //Assert
        assertThat(actual).isEqualTo(expected);
    }
}
