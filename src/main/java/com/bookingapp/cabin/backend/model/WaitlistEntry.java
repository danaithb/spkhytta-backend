package com.bookingapp.cabin.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "waitlist_entries")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WaitlistEntry {

    @Id
    @Column(name = "booking_id")
    private Long bookingId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "booking_id", referencedColumnName = "booking_id")
    private Booking booking;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
