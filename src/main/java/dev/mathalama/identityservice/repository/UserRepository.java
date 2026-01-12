package dev.mathalama.identityservice.repository;

import dev.mathalama.identityservice.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    void deleteByEmail(String email);
    void deleteByUsername(String username);
    boolean getUsersByEmail(String email);
    Optional<Users> findByEmail(String email);

    /// for login
    Optional<Users> findByEmailIgnoreCaseOrUsernameIgnoreCase(
            String email,
            String username
    );
}
