package gr.aegean.service.analysis;

import gr.aegean.model.analysis.quality.QualityMetric;
import gr.aegean.model.analysis.sonarqube.HotspotsReport;
import gr.aegean.model.analysis.sonarqube.IssuesReport;
import gr.aegean.model.analysis.sonarqube.Severity;
import gr.aegean.model.analysis.sonarqube.VulnerabilityProbability;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
    For a quality metric the relative utility function(utf) is applied.
 */
@Service
public class MetricCalculationService {
    private static final double LINE_COST = 0.06;

    public EnumMap<QualityMetric, Double> applyUtf(EnumMap<QualityMetric, Double> metricsReport,
                                                   List<IssuesReport.IssueDetails> issuesDetails,
                                                   List<HotspotsReport.HotspotDetails> hotspotsDetails) {
        EnumMap<QualityMetric, Double> updatedMetricsReport = new EnumMap<>(QualityMetric.class);
        Double linesOfCode = metricsReport.get(QualityMetric.LINES_OF_CODE);
        updatedMetricsReport.put(QualityMetric.COMMENT_RATE, metricsReport.get(QualityMetric.COMMENT_RATE));

        metricsReport.forEach((metric, value) -> {
            switch (metric) {
                //case METHOD_SIZE -> updatedMetricsReport.put(metric, applyMethodSizeUtf(value));
                case DUPLICATION -> updatedMetricsReport.put(metric, applyDuplicationUtf(value));
                case TECHNICAL_DEBT_RATIO -> updatedMetricsReport.put(metric, applyTechnicalDebtRatioUtf(value));
//                case RELIABILITY_REMEDIATION_EFFORT -> updatedMetricsReport.put(
//                        metric,
//                        applyReliabilityRemediationEffortUtf(value, linesOfCode));
                case CYCLOMATIC_COMPLEXITY, COGNITIVE_COMPLEXITY -> updatedMetricsReport.put(
                        metric,
                        applyComplexityUtf(value, linesOfCode));
                case SECURITY_REMEDIATION_EFFORT -> updatedMetricsReport.put(
                        metric,
                        applySecurityRemediationEffortUtf(value, linesOfCode));
            }
        });

        double value = applyMetricsUtf(issuesDetails, "BUG");
        updatedMetricsReport.put(QualityMetric.BUG_SEVERITY, value);

        value = applyMetricsUtf(issuesDetails, "VULNERABILITY");
        updatedMetricsReport.put(QualityMetric.VULNERABILITY_SEVERITY, value);

        value = applyHotSpotPriorityUtf(hotspotsDetails);
        updatedMetricsReport.put(QualityMetric.HOTSPOT_PRIORITY, value);

        return updatedMetricsReport;
    }

    private double applyDuplicationUtf(double duplication) {
        return 1 - duplication;
    }

    private double applyMethodSizeUtf(double methodSize) {
        return Math.pow(2, (70 - methodSize) / 21.0) / 3.0;
    }

    private double applyTechnicalDebtRatioUtf(double technicalDeptRatio) {
        return 1 - technicalDeptRatio;
    }

    private double applyReliabilityRemediationEffortUtf(double reliabilityRemediationEffort, double linesOfCode) {
        return 1 - (reliabilityRemediationEffort / (linesOfCode * LINE_COST));
    }

    private double applyComplexityUtf(double complexity, double linesOfCode) {
        return  1 - complexity / linesOfCode;
    }

    private double applySecurityRemediationEffortUtf(double securityRemediationEffort, double linesOfCode) {
        return 1 - securityRemediationEffort / (linesOfCode * LINE_COST);
    }

