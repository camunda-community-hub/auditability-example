package org.camunda.custom.operate.data;

import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
public class ProcDefinition extends BaseEntity {

  private Long defKey;
  private String name;
  private String bpmnProcessId;
  private Long version;
  @Lob private String xml;

  public Long getDefKey() {
    return defKey;
  }

  public void setDefKey(Long defKey) {
    this.defKey = defKey;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getBpmnProcessId() {
    return bpmnProcessId;
  }

  public void setBpmnProcessId(String bpmnProcessId) {
    this.bpmnProcessId = bpmnProcessId;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public String getXml() {
    return xml;
  }

  public void setXml(String xml) {
    this.xml = xml;
  }
}
