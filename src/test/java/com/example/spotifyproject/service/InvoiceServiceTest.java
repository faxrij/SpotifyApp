package com.example.spotifyproject.service;

import com.example.spotifyproject.entity.*;
import com.example.spotifyproject.exception.BusinessException;
import com.example.spotifyproject.model.request.PayInvoiceRequest;
import com.example.spotifyproject.model.response.InvoiceResponse;
import com.example.spotifyproject.repository.InvoiceRepository;
import com.example.spotifyproject.repository.PaymentRepository;
import com.example.spotifyproject.repository.UserRepository;
import com.example.spotifyproject.service.mapper.FromInvoiceToInvoiceResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InvoiceServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private FromInvoiceToInvoiceResponse fromInvoiceToInvoiceResponse;

    @InjectMocks
    private InvoiceService invoiceService;

    @Mock
    private PaymentRepository paymentRepository;

    @Test
    public void testGetAllInvoicesAsAdmin() {
        // given
        User adminUser = new User();
        adminUser.setRole(Role.ADMIN);
        when(userRepository.findById("123")).thenReturn(Optional.of(adminUser));

        Pageable pageable = PageRequest.of(0, 10);
        List<Invoice> invoiceList = new ArrayList<>();
        invoiceList.add(new Invoice());
        invoiceList.add(new Invoice());
        Page<Invoice> pageOfInvoices = new PageImpl<>(invoiceList, pageable, 2);
        when(invoiceRepository.findAll(pageable)).thenReturn(pageOfInvoices);

        InvoiceResponse invoiceResponse1 = new InvoiceResponse();
        InvoiceResponse invoiceResponse2 = new InvoiceResponse();
        when(fromInvoiceToInvoiceResponse.setterFromInvoiceToInvoiceResponse(Mockito.any())).thenReturn(invoiceResponse1, invoiceResponse2);

        // when
        Page<InvoiceResponse> result = invoiceService.getInvoices(pageable, "123");

        // then
        Assertions.assertEquals(2, result.getContent().size());
        Mockito.verify(userRepository).findById("123");
        Mockito.verify(invoiceRepository).findAll(pageable);
        Mockito.verify(fromInvoiceToInvoiceResponse, times(2)).setterFromInvoiceToInvoiceResponse(Mockito.any());
    }

    @Test
    public void testGetAllInvoicesAsNonAdmin() {
        // given
        User nonAdminUser = new User();
        nonAdminUser.setRole(Role.MEMBER);
        when(userRepository.findById("nonAdminUserId")).thenReturn(Optional.of(nonAdminUser));

        assertThrows(BusinessException.class, () ->  invoiceService.getInvoices(PageRequest.of(0, 10), "nonAdminUserId"));
    }


    @Test
    public void testGetInvoiceById() {
        // given
        User user = new User();
        user.setId("1");
        user.setRole(Role.ADMIN);

        Invoice invoice = new Invoice();
        invoice.setId("1");

        InvoiceResponse response = new InvoiceResponse();
        response.setId(invoice.getId());

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(fromInvoiceToInvoiceResponse.setterFromInvoiceToInvoiceResponse(invoice)).thenReturn(response);
        when(invoiceRepository.findById("1")).thenReturn(Optional.of(invoice));

        InvoiceResponse invoiceResponse = invoiceService.getInvoiceById("1", "1");
        assertNotNull(invoiceResponse);
        assertEquals("1", invoiceResponse.getId());
    }

    @Test
    public void testGetInvoiceByIdWithInvalidUserId() {
        when(userRepository.findById("user1")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->invoiceService.getInvoiceById("invoice1", "user1"));
    }

    @Test
    public void testGetInvoiceByIdWithNonAdminUser() {
        User user = new User();
        user.setId("1");
        user.setRole(Role.MEMBER);

        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        assertThrows(BusinessException.class, () -> invoiceService.getInvoiceById("1", "1"));
    }

    @Test
    public void testGetInvoiceByIdWithInvalidInvoiceId() {
        User user = new User();
        user.setId("user1");
        user.setRole(Role.ADMIN);
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));

        when(invoiceRepository.findById("invoice1")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> invoiceService.getInvoiceById("invoice1", "user1"));
    }

    @Test
    public void testPayInvoice() {
        User user = new User();
        user.setId("user1");

        Invoice invoice = new Invoice();
        invoice.setId("invoice1");
        invoice.setFee(100);
        ContractRecord contract = new ContractRecord();
        contract.setUser_fk(user);
        invoice.setContract_fk(contract);

        PayInvoiceRequest request = new PayInvoiceRequest();
        request.setAmount(50);
        request.setSenderCard("1111222233334444");
        request.setReceiverCard("5555666677778888");

        when(userRepository.findById("user1")).thenReturn(Optional.of(user));

        when(invoiceRepository.findById("invoice1")).thenReturn(Optional.of(invoice));

        invoiceService.payInvoice("invoice1", request, "user1");
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    public void testPayInvoiceWithInvalidUserId() {
        when(userRepository.findById("1")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> invoiceService.payInvoice("1", new PayInvoiceRequest(), "1"));
    }

    @Test
    public void testPayInvoiceWithInvalidInvoiceId() {
        User user = new User();
        user.setId("user1");
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));

        when(invoiceRepository.findById("invoice1")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> invoiceService.payInvoice("invoice1", new PayInvoiceRequest(), "user1"));
    }

    @Test
    public void testPayInvoiceWithUnownedInvoice() {
        User user1 = new User();
        user1.setId("user1");
        User user2 = new User();
        user2.setId("user2");
        Invoice invoice = new Invoice();
        invoice.setId("invoice1");
        invoice.setFee(100);
        ContractRecord contract = new ContractRecord();
        contract.setUser_fk(user2);
        invoice.setContract_fk(contract);

        when(userRepository.findById("user1")).thenReturn(Optional.of(user1));
        when(invoiceRepository.findById("invoice1")).thenReturn(Optional.of(invoice));

        assertThrows(BusinessException.class, () -> invoiceService.payInvoice("invoice1", new PayInvoiceRequest(), "user1"));
    }

    @Test
    void testPayInvoice_ThrowsPaidMoreError() {
        // given
        String invoiceId = "123";
        int invoiceFee = 100;
        int requestedPaymentAmount = 60;
        PayInvoiceRequest request = new PayInvoiceRequest();

        request.setAmount(requestedPaymentAmount);
        request.setSenderCard("13243535352");
        request.setReceiverCard("4635737373");

        Invoice invoice = new Invoice();
        invoice.setFee(invoiceFee);
        invoice.setId(invoiceId);
        User user = new User();
        ContractRecord contract = new ContractRecord();
        contract.setUser_fk(user);
        invoice.setContract_fk(contract);

        Payment payment1 = new Payment();
        payment1.setAmount(30);

        Payment payment2 = new Payment();
        payment2.setAmount(20);

        List<Payment> payments = Arrays.asList(payment1, payment2);

        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(invoiceRepository.findById(anyString())).thenReturn(Optional.of(invoice));
        when(paymentRepository.getPaymentsByInvoice_fk(anyString())).thenReturn(payments);

        // when + then
        BusinessException exception = assertThrows(BusinessException.class, () -> invoiceService.payInvoice(invoiceId, request, "userId"));
        assertEquals("paid_more", exception.getErrorCode());
    }

    @Test
    void testPayInvoice_DoesNotThrowPaidMoreError() {
        // given
        String invoiceId = "123";
        int invoiceFee = 100;
        int requestedPaymentAmount = 40;
        PayInvoiceRequest request = new PayInvoiceRequest();
        request.setAmount(requestedPaymentAmount);
        request.setSenderCard("13243535352");
        request.setReceiverCard("4635737373");

        Invoice invoice = new Invoice();
        invoice.setFee(invoiceFee);
        invoice.setId(invoiceId);
        User user = new User();
        ContractRecord contract = new ContractRecord();
        contract.setUser_fk(user);
        invoice.setContract_fk(contract);

        Payment payment1 = new Payment();
        payment1.setAmount(30);

        Payment payment2 = new Payment();
        payment2.setAmount(20);

        List<Payment> payments = Arrays.asList(payment1, payment2);

        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(invoiceRepository.findById(anyString())).thenReturn(Optional.of(invoice));
        when(paymentRepository.getPaymentsByInvoice_fk(anyString())).thenReturn(payments);

        // when
        invoiceService.payInvoice(invoiceId, request, "userId");

        // then
        verify(paymentRepository).save(any(Payment.class));
    }

}
