package org.camunda.custom.operate.data;

import io.camunda.operate.dto.FlownodeInstance;
import io.camunda.operate.dto.Variable;
import java.util.Date;
import java.util.List;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
public class ArchivedInstance extends BaseEntity {
  private Long processInstanceKey;
  private Long processVersion;
  private String bpmnProcessId;
  private Long parentKey;
  private Date startDate;
  private Date endDate;
  private Long processDefinitionKey;

  @Lob
  @Convert(converter = JsonHistoryConverter.class)
  private List<FlownodeInstance> history;

  @Lob
  @Convert(converter = JsonVariableConverter.class)
  private List<Variable> variables;

  public Long getProcessInstanceKey() {
    return processInstanceKey;
  }

  public void setProcessInstanceKey(Long processInstanceKey) {
    this.processInstanceKey = processInstanceKey;
  }

  public Long getProcessVersion() {
    return processVersion;
  }

  public void setProcessVersion(Long processVersion) {
    this.processVersion = processVersion;
  }

  public String getBpmnProcessId() {
    return bpmnProcessId;
  }

  public void setBpmnProcessId(String bpmnProcessId) {
    this.bpmnProcessId = bpmnProcessId;
  }

  public Long getParentKey() {
    return parentKey;
  }

  public void setParentKey(Long parentKey) {
    this.parentKey = parentKey;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public Long getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(Long processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public List<FlownodeInstance> getHistory() {
    return history;
  }

  public void setHistory(List<FlownodeInstance> history) {
    this.history = history;
  }

  public List<Variable> getVariables() {
    return variables;
  }

  public void setVariables(List<Variable> variables) {
    this.variables = variables;
  }
}
