package com.bookingapp.cabin.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private Users user;

    @ManyToOne
    @JoinColumn(name = "cabin_id", referencedColumnName = "cabin_id")
    private Cabin cabin;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "queue_position")
    private Integer queuePosition;

    @Column(name = "status")
    private String status;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "booking_created_date")
    private LocalDateTime bookingCreatedDate;


    @Column(name = "points_required")
    private int pointsRequired;

    @Column(name = "number_of_guests", nullable = false)
    private int numberOfGuests;

    //referansenummer for bookingen
    @Column(name = "booking_code", unique = true)
    private String bookingCode;


    public Booking(Users user, Cabin cabin, LocalDate startDate, LocalDate endDate, String status) {
        this.user = user;
        this.cabin = cabin;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.queuePosition = null;
    }

}
