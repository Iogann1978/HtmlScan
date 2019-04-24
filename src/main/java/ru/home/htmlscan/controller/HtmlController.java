package ru.home.htmlscan.controller;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.home.htmlscan.service.MailService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("htmlscan")
@Slf4j
public class HtmlController {

    private MailService mailService;

    @Autowired
    public HtmlController(MailService mailService) {
        this.mailService = mailService;
    }

    @GetMapping("/list")
    public List<String> getList() {
        return mailService.getRegister().entrySet().stream().
                map(e -> "uri=" + e.getKey() + " state=" + e.getValue()).collect(Collectors.toList());
    }

    @PostMapping("/mailcheck")
    public void mailCheck(@RequestParam("subject") String subject, @RequestParam("message") String message) {
        mailService.sendMessage(subject, message);
    }
}
