package com.example.spotifyproject.repository;

import com.example.spotifyproject.entity.Invoice;
import com.example.spotifyproject.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    @Query(value = "select * from payment p where p.invoice_fk=:invoiceId",nativeQuery = true)
    List<Payment> getPaymentsByInvoice_fk(String invoiceId);
}
