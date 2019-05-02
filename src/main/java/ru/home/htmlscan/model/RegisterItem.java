package ru.home.htmlscan.model;

import lombok.*;
import org.jsoup.Jsoup;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Data
@ToString
@NoArgsConstructor
public class RegisterItem {
    private String PASSPORT_NUMBER, PASSPORT_GET, PASSPORT_GET_DATE, YEAR;
    @NonNull
    private String FIO, PHONE, EMAIL;

    public MultiValueMap<String, String> form(String html) {
        val doc = Jsoup.parse(html);
        val form = doc.selectFirst("form");
        val fields = new LinkedMultiValueMap<String, String>();
        form.getAllElements().stream()
                .filter(element -> !element.attr("name").isEmpty())
                .forEach(element_filtered -> {
                    val key = element_filtered.attr("name");
                    switch(key) {
                        case "FIO":
                            fields.add(key, FIO);
                            break;
                        case "PHONE":
                            fields.add(key, PHONE);
                            break;
                        case "EMAIL":
                            fields.add(key, EMAIL);
                            break;
                        case "PASSPORT_NUMBER":
                            fields.add(key, PASSPORT_NUMBER);
                            break;
                        case "PASSPORT_GET":
                            fields.add(key, PASSPORT_GET);
                            break;
                        case "PASSPORT_GET_DATE":
                            fields.add(key, PASSPORT_GET_DATE);
                            break;
                        case "YEAR":
                            fields.add(key, YEAR);
                            break;
                        default:
                            fields.add(key, element_filtered.attr("value"));
                            break;
                    }
                });
        return fields;
    }
}
