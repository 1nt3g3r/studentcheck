package com.intgroup.htmlcheck.feature.mq.settings;

import lombok.Data;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.StringJoiner;

@Entity
@Data
public class Setting {
    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String name;

    @Column
    private String tag;

    @Column
    private String date;

    @Column(length = 1000)
    private String value;

    public String getFullName() {
        StringBuilder result = new StringBuilder();

        result.append(name);

        if (tag != null && !tag.isBlank()) {
            result.append("Tag");
        }

        if (date != null && !date.isBlank()) {
            result.append("Date");
        }

        return result.toString();
    }

    public String getHumanReadableDescription() {
        String fullName = getFullName();

        StringJoiner additionalParts = new StringJoiner(", ");
        if (tag != null && !tag.isBlank()) {
            additionalParts.add(tag);
        }
        if (date != null && !date.isBlank()) {
            additionalParts.add(date);
        }

        if (additionalParts.length() > 0) {
            return fullName + " (" + additionalParts + ")";
        }

        return fullName;
    }
}
