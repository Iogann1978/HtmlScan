package ru.home.htmlscan.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserItem {
    @NonNull
    private String name;
    @NonNull
    private String email;
    @NonNull
    private String phone;
}
