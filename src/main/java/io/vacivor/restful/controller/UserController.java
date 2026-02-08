package io.vacivor.restful.controller;

import com.flipkart.zjsonpatch.Jackson3JsonPatch;
import io.vacivor.restful.assembler.UserModelAssembler;
import io.vacivor.restful.common.exception.BadRequestException;
import io.vacivor.restful.common.exception.NotFoundException;
import io.vacivor.restful.common.ordering.OrderingDelimiters;
import io.vacivor.restful.common.ordering.OrderingParameters;
import io.vacivor.restful.common.ordering.SpringDataOrderingAdapter;
import io.vacivor.restful.common.pagination.PaginationInfo;
import io.vacivor.restful.common.pagination.PaginationParameters;
import io.vacivor.restful.domain.User;
import io.vacivor.restful.domain.UserStatusEnum;
import io.vacivor.restful.dto.UserCreateRequest;
import io.vacivor.restful.dto.UserResponse;
import io.vacivor.restful.hateoas.MethodLink;
import io.vacivor.restful.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/users")
public class UserController {

  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;
  private final UserModelAssembler assembler;
  private static final String ORDER_BY_DELIMITER = ",";

  public UserController(
      UserRepository userRepository,
      ObjectMapper objectMapper,
      UserModelAssembler assembler) {
    this.userRepository = userRepository;
    this.objectMapper = objectMapper;
    this.assembler = assembler;
  }

