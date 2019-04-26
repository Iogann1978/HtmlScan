package ru.home.htmlscan.model;

import lombok.*;
import org.jsoup.Jsoup;

@Data
@Builder
@ToString
public class RegisterItem {
    private String EVENT, EVENT_TYPE, TYPE_XML_ID, MEET_PLACE, ADRES, MONTH, DAY, TIME,
            OBJECT_ID, PASSPORT_NUMBER, PASSPORT_GET, PASSPORT_GET_DATE, YEAR;
    private String TIME_ID, FIO, PHONE, EMAIL;

    public static RegisterItem get(String html) {
        val doc = Jsoup.parse(html);
        return RegisterItem.builder()
                .EVENT(doc.getElementsByAttributeValue("name", "EVENT").attr("value"))
                .EVENT_TYPE(doc.getElementsByAttributeValue("name", "EVENT_TYPE").attr("value"))
                .TYPE_XML_ID(doc.getElementsByAttributeValue("name", "TYPE_XML_ID").attr("value"))
                .MEET_PLACE(doc.getElementsByAttributeValue("name", "MEET_PLACE").attr("value"))
                .ADRES(doc.getElementsByAttributeValue("name", "ADRES").attr("value"))
                .MONTH(doc.getElementsByAttributeValue("name", "MONTH").attr("value"))
                .DAY(doc.getElementsByAttributeValue("name", "DAY").attr("value"))
                .TIME(doc.getElementsByAttributeValue("name", "TIME").attr("value"))
                .TIME_ID(doc.getElementsByAttributeValue("name", "TIME_ID").attr("value"))
                .OBJECT_ID(doc.getElementsByAttributeValue("name", "OBJECT_ID").attr("value"))
                .build();
    }
}
