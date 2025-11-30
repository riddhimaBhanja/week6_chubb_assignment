package com.flightapp.service.impl;

import com.flightapp.service.PdfService;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;

@Service
@Slf4j
public class PdfServiceImpl implements PdfService {

    @Override
    public Mono<byte[]> generateTicketPdf(String pnr, String passengerName, String flight, String seat) {
        return Mono.fromCallable(() -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            document.add(new Paragraph("Flight Ticket"));
            document.add(new Paragraph("PNR: " + pnr));
            document.add(new Paragraph("Passenger: " + passengerName));
            document.add(new Paragraph("Flight: " + flight));
            document.add(new Paragraph("Seat: " + seat));

            document.close();
            return baos.toByteArray();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
