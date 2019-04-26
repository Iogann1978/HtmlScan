package ru.home.htmlscan.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.home.htmlscan.model.SiteItem;
import ru.home.htmlscan.model.SiteState;

import javax.mail.MessagingException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MailService {
    private JavaMailSender sender;
    private static final String phrase = ">Регистрация<";

    @Getter
    private Map<String, SiteItem> register = new ConcurrentHashMap<>();

    @Autowired
    public MailService(JavaMailSender sender) {
        this.sender = sender;
    }

    @Async("mailExecutor")
    public CompletableFuture<Boolean> checkSite(String uri, String code) {
        SiteItem item = register.computeIfAbsent(uri, key -> {
            val siteItem = SiteItem.builder()
                    .attemps(0)
                    .sended(0)
                    .visits(0L)
                    .state(SiteState.ADDED)
                    .build();
            log.info("Site {} has added to register", uri);
            return siteItem;
        });

        item.visitsInc();

        if(item.getState() == SiteState.REGISTERED) {
            return CompletableFuture.completedFuture(false);
        }

        if(code.contains(phrase) && item.getState() != SiteState.REG_OPENED) {
            log.info("Registration for site {} is open!", uri);
            item.attempsInc();
            sendMessage("Registration has opened!", String.format("Registration for site %s is open!", uri));
            item.sendedInc();
            item.setState(SiteState.REG_OPENED);
            return CompletableFuture.completedFuture(true);
        }

        if(!code.contains(phrase) && item.getState() != SiteState.REG_CLOSED) {
            item.setState(SiteState.REG_CLOSED);
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.completedFuture(false);
    }

    @Async("mailExecutor")
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

    public void setRegistered(String uri) {
        register.get(uri).setState(SiteState.REGISTERED);
    }
}
