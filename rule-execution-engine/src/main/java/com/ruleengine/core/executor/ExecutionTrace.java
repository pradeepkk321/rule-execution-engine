package com.ruleengine.core.executor;

import com.ruleengine.core.context.ExecutionContext;
import com.ruleengine.core.context.ExecutionStep;
import com.ruleengine.core.context.ExecutionStep.StepType;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Comprehensive execution trace for debugging and analysis.
 */
public class ExecutionTrace {
    
    private String entryPoint;
    private final Instant startTime;
    private Instant endTime;
    private final List<ExecutionStep> steps;
    private final Map<String, Map<String, Object>> variableSnapshots;
    private final List<String> rulesExecuted;
    private final List<String> actionsExecuted;
    private boolean success;
    private String errorMessage;
    
    public ExecutionTrace() {  // No-arg constructor
        this.startTime = Instant.now();
        this.steps = new ArrayList<>();
        this.variableSnapshots = new HashMap<>();
        this.rulesExecuted = new ArrayList<>();
        this.actionsExecuted = new ArrayList<>();
        this.success = true;
    }
    
    public void setEntryPoint(String entryPoint) {
        this.entryPoint = entryPoint;
    }
    
    public void recordStep(ExecutionStep step) {
        steps.add(step);
        
        if (step.getRuleId() != null && !rulesExecuted.contains(step.getRuleId())) {
            rulesExecuted.add(step.getRuleId());
        }
        
        if (step.getActionId() != null && !actionsExecuted.contains(step.getActionId())) {
            actionsExecuted.add(step.getActionId());
        }
    }
    
    public void snapshotVariables(String label, ExecutionContext context) {
        variableSnapshots.put(label, new HashMap<>(context.getAllVariables()));
    }
    
    public void complete(boolean success, String errorMessage) {
        this.endTime = Instant.now();
        this.success = success;
        this.errorMessage = errorMessage;
    }
    
    public long getDurationMs() {
        if (endTime == null) {
            return System.currentTimeMillis() - startTime.toEpochMilli();
        }
        return endTime.toEpochMilli() - startTime.toEpochMilli();
    }
    
    /**
     * Generate visual execution flow in Mermaid diagram format.
     */
    public String toMermaidDiagram() {
        StringBuilder sb = new StringBuilder();
        sb.append("```mermaid\n");
        sb.append("graph TD\n");
        sb.append("    Start[\"Entry: ").append(entryPoint).append("\"]\n");
        
        String lastNode = "Start";
        int nodeId = 1;
        boolean linkEstablished = false;
        
        for (ExecutionStep step : steps) {
            String currentNode = "N" + nodeId;
            
            switch (step.getType()) {
                case RULE_ENTERED:
                	nodeId++;
                    sb.append("    ").append(currentNode).append("[\"Rule: ")
                      .append(step.getRuleId()).append("\"]\n");
                    if(!linkEstablished) {
                    	sb.append("    ").append(lastNode).append(" --> ").append(currentNode).append("\n");
                        linkEstablished = false;	
                    }
                    lastNode = currentNode;
                    break;
                    
                case ACTION_COMPLETED:
                	nodeId++;
                    sb.append("    ").append(currentNode).append("{{\"Action: ")
                      .append(step.getActionId()).append(" (").append(step.getDurationMs())
                      .append("ms)\"}}\n");
                    sb.append("    ").append(lastNode).append(" --> ").append(currentNode).append("\n");
                    lastNode = currentNode;
                    break;
                    
                case ACTION_FAILED:
                	nodeId++;
                    sb.append("    ").append(currentNode).append("[\"Action Failed: ")
                      .append(step.getActionId()).append("\"]\n");
                    sb.append("    ").append(lastNode).append(" -->|Error| ").append(currentNode).append("\n");
                    sb.append("    style ").append(currentNode).append(" fill:#f99\n");
                    lastNode = currentNode;
                    break;
                    
                case TRANSITION_EVALUATED:
                    Object result = step.getMetadata().get("result");
                    if (Boolean.TRUE.equals(result)) {
                        String target = (String) step.getMetadata().get("targetRule");
                        sb.append("    ").append(lastNode).append(" -->|\"")
                          .append(step.getMetadata().get("condition"))
                          .append(" = true\"| ").append(currentNode).append("\n");
                        linkEstablished = true;
                    }
                    break;
            }
        }
        
        sb.append("    End[\"").append(success ? "Success" : "Failed").append("\"]\n");
        sb.append("    ").append(lastNode).append(" --> End\n");
        
        if (!success) {
            sb.append("    style End fill:#f99\n");
        } else {
            sb.append("    style End fill:#9f9\n");
        }
        sb.append("```\n");
        return sb.toString();
    }
    