    /*
        This method will be applied for bug severity and vulnerability severity metrics.
     */
    private double applyMetricsUtf(List<IssuesReport.IssueDetails> issuesDetails,
                                   String issueType) {
        Map<Severity, Long> severityCount = countSeverityByType(issuesDetails, issueType);

        /*
            Getting the count for each severity, if its null, meaning no severity of the specific type was found we
            return 0, otherwise we would have NullPointerException.
         */
        long blockerCount = severityCount.getOrDefault(Severity.BLOCKER, 0L);
        long criticalCount = severityCount.getOrDefault(Severity.CRITICAL, 0L);
        long majorCount = severityCount.getOrDefault(Severity.MAJOR, 0L);
        long minorCount = severityCount.getOrDefault(Severity.MINOR, 0L);
        long infoCount = severityCount.getOrDefault(Severity.INFO, 0L);

        if(blockerCount > 0) {
          return 0.2 * 1 / (blockerCount * (1 +
                  Math.pow(10, -1) * utf(criticalCount) +
                  Math.pow(10, -2) * utf(majorCount) +
                  Math.pow(10, -3) * utf(minorCount) +
                  Math.pow(10, -4) * utf(infoCount)));
        }

        if(criticalCount > 0) {
            return 0.2 * 1 / (criticalCount * (1 +
                    Math.pow(10, -1) * utf(majorCount) +
                    Math.pow(10, -2) * utf(minorCount) +
                    Math.pow(10, -3) * utf(infoCount))) + 0.2;
        }

        if(majorCount > 0) {
            return 0.2 * 1 / (majorCount * (1 +
                    Math.pow(10, -1) * utf(minorCount) +
                    Math.pow(10, -2) * utf(infoCount))) + 0.4;
        }

        if(minorCount > 0) {
            return 0.2 * 1 / (minorCount * (1 +
                    Math.pow(10, -1) * utf(infoCount))) + 0.6;
        }

        if(infoCount > 0) {
            return 0.2 * 1 / infoCount + 0.8;
        }

        /*
            If no severities are found we have the max value.
         */
        return 1.0;
    }

    private Map<Severity, Long> countSeverityByType(List<IssuesReport.IssueDetails> issuesDetails,
                                                    String issueType) {
        /*
            Returns a list of all the severities found.[BLOCKER, MAJOR, MINOR, MAJOR,  CRITICAL, CRITICAL]
         */
        List<Severity> sortedSeverities = issuesDetails.stream()
                .filter(issue -> issue.getType().equals(issueType))
                .map(IssuesReport.IssueDetails::getSeverity)
                .toList();

        /*
            BLOCKER: 3,
            CRITICAL: 1,
            MAJOR: 1,
            MINOR: 2,
            INFO: 1

            We know the total occurrences of each severity, so we can apply the relative utf.
         */
        return sortedSeverities.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    private double applyHotSpotPriorityUtf(List<HotspotsReport.HotspotDetails> hotspotDetails) {
        Map<VulnerabilityProbability, Long> vulnerabilityProbabilityCount = countVulnerabilityProbabilityByType(
                hotspotDetails);

        /*
            Getting the count for each vulnerability probability, if its null, meaning no vulnerability probability
            of the specific type was found we return 0, otherwise we would have NullPointerException.
         */
        long highCount = vulnerabilityProbabilityCount.getOrDefault(VulnerabilityProbability.HIGH, 0L);
        long mediumCount = vulnerabilityProbabilityCount.getOrDefault(VulnerabilityProbability.MEDIUM, 0L);
        long lowCount = vulnerabilityProbabilityCount.getOrDefault(VulnerabilityProbability.LOW, 0L);

        if(highCount > 0) {
            return 0.33 * 1 / (highCount * (1 +
                    Math.pow(10, -1) * utf(mediumCount) +
                    Math.pow(10, -2) * utf(lowCount)));
        }

        if(mediumCount > 0) {
            return 0.33 * 1 / (mediumCount * (1 +
                    Math.pow(10, -1) * utf(lowCount) + 0.33));
        }

        if(lowCount > 0) {
            return 0.33 * 1 / lowCount + 0.66;
        }

        /*
            If no vulnerability probabilities are found we have the max value.
         */
        return 1.0;
    }

    private Map<VulnerabilityProbability, Long> countVulnerabilityProbabilityByType(
            List<HotspotsReport.HotspotDetails> hotspotDetails) {
        /*
            Returns a list of all vulnerability probability found. [HIGH, LOW, MEDIUM, MEDIUM,]
         */
        List<VulnerabilityProbability> vulnerabilityProbabilities = hotspotDetails.stream()
                .map(HotspotsReport.HotspotDetails::getVulnerabilityProbability)
                .toList();

        return vulnerabilityProbabilities.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    private double utf(long count) {
        return count == 0 ? 0 : 1.0 / (1.0 + 1.0 / (1.0 + count));
    }
}