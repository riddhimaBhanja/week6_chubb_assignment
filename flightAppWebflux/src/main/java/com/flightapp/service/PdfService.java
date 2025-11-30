package com.flightapp.service;

import reactor.core.publisher.Mono;

public interface PdfService {
    /**
     * Generate a PDF binary (byte[]) representing a ticket with given details.
     * Returns Mono<byte[]> so it can be used reactively.
     */
    Mono<byte[]> generateTicketPdf(String pnr, String passengerName, String flight, String seat);
}
