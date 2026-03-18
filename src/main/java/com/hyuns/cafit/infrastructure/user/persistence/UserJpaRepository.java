package com.hyuns.cafit.infrastructure.user.persistence;

import com.hyuns.cafit.domain.user.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserJpaRepository extends CrudRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
