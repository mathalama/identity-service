package dev.mathalama.identityservice.repository;

import dev.mathalama.identityservice.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
