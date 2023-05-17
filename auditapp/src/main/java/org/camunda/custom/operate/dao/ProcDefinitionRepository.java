package org.camunda.custom.operate.dao;

import java.util.List;
import org.camunda.custom.operate.data.ProcDefinition;

public interface ProcDefinitionRepository extends BaseRepository<ProcDefinition> {

  ProcDefinition findByDefKey(Long key);

  ProcDefinition findByBpmnProcessIdAndVersion(String bpmnProcessId, Long version);

  List<ProcDefinition> findByBpmnProcessId(String bpmnProcessId);

  List<ProcDefinition> findAllByOrderByVersionDesc();
}
