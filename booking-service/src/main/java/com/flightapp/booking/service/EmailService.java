package com.flightapp.booking.service;

import com.flightapp.booking.event.BookingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @RabbitListener(queues = "${rabbitmq.queue}")
    public void handleBookingEvent(BookingEvent event) {
        log.info("Received booking event: {} for PNR: {}", event.getEventType(), event.getPnr());
        sendEmail(event);
    }

    private void sendEmail(BookingEvent event) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(event.getUserEmail());
            message.setSubject(getEmailSubject(event.getEventType()));
            message.setText(buildEmailBody(event));
            message.setFrom("riddhimabhanja2003@gmail.com");

            mailSender.send(message);
            log.info("Email sent successfully to: {}", event.getUserEmail());
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
        }
    }

    private String getEmailSubject(String eventType) {
        if ("BOOKING_CONFIRMED".equals(eventType)) {
            return "Flight Booking Confirmed";
        } else if ("BOOKING_CANCELLED".equals(eventType)) {
            return "Flight Booking Cancelled";
        }
        return "Flight Booking Update";
    }

    private String buildEmailBody(BookingEvent event) {
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(event.getUserName()).append(",\n\n");

        if ("BOOKING_CONFIRMED".equals(event.getEventType())) {
            body.append("Your flight booking has been confirmed!\n\n");
        } else if ("BOOKING_CANCELLED".equals(event.getEventType())) {
            body.append("Your flight booking has been cancelled.\n\n");
        }

        body.append("Booking Details:\n");
        body.append("PNR: ").append(event.getPnr()).append("\n");
        body.append("Flight: ").append(event.getFlightNumber()).append("\n");
        body.append("Route: ").append(event.getFromPlace()).append(" to ").append(event.getToPlace()).append("\n");
        body.append("Departure: ").append(event.getDepartureDateTime()).append("\n");
        body.append("Total Amount: $").append(event.getTotalAmount()).append("\n\n");
        body.append("Thank you for choosing our service!\n\n");
        body.append("Best regards,\nFlight Booking Team");

        return body.toString();
    }
}