    /**
     * Generate detailed text summary.
     */
    public String toDetailedSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Execution Trace ===\n");
        sb.append("Entry Point: ").append(entryPoint).append("\n");
        sb.append("Duration: ").append(getDurationMs()).append("ms\n");
        sb.append("Status: ").append(success ? "SUCCESS" : "FAILED").append("\n");
        
        if (!success && errorMessage != null) {
            sb.append("Error: ").append(errorMessage).append("\n");
        }
        
        sb.append("\nRules Executed: ").append(rulesExecuted.size()).append("\n");
        rulesExecuted.forEach(rule -> sb.append("  - ").append(rule).append("\n"));
        
        sb.append("\nActions Executed: ").append(actionsExecuted.size()).append("\n");
        actionsExecuted.forEach(action -> sb.append("  - ").append(action).append("\n"));
        
        sb.append("\nExecution Steps: ").append(steps.size()).append("\n");
        for (int i = 0; i < steps.size(); i++) {
            ExecutionStep step = steps.get(i);
            sb.append(String.format("%3d. [%s] ", i + 1, step.getType()));
            
            if (step.getRuleId() != null) {
                sb.append("Rule: ").append(step.getRuleId());
            }
            if (step.getActionId() != null) {
                sb.append(", Action: ").append(step.getActionId());
            }
            if (step.getDurationMs() > 0) {
                sb.append(" (").append(step.getDurationMs()).append("ms)");
            }
            sb.append("\n");
            
            if (!step.getMetadata().isEmpty()) {
                step.getMetadata().forEach((key, value) -> 
                    sb.append("     ").append(key).append(": ").append(value).append("\n")
                );
            }
        }
        
        if (!variableSnapshots.isEmpty()) {
            sb.append("\nVariable Snapshots:\n");
            variableSnapshots.forEach((label, vars) -> {
                sb.append("  ").append(label).append(":\n");
                vars.forEach((key, value) -> 
                    sb.append("    ").append(key).append(" = ").append(value).append("\n")
                );
            });
        }
        
        return sb.toString();
    }
    
    /**
     * Get performance metrics.
     */
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalDurationMs", getDurationMs());
        metrics.put("rulesExecuted", rulesExecuted.size());
        metrics.put("actionsExecuted", actionsExecuted.size());
        metrics.put("stepsExecuted", steps.size());
        
        // Calculate action timings
        Map<String, Long> actionDurations = steps.stream()
                .filter(s -> s.getType() == ExecutionStep.StepType.ACTION_COMPLETED && s.getDurationMs() > 0)
                .collect(Collectors.toMap(
                    ExecutionStep::getActionId,
                    ExecutionStep::getDurationMs,
                    Long::sum
                ));
        
        metrics.put("actionDurations", actionDurations);
        
        long totalActionTime = actionDurations.values().stream().mapToLong(Long::longValue).sum();
        metrics.put("totalActionTimeMs", totalActionTime);
        
        // Count failures
        long failedActions = steps.stream()
                .filter(s -> s.getType() == ExecutionStep.StepType.ACTION_FAILED)
                .count();
        metrics.put("failedActions", failedActions);
        
        return metrics;
    }
    
    // Getters
    
    public String getEntryPoint() {
        return entryPoint;
    }
    
    public Instant getStartTime() {
        return startTime;
    }
    
    public Instant getEndTime() {
        return endTime;
    }
    
    public List<ExecutionStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }
    
    public Map<String, Map<String, Object>> getVariableSnapshots() {
        return Collections.unmodifiableMap(variableSnapshots);
    }
    
    public List<String> getRulesExecuted() {
        return Collections.unmodifiableList(rulesExecuted);
    }
    
    public List<String> getActionsExecuted() {
        return Collections.unmodifiableList(actionsExecuted);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
}