package ru.home.htmlscan.controller;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import ru.home.htmlscan.model.MailItem;
import ru.home.htmlscan.service.MailService;
import ru.home.htmlscan.service.ScanService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("htmlscan")
@Slf4j
public class HtmlController {

    private MailService mailService;
    private ScanService scanService;
    private ThreadPoolTaskExecutor htmlExecutor, mailExecutor;

    @Autowired
    public HtmlController(MailService mailService, ScanService scanService,
                          @Qualifier("htmlExecutor") TaskExecutor htmlExecutor,
                          @Qualifier("mailExecutor") TaskExecutor mailExecutor) {
        this.mailService = mailService;
        this.scanService = scanService;
        this.htmlExecutor = (ThreadPoolTaskExecutor) htmlExecutor;
        this.mailExecutor = (ThreadPoolTaskExecutor) mailExecutor;
    }

    @GetMapping("/list")
    public List<String> getList() {
        return scanService.getRegister().entrySet().stream().
                map(e -> "uri=" + e.getKey() + ", state=" + e.getValue().getState() +
                ", attempts=" + e.getValue().getAttemps() +
                ", sended=" + e.getValue().getSended() +
                ", visits=" + e.getValue().getVisits()).collect(Collectors.toList());
    }

    @GetMapping("/tasks")
    public String getTasks() {
        val sb = new StringBuilder();
        sb.append("Html tasks:\n");
        sb.append("active count = " + htmlExecutor.getActiveCount());
        sb.append("\n");
        sb.append("pool size = " + htmlExecutor.getPoolSize());
        sb.append("\n");
        sb.append("Mail executor:\n");
        sb.append("active count = " + mailExecutor.getActiveCount());
        sb.append("\n");
        sb.append("pool size = " + mailExecutor.getPoolSize());
        sb.append("\n");
        return sb.toString();
    }

    @PostMapping("/mailcheck")
    public void mailCheck(@RequestBody MailItem item) {
        mailService.sendMessage(item.getSubject(), item.getMessage());
    }
}
