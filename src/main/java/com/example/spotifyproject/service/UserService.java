package com.example.spotifyproject.service;

import com.example.spotifyproject.entity.*;
import com.example.spotifyproject.exception.BusinessException;
import com.example.spotifyproject.exception.ErrorCode;
import com.example.spotifyproject.model.response.InvoiceResponse;
import com.example.spotifyproject.model.response.UserResponse;
import com.example.spotifyproject.repository.*;
import com.example.spotifyproject.service.mapper.FromInvoiceToInvoiceResponse;
import com.example.spotifyproject.service.mapper.FromUserToUserResponse;
import com.example.spotifyproject.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final InvoiceRepository invoiceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final FromInvoiceToInvoiceResponse fromInvoiceToInvoiceResponse;
    private final FromUserToUserResponse fromUserToUserResponse;

    public Page<UserResponse> getUsers(Pageable pageable, String userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.resource_missing, "User does not exist")
        );

        if (!user.getRole().equals(Role.ADMIN)) {
            throw new BusinessException(ErrorCode.unauthorized, "User is not authorized");
        }

        Page<User> userList = userRepository.findAll(pageable);
        return userList.map(fromUserToUserResponse::fromUserToUserResponse);
    }


    public UserResponse getUserById(String id, String userId) {

        checker(id, userId);

        User foundUser = userRepository.findById(id).orElseThrow(
                () -> new BusinessException(ErrorCode.resource_missing, "User does not exist")
        );

        return fromUserToUserResponse.fromUserToUserResponse(foundUser);
    }

    public Page<InvoiceResponse> getInvoicesByUserId(Pageable pageable, String id, String userId) {

        checker(id, userId);

        userRepository.findById(id).orElseThrow(
                () -> new BusinessException(ErrorCode.resource_missing, "User does not exist")
        );

        Page<Invoice> invoices = invoiceRepository.findAllInvoicesByUserId(pageable, id);

        return invoices.map(fromInvoiceToInvoiceResponse::setterFromInvoiceToInvoiceResponse);

    }

    private void checker(String id, String userId) {
        if (id.equals(userId)) {
            throw new BusinessException(ErrorCode.forbidden, "You cannot look at details of yourself");
        }
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.resource_missing, "User does not exist")
        );

        if (!user.getRole().equals(Role.ADMIN)) {
            throw new BusinessException(ErrorCode.unauthorized, "User is not authorized");
        }
    }
    @Scheduled(cron = "0 0 0 16 * ?")  // midnight at 16th day of every month
    public void checkInvoicesAtTheEndOf15thDayOfEveryMonth() {
       List<Invoice> invoices = invoiceRepository.findAllUnpaidInvoicesThatWereCreatedInLessThan16Days();
       List<ContractRecord> contractRecords = new ArrayList<>();

        for (Invoice temp: invoices) {
            if (!contractRepository.findById(temp.getContract_fk().getId()).isPresent()) {
                throw new BusinessException(ErrorCode.internal_server_error, "Error");
            }
            contractRecords.add(contractRepository.findById(temp.getContract_fk().getId()).get());
        }

        for (ContractRecord temp: contractRecords) {
            User user = temp.getUser_fk();
            user.setRole(Role.INACTIVE_MEMBER);
            userRepository.save(user);
        }

    }

    public void subscribe(String userId, String subId, String currentUserId) {

        if (!userId.equals(currentUserId)) {
            throw new BusinessException(ErrorCode.forbidden, "Not allowed");
        }

        User user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.resource_missing, "Account does not exist")
        );

        if (!user.isVerified()) {
            throw new BusinessException(ErrorCode.account_not_verified, "Account is not verified");
        }

        Subscription subscription = subscriptionRepository.findById(subId).orElseThrow(
                () -> new BusinessException(ErrorCode.resource_missing, "Subscription with provided Id does not exist")
        );

        ContractRecord currentContractRecord = contractRepository.findContractRecordByUserIdAndIsActive(userId);

        if (currentContractRecord!=null) {
            if (currentContractRecord.getDuration() >= subscription.getDuration()) {
                throw new BusinessException(ErrorCode.forbidden, "You cannot subscribe to a deal that is less or equal in duration");
            } else {
                currentContractRecord.setActive(false);
            }
        }

        ContractRecord contractRecord = new ContractRecord();
        contractRecord.setName(subscription.getName());
        contractRecord.setUser_fk(user);
        contractRecord.setActive(true);
        contractRecord.setDuration(subscription.getDuration());
        contractRecord.setMonthlyFee(subscription.getMonthlyFee());
        contractRecord.setCreatedDate(DateUtil.now());
        contractRecord.setModifiedDate((DateUtil.now()));

        contractRepository.save(contractRecord);

        user.setRole(Role.MEMBER);
        userRepository.save(user);
    }
}