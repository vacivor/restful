package io.vacivor.restful.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserStatusConverter implements AttributeConverter<UserStatusEnum, Integer> {

  @Override
  public Integer convertToDatabaseColumn(UserStatusEnum attribute) {
    return attribute == null ? null : attribute.getCode();
  }

  @Override
  public UserStatusEnum convertToEntityAttribute(Integer dbData) {
    return UserStatusEnum.fromCode(dbData);
  }
}
