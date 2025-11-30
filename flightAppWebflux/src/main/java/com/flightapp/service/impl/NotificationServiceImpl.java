package com.flightapp.service.impl;

import com.flightapp.dto.NotificationRequest;
import com.flightapp.entity.Passenger;
import com.flightapp.repository.PassengerRepository;
import com.flightapp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final PassengerRepository passengerRepository;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    public Mono<String> sendNotification(NotificationRequest request) {
        return passengerRepository.findById(request.getPassengerId())
                .switchIfEmpty(Mono.error(new RuntimeException("Passenger not found")))
                .flatMap(passenger -> sendEmail(passenger, request))
                .map(success -> "Email sent successfully to passenger");
    }

    private Mono<Boolean> sendEmail(Passenger passenger, NotificationRequest req) {

        return Mono.fromCallable(() -> {

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(passenger.getEmail());
            helper.setSubject(req.getSubject());

            // Build template
            Context context = new Context();
            context.setVariable("name", passenger.getName());
            context.setVariable("message", req.getMessage());

            String htmlContent = templateEngine.process("notification", context);

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);

            return true;
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
