package org.camunda.custom.operate.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.camunda.operate.CamundaOperateClient;
import io.camunda.operate.auth.SaasAuthentication;
import io.camunda.operate.auth.SelfManagedAuthentication;
import io.camunda.operate.dto.FlownodeInstance;
import io.camunda.operate.dto.ProcessDefinition;
import io.camunda.operate.dto.ProcessInstance;
import io.camunda.operate.dto.ProcessInstanceState;
import io.camunda.operate.dto.SearchResult;
import io.camunda.operate.dto.Variable;
import io.camunda.operate.exception.OperateException;
import io.camunda.operate.search.FlownodeInstanceFilter;
import io.camunda.operate.search.SearchQuery;
import io.camunda.operate.search.Sort;
import io.camunda.operate.search.SortOrder;
import io.camunda.operate.search.VariableFilter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.camunda.custom.operate.dao.ArchivedInstanceRepository;
import org.camunda.custom.operate.dao.ProcDefinitionRepository;
import org.camunda.custom.operate.data.ArchivedInstance;
import org.camunda.custom.operate.data.ProcDefinition;
import org.camunda.custom.operate.facade.dto.AuditRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

@Service
@EnableCaching
public class OperateService {

  private static final Logger LOG = LoggerFactory.getLogger(OperateService.class);

  private final Cache<Long, AuditRequest> auditRequests =
      CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();

  @Value("${zeebe.client.cloud.client-id:notProvided}")
  private String clientId;

  @Value("${zeebe.client.cloud.client-secret:notProvided}")
  private String clientSecret;

  @Value("${zeebe.client.cloud.clusterId:notProvided}")
  private String clusterId;

  @Value("${zeebe.client.cloud.region:notProvided}")
  private String region;

  @Value("${identity.clientId:notProvided}")
  private String identityClientId;

  @Value("${identity.clientSecret:notProvided}")
  private String identityClientSecret;

  @Value("${operateUrl:notProvided}")
  private String operateUrl;

  @Value("${keycloakUrl:notProvided}")
  private String keycloakUrl;

  @Value("${operate.sync.scheduled:false}")
  private boolean scheduledSync;

  private CamundaOperateClient client;

  @Autowired private RetryTemplate retryTemplate;
  @Autowired private ProcDefinitionRepository procDefRepository;
  @Autowired private ArchivedInstanceRepository archivedInstanceRepository;

  private CamundaOperateClient getCamundaOperateClient() throws OperateException {
    if (client == null) {
      if (!"notProvided".equals(clientId)) {
        SaasAuthentication sa = new SaasAuthentication(clientId, clientSecret);
        client =
            new CamundaOperateClient.Builder()
                .operateUrl("https://" + region + ".operate.camunda.io/" + clusterId)
                .authentication(sa)
                .build();
      } else {
        SelfManagedAuthentication la =
            new SelfManagedAuthentication()
                .clientId(identityClientId)
                .clientSecret(identityClientSecret)
                .keycloakUrl(keycloakUrl);
        client =
            new CamundaOperateClient.Builder().operateUrl(operateUrl).authentication(la).build();
      }
    }
    return client;
  }

  private void loadAuditLogsFromOperate(AuditRequest request) throws OperateException {
    retryTemplate.execute(
        arg0 -> {
          ProcessInstance instance = getProcessInstance(request.getProcessInstanceKey());
          if (!instance.getState().equals(ProcessInstanceState.COMPLETED)) {
            throw new OperateException(
                "Retry later, instance "
                    + request.getProcessInstanceKey()
                    + " is not completed in Operate");
          }
          storeAuditLogs(instance);
          request.setStatus("COMPLETED");
          auditRequests.put(request.getProcessInstanceKey(), request);
          return null;
        });
  }

