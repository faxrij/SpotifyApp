package com.example.spotifyproject.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;


@Table(name = "users")
@Entity
@Getter
@Setter
public class User extends Auditable {

    @Column(name = "name")
    private String name;
    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "is_verified")
    private boolean verified;

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "verification_code_expired_date")
    private ZonedDateTime verificationCodeExpiredDate;

    @Column(name = "recovery_code")
    private String recoveryCode;

    @Column(name = "recovery_code_expired_date")
    private ZonedDateTime recoveryCodeExpiredDate;

    @OneToMany(targetEntity = ContractRecord.class, cascade = CascadeType.ALL, mappedBy = "user_fk")
    private List<ContractRecord> contractRecords;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "User_Liked_Song_Table",
            joinColumns = {
                    @JoinColumn(name = "user_id", referencedColumnName = "id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "song_id",referencedColumnName = "id")
            })
    private List<Song> songs;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "User_Liked_Category_Table",
            joinColumns = {
                    @JoinColumn(name = "user_id", referencedColumnName = "id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "category_id",referencedColumnName = "id")
            })
    private List<Category> categories;

}