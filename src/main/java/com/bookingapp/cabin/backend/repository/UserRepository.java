package com.bookingapp.cabin.backend.repository;

import com.bookingapp.cabin.backend.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

//Repository for users, håndterer databaseoperasjoner
@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);
    Optional<Users> findByFirebaseUid(String firebaseUid);
}