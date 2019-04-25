package ru.home.htmlscan.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SiteItem {
    private SiteState state;
    private int attemps, sended;
    private long visits;

    public void attempsInc() {
        attemps++;
    }

    public void sendedInc() {
        sended++;
    }

    public void visitsInc() {
        visits++;
    }
}