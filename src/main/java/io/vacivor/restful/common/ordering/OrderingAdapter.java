package io.vacivor.restful.common.ordering;

import java.util.List;

public interface OrderingAdapter<S, O> {
  O parseOne(String property, String direction);

  S buildSort(List<O> orders);

  String getOrderByClause(S sort);
}
