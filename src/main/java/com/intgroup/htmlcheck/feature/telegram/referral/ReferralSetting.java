package com.intgroup.htmlcheck.feature.telegram.referral;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class ReferralSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private long id;
    
    @Column
    private String sourceTag;
    
    @Column
    private String targetTag;
}
