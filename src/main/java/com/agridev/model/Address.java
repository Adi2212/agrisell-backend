package com.agridev.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Entity class to store address details of user
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "addresses")
public class Address extends BaseEntity {

    private String street;

    private String city;

    private String state;

    private String district;

    private String postalCode;

    private String country;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
