package com.iot.management.repository;

import com.iot.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    @org.springframework.data.jpa.repository.Lock(javax.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from User u where u.email = :email")
    Optional<User> lockByEmail(@org.springframework.data.repository.query.Param("email") String email);
    java.util.List<User> findByRegionAndEnabledTrueAndLockedFalse(String region);
    boolean existsByEmail(String email);
}
