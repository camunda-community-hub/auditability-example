package org.camunda.custom.operate.dao;

import java.util.List;
import org.camunda.custom.operate.data.ArchivedInstance;

public interface ArchivedInstanceRepository extends BaseRepository<ArchivedInstance> {

  ArchivedInstance findByProcessInstanceKey(Long processInstanceKey);

  List<ArchivedInstance> findByBpmnProcessIdAndProcessVersion(
      String bpmnProcessId, Long processVersion);

  List<ArchivedInstance> findByBpmnProcessId(String bpmnProcessId);
}
