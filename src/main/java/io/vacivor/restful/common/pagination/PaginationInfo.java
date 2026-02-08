package io.vacivor.restful.common.pagination;

public record PaginationInfo(int page, int pageSize, long total, long totalPage) {}
