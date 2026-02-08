package io.vacivor.restful.common.ordering;

public final class OrderingDelimiters {

    private final String itemDelimiter;

    private final String fieldDelimiterRegex;

    private OrderingDelimiters(String itemDelimiter, String fieldDelimiterRegex) {
        this.itemDelimiter = itemDelimiter;
        this.fieldDelimiterRegex = fieldDelimiterRegex;
    }

    public String itemDelimiter() {
        return itemDelimiter;
    }

    public String fieldDelimiterRegex() {
        return fieldDelimiterRegex;
    }

    public static OrderingDelimiters defaultDelimiters() {
        return new OrderingDelimiters(";", ",");
    }

    public static OrderingDelimiters of(String itemDelimiter, String fieldDelimiterRegex) {
        return new OrderingDelimiters(itemDelimiter, fieldDelimiterRegex);
    }
}
