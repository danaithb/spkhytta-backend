package com.bookingapp.cabin.backend.repository;

import com.bookingapp.cabin.backend.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

//Repository for booking, håndterer databaseoperasjoner
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCabin_CabinIdAndStatus(Long cabinId, String status);
    List<Booking> findByCabin_CabinIdAndStatusAndStartDateBetween(Long cabinId, String status, LocalDate start, LocalDate end);
    List<Booking> findByCabin_CabinIdAndStatusOrderByQueuePositionAsc(Long cabinId, String status);
    List<Booking> findByCabin_CabinIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long cabinId, String status, LocalDate endDate, LocalDate startDate);
    Booking findTopByUser_UserIdAndStatusOrderByEndDateDesc(Long userId, String status);
    List<Booking> findByUser_FirebaseUidOrderByStartDateDesc(String firebaseUid);
    Optional<Booking> findByBookingCode(String bookingCode);
    List<Booking> findByStartDateGreaterThanEqualAndEndDateLessThanEqual(LocalDate startDate, LocalDate endDate);
    List<Booking> findByStatusIgnoreCaseAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            String status, LocalDate endDate, LocalDate startDate
    );

}
