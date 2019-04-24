package ru.home.htmlscan.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.home.htmlscan.model.SiteStates;

import javax.mail.MessagingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MailService {
    private JavaMailSender sender;
    private static final String phrase = ">Регистрация<";

    private Map<String, SiteStates> register = new ConcurrentHashMap<>();

    @Autowired
    public MailService(JavaMailSender sender) {
        this.sender = sender;
    }

    @Async("mailExecutor")
    public void checkSite(String uri, String code) {
        if(!register.containsKey(uri)) {
            register.put(uri, SiteStates.ADDED);
            log.info("Site {} has added to register", uri);
        }

        if(code.contains(phrase) && register.get(uri) != SiteStates.REG_OPENED) {
            log.info("Registration for site {} is open!", uri);
            sendMessage("Registration has opened!", String.format("Registration for site %s is open!", uri));
            register.put(uri, SiteStates.REG_OPENED);
            return;
        }

        if(!code.contains(phrase) && register.get(uri) != SiteStates.REG_CLOSED) {
            register.put(uri, SiteStates.REG_CLOSED);
            return;
        }
    }

    public void sendMessage(String subject, String text) {
        val message = sender.createMimeMessage();
        val helper = new MimeMessageHelper(message);
        try {
            helper.setTo("iogann1978@gmail.com");
            helper.setText(text);
            helper.setSubject(subject);
        } catch (MessagingException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        sender.send(message);
    }
}
