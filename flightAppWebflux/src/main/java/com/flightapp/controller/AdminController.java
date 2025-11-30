package com.flightapp.controller;

import com.flightapp.entity.NotificationLog;
import com.flightapp.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Flux;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final NotificationLogRepository logRepository;

    @GetMapping("/admin/notifications")
    public String notificationsPage(Model model) {
        // Thymeleaf will call AJAX to fetch logs; to keep it reactive, return page only
        return "admin-notifications";
    }
}
