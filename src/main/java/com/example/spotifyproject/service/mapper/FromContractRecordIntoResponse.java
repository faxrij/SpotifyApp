package com.example.spotifyproject.service.mapper;

import com.example.spotifyproject.entity.ContractRecord;
import com.example.spotifyproject.model.response.ContractRecordResponse;
import com.example.spotifyproject.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Period;

@Service
@RequiredArgsConstructor
public class FromContractRecordIntoResponse {
    private final FromUserToUserResponse fromUserToUserResponse;
    public ContractRecordResponse setterFromContractToContractResponse(ContractRecord contractRecord) {

        ContractRecordResponse contractRecordResponse = new ContractRecordResponse();
        contractRecordResponse.setName(contractRecord.getName());
        contractRecordResponse.setActive(contractRecord.isActive());
        contractRecordResponse.setDuration(contractRecord.getDuration());
        contractRecordResponse.setUser(fromUserToUserResponse.fromUserToUserResponse(contractRecord.getUser_fk()));
        contractRecordResponse.setMonthlyFee(contractRecord.getMonthlyFee());

        contractRecordResponse.setRemainingDuration(String.valueOf(Period.between(DateUtil.now().toLocalDate(),
                contractRecord.getCreatedDate().plusMonths(contractRecord.getDuration()).toLocalDate()).toTotalMonths()));

        contractRecordResponse.setId(contractRecord.getId());
        contractRecordResponse.setCreatedDate(contractRecord.getCreatedDate());
        contractRecordResponse.setModifiedDate(contractRecord.getModifiedDate());

        return contractRecordResponse;
    }

    }
