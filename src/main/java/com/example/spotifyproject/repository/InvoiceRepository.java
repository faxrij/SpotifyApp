package com.example.spotifyproject.repository;

import com.example.spotifyproject.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface InvoiceRepository extends PagingAndSortingRepository<Invoice, String> {

    @Query(value = "select * " +
            "from invoice i " +
            "WHERE i.contract_fk in"+
            "(select id from contract_record c where c.user_fk=:userId)",
            nativeQuery = true)
    Page<Invoice> findAllInvoicesByUserId(Pageable pageable, String userId);

    @Query(value = "select * " +
            "from invoice i " +
            "WHERE is_paid=false and i.contract_fk in"+
            "(select id from contract_record c where c.user_fk=:userId)",
            nativeQuery = true)
    List<Invoice> findAllUnpaidInvoices(String userId);

    @Query(value = "select * " +
            "from invoice i " +
            "WHERE is_paid=false and i.created_date  <= now() and i.created_date>= NOW() - interval '16 DAY'",
            nativeQuery = true)
    List<Invoice> findAllUnpaidInvoicesThatWereCreatedInLessThan16Days();
}
