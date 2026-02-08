package io.vacivor.restful.assembler;

import io.vacivor.restful.domain.User;
import io.vacivor.restful.dto.UserResponse;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class UserModelAssembler implements RepresentationModelAssembler<User, UserResponse> {

  @Override
  public UserResponse toModel(User user) {
    UserResponse dto = new UserResponse();
    dto.setId(user.getId());
    dto.setUsername(user.getUsername());
    dto.setEmail(user.getEmail());
    dto.setStatus(user.getStatus() == null ? null : user.getStatus().getCode());
    dto.setCreatedAt(user.getCreatedAt());
    return dto;
  }
}
