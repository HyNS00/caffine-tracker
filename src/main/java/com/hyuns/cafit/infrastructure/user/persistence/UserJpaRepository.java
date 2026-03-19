package com.hyuns.cafit.infrastructure.user.persistence;

import com.hyuns.cafit.domain.user.User;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface UserJpaRepository extends ListCrudRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
