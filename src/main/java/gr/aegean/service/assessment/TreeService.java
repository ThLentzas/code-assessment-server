package gr.aegean.service.assessment;

import gr.aegean.model.analysis.quality.TreeNode;

import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TreeService {

    public TreeNode buildTree() {
        TreeNode root = new TreeNode("Rank");
        TreeNode quality = new TreeNode("QUALITY");
        TreeNode security = new TreeNode("SECURITY");
        TreeNode comprehension = new TreeNode("COMPREHENSION");
        TreeNode simplicity = new TreeNode("SIMPLICITY");
        TreeNode maintainability = new TreeNode("MAINTAINABILITY");
        TreeNode reliability = new TreeNode("RELIABILITY");
        TreeNode complexity = new TreeNode("COMPLEXITY");
        TreeNode commentRate = new TreeNode("COMMENT_RATE");
        TreeNode methodSize = new TreeNode("METHOD_SIZE");
        TreeNode duplication = new TreeNode("DUPLICATION");
        TreeNode technicalDebtRatio = new TreeNode("TECHNICAL_DEBT_RATIO");
        TreeNode bugSeverity = new TreeNode("BUG_SEVERITY");
        TreeNode reliabilityRemediationEffort = new TreeNode("RELIABILITY_REMEDIATION_EFFORT");
        TreeNode cyclomaticComplexity = new TreeNode("CYCLOMATIC_COMPLEXITY");
        TreeNode cognitiveComplexity = new TreeNode("COGNITIVE_COMPLEXITY");
        TreeNode vulnerabilitySeverity = new TreeNode("VULNERABILITY_SEVERITY");
        TreeNode hotSpotPriority = new TreeNode("HOTSPOT_PRIORITY");
        TreeNode securityRemediationEffort = new TreeNode("SECURITY_REMEDIATION_EFFORT");

        addChildren(root, List.of(quality, security));
        addChildren(quality, List.of(comprehension, simplicity, maintainability, reliability, complexity));
        addChildren(security, List.of(vulnerabilitySeverity, hotSpotPriority, securityRemediationEffort));
        addChildren(comprehension, List.of(commentRate));
        addChildren(simplicity, List.of(methodSize));
        addChildren(maintainability, List.of(duplication, technicalDebtRatio));
        addChildren(reliability, List.of(bugSeverity, reliabilityRemediationEffort));
        addChildren(complexity, List.of(cyclomaticComplexity, cognitiveComplexity));

        return root;
    }

    private void addChildren(TreeNode parent, List<TreeNode> children) {
        for(TreeNode child : children) {
            parent.addChild(child);
        }
    }
}