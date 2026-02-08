package io.vacivor.restful.common.ordering;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

public final class DefaultOrderingAdapter implements OrderingAdapter<DefaultSort, DefaultOrder> {

  @Override
  public DefaultOrder parseOne(String property, String direction) {
    String dir = direction == null ? "ASC" : direction.trim().toUpperCase(Locale.ROOT);
    if (!"ASC".equals(dir) && !"DESC".equals(dir)) {
      dir = "ASC";
    }
    return DefaultOrder.of(property, dir);
  }

  @Override
  public DefaultSort buildSort(List<DefaultOrder> orders) {
    return new DefaultSort(orders == null ? List.of() : new ArrayList<>(orders));
  }

  @Override
  public String getOrderByClause(DefaultSort sort) {
    if (sort == null || sort.isEmpty()) {
      return "";
    }
    StringJoiner joiner = new StringJoiner(", ");
    for (DefaultOrder defaultOrder : sort.getOrders()) {
      joiner.add(defaultOrder.getProperty() + " " + defaultOrder.getDirection().name());
    }
    return joiner.toString();
  }
}
