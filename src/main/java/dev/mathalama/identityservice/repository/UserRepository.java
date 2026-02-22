package dev.mathalama.identityservice.repository;

import dev.mathalama.identityservice.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID> {
    /// for login
    Optional<Users> findByEmailIgnoreCaseOrUsernameIgnoreCase(
            String email,
            String username
    );

    Optional<Users> findByUsername(String username);
}
