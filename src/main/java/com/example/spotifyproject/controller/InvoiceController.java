package com.example.spotifyproject.controller;

import com.example.spotifyproject.model.request.PayInvoiceRequest;
import com.example.spotifyproject.model.response.InvoiceResponse;
import com.example.spotifyproject.service.AuthenticationService;
import com.example.spotifyproject.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/invoice")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;
    private final AuthenticationService authenticationService;

    @GetMapping
    public Page<InvoiceResponse> getInvoices(Pageable pageable) {
        return invoiceService.getInvoices(pageable, authenticationService.getAuthenticatedUserId());
    }

    @GetMapping("/{id}")
    public InvoiceResponse getInvoiceById(@PathVariable String id) {
        return invoiceService.getInvoiceById(id, authenticationService.getAuthenticatedUserId());
    }

    @PostMapping("/{id}/pay")
    public void payInvoice(@PathVariable String id,
                           @Valid @RequestBody PayInvoiceRequest payInvoiceRequest) {
        invoiceService.payInvoice(id, payInvoiceRequest, authenticationService.getAuthenticatedUserId());
    }
}
