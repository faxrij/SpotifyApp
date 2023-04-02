package com.example.spotifyproject.model.response;

import lombok.*;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.ZonedDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class CommonResponseField implements Serializable {
    private String id;
    private ZonedDateTime createdDate;
    private ZonedDateTime modifiedDate;
}
