package dev.mathalama.identityservice.infrastructure.repository;

import dev.mathalama.identityservice.domain.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID> {
    
    Optional<Users> findByEmailIgnoreCaseOrUsernameIgnoreCase(String email, String username);
    
    Optional<Users> findByUsername(String username);
    
    Optional<Users> findByEmail(String email);
}
