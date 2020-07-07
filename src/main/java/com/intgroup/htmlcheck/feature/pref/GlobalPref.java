package com.intgroup.htmlcheck.feature.pref;

import lombok.Data;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

@Data
@Entity
@Table(name = "global_pref")
public class GlobalPref {
    @Id
    @Column(length = 200)
    private String name;

    @Column(length = 3000)
    private String value;
}
