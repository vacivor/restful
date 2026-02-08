package io.vacivor.restful.common.ordering;

import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

public final class SpringDataOrderingAdapter implements OrderingAdapter<org.springframework.data.domain.Sort, org.springframework.data.domain.Sort.Order> {

  @Override
  public org.springframework.data.domain.Sort.Order parseOne(String property, String direction) {
    String dir = direction == null ? "ASC" : direction.trim().toUpperCase(Locale.ROOT);
    if ("DESC".equals(dir)) {
      return org.springframework.data.domain.Sort.Order.desc(property);
    }
    return org.springframework.data.domain.Sort.Order.asc(property);
  }

  @Override
  public org.springframework.data.domain.Sort buildSort(List<org.springframework.data.domain.Sort.Order> orders) {
    if (orders == null || orders.isEmpty()) {
      return org.springframework.data.domain.Sort.unsorted();
    }
    return org.springframework.data.domain.Sort.by(orders);
  }

  @Override
  public String getOrderByClause(org.springframework.data.domain.Sort sort) {
    if (sort == null || sort.isUnsorted()) {
      return "";
    }
    StringJoiner joiner = new StringJoiner(", ");
    for (org.springframework.data.domain.Sort.Order order : sort) {
      joiner.add(order.getProperty() + " " + order.getDirection().name());
    }
    return joiner.toString();
  }
}
