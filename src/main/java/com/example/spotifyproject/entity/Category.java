package com.example.spotifyproject.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Table(name = "category")
@Entity
@Getter
@Setter
public class Category extends Auditable{

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "parent")
    private Category parent;

    @Column(name = "is_super_category", updatable = false)
    private boolean isSuperCategory;

    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    private List<Song> song_list;

    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    private List<User> users;

}
