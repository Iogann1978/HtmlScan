package ru.home.htmlscan.controller;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import ru.home.htmlscan.config.HtmlProperties;
import ru.home.htmlscan.config.UserProperties;
import ru.home.htmlscan.model.MailItem;
import ru.home.htmlscan.model.SiteState;
import ru.home.htmlscan.service.HtmlService;
import ru.home.htmlscan.service.MailService;
import ru.home.htmlscan.service.ScanService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("htmlscan")
@Slf4j
public class HtmlController {
    private HtmlProperties htmlProperties;
    private UserProperties userProperties;
    private HtmlService htmlService;
    private MailService mailService;
    private ScanService scanService;
    private ThreadPoolTaskExecutor htmlExecutor, mailExecutor;

    @Autowired
    public HtmlController(HtmlProperties htmlProperties, UserProperties userProperties,
                          HtmlService htmlService, MailService mailService, ScanService scanService,
                          @Qualifier("htmlExecutor") TaskExecutor htmlExecutor,
                          @Qualifier("mailExecutor") TaskExecutor mailExecutor) {
        this.htmlProperties = htmlProperties;
        this.userProperties = userProperties;
        this.htmlService = htmlService;
        this.mailService = mailService;
        this.scanService = scanService;
        this.htmlExecutor = (ThreadPoolTaskExecutor) htmlExecutor;
        this.mailExecutor = (ThreadPoolTaskExecutor) mailExecutor;
    }

    @GetMapping("/list")
    public List<String> getList() {
        return scanService.getRegister().entrySet().stream().
                map(e -> "uri=" + e.getKey() + ", state=" + e.getValue().getState() +
                ", attempts=" + e.getValue().getAttempts() +
                ", sended=" + e.getValue().getSent() +
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
        userProperties.getUsers().stream().forEach(user ->
                mailService.sendMessage(item.getSubject(), item.getMessage(), user.getEMAIL()));
    }

    @GetMapping("/events")
    public Map<String, Map.Entry<String, SiteState>> getEvents() {
        try {
            val html = htmlService.getHtml(htmlProperties.getUrilist()).get().get();
            return htmlService.getEvents(html).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping("/send_data")
    public String sendData(@RequestParam Map<String, String> params, @RequestBody String body) {
        val sb = new StringBuilder();
        params.entrySet().stream().forEach(param -> sb.append(param.getKey() + "=" + param.getValue() + ";"));
        log.info("params: {}", sb.toString());
        log.info("BODY: {}", body);
        sb.append("body=" + body);
        return sb.toString();
    }

    @GetMapping("/send_data")
    public String sendData(@RequestParam Map<String, String> params) {
        val sb = new StringBuilder();
        params.entrySet().stream().forEach(param -> sb.append(param.getKey() + "=" + param.getValue() + ";"));
        log.info("params: {}", sb.toString());
        return sb.toString();
    }
}
