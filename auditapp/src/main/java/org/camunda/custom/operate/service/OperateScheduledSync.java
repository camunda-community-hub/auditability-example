package org.camunda.custom.operate.service;

import io.camunda.operate.dto.ProcessInstance;
import io.camunda.operate.dto.ProcessInstanceState;
import io.camunda.operate.dto.SearchResult;
import io.camunda.operate.exception.OperateException;
import io.camunda.operate.search.ProcessInstanceFilter;
import io.camunda.operate.search.SearchQuery;
import io.camunda.operate.search.Sort;
import io.camunda.operate.search.SortOrder;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

public class OperateScheduledSync {
  private static final Logger LOG = LoggerFactory.getLogger(OperateScheduledSync.class);

  @Autowired private OperateService operateService;
  @Autowired private SyncSchedulingService syncSchedulingService;

  @Scheduled(fixedRate = 4, initialDelay = 0, timeUnit = TimeUnit.HOURS)
  public void startSomeProcessInstances() {
    try {
      ProcessInstanceFilter search =
          new ProcessInstanceFilter.Builder().state(ProcessInstanceState.COMPLETED).build();
      SearchQuery query =
          new SearchQuery.Builder()
              .filter(search)
              .searchAfter(syncSchedulingService.getLastSortValues())
              .sort(new Sort("endDate", SortOrder.ASC))
              .size(100)
              .build();

      SearchResult<ProcessInstance> instances = operateService.searchProcessInstanceResults(query);
      while (instances.getItems().size() > 0) {
        for (ProcessInstance instance : instances.getItems()) {
          operateService.storeAuditLogs(instance);
        }
        syncSchedulingService.setLastSortValues(instances.getSortValues());
        query.setSearchAfter(instances.getSortValues());
        instances = operateService.searchProcessInstanceResults(query);
      }
    } catch (OperateException e) {
      LOG.error("Error while syncing completed instances from Operate");
    }
  }
}
