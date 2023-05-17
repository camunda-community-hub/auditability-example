package org.camunda.custom.operate.facade.dto;

public class AuditRequest {

  private Long processInstanceKey;
  private String status;

  public Long getProcessInstanceKey() {
    return processInstanceKey;
  }

  public void setProcessInstanceKey(Long processInstanceKey) {
    this.processInstanceKey = processInstanceKey;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
