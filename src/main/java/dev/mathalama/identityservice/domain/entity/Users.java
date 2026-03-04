package dev.mathalama.identityservice.domain.entity;

import dev.mathalama.identityservice.domain.enums.AccountState;
import dev.mathalama.identityservice.domain.enums.SecurityStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Users implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    private String permissions;

    @Column(name = "account_state", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountState accountState;

    @Column(name = "security_status")
    @Enumerated(EnumType.STRING)
    private SecurityStatus securityStatus;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "verified_at")
    private Date verifiedAt;

    @Column(name = "last_verification_sent_at")
    private Date lastVerificationSentAt;

    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    private Users(UsersBuilder builder) {
        this.email = builder.email;
        this.password = builder.password;
        this.username = builder.username;
        this.roles = builder.roles;
    }

    public static UsersBuilder builder() {
        return new UsersBuilder();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountState != AccountState.DELETED;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountState != AccountState.DISABLED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return accountState == AccountState.ACTIVE
                || accountState == AccountState.PENDING_VERIFICATION;
    }

    public static class UsersBuilder {
        private String email;
        private String password;
        private String username;
        private Set<Role> roles = new HashSet<>();

        public UsersBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UsersBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UsersBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UsersBuilder roles(Set<Role> roles) {
            this.roles = roles;
            return this;
        }

        public Users build() {
            if (email == null || password == null || username == null) {
                throw new IllegalStateException("User fields must not be null");
            }
            return new Users(this);
        }
    }
}
