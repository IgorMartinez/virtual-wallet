package br.com.igormartinez.virtualwallet.services;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.igormartinez.virtualwallet.data.dto.PersonalTransactionDTO;
import br.com.igormartinez.virtualwallet.data.dto.TransactionDTO;
import br.com.igormartinez.virtualwallet.data.dto.TransferTransactionDTO;
import br.com.igormartinez.virtualwallet.enums.TransactionType;
import br.com.igormartinez.virtualwallet.exceptions.RequestValidationException;
import br.com.igormartinez.virtualwallet.exceptions.ResourceNotFoundException;
import br.com.igormartinez.virtualwallet.exceptions.UserUnauthorizedException;
import br.com.igormartinez.virtualwallet.models.Transaction;
import br.com.igormartinez.virtualwallet.models.User;
import br.com.igormartinez.virtualwallet.repositories.TransactionRepository;
import br.com.igormartinez.virtualwallet.repositories.UserRepository;
import br.com.igormartinez.virtualwallet.security.SecurityContextManager;

@Service
public class TransactionService {
    private final TransactionRepository repository;
    private final UserRepository userRepository;
    private final SecurityContextManager securityContextManager;

    public TransactionService(TransactionRepository repository, UserRepository userRepository,
            SecurityContextManager securityContextManager) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.securityContextManager = securityContextManager;
    }

    public TransactionDTO findById(Long id) {
        if (id == null || id <= 0)
            throw new RequestValidationException("The transaction-id must be a positive integer value.");

        Transaction transaction = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("The transaction was not found with the given ID."));

        if (!securityContextManager.checkSameUser(transaction.getUser().getId()))
            throw new UserUnauthorizedException();

        return new TransactionDTO(
            transaction.getId(), 
            transaction.getType().name(), 
            transaction.getValue(), 
            transaction.getDatetime());
    }

    public List<TransactionDTO> findAllByUser(Long userId) {
        if (userId == null || userId <= 0)
            throw new RequestValidationException("The user-id must be a positive integer value.");

        if (!securityContextManager.checkSameUser(userId))
            throw new UserUnauthorizedException();

        return repository.findAllByUserId(userId)
            .stream()
            .map(t -> new TransactionDTO(t.getId(), t.getType().name(), t.getValue(), t.getDatetime()))
            .toList();
    }

    @Transactional
    public TransactionDTO createPersonalTransaction(PersonalTransactionDTO transactionDTO, TransactionType type) {
        if (!securityContextManager.checkSameUser(transactionDTO.user()))
            throw new UserUnauthorizedException();
        
        User user = userRepository.findById(transactionDTO.user())
            .orElseThrow(() -> new ResourceNotFoundException("The user was not found with the given ID."));

        switch(type) {
            case DEPOSIT:
                user.setAccountBalance(user.getAccountBalance().add(transactionDTO.value()));
                break;
            case WITHDRAWAL:
                if (user.getAccountBalance().compareTo(transactionDTO.value()) == -1)
                    throw new RequestValidationException("The user does not have enough balance to carry out the transaction.");
                user.setAccountBalance(user.getAccountBalance().subtract(transactionDTO.value()));
                break;
            default:
                throw new IllegalArgumentException("Unsupported transaction type.");
        }

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setType(type);
        transaction.setValue(transactionDTO.value());
        transaction.setDatetime(ZonedDateTime.now());

        userRepository.save(user);
        Transaction persistedTransaction = repository.save(transaction);
        
        return new TransactionDTO(
            persistedTransaction.getId(), 
            persistedTransaction.getType().name(), 
            persistedTransaction.getValue(), 
            persistedTransaction.getDatetime());
    }

    @Transactional
    public TransactionDTO createTranfer(TransferTransactionDTO transactionDTO) {
        if (transactionDTO.payer().equals(transactionDTO.payee()))
            throw new RequestValidationException("The transfer must be between two different users.");

        if (!securityContextManager.checkSameUser(transactionDTO.payer()))
            throw new UserUnauthorizedException();
            
        User payer = userRepository.findById(transactionDTO.payer())
            .orElseThrow(() -> new ResourceNotFoundException("The payer was not found with the given ID."));

        if (payer.getAccountBalance().compareTo(transactionDTO.value()) == -1)
            throw new RequestValidationException("The payer does not have enough balance to carry out the transaction.");
            
        User payee = userRepository.findById(transactionDTO.payee())
            .orElseThrow(() -> new ResourceNotFoundException("The payee was not found with the given ID."));
            
        payer.setAccountBalance(payer.getAccountBalance().subtract(transactionDTO.value()));
        payee.setAccountBalance(payee.getAccountBalance().add(transactionDTO.value()));

        Transaction transaction = new Transaction();
        transaction.setUser(payer);
        transaction.setCounterpartyUser(payee);
        transaction.setType(TransactionType.TRANSFER);
        transaction.setValue(transactionDTO.value());
        transaction.setDatetime(ZonedDateTime.now());

        userRepository.saveAll(List.of(payer, payee));
        Transaction persistedTransaction = repository.save(transaction);

        return new TransactionDTO(
            persistedTransaction.getId(), 
            persistedTransaction.getType().name(), 
            persistedTransaction.getValue(), 
            persistedTransaction.getDatetime());
    }
}