  @GetMapping
  public ResponseEntity<List<UserResponse>> list(
      @RequestParam(name = "page", required = false) Integer page,
      @RequestParam(name = "pageSize", required = false) Integer pageSize,
      @RequestParam(name = "orderBy", required = false) String orderBy,
      HttpServletRequest request) {

    int requestedPage = page == null ? 1 : page;
    if (requestedPage < 1) {
      throw new BadRequestException("page must be >= 1");
    }
    PaginationParameters paginationParameters = PaginationParameters.of(requestedPage - 1,
        pageSize);

    OrderingParameters<Sort, Sort.Order> ordering = new OrderingParameters<>(
        orderBy, new SpringDataOrderingAdapter())
        .allow(allowedOrderFields())
        .delimiters(OrderingDelimiters.of(ORDER_BY_DELIMITER, "\\s+"))
        .parse();

    Sort sort = ordering.getSort();

    int pageIndex = paginationParameters.getPage();
    int size = paginationParameters.getPageSize();

    PageRequest pageable = PageRequest.of(pageIndex, size, sort);
    Page<User> result = userRepository.findAll(pageable);

    List<UserResponse> items = result.getContent().stream()
        .map(assembler::toModel)
        .map(item -> {
          attachMethodLinks(item);
          return item;
        })
        .toList();

    long total = result.getTotalElements();
    long totalPage = total == 0 ? 0 : (total + size - 1) / size;
    PaginationInfo pagination = new PaginationInfo(
        pageIndex + 1, size, total, totalPage);

    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Pagination", toJson(pagination));
    String linkHeader = buildPaginationLinks(
        request, pageIndex + 1, size, totalPage);
    if (!linkHeader.isEmpty()) {
      headers.add(HttpHeaders.LINK, linkHeader);
    }

    return ResponseEntity.ok().headers(headers).body(items);
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> get(@PathVariable("id") long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("User not found"));
    UserResponse dto = assembler.toModel(user);
    attachMethodLinks(dto);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.LINK, formatLink(WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(UserController.class).get(dto.getId())).toUri()
        .toString(), "self"));

    return ResponseEntity.ok().headers(headers).body(dto);
  }

  @PostMapping
  public ResponseEntity<UserResponse> create(
      @RequestBody UserCreateRequest payload,
      HttpServletRequest request) {
    if (payload == null) {
      throw new BadRequestException("Body is required");
    }
    String username = trimToNull(payload.getUsername());
    String email = trimToNull(payload.getEmail());
    if (username == null || email == null) {
      throw new BadRequestException("username and email are required");
    }

    User user = new User();
    user.setUsername(username);
    user.setEmail(email);
    user.setStatus(UserStatusEnum.ENABLED);
    User saved = userRepository.save(user);

    UserResponse dto = assembler.toModel(saved);
    attachMethodLinks(dto);

    String selfUrl = buildEntityUrl(request, saved.getId());
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.LOCATION, selfUrl);
    headers.add(HttpHeaders.LINK, formatLink(selfUrl, "self"));

    return ResponseEntity.status(201).headers(headers).body(dto);
  }

  @PatchMapping(path = "/{id}", consumes = "application/json-patch+json")
  public ResponseEntity<Object> patch(
      @PathVariable("id") long id,
      @RequestBody JsonNode patch,
      HttpServletRequest request) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("User not found"));
    validatePatchAllowed(patch);
    User patched = applyPatch(patch, user);
    User saved = userRepository.save(patched);

    UserResponse dto = assembler.toModel(saved);
    attachMethodLinks(dto);

    String selfUrl = buildEntityUrl(request, saved.getId());
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.LINK, formatLink(selfUrl, "self"));

    return ResponseEntity.ok().headers(headers).body(dto);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Object> delete(@PathVariable("id") long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("User not found"));
    userRepository.delete(user);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}:enable")
  public ResponseEntity<UserResponse> enable(@PathVariable("id") long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("User not found"));
    user.setStatus(UserStatusEnum.ENABLED);
    User saved = userRepository.save(user);

    UserResponse dto = assembler.toModel(saved);
    attachMethodLinks(dto);

    HttpHeaders headers = new HttpHeaders();

    return ResponseEntity.ok().headers(headers).body(dto);
  }

  @PostMapping("/{id}:disable")
  public ResponseEntity<UserResponse> disable(@PathVariable("id") long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("User not found"));
    user.setStatus(UserStatusEnum.DISABLED);
    User saved = userRepository.save(user);

    UserResponse dto = assembler.toModel(saved);
    attachMethodLinks(dto);

    HttpHeaders headers = new HttpHeaders();

    return ResponseEntity.ok().headers(headers).body(dto);
  }

  private Set<String> allowedOrderFields() {
    return Set.of("id", "username", "email", "status", "createdAt");
  }

  private Set<String> allowedPatchFields() {
    return Set.of("username", "email");
  }

  private String toJson(PaginationInfo pagination) {
    try {
      return objectMapper.writeValueAsString(pagination);
    } catch (JacksonException e) {
      throw new RuntimeException("Failed to encode pagination info", e);
    }
  }

  private User applyPatch(JsonNode patch, User target) {
    try {
      JsonNode targetNode = objectMapper.convertValue(target, JsonNode.class);
      JsonNode patchedNode = Jackson3JsonPatch.apply(patch, targetNode);
      return objectMapper.treeToValue(patchedNode, User.class);
    } catch (JacksonException e) {
      throw new BadRequestException("Invalid JSON Patch");
    }
  }

  private void validatePatchAllowed(JsonNode patch) {
    if (patch == null || !patch.isArray()) {
      throw new BadRequestException("Invalid JSON Patch");
    }
    for (JsonNode op : patch) {
      JsonNode pathNode = op.get("path");
      if (pathNode == null || pathNode.isNull()) {
        throw new BadRequestException("Invalid JSON Patch");
      }
      String path = pathNode.asText();
      if (path == null || !path.startsWith("/")) {
        throw new BadRequestException("Invalid JSON Patch");
      }
      String field = path.substring(1).split("/", 2)[0];
      if (!allowedPatchFields().contains(field)) {
        throw new BadRequestException("Patch not allowed for field: " + field);
      }
    }
  }

  private void attachMethodLinks(UserResponse dto) {
    String selfHref = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(UserController.class).get(dto.getId()))
        .toUri()
        .toString();
    String patchHref = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(UserController.class).patch(dto.getId(), null, null))
        .toUri()
        .toString();
    String deleteHref = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(UserController.class).delete(dto.getId()))
        .toUri()
        .toString();
    String enableHref = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(UserController.class).enable(dto.getId()))
        .toUri()
        .toString();
    String disableHref = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(UserController.class).disable(dto.getId()))
        .toUri()
        .toString();

    dto.add(MethodLink.of(selfHref, "self", "GET"));
    dto.add(MethodLink.of(patchHref, "patch", "PATCH"));
    dto.add(MethodLink.of(deleteHref, "delete", "DELETE"));
    dto.add(MethodLink.of(enableHref, "enable", "POST"));
    dto.add(MethodLink.of(disableHref, "disable", "POST"));
  }

  private String buildEntityUrl(HttpServletRequest request, Object id) {
    return ServletUriComponentsBuilder.fromRequestUri(request)
        .replacePath("/users/" + id)
        .replaceQuery(null)
        .toUriString();
  }

  private String buildPaginationLinks(
      HttpServletRequest request, int page, int pageSize, long totalPage) {
    if (totalPage == 0) {
      return "";
    }
    int lastPage = (int) totalPage;
    List<String> links = new ArrayList<>();

    String self = buildPageUrl(request, page, pageSize);
    links.add(formatLink(self, "self"));

    String first = buildPageUrl(request, 1, pageSize);
    links.add(formatLink(first, "first"));

    if (page > 1) {
      String prev = buildPageUrl(request, page - 1, pageSize);
      links.add(formatLink(prev, "prev"));
    }

    if (page < lastPage) {
      String next = buildPageUrl(request, page + 1, pageSize);
      links.add(formatLink(next, "next"));
    }

    String last = buildPageUrl(request, lastPage, pageSize);
    links.add(formatLink(last, "last"));

    return String.join(", ", links);
  }

  private String buildPageUrl(HttpServletRequest request, int page, int pageSize) {
    return ServletUriComponentsBuilder.fromRequest(request)
        .replaceQueryParam("page", page)
        .replaceQueryParam("pageSize", pageSize)
        .toUriString();
  }

  private String formatLink(String url, String rel) {
    return "<" + url + ">; rel=\"" + rel + "\"; method=\"GET\"";
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
