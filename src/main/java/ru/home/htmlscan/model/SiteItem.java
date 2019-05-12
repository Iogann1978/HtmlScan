package ru.home.htmlscan.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SiteItem {
    private SiteState state;
    private int attempts, sent;
    private long visits;

    public void attemptsInc() {
        attempts++;
    }

    public void sentInc() {
        sent++;
    }

    public void visitsInc() {
        visits++;
    }
}
