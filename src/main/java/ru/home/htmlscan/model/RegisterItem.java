package ru.home.htmlscan.model;

import lombok.*;
import org.jsoup.Jsoup;

@Data
@ToString
public class RegisterItem {
    private String modalObj, TIME_ID;
    private String PASSPORT_NUMBER, PASSPORT_GET, PASSPORT_GET_DATE, YEAR;
    private String FIO, PHONE, EMAIL;

    public void form(String html) {
        val doc = Jsoup.parse(html);
        TIME_ID = doc.getElementById("TIME_ID").attr("value");
        val form = doc.selectFirst("form");
        form.getElementById("FIO").attr("value", FIO);
        form.getElementById("PHONE").attr("value", PHONE);
        form.getElementById("EMAIL").attr("value", EMAIL);
        modalObj = form.html();
    }
}
