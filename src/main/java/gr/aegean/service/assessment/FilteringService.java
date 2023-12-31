package gr.aegean.service.assessment;

import gr.aegean.entity.AnalysisReport;
import gr.aegean.entity.Constraint;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;


@Service
public class FilteringService {

    public List<List<AnalysisReport>> filter(List<AnalysisReport> reports, List<Constraint> constraints) {

        /*
            An empty list will be returned for compliant if all reports belong to non-compliant and vice versa. A report
            that's compliant to all constraints will be added to compliant list otherwise to the non-compliant list
         */
        List<AnalysisReport> compliant = new ArrayList<>();
        List<AnalysisReport> nonCompliant = new ArrayList<>();

        for(AnalysisReport report : reports) {
            boolean isCompliant = constraints.stream()
                    .allMatch(constraint -> constraint.matchOperatorToCondition(
                                    report.getQualityMetricsReport().get(constraint.getQualityMetric())));

            if (isCompliant) {
                compliant.add(report);
            } else {
                nonCompliant.add(report);
            }
        }

        return List.of(compliant, nonCompliant);
    }
}
