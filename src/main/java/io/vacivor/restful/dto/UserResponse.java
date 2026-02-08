package io.vacivor.restful.dto;

import io.vacivor.restful.hateoas.Shapable;
import java.time.OffsetDateTime;
import org.springframework.hateoas.RepresentationModel;

public class UserResponse extends RepresentationModel<UserResponse> implements Shapable {

  private Long id;
  private String username;
  private String email;
  private Integer status;
  private OffsetDateTime createdAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
