package com.example.spotifyproject.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Table(name = "file")
@Entity
@Getter
@Setter
public class File extends Auditable{
    @Column(name = "name")
    private String name;
    @Column(name = "url")
    private String url;

    @Column(name = "content_type")
    private String contentType;

}