  public void storeAuditLogs(ProcessInstance instance) throws OperateException {

    ProcDefinition def = procDefRepository.findByDefKey(instance.getProcessDefinitionKey());
    if (def == null) {
      def = new org.camunda.custom.operate.data.ProcDefinition();
      ProcessDefinition procDef = client.getProcessDefinition(instance.getProcessDefinitionKey());
      def.setDefKey(instance.getProcessDefinitionKey());
      def.setName(procDef.getName());
      def.setVersion(instance.getProcessVersion());
      def.setBpmnProcessId(instance.getBpmnProcessId());
      def.setXml(
          getCamundaOperateClient().getProcessDefinitionXml(instance.getProcessDefinitionKey()));
      procDefRepository.save(def);
    }

    List<FlownodeInstance> history = getProcessInstanceHistory(instance.getKey());
    List<Variable> variables = getProcessInstanceVariables(instance.getKey());
    ArchivedInstance instanceHistory = new ArchivedInstance();
    instanceHistory.setProcessInstanceKey(instance.getKey());
    instanceHistory.setProcessVersion(instance.getProcessVersion());
    instanceHistory.setBpmnProcessId(instance.getBpmnProcessId());
    instanceHistory.setParentKey(instance.getParentKey());
    instanceHistory.setStartDate(instance.getStartDate());
    instanceHistory.setEndDate(instance.getEndDate());
    instanceHistory.setProcessDefinitionKey(instance.getProcessDefinitionKey());
    instanceHistory.setHistory(history);
    instanceHistory.setVariables(variables);
    archivedInstanceRepository.save(instanceHistory);
  }

  public AuditRequest getAuditLogs(AuditRequest request) throws OperateException {
    if (scheduledSync) {
      request.setStatus("DISCARDED_FOR_SCHEDULED_SYNC");
      return request;
    }
    AuditRequest existing = auditRequests.getIfPresent(request.getProcessInstanceKey());
    if (existing != null && !existing.getStatus().equals("ERROR")) {
      return existing;
    }
    request.setStatus("PENDING");
    auditRequests.put(request.getProcessInstanceKey(), request);
    new Thread(
            () -> {
              try {
                Thread.sleep(5000L);
                loadAuditLogsFromOperate(request);
              } catch (OperateException | InterruptedException e) {
                LOG.error("Error loading audit logs", e);
                request.setStatus("ERROR");
                auditRequests.put(request.getProcessInstanceKey(), request);
              }
            })
        .start();

    return request;
  }

  public List<ProcDefinition> getProcDefinitions() throws OperateException {
    return procDefRepository.findAllByOrderByVersionDesc();
  }

  public List<ProcDefinition> getProcDefinitionByBpmnProcessId(String bpmnProcessId)
      throws OperateException {
    return procDefRepository.findByBpmnProcessId(bpmnProcessId);
  }

  public ProcDefinition getProcDefinition(Long Definitionkey) throws OperateException {
    return procDefRepository.findByDefKey(Definitionkey);
  }

  public List<ArchivedInstance> getInstancesByBpmnProcessId(String bpmnProcessId)
      throws OperateException {
    return archivedInstanceRepository.findByBpmnProcessId(bpmnProcessId);
  }

  public List<ArchivedInstance> getInstancesByBpmnProcessIdAndVersion(
      String bpmnProcessId, Long version) throws OperateException {

    return archivedInstanceRepository.findByBpmnProcessIdAndProcessVersion(bpmnProcessId, version);
  }

  public ArchivedInstance getInstance(Long processInstanceKey) throws OperateException {
    return archivedInstanceRepository.findByProcessInstanceKey(processInstanceKey);
  }

  private List<FlownodeInstance> getProcessInstanceHistory(Long processInstanceKey)
      throws OperateException {
    FlownodeInstanceFilter flowNodeFilter =
        new FlownodeInstanceFilter.Builder().processInstanceKey(processInstanceKey).build();
    SearchQuery procInstQuery =
        new SearchQuery.Builder()
            .filter(flowNodeFilter)
            .size(1000)
            .sort(new Sort("startDate", SortOrder.DESC))
            .build();

    return getCamundaOperateClient().searchFlownodeInstances(procInstQuery);
  }

  private ProcessInstance getProcessInstance(Long processInstanceKey) throws OperateException {
    return getCamundaOperateClient().getProcessInstance(processInstanceKey);
  }

  private List<Variable> getProcessInstanceVariables(Long processInstanceKey)
      throws OperateException {
    VariableFilter variableFilter =
        new VariableFilter.Builder().processInstanceKey(processInstanceKey).build();
    SearchQuery varQuery =
        new SearchQuery.Builder()
            .filter(variableFilter)
            .size(1000)
            .sort(new Sort("name", SortOrder.ASC))
            .build();
    return getCamundaOperateClient().searchVariables(varQuery);
  }

  public SearchResult<ProcessInstance> searchProcessInstanceResults(SearchQuery query)
      throws OperateException {
    return getCamundaOperateClient().searchProcessInstanceResults(query);
  }
}
