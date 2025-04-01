package com.bookingapp.cabin.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private double price;


    public Booking(Users user, Cabin cabin, LocalDate startDate, LocalDate endDate, String status) {
        this.user = user;
        this.cabin = cabin;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.price = 0.0;
        this.queuePosition = null;
    }

}
