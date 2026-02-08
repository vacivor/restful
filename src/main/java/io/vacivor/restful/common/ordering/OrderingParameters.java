package io.vacivor.restful.common.ordering;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class OrderingParameters<S, O> {

  private String raw;
  private Set<String> allowedProperties;
  private OrderingDelimiters delimiters = OrderingDelimiters.defaultDelimiters();
  private final Set<String> seen = new HashSet<>();
  private final OrderingAdapter<S, O> adapter;
  private List<O> orders;
  private boolean parsed = false;

  @SuppressWarnings("unchecked")
  public OrderingParameters() {
    this.adapter = (OrderingAdapter<S, O>) new DefaultOrderingAdapter();
  }

  public OrderingParameters(OrderingAdapter<S, O> adapter) {
    if (adapter == null) {
      throw new IllegalArgumentException("Ordering adapter must be provided");
    }
    this.adapter = adapter;
  }

  public OrderingParameters(String raw, OrderingAdapter<S, O> adapter) {
    if (adapter == null) {
      throw new IllegalArgumentException("Ordering adapter must be provided");
    }
    this.raw = raw;
    this.adapter = adapter;
  }

  public OrderingParameters<S, O> raw(String raw) {
    ensureNotParsed();
    this.raw = raw;
    this.orders = null;
    return this;
  }

  public OrderingParameters<S, O> allow(String... fields) {
    ensureNotParsed();
    this.allowedProperties = new HashSet<>();
    for (String field : fields) {
      if (field != null) {
        this.allowedProperties.add(field.toLowerCase(Locale.ROOT));
      }
    }
    return this;
  }

  public OrderingParameters<S, O> allow(Collection<String> fields) {
    ensureNotParsed();
    this.allowedProperties = new HashSet<>();
    for (String field : fields) {
      if (field != null) {
        this.allowedProperties.add(field.toLowerCase(Locale.ROOT));
      }
    }
    return this;
  }

  public OrderingParameters<S, O> allow(Class<?> entityClass) {
    ensureNotParsed();
    this.allowedProperties = resolveProperties(entityClass);
    return this;
  }

  public OrderingParameters<S, O> append(String field, String direction) {
    if (!parsed) {
      throw new IllegalStateException("Ordering not parsed. Call parse() first.");
    }
    if (field == null || field.isBlank()) {
      return this;
    }
    String property = field.trim();
    String propertyKey = property.toLowerCase(Locale.ROOT);
    if (seen.add(propertyKey)) {
      O order = adapter.parseOne(property, direction);
      if (order != null) {
        orders.add(order);
      }
    }
    return this;
  }

  public OrderingParameters<S, O> delimiters(OrderingDelimiters delimiters) {
    ensureNotParsed();
    this.delimiters = delimiters;
    return this;
  }

  public OrderingParameters<S, O> parse() {
    ensureNotParsed();
    List<O> result = new ArrayList<>();

    if (raw == null || raw.isBlank()) {
      // keep empty base ordering
    } else {
      if (allowedProperties == null) {
        throw new IllegalStateException("Allowed properties must be configured before parsing");
      }
      String[] items = raw.split(delimiters.itemDelimiter());

      for (String item : items) {
        String trimmed = item.trim();
        if (trimmed.isBlank()) {
          continue;
        }

        String[] tokens = trimmed.split(delimiters.fieldDelimiterRegex());
        String property = tokens[0].trim();
        String propertyKey = property.toLowerCase(Locale.ROOT);

        if (!allowedProperties.contains(propertyKey)) {
          continue;
        }

        String direction = tokens.length > 1 ? tokens[1] : null;
        O order = adapter.parseOne(property, direction);
        if (order != null && seen.add(propertyKey)) {
          result.add(order);
        }
      }
    }

    this.orders = result;
    this.parsed = true;
    return this;
  }

  public boolean isEmpty() {
    if (!parsed || orders == null) {
      throw new IllegalStateException("Ordering not parsed. Call parse() first.");
    }
    return orders.isEmpty();
  }

  public List<O> getOrders() {
    if (!parsed || orders == null) {
      throw new IllegalStateException("Ordering not parsed. Call parse() first.");
    }
    return List.copyOf(orders);
  }

  public S getSort() {
    if (!parsed || orders == null) {
      throw new IllegalStateException("Ordering not parsed. Call parse() first.");
    }
    return adapter.buildSort(orders);
  }

  public String getOrderByClause() {
    if (!parsed || orders == null) {
      throw new IllegalStateException("Ordering not parsed. Call parse() first.");
    }
    return adapter.getOrderByClause(adapter.buildSort(orders));
  }

  private void ensureNotParsed() {
    if (parsed) {
      throw new IllegalStateException("Ordering already parsed. Create a new instance to reconfigure inputs.");
    }
  }

  private Set<String> resolveProperties(Class<?> type) {
    Set<String> fields = new HashSet<>();
    while (type != null && type != Object.class) {
      for (Field f : type.getDeclaredFields()) {
        fields.add(f.getName().toLowerCase(Locale.ROOT));
      }
      type = type.getSuperclass();
    }
    return fields;
  }
}
