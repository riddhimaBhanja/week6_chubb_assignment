package com.flightapp.service.impl;

import com.flightapp.dto.AdvancedNotificationRequest;
import com.flightapp.entity.NotificationLog;
import com.flightapp.entity.Passenger;
import com.flightapp.notification.NotificationType;
import com.flightapp.repository.NotificationLogRepository;
import com.flightapp.repository.PassengerRepository;
import com.flightapp.service.AdvancedNotificationService;
import com.flightapp.service.PdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import jakarta.activation.DataSource;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvancedNotificationServiceImpl implements AdvancedNotificationService {

    private final PassengerRepository passengerRepository;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final PdfService pdfService;
    private final NotificationLogRepository logRepository;

    @Override
    public Mono<String> notify(AdvancedNotificationRequest request) {

        if (request == null || request.getPassengerId() == null) {
            return Mono.just("FAILURE: invalid request");
        }

        return passengerRepository.findById(request.getPassengerId())
                .switchIfEmpty(Mono.error(new RuntimeException("Passenger not found")))
                .flatMap(passenger -> prepareAndSend(passenger, request))
                .onErrorResume(err -> {
                    log.error("notification error", err);
                    return Mono.just("FAILURE: " + err.getMessage());
                });
    }

    private Mono<String> prepareAndSend(Passenger passenger, AdvancedNotificationRequest req) {
        String templateName = templateNameFor(req.getType());

        Context ctx = new Context();
        if (req.getTemplateVars() != null) {
            req.getTemplateVars().forEach(ctx::setVariable);
        }
        ctx.setVariable("name", passenger.getName());

        String html = templateEngine.process(templateName, ctx);

        return Mono.fromCallable(() -> {

            MimeMessage mime = mailSender.createMimeMessage();
            boolean attachPdf = req.isAttachPdf();
            MimeMessageHelper helper = new MimeMessageHelper(mime, attachPdf);

            helper.setTo(passenger.getEmail());
            helper.setSubject(req.getSubject() != null ? req.getSubject() : defaultSubjectFor(req.getType()));
            helper.setText(html, true);

            if (attachPdf) {
                Map<String, Object> vars = req.getTemplateVars();

                String pnr = vars != null && vars.get("pnr") != null ? vars.get("pnr").toString() : "PNR-UNKNOWN";
                String flight = vars != null && vars.get("flight") != null ? vars.get("flight").toString() : "N/A";
                String seat = vars != null && vars.get("seat") != null ? vars.get("seat").toString() : "N/A";

                byte[] pdfBytes = pdfService.generateTicketPdf(
                                pnr,
                                passenger.getName(),
                                flight,
                                seat
                        )
                        .publishOn(Schedulers.boundedElastic())
                        .block();

                if (pdfBytes != null) {
                    DataSource ds = new ByteArrayDataSource(pdfBytes, "application/pdf");
                    helper.addAttachment("ticket_" + pnr + ".pdf", ds);
                }
            }

            mailSender.send(mime);
            return true;

        }).subscribeOn(Schedulers.boundedElastic())
          .flatMap(sent -> saveLog(passenger, req, "SUCCESS", "Email sent"))
          .thenReturn("SUCCESS")
          .onErrorResume(ex -> saveLog(passenger, req, "FAILURE", ex.getMessage())
                  .thenReturn("FAILURE: " + ex.getMessage()));
    }

    private Mono<NotificationLog> buildAndSaveLog(Passenger passenger,
                                                  AdvancedNotificationRequest req,
                                                  String status,
                                                  String details) {

        NotificationLog log = new NotificationLog();
        log.setPassengerId(passenger.getId());
        log.setEmail(passenger.getEmail());
        log.setSubject(req.getSubject());
        log.setType(req.getType() != null ? req.getType().name() : NotificationType.CUSTOM.name());
        log.setStatus(status);
        log.setDetails(details);
        log.setCreatedAt(Instant.now());

        return logRepository.save(log);
    }

    private Mono<String> saveLog(Passenger passenger,
                                 AdvancedNotificationRequest req,
                                 String status,
                                 String details) {

        return buildAndSaveLog(passenger, req, status, details)
                .map(x -> status.equals("SUCCESS") ? "SUCCESS" : ("FAILURE: " + details));
    }

    private String templateNameFor(NotificationType type) {
        if (type == null) return "booking-confirmation";
        return switch (type) {
            case BOOKING_CONFIRMATION -> "booking-confirmation";
            case CANCELLATION -> "cancellation";
            case DELAY -> "delay";
            case PAYMENT_RECEIPT -> "payment-receipt";
            case OTP -> "otp";
            case TICKET -> "ticket-email";
            default -> "booking-confirmation";
        };
    }

    private String defaultSubjectFor(NotificationType type) {
        if (type == null) return "Notification from FlightApp";
        return switch (type) {
            case BOOKING_CONFIRMATION -> "Booking Confirmed";
            case CANCELLATION -> "Booking Cancelled";
            case DELAY -> "Flight Delay Alert";
            case PAYMENT_RECEIPT -> "Payment Receipt";
            case OTP -> "Your OTP";
            case TICKET -> "Your E-ticket";
            default -> "Notification from FlightApp";
        };
    }
}
