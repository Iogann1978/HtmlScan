package ru.home.htmlscan.model;

import lombok.Getter;

public enum SiteState {
    ADDED(0), REG_OPENED(1), REG_CLOSED(2), SENDED(3), REGISTERED(4);
    @Getter
    private int num;
    SiteState(int num) {
        this.num = num;
    }
}
