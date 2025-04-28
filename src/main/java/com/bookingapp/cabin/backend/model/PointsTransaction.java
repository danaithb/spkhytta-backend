package com.bookingapp.cabin.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "points_transactions")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PointsTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "points_id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private Users user;

    @Column(name = "points_change", nullable = false)
    private int pointsChange;

    @Column(name = "type", length = 50, nullable = false)
    private String type;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

}
