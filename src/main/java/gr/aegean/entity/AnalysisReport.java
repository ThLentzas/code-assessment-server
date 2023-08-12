package gr.aegean.entity;

import gr.aegean.model.analysis.quality.QualityMetric;
import gr.aegean.model.analysis.sonarqube.HotspotsReport;
import gr.aegean.model.analysis.sonarqube.IssuesReport;
import gr.aegean.model.analysis.sonarqube.Rule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

import org.springframework.hateoas.Link;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisReport {
    private Integer id;
    private Integer analysisId;
    private Link projectUrl;
    private Map<String, Double> languages;
    private IssuesReport issuesReport;
    private HotspotsReport hotspotsReport;
    private Map<String, Rule> ruleDetails;
    private Map<QualityMetric, Double> qualityMetricsReport;
    private Double rank;

    public AnalysisReport(IssuesReport issuesReport,
                          HotspotsReport hotspotsReport,
                          Map<String, Rule> ruleDetails,
                          Map<QualityMetric, Double> qualityMetricsReport) {
        this.issuesReport = issuesReport;
        this.hotspotsReport = hotspotsReport;
        this.ruleDetails = ruleDetails;
        this.qualityMetricsReport = qualityMetricsReport;
    }
}

