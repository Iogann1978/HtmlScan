package ru.home.htmlscan.controller;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import ru.home.htmlscan.service.MailService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("htmlscan")
@Slf4j
public class HtmlController {

    private MailService mailService;
    private ThreadPoolTaskExecutor htmlExecutor, mailExecutor;

    @Autowired
    public HtmlController(MailService mailService,
                          @Qualifier("htmlExecutor") TaskExecutor htmlExecutor,
                          @Qualifier("mailExecutor") TaskExecutor mailExecutor) {
        this.mailService = mailService;
        this.htmlExecutor = (ThreadPoolTaskExecutor) htmlExecutor;
        this.mailExecutor = (ThreadPoolTaskExecutor) mailExecutor;
    }

    @GetMapping("/list")
    public List<String> getList() {
        return mailService.getRegister().entrySet().stream().
                map(e -> "uri=" + e.getKey() + " state=" + e.getValue()).collect(Collectors.toList());
    }

    @GetMapping("/tasks")
    public String getTasks() {
        val sb = new StringBuilder();
        sb.append("Html tasks:\n");
        sb.append("active count =" + htmlExecutor.getActiveCount());
        sb.append("\n");
        sb.append("pool size =" + htmlExecutor.getPoolSize());
        sb.append("\n");
        sb.append("Mail executor:\n");
        sb.append("active count=" + mailExecutor.getActiveCount());
        sb.append("\n");
        sb.append("pool size =" + mailExecutor.getPoolSize());
        return sb.toString();
    }

    @PostMapping("/mailcheck")
    public void mailCheck(@RequestParam("subject") String subject, @RequestParam("message") String message) {
        mailService.sendMessage(subject, message);
    }
}
