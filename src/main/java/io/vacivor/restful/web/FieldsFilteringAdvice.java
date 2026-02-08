package io.vacivor.restful.web;

import io.vacivor.restful.common.exception.BadRequestException;
import io.vacivor.restful.hateoas.Shapable;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@RestControllerAdvice
public class FieldsFilteringAdvice implements ResponseBodyAdvice<Object> {

  private final ObjectMapper objectMapper;

  public FieldsFilteringAdvice(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public boolean supports(
      MethodParameter returnType,
      Class<? extends HttpMessageConverter<?>> converterType) {
    return true;
  }

  @Override
  public Object beforeBodyWrite(
      Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {
    if (body == null || !isShapableBody(body) || !(request instanceof ServletServerHttpRequest r)) {
      return body;
    }
    Set<String> fieldSet = parseFields(r.getServletRequest().getParameter("fields"));
    if (fieldSet.isEmpty()) {
      return body;
    }
    try {
      Object filtered = filterValue(body, fieldSet);
      response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
      return filtered;
    } catch (JacksonException e) {
      throw new BadRequestException("Invalid fields");
    }
  }

  private Set<String> parseFields(String fields) {
    Set<String> result = new LinkedHashSet<>();
    if (fields == null || fields.isBlank()) {
      return result;
    }
    for (String raw : fields.split(",")) {
      String v = raw.trim();
      if (!v.isEmpty()) {
        result.add(v);
      }
    }
    if (result.contains("_links")) {
      result.add("links");
    }
    return result;
  }

  private Object filterValue(Object value, Set<String> fieldSet) {
    if (value == null || !(value instanceof Shapable)
        && !(value instanceof Iterable) && !(value instanceof Map)) {
      return value;
    }
    if (value instanceof Iterable<?> iterable) {
      List<Object> items = new ArrayList<>();
      for (Object item : iterable) {
        items.add(item instanceof Shapable ? filterValue(item, fieldSet) : item);
      }
      return items;
    }
    if (value instanceof Map<?, ?> mapValue) {
      @SuppressWarnings("unchecked")
      Map<String, Object> map = (Map<String, Object>) mapValue;
      Map<String, Object> result = new LinkedHashMap<>();
      for (Map.Entry<String, Object> entry : map.entrySet()) {
        Object entryValue = entry.getValue();
        result.put(entry.getKey(),
            entryValue instanceof Shapable ? filterValue(entryValue, fieldSet) : entryValue);
      }
      return result;
    }
    return filterMap(objectMapper.convertValue(
        value, new TypeReference<LinkedHashMap<String, Object>>() {
        }), fieldSet);
  }

  private Map<String, Object> filterMap(Map<String, Object> map, Set<String> fieldSet) {
    map.keySet().removeIf(key -> !fieldSet.contains(key));
    if (fieldSet.contains("_links") && map.containsKey("links") && !map.containsKey("_links")) {
      map.put("_links", map.remove("links"));
    }
    return map;
  }

  private boolean isShapableBody(Object body) {
    if (body == null) {
      return false;
    }
    if (body instanceof Shapable) {
      return true;
    }
    if (body instanceof Map<?, ?> map) {
      for (Object v : map.values()) {
        if (v instanceof Shapable) {
          return true;
        }
      }
      return false;
    }
    if (body instanceof Iterable<?> it) {
      for (Object v : it) {
        if (v instanceof Shapable) {
          return true;
        }
      }
      return false;
    }
    return false;
  }
}
