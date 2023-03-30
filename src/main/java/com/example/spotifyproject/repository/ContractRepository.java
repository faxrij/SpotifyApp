package com.example.spotifyproject.repository;

import com.example.spotifyproject.entity.ContractRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends PagingAndSortingRepository<ContractRecord, String> {
    @Query(value = "select * " +
            "from contract_record c " +
            "WHERE c.is_active=true "+
            "order by c.id",
            nativeQuery = true)
    List<ContractRecord> findAllActiveContracts();

    @Query(value = "select * from contract_record c where c.user_fk=:userId and c.is_active=true", nativeQuery = true)
    ContractRecord findContractRecordByUserIdAndIsActive(String userId);

    @Query(value = "select * from contract_record c where c.user_fk=:userId", nativeQuery = true)
    List<ContractRecord> findAllContractRecordByUserId(String userId);
}
