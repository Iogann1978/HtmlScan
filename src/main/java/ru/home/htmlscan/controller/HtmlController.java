package ru.home.htmlscan.controller;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.home.htmlscan.service.MailService;

import java.util.ArrayList;
import java.util.List;

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
        val list = new ArrayList<String>();
        mailService.getRegister().entrySet().stream().forEach(e -> list.add("uti=" + e.getKey() + " state=" + e.getValue()));
        return list;
    }

    @PostMapping("/mailcheck")
    public void mailCheck(@RequestParam("subject") String subject, @RequestParam("message") String message) {
        mailService.sendMessage(subject, message);
    }
}
