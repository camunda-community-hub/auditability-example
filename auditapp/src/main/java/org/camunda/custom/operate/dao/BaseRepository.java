package org.camunda.custom.operate.dao;

import java.util.Optional;
import org.camunda.custom.operate.data.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BaseRepository<T extends BaseEntity>
    extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

  Optional<T> findById(Long id);
}
