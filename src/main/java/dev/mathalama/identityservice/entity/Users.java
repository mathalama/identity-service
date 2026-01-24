package dev.mathalama.identityservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Users implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String username;

    @Column(unique = true)
    String email;

    String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

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
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
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
