package io.vacivor.restful.common.ordering;

import java.util.Locale;
import java.util.Objects;

public class DefaultOrder {

  private final String property;
  private final Direction direction;

  public String getProperty() {
    return property;
  }

  public Direction getDirection() {
    return direction;
  }

  public static DefaultOrder of(String property, String direction) {
    return new DefaultOrder(property, Direction.fromString(direction));
  }

  public static DefaultOrder of(String property, Direction direction) {
    return new DefaultOrder(property, direction);
  }

  public DefaultOrder(String property, Direction direction) {
    this.property = property;
    this.direction = direction;
  }

  public enum Direction {

    DESC,
    ASC;

    public static Direction fromString(String value) {
      if (value == null || value.isEmpty()) {
        return ASC;
      }
      try {
        return valueOf(value.toUpperCase(Locale.US));
      } catch (Exception var2) {
        return ASC;
      }
    }

  }

  @Override
  public String toString() {
    return "DefaultOrder{" +
        "property='" + property + '\'' +
        ", direction=" + direction +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DefaultOrder defaultOrder = (DefaultOrder) o;
    return Objects.equals(property, defaultOrder.property) && direction == defaultOrder.direction;
  }

  @Override
  public int hashCode() {
    return Objects.hash(property, direction);
  }
}
