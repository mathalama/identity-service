package dev.mathalama.identityservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String username;

    @Column(unique = true)
    String email;

    String password;

    private Users(UsersBuilder builder) {
        this.email = builder.email;
        this.password = builder.password;
        this.username = builder.username;
    }

    public static UsersBuilder builder() {
        return new UsersBuilder();
    }

    public static class UsersBuilder {
        private String email;
        private String password;
        private String username;

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

        public Users build() {
            if (email == null || password == null || username == null) {
                throw new IllegalStateException("User fields must not be null");
            }
            return new Users(this);
        }
    }
}
