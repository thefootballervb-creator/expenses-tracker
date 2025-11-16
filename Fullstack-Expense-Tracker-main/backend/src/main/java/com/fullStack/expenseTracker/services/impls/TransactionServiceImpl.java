package com.fullStack.expenseTracker.services.impls;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fullStack.expenseTracker.dto.reponses.ApiResponseDto;
import com.fullStack.expenseTracker.dto.reponses.PageResponseDto;
import com.fullStack.expenseTracker.dto.reponses.TransactionResponseDto;
import com.fullStack.expenseTracker.dto.requests.TransactionRequestDto;
import com.fullStack.expenseTracker.enums.ApiResponseStatus;
import com.fullStack.expenseTracker.enums.ETransactionFrequency;
import com.fullStack.expenseTracker.exceptions.CategoryNotFoundException;
import com.fullStack.expenseTracker.exceptions.TransactionNotFoundException;
import com.fullStack.expenseTracker.exceptions.TransactionServiceLogicException;
import com.fullStack.expenseTracker.exceptions.UserNotFoundException;
import com.fullStack.expenseTracker.models.SavedTransaction;
import com.fullStack.expenseTracker.models.Transaction;
import com.fullStack.expenseTracker.models.User;
import com.fullStack.expenseTracker.repository.SavedTransactionRepository;
import com.fullStack.expenseTracker.repository.TransactionRepository;
import com.fullStack.expenseTracker.services.CategoryService;
import com.fullStack.expenseTracker.services.TransactionService;
import com.fullStack.expenseTracker.services.UserService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    SavedTransactionRepository savedTransactionRepository;

    @Autowired
    UserService userService;

    @Autowired
    CategoryService categoryService;

    @Override
    public ResponseEntity<ApiResponseDto<?>> addTransaction(TransactionRequestDto transactionRequestDto)
            throws UserNotFoundException, CategoryNotFoundException, TransactionServiceLogicException {
        Transaction transaction = Objects.requireNonNull(
                TransactionRequestDtoToTransaction(transactionRequestDto),
                "Transaction mapping returned null"
        );
        try {
            // Save main transaction
            transactionRepository.save(transaction);

            // Automatically create a corresponding saved transaction entry
            User user = userService.findByEmail(transactionRequestDto.getUserEmail());
            int categoryId = transactionRequestDto.getCategoryId();

            SavedTransaction savedTransaction = SavedTransaction.builder()
                    .userId(user.getId())
                    .transactionTypeId(
                            categoryService.getCategoryById(categoryId)
                                    .getTransactionType()
                                    .getTransactionTypeId()
                    )
                    .categoryId(categoryId)
                    .amount(transactionRequestDto.getAmount())
                    .description(transactionRequestDto.getDescription())
                    // Treat automatically created entries as one-time with the same date
                    .frequency(ETransactionFrequency.ONE_TIME)
                    .upcomingDate(transactionRequestDto.getDate())
                    .build();

            savedTransactionRepository.save(
                    Objects.requireNonNull(savedTransaction, "SavedTransaction must not be null")
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ApiResponseDto<>(
                            ApiResponseStatus.SUCCESS,
                            HttpStatus.CREATED,
                            "Transaction has been successfully recorded!"
                    )
            );

        } catch (UserNotFoundException | CategoryNotFoundException e) {
            // Let declared checked exceptions propagate as-is
            throw e;
        } catch (RuntimeException e) {
            log.error("Error happen when adding new transaction: " + e.getMessage(), e);
            log.error("TransactionRequestDto: userEmail={}, categoryId={}, description={}, amount={}, date={}", 
                    transactionRequestDto.getUserEmail(), 
                    transactionRequestDto.getCategoryId(),
                    transactionRequestDto.getDescription(),
                    transactionRequestDto.getAmount(),
                    transactionRequestDto.getDate());
            throw new TransactionServiceLogicException("Failed to record your new transaction: " + e.getMessage());
        }

    }

    @Override
    public ResponseEntity<ApiResponseDto<?>> getTransactionsByUser(String email,
                                                                   int pageNumber, int pageSize,
                                                                   String searchKey, String sortField,
                                                                   String sortDirec, String transactionType)
            throws TransactionServiceLogicException {

        Sort.Direction direction = Sort.Direction.ASC;
        if (sortDirec.equalsIgnoreCase("DESC")) {
            direction = Sort.Direction.DESC;
        }

        Pageable pageable =  PageRequest.of(pageNumber, pageSize).withSort(direction, sortField);

        Page<Transaction> transactions = transactionRepository.findByUser(email,
                pageable, searchKey, transactionType);

        try {
            if (transactions.getTotalElements() == 0) {
                return ResponseEntity.status(HttpStatus.OK).body(
                        new ApiResponseDto<>(
                                ApiResponseStatus.SUCCESS,
                                HttpStatus.OK,
                                new PageResponseDto<>(
                                        new ArrayList<>(),
                                        0,
                                        0L
                                )
                        )
                );
            }

            List<TransactionResponseDto> transactionResponseDtoList = new ArrayList<>();

            for (Transaction transaction: transactions) {
                transactionResponseDtoList.add(transactionToTransactionResponseDto(transaction));
            }

            return ResponseEntity.status(HttpStatus.OK).body(
                    new ApiResponseDto<>(
                            ApiResponseStatus.SUCCESS,
                            HttpStatus.OK,
                            new PageResponseDto<>(
                                    groupTransactionsByDate(transactionResponseDtoList),
                                    transactions.getTotalPages(),
                                    transactions.getTotalElements()
                            )
                    )
            );
        } catch (Exception e) {
            log.error("Error happen when retrieving transactions of a user: " + e.getMessage());
            throw new TransactionServiceLogicException("Failed to fetch your transactions! Try again later");
        }

    }

    @Override
    public ResponseEntity<ApiResponseDto<?>> getTransactionById(Long transactionId)
            throws TransactionNotFoundException {
        Long id = Objects.requireNonNull(transactionId, "transactionId must not be null");
        Transaction transaction = transactionRepository.findById(id).orElseThrow(
                () -> new TransactionNotFoundException("Transaction not found with id : " + id)
        );

        return ResponseEntity.ok(
                new ApiResponseDto<>(
                        ApiResponseStatus.SUCCESS,
                        HttpStatus.OK,
                        transactionToTransactionResponseDto(transaction)
                )
        );
    }

    @Override
    public ResponseEntity<ApiResponseDto<?>> updateTransaction(Long transactionId, TransactionRequestDto transactionRequestDto)
            throws TransactionNotFoundException, UserNotFoundException, CategoryNotFoundException, TransactionServiceLogicException {

        Long id = Objects.requireNonNull(transactionId, "transactionId must not be null");
        Transaction transaction = transactionRepository.findById(id).orElseThrow(
                () -> new TransactionNotFoundException("Transaction not found with id : " + id)
        );

        transaction.setAmount(transactionRequestDto.getAmount());
        transaction.setDate(transactionRequestDto.getDate());
        transaction.setUser(userService.findByEmail(transactionRequestDto.getUserEmail()));
        transaction.setCategory(categoryService.getCategoryById(transactionRequestDto.getCategoryId()));
        transaction.setDescription(transactionRequestDto.getDescription());

        try {
            transactionRepository.save(transaction);
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ApiResponseDto<>(
                            ApiResponseStatus.SUCCESS,
                            HttpStatus.OK,
                            "Transaction has been successfully updated!"
                    )
            );
        }catch(Exception e) {
            log.error("Error happen when retrieving transactions of a user: " + e.getMessage());
            throw new TransactionServiceLogicException("Failed to update your transactions! Try again later");
        }

    }

    @Override
    public ResponseEntity<ApiResponseDto<?>> deleteTransaction(Long transactionId) throws TransactionNotFoundException, TransactionServiceLogicException {

        Long id = Objects.requireNonNull(transactionId, "transactionId must not be null");
        if (transactionRepository.existsById(id)) {
            try {transactionRepository.deleteById(id);
                return ResponseEntity.status(HttpStatus.OK).body(
                        new ApiResponseDto<>(
                                ApiResponseStatus.SUCCESS,
                                HttpStatus.OK,
                                "Transaction has been successfully deleted!"
                        )
                );
            }catch(Exception e) {
                log.error("Error happen when retrieving transactions of a user: " + e.getMessage());
                throw new TransactionServiceLogicException("Failed to delete your transactions! Try again later");
            }
        }else {
            throw new TransactionNotFoundException("Transaction not found with id : " + transactionId);
        }

    }

    @Override
    public ResponseEntity<ApiResponseDto<?>> getAllTransactions(int pageNumber, int pageSize, String searchKey) throws TransactionServiceLogicException {
        Pageable pageable =  PageRequest.of(pageNumber, pageSize).withSort(Sort.Direction.DESC, "transaction_id");

        Page<Transaction> transactions = transactionRepository.findAll(pageable, searchKey);

        try {
            if (transactions.getTotalElements() == 0) {
                return ResponseEntity.status(HttpStatus.OK).body(
                        new ApiResponseDto<>(
                                ApiResponseStatus.SUCCESS,
                                HttpStatus.OK,
                                new PageResponseDto<>(
                                        new ArrayList<>(),
                                        0,
                                        0L
                                )
                        )
                );
            }
            List<TransactionResponseDto> transactionResponseDtoList = new ArrayList<>();

            for (Transaction transaction: transactions) {
                transactionResponseDtoList.add(transactionToTransactionResponseDto(transaction));
            }

            return ResponseEntity.status(HttpStatus.OK).body(
                    new ApiResponseDto<>(
                            ApiResponseStatus.SUCCESS,
                            HttpStatus.OK,
                            new PageResponseDto<>(
                                    transactionResponseDtoList,
                                    transactions.getTotalPages(),
                                    transactions.getTotalElements()
                            )
                    )
            );
        }catch (Exception e) {
            log.error("Failed to fetch All transactions: " + e.getMessage());
            throw new TransactionServiceLogicException("Failed to fetch All transactions: Try again later!");
        }
    }

    private Transaction TransactionRequestDtoToTransaction(TransactionRequestDto transactionRequestDto) throws UserNotFoundException, CategoryNotFoundException {
        return new Transaction(
                userService.findByEmail(transactionRequestDto.getUserEmail()),
                categoryService.getCategoryById(transactionRequestDto.getCategoryId()),
                transactionRequestDto.getDescription(),
                transactionRequestDto.getAmount(),
                transactionRequestDto.getDate()
        );
    }

    private TransactionResponseDto transactionToTransactionResponseDto(Transaction transaction) {
        return new TransactionResponseDto(
                transaction.getTransactionId(),
                transaction.getCategory().getCategoryId(),
                transaction.getCategory().getCategoryName(),
                transaction.getCategory().getTransactionType().getTransactionTypeId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getUser().getEmail()
        );
    }

    private Map<String, List<TransactionResponseDto>> groupTransactionsByDate(List<TransactionResponseDto> transactionResponseDtoList) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        return transactionResponseDtoList.stream().collect(Collectors.groupingBy(t -> {

            if (t.getDate().equals(today)) {
                return "Today";
            }else if (t.getDate().equals(yesterday)) {
                return "Yesterday";
            }else {
                return t.getDate().toString();
            }
        }))
                .entrySet().stream()
                .sorted((entry1, entry2 ) -> {
                    if (entry1.getKey().equals("Today")) return -1;
                    else if (entry2.getKey().equals("Today")) return 1;
                    else if (entry1.getKey().equals("Yesterday")) return -1;
                    else if (entry2.getKey().equals("Yesterday")) return 1;
                    else return entry2.getKey().compareTo(entry1.getKey());
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }
}
