package com.openclassrooms.ycyw.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.validator.constraints.UUID;


/**
 * Represents a user, may be persisted
 * @author Wilhelm Zwertvaegher
 * Date:02/16/2025
 * Time:10:36
 */

@Entity
@Table(name="user")
public class User {

    @Id
    @UUID
    private String id;

    @Column(name = "user_name", unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

}
