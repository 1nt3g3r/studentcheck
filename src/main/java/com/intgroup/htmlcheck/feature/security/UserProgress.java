package com.intgroup.htmlcheck.feature.security;

import lombok.Data;

@Data
public class UserProgress {
    private int totalTaskCount;
    private int passedTaskCount;
    private int progressPercent;
    private boolean canReceiveCertificate;
}
