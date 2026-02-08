package io.vacivor.restful.common.ordering;

import java.util.List;

public class DefaultSort {
  private final List<DefaultOrder> defaultOrders;

  public DefaultSort(List<DefaultOrder> defaultOrders) {
    this.defaultOrders = defaultOrders == null ? List.of() : List.copyOf(defaultOrders);
  }

  public boolean isEmpty() {
    return defaultOrders.isEmpty();
  }

  public List<DefaultOrder> getOrders() {
    return defaultOrders;
  }
}
