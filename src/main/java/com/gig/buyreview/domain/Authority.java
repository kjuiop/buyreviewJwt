package com.gig.buyreview.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * @author : Jake
 * @date : 2022-01-08
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Authority {

    @Id
    @Column(name = "authority_name")
    private String authorityName;
}
