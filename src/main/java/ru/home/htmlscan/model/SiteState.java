package ru.home.htmlscan.model;

import lombok.Getter;

public enum SiteState {
    ADDED(0), REG_OPENED(1), REG_CLOSED(2), EMBASSY_SENDED(3), OPEN_SENDED(4), REGISTERED(5);
    @Getter
    private int num;
    SiteState(int num) {
        this.num = num;
    }
}
