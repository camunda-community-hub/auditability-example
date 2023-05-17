package org.camunda.custom.operate.service;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import org.camunda.custom.operate.dao.LastScheduledSyncRepository;
import org.camunda.custom.operate.data.LastScheduledSync;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class SyncSchedulingService {

  private LastScheduledSync lastSync = null;

  @Autowired private LastScheduledSyncRepository lastScheduledSyncRepository;

  @Bean
  @ConditionalOnProperty(
      value = "operate.sync.scheduled",
      matchIfMissing = false,
      havingValue = "true")
  public OperateScheduledSync operateScheduledSync() {
    return new OperateScheduledSync();
  }

  private LastScheduledSync getLastSync() {
    if (lastSync != null) {
      return lastSync;
    }
    Optional<LastScheduledSync> lastSyncDb = lastScheduledSyncRepository.findById(1L);
    if (!lastSyncDb.isPresent()) {
      return null;
    }
    lastSync = lastSyncDb.get();
    return lastSync;
  }

  public List<Object> getLastSortValues() {
    LastScheduledSync sync = getLastSync();
    if (sync == null) {
      return null;
    }
    if (sync.getSortValue1() == null) {
      return Lists.newArrayList(sync.getSortValue0());
    }
    return Lists.newArrayList(sync.getSortValue0(), sync.getSortValue1());
  }

  public void saveLastSync(LastScheduledSync sync) {
    lastSync = sync;
  }

  public void setLastSortValues(List<Object> sortValues) {
    if (sortValues != null && !sortValues.isEmpty()) {
      if (lastSync == null) {
        lastSync = new LastScheduledSync();
        lastSync.setId(1L);
      }
      lastSync.setSortValue0((Long) sortValues.get(0));
      if (sortValues.size() > 1) {
        lastSync.setSortValue1((Long) sortValues.get(1));
      }
      lastScheduledSyncRepository.save(lastSync);
    }
  }
}
