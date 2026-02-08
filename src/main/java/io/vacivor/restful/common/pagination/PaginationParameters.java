package io.vacivor.restful.common.pagination;

public class PaginationParameters {

  private static final int DEFAULT_PAGE = 0;
  private static final int DEFAULT_PAGE_SIZE = 20;

  private Integer page;
  private Integer pageSize;
  private Integer maxPage;
  private Integer maxPageSize;
  private String cursor;

  public PaginationParameters() {
  }

  public PaginationParameters(Integer page, Integer pageSize) {
    this.page = page;
    this.pageSize = pageSize;
  }

  public static PaginationParameters of(Integer page, Integer pageSize) {
    return new PaginationParameters(page, pageSize);
  }

  public PaginationParameters setPage(Integer page) {
    this.page = page;
    return this;
  }

  public PaginationParameters setPageSize(Integer pageSize) {
    this.pageSize = pageSize;
    return this;
  }

  public PaginationParameters setMaxPage(Integer maxPage) {
    if (maxPage != null && maxPage < 0) {
      throw new IllegalArgumentException("maxPage must be >= 0");
    }
    this.maxPage = maxPage;
    return this;
  }

  public PaginationParameters setMaxPageSize(Integer maxPageSize) {
    if (maxPageSize != null && maxPageSize < 1) {
      throw new IllegalArgumentException("maxPageSize must be >= 1");
    }
    this.maxPageSize = maxPageSize;
    return this;
  }

  public PaginationParameters setCursor(String cursor) {
    this.cursor = cursor;
    return this;
  }

  public int getPage() {
    return normalizePage(page, DEFAULT_PAGE, maxPage);
  }

  public int getPageSize() {
    return normalizePageSize(pageSize, DEFAULT_PAGE_SIZE, maxPageSize);
  }

  public String getCursor() {
    return cursor;
  }

  private int normalizePage(Integer value, int defaultValue, Integer maxValue) {
    int result = (value != null) ? value : defaultValue;
    result = Math.max(0, result);
    if (maxValue != null) {
      result = Math.min(result, maxValue);
    }
    return result;
  }

  private int normalizePageSize(Integer value, int defaultValue, Integer maxValue) {
    int result = (value != null) ? value : defaultValue;
    result = Math.max(1, result);
    if (maxValue != null) {
      result = Math.min(result, maxValue);
    }
    return result;
  }

  @Override
  public String toString() {
    return "PaginationParameters{" +
        "page=" + page +
        ", pageSize=" + pageSize +
        ", maxPage=" + maxPage +
        ", maxPageSize=" + maxPageSize +
        ", cursor='" + cursor + '\'' +
        '}';
  }
}
