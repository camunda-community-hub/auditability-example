package org.camunda.custom.operate.facade;

import io.camunda.operate.dto.FlownodeInstance;
import io.camunda.operate.dto.Variable;
import io.camunda.operate.exception.OperateException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.camunda.custom.operate.data.ArchivedInstance;
import org.camunda.custom.operate.data.ProcDefinition;
import org.camunda.custom.operate.facade.dto.AuditRequest;
import org.camunda.custom.operate.security.annotation.CanSeeVariables;
import org.camunda.custom.operate.security.annotation.IsAuthenticated;
import org.camunda.custom.operate.service.OperateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api/audit")
public class AuditController {

  private static final Logger LOG = LoggerFactory.getLogger(AuditController.class);

  private final OperateService operateService;

  public AuditController(OperateService operateService) {
    this.operateService = operateService;
  }

  @PostMapping
  public AuditRequest getProcessInstanceVariables(@RequestBody AuditRequest auditRequest)
      throws OperateException {
    return operateService.getAuditLogs(auditRequest);
  }

  @IsAuthenticated
  @GetMapping("/definition/latest")
  public List<ProcDefinition> latestDefinitions() throws OperateException {
    Set<String> present = new HashSet<>();
    List<ProcDefinition> result = new ArrayList<>();
    List<ProcDefinition> processDefs = operateService.getProcDefinitions();
    if (processDefs != null) {
      for (ProcDefinition def : processDefs) {
        if (!present.contains(def.getBpmnProcessId())) {
          result.add(def);
          present.add(def.getBpmnProcessId());
        }
      }
    }
    return result;
  }

  @IsAuthenticated
  @GetMapping("/definition/{bpmnProcessId}/versions")
  public List<ProcDefinition> latestDefinitions(@PathVariable String bpmnProcessId)
      throws OperateException {
    return operateService.getProcDefinitionByBpmnProcessId(bpmnProcessId);
  }

  @IsAuthenticated
  @GetMapping("/definition/{key}/xml")
  public String getProcessDefinitionXmlByKey(@PathVariable Long key) throws OperateException {
    return operateService.getProcDefinition(key).getXml();
  }

  @IsAuthenticated
  @GetMapping("/{bpmnProcessId}/instances")
  public List<ArchivedInstance> instances(@PathVariable String bpmnProcessId)
      throws OperateException {
    return operateService.getInstancesByBpmnProcessId(bpmnProcessId);
  }

  @IsAuthenticated
  @GetMapping("/{bpmnProcessId}/{version}/instances")
  public List<ArchivedInstance> instances(
      @PathVariable String bpmnProcessId, @PathVariable Long version) throws OperateException {
    return operateService.getInstancesByBpmnProcessIdAndVersion(bpmnProcessId, version);
  }

  @IsAuthenticated
  @GetMapping("/{processInstanceKey}")
  public ArchivedInstance getProcessInstance(@PathVariable Long processInstanceKey)
      throws OperateException {
    return operateService.getInstance(processInstanceKey);
  }

  @IsAuthenticated
  @GetMapping("/{processInstanceKey}/flownodes")
  public List<FlownodeInstance> getProcessInstanceHistory(@PathVariable Long processInstanceKey)
      throws OperateException {
    return operateService.getInstance(processInstanceKey).getHistory();
  }

  @CanSeeVariables
  @GetMapping("/{processInstanceKey}/variables")
  public List<Variable> getProcessInstanceVariables(@PathVariable Long processInstanceKey)
      throws OperateException {
    return operateService.getInstance(processInstanceKey).getVariables();
  }
}
