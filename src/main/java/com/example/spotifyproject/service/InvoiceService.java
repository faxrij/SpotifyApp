package com.example.spotifyproject.service;

import com.example.spotifyproject.entity.*;
import com.example.spotifyproject.exception.BusinessException;
import com.example.spotifyproject.exception.ErrorCode;
import com.example.spotifyproject.model.request.PayInvoiceRequest;
import com.example.spotifyproject.model.response.InvoiceResponse;
import com.example.spotifyproject.repository.ContractRepository;
import com.example.spotifyproject.repository.InvoiceRepository;
import com.example.spotifyproject.repository.PaymentRepository;
import com.example.spotifyproject.repository.UserRepository;
import com.example.spotifyproject.service.mapper.FromInvoiceToInvoiceResponse;
import com.example.spotifyproject.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final PaymentRepository paymentRepository;
    private final FromInvoiceToInvoiceResponse fromInvoiceToInvoiceResponse;

    public Page<InvoiceResponse> getInvoices(Pageable pageable, String userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.account_missing, "User does not exist")
        );

        if (!user.getRole().equals(Role.ADMIN)){
            throw new BusinessException(ErrorCode.unauthorized, "User is not authorized");
        }

        Page<Invoice> invoices = invoiceRepository.findAll(pageable);

        return invoices.map(fromInvoiceToInvoiceResponse::setterFromInvoiceToInvoiceResponse);
    }

    public InvoiceResponse getInvoiceById(String id, String userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.account_missing, "User does not exist")
        );

        if (!user.getRole().equals(Role.ADMIN)){
            throw new BusinessException(ErrorCode.unauthorized, "User is not authorized");
        }

        Invoice invoice = invoiceRepository.findById(id).orElseThrow(
                () -> new BusinessException(ErrorCode.resource_missing, "Invoice does not exist")
        );
        return fromInvoiceToInvoiceResponse.setterFromInvoiceToInvoiceResponse(invoice);
    }

    public void payInvoice(String invoiceId, PayInvoiceRequest body, String userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.account_missing, "User does not exist")
        );

        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(
                () -> new BusinessException(ErrorCode.resource_missing, "Invoice is not found")
        );

        if (!invoice.getContract_fk().getUser_fk().equals(user)) {
            throw new BusinessException(ErrorCode.forbidden, "You do not own invoice with provided id");
        }

        List<Payment> payments = paymentRepository.getPaymentsByInvoice_fk(invoiceId);

        int alreadyPaidAmount = 0;

        if (!payments.isEmpty()) {
            for (Payment temp:payments) {
                alreadyPaidAmount+=temp.getAmount();
            }
        }

        if ((alreadyPaidAmount + body.getAmount()) > invoice.getFee()) {
            throw new BusinessException(ErrorCode.paid_more, "You are paying more than it should be");
        }

        Payment payment = new Payment();

        payment.setInvoice_fk(invoice);
        payment.setCreatedDate(DateUtil.now());
        payment.setModifiedDate(DateUtil.now());
        payment.setSenderCard(body.getSenderCard());
        payment.setReceiverCard(body.getReceiverCard());
        payment.setAmount(body.getAmount());

        paymentRepository.save(payment);
    }

    @Scheduled(cron = "0 0 0 L * *") // last day of the month at midnight
    public void createInvoicesAtTheEndOfEveryMonth() {
        List<ContractRecord> contractRecordList = contractRepository.findAllActiveContracts();

        removeContractsThatAreExpired(contractRecordList);

        for (ContractRecord temp: contractRecordList) {

            Invoice invoice = new Invoice();
            invoice.setFee(temp.getMonthlyFee());
            invoice.setContract_fk(temp);
            invoice.setCreatedDate(DateUtil.now());
            invoice.setModifiedDate(DateUtil.now());

            invoiceRepository.save(invoice);
        }
    }

    private void removeContractsThatAreExpired(List<ContractRecord> contractRecordList) {
        for (ContractRecord temp: contractRecordList) {
            if (temp.getCreatedDate().plusMonths(temp.getDuration()).isBefore(DateUtil.now())) {
                temp.setActive(false);
                User user = temp.getUser_fk();
                user.setRole(Role.GUEST);
                userRepository.save(user);

                contractRepository.save(temp);
                contractRecordList.remove(temp);
            }
        }
    }
}
