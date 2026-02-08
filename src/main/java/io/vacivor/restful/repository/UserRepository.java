package io.vacivor.restful.repository;

import io.vacivor.restful.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {}
