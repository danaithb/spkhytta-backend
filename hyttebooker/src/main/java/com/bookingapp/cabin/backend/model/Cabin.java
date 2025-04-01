package com.bookingapp.cabin.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Cabin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cabin_id")
    private Long cabinId;


    @Column(name = "cabin_name")
    private String cabinName;

}

