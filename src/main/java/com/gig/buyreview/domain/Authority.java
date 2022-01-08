package com.gig.buyreview.domain;

import lombok.*;

import javax.persistence.*;

/**
 * @author : Jake
 * @date : 2022-01-08
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Authority {

    @Id
    @Column(name = "authority_name")
    private String authorityName;
}
