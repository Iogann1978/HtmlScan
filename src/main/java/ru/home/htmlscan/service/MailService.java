package ru.home.htmlscan.service;

public interface MailService {
    void sendMessage(String subject, String text, String email);
}
