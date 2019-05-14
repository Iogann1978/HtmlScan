package ru.home.htmlscan.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

import javax.validation.constraints.*;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Data
@ToString
@NoArgsConstructor
@Slf4j
public class RegisterItem {
    @NotNull
    private String PASSPORT_NUMBER, PASSPORT_GET, PASSPORT_DATE;
    @Max(2019)
    @Min(1900)
    private int YEAR;
    @NotNull
    private String FIO, PHONE;
    @Email
    private String EMAIL;

    public List<Map<String, String>> form(String html) {
        val forms = Jsoup.parse(html).select("form");

        val list = new ArrayList<Map<String, String>>();
        forms.stream().forEach(form -> {
            val fields = new HashMap<String, String>();
            form.getAllElements().stream()
                    .filter(element -> !element.attr("name").isEmpty())
                    .forEach(element_filtered -> {
                        val key = element_filtered.attr("name");
                        switch(key) {
                            case "FIO":
                                fields.put(key, encodeField(FIO));
                                break;
                            case "PHONE":
                                fields.put(key, encodeField(PHONE));
                                break;
                            case "EMAIL":
                                fields.put(key, encodeField(EMAIL));
                                break;
                            case "PASSPORT_NUMBER":
                                fields.put(key, encodeField(PASSPORT_NUMBER));
                                break;
                            case "PASSPORT_GET":
                                fields.put(key, encodeField(PASSPORT_GET));
                                break;
                            case "PASSPORT_DATE":
                                fields.put(key, encodeField(PASSPORT_DATE));
                                break;
                            case "YEAR":
                                fields.put(key, encodeField(String.valueOf(YEAR)));
                                break;
                            default:
                                fields.put(key, encodeField(element_filtered.attr("value")));
                                break;
                        }
                    });
            list.add(fields);
        });
        return list;
    }

    public static String encodeField(String field) {
        String encoded = "";
        try {
            encoded = URLEncoder.encode(field, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return encoded;
    }
}
