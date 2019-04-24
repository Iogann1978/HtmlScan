package ru.home.htmlscan.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class MailItem {
    @NonNull
    private String subject;
    @NonNull
    private String message;
}
