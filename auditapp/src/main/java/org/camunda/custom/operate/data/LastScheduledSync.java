package org.camunda.custom.operate.data;

import javax.persistence.Entity;

@Entity
public class LastScheduledSync extends BaseEntity {

  private Long sortValue0;
  private Long sortValue1;

  public Long getSortValue0() {
    return sortValue0;
  }

  public void setSortValue0(Long sortValue0) {
    this.sortValue0 = sortValue0;
  }

  public Long getSortValue1() {
    return sortValue1;
  }

  public void setSortValue1(Long sortValue1) {
    this.sortValue1 = sortValue1;
  }
}
