package com.example.spotifyproject.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Table(name = "song")
@Entity
@Getter
@Setter
public class Song extends Auditable {
    @Column(name = "name")
    private String name;
    @Column(name = "title")
    private String title;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "Song_Category_Table",
            joinColumns = {
                    @JoinColumn(name = "song_id", referencedColumnName = "id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "category_id",referencedColumnName = "id")
            })
    private List<Category> categories;

    @Column(name = "lyrics", columnDefinition = "TEXT")
    private String lyrics;

    @Column(name = "composer")
    private String composerName;

    @ManyToMany(mappedBy = "songs", fetch = FetchType.LAZY)
    private List<User> users;
}
