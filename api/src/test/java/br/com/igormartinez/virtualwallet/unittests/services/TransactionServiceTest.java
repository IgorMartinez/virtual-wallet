package br.com.igormartinez.virtualwallet.unittests.services;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.igormartinez.virtualwallet.data.dto.PersonalTransactionDTO;
import br.com.igormartinez.virtualwallet.data.dto.TransactionDTO;
import br.com.igormartinez.virtualwallet.data.dto.TransferTransactionDTO;
import br.com.igormartinez.virtualwallet.enums.TransactionType;
import br.com.igormartinez.virtualwallet.exceptions.RequestValidationException;
import br.com.igormartinez.virtualwallet.exceptions.ResourceNotFoundException;
import br.com.igormartinez.virtualwallet.exceptions.UserUnauthorizedException;
import br.com.igormartinez.virtualwallet.mocks.TransactionMock;
import br.com.igormartinez.virtualwallet.mocks.UserMock;
import br.com.igormartinez.virtualwallet.models.Transaction;
import br.com.igormartinez.virtualwallet.models.User;
import br.com.igormartinez.virtualwallet.repositories.TransactionRepository;
import br.com.igormartinez.virtualwallet.repositories.UserRepository;
import br.com.igormartinez.virtualwallet.security.SecurityContextManager;
import br.com.igormartinez.virtualwallet.services.TransactionService;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    private TransactionService service;
    private UserMock userMock;
    private TransactionMock transactionMock;

    @Mock
    private TransactionRepository repository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private SecurityContextManager securityContextManager;

    @BeforeEach
    void setup() {
        userMock = new UserMock();
        transactionMock = new TransactionMock();

        service = new TransactionService(
            repository,
            userRepository,
            securityContextManager
        );
    }

    @Test
    void testFindByIdWithIdNull() {
        Exception output = assertThrows(RequestValidationException.class, () -> {
            service.findById(null);
        });
        String expectedMessage = "The transaction-id must be a positive integer value.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testFindByIdWithIdNegative() {
        Exception output = assertThrows(RequestValidationException.class, () -> {
            service.findById(-123L);
        });
        String expectedMessage = "The transaction-id must be a positive integer value.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testFindByIdWithIdZero() {
        Exception output = assertThrows(RequestValidationException.class, () -> {
            service.findById(0L);
        });
        String expectedMessage = "The transaction-id must be a positive integer value.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testFindByIdWithTransactionNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.ofNullable(null));

        Exception output = assertThrows(ResourceNotFoundException.class, () -> {
            service.findById(1L);
        });
        String expectedMessage = "The transaction was not found with the given ID.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testFindByIdWithOtherUser() {
        Transaction mockedTransaction = transactionMock.mockEntity(1);

        when(repository.findById(1L)).thenReturn(Optional.of(mockedTransaction));
        when(securityContextManager.checkSameUser(mockedTransaction.getUser().getId())).thenReturn(Boolean.FALSE);

        Exception output = assertThrows(UserUnauthorizedException.class, () -> {
            service.findById(1L);
        });
        String expectedMessage = "The user is not authorized to access this resource.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testFindByIdWithSameUser() {
        Transaction mockedTransaction = transactionMock.mockEntity(1);

        when(repository.findById(1L)).thenReturn(Optional.of(mockedTransaction));
        when(securityContextManager.checkSameUser(mockedTransaction.getUser().getId())).thenReturn(Boolean.TRUE);

        TransactionDTO output = service.findById(1L);
        assertEquals(1L, output.id());
        assertEquals(TransactionType.WITHDRAWAL.name(), output.type());
        assertEquals(new BigDecimal("1.99"), output.value());
        assertNotNull(output.datetime());
    }

    @Test
    void testFindAllByUserWithIdNull() {
        Exception output = assertThrows(RequestValidationException.class, () -> {
            service.findAllByUser(null);
        });
        String expectedMessage = "The user-id must be a positive integer value.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testFindAllByUserWithIdNegative() {
        Exception output = assertThrows(RequestValidationException.class, () -> {
            service.findAllByUser(-123L);
        });
        String expectedMessage = "The user-id must be a positive integer value.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testFindAllByUserWithIdZero() {
        Exception output = assertThrows(RequestValidationException.class, () -> {
            service.findAllByUser(0L);
        });
        String expectedMessage = "The user-id must be a positive integer value.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testFindAllByUserWithOtherUser() {
        when(securityContextManager.checkSameUser(1L)).thenReturn(Boolean.FALSE);

        Exception output = assertThrows(UserUnauthorizedException.class, () -> {
           service.findAllByUser(1L);
        });
        String expectedMessage = "The user is not authorized to access this resource.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testFindAllByUserWithSameUser() {
        List<Transaction> mockedTransactions = transactionMock.mockEntityList(9);

        when(securityContextManager.checkSameUser(1L)).thenReturn(Boolean.TRUE);
        when(repository.findAllByUserId(1L)).thenReturn(mockedTransactions);

        List<TransactionDTO> output = service.findAllByUser(1L);
        assertEquals(9, output.size());

        TransactionDTO outputPosition0 = output.get(0);
        assertEquals(1L, outputPosition0.id());
        assertEquals(TransactionType.WITHDRAWAL.name(), outputPosition0.type());
        assertEquals(new BigDecimal("1.99"), outputPosition0.value());
        assertNotNull(outputPosition0.datetime());

        TransactionDTO outputPosition4 = output.get(4);
        assertEquals(5L, outputPosition4.id());
        assertEquals(TransactionType.TRANSFER.name(), outputPosition4.type());
        assertEquals(new BigDecimal("5.99"), outputPosition4.value());
        assertNotNull(outputPosition4.datetime());

        TransactionDTO outputPosition8 = output.get(8);
        assertEquals(9L, outputPosition8.id());
        assertEquals(TransactionType.DEPOSIT.name(), outputPosition8.type());
        assertEquals(new BigDecimal("9.99"), outputPosition8.value());
        assertNotNull(outputPosition8.datetime());
    }

    @Test
    void testCreatePersonalTransactionWithOtherUser() {
        PersonalTransactionDTO transaction = new PersonalTransactionDTO(1L, new BigDecimal("0.01"));

        when(securityContextManager.checkSameUser(transaction.user())).thenReturn(Boolean.FALSE);

        Exception output = assertThrows(UserUnauthorizedException.class, () -> {
            service.createPersonalTransaction(transaction, TransactionType.DEPOSIT);
        });
        String expectedMessage = "The user is not authorized to access this resource.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testCreatePersonalTransactionWithNotExistingUser() {
        PersonalTransactionDTO transaction = new PersonalTransactionDTO(1L, new BigDecimal("0.01"));

        when(securityContextManager.checkSameUser(transaction.user())).thenReturn(Boolean.TRUE);
        when(userRepository.findById(transaction.user())).thenReturn(Optional.ofNullable(null));

        Exception output = assertThrows(ResourceNotFoundException.class, () -> {
            service.createPersonalTransaction(transaction, TransactionType.DEPOSIT);
        });
        String expectedMessage = "The user was not found with the given ID.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testCreatePersonalTransactionAsDeposit() {
        // Setup
        PersonalTransactionDTO personalTransactionDTO 
            = new PersonalTransactionDTO(1L, new BigDecimal("0.01"));
        User user = userMock.mockEntity(1);
        Transaction mockedTransaction 
            = transactionMock.mockEntity(1, user, TransactionType.DEPOSIT, new BigDecimal("0.01"));

        // Mock the results
        when(securityContextManager.checkSameUser(personalTransactionDTO.user())).thenReturn(Boolean.TRUE);
        when(userRepository.findById(personalTransactionDTO.user())).thenReturn(Optional.of(user));
        when(repository.save(any(Transaction.class))).thenReturn(mockedTransaction);

        // Verify the return of function
        TransactionDTO transactionDTO 
            = service.createPersonalTransaction(personalTransactionDTO, TransactionType.DEPOSIT);
        assertNotNull(transactionDTO.id());
        assertEquals(TransactionType.DEPOSIT.name(), transactionDTO.type());
        assertEquals(new BigDecimal("0.01"), transactionDTO.value());
        assertNotNull(transactionDTO.datetime());

        // Verify the setup of entities send to save in database
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userArgumentCaptor.capture());
        User userCapturedObject = userArgumentCaptor.getValue();
        assertEquals(new BigDecimal("2.00"), userCapturedObject.getAccountBalance());

        ArgumentCaptor<Transaction> transactionArgumentCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(repository).save(transactionArgumentCaptor.capture());
        Transaction transactionCapturedObject = transactionArgumentCaptor.getValue();
        assertNull(transactionCapturedObject.getId());
        assertEquals(1L, transactionCapturedObject.getUser().getId());
        assertNull(transactionCapturedObject.getCounterpartyUser());
        assertEquals(TransactionType.DEPOSIT, transactionCapturedObject.getType());
        assertEquals(new BigDecimal("0.01"), transactionCapturedObject.getValue());
        assertNotNull(transactionCapturedObject.getDatetime());
    }

    @Test
    void testCreatePersonalTransactionAsWithdrawalWithInsuficientBalance() {
        PersonalTransactionDTO personalTransactionDTO 
            = new PersonalTransactionDTO(1L, new BigDecimal("10.00"));
        User user = userMock.mockEntity(1);

        when(securityContextManager.checkSameUser(personalTransactionDTO.user())).thenReturn(Boolean.TRUE);
        when(userRepository.findById(personalTransactionDTO.user())).thenReturn(Optional.of(user));

        Exception output = assertThrows(RequestValidationException.class, () -> {
            service.createPersonalTransaction(personalTransactionDTO, TransactionType.WITHDRAWAL);
        });
        String expectedMessage = "The user does not have enough balance to carry out the transaction.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testCreatePersonalTransactionAsWithdrawalWithSuficientBalance() {
        // Setup
        PersonalTransactionDTO personalTransactionDTO 
            = new PersonalTransactionDTO(1L, new BigDecimal("0.02"));
        User user = userMock.mockEntity(1);
        Transaction mockedTransaction 
            = transactionMock.mockEntity(1, user, TransactionType.WITHDRAWAL, new BigDecimal("0.02"));

        // Mock the results
        when(securityContextManager.checkSameUser(personalTransactionDTO.user())).thenReturn(Boolean.TRUE);
        when(userRepository.findById(personalTransactionDTO.user())).thenReturn(Optional.of(user));
        when(repository.save(any(Transaction.class))).thenReturn(mockedTransaction);

        // Verify the return of function
        TransactionDTO transactionDTO 
            = service.createPersonalTransaction(personalTransactionDTO, TransactionType.WITHDRAWAL);
        assertNotNull(transactionDTO.id());
        assertEquals(TransactionType.WITHDRAWAL.name(), transactionDTO.type());
        assertEquals(new BigDecimal("0.02"), transactionDTO.value());
        assertNotNull(transactionDTO.datetime());

        // Verify the setup of entities send to save in database
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userArgumentCaptor.capture());
        User userCapturedObject = userArgumentCaptor.getValue();
        assertEquals(new BigDecimal("1.97"), userCapturedObject.getAccountBalance());

        ArgumentCaptor<Transaction> transactionArgumentCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(repository).save(transactionArgumentCaptor.capture());
        Transaction transactionCapturedObject = transactionArgumentCaptor.getValue();
        assertNull(transactionCapturedObject.getId());
        assertEquals(1L, transactionCapturedObject.getUser().getId());
        assertNull(transactionCapturedObject.getCounterpartyUser());
        assertEquals(TransactionType.WITHDRAWAL, transactionCapturedObject.getType());
        assertEquals(new BigDecimal("0.02"), transactionCapturedObject.getValue());
        assertNotNull(transactionCapturedObject.getDatetime());
    }

    @Test
    void testCreatePersonalTransactionAsTransfer() {
        PersonalTransactionDTO personalTransactionDTO 
            = new PersonalTransactionDTO(1L, new BigDecimal("10.00"));
        User user = userMock.mockEntity(1);

        when(securityContextManager.checkSameUser(personalTransactionDTO.user())).thenReturn(Boolean.TRUE);
        when(userRepository.findById(personalTransactionDTO.user())).thenReturn(Optional.of(user));

        Exception output = assertThrows(IllegalArgumentException.class, () -> {
            service.createPersonalTransaction(personalTransactionDTO, TransactionType.TRANSFER);
        });
        String expectedMessage = "Unsupported transaction type.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testCreateTransferWithPayerAndPayeeEqual() {
        TransferTransactionDTO transferTransactionDTO 
            = new TransferTransactionDTO(1L, 1L, new BigDecimal("0.01"));

        Exception output = assertThrows(RequestValidationException.class, () -> {
            service.createTranfer(transferTransactionDTO);
        });
        String expectedMessage = "The transfer must be between two different users.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testCreateTransferWithOtherUser() {
        TransferTransactionDTO transferTransactionDTO 
            = new TransferTransactionDTO(1L, 2L, new BigDecimal("0.01"));

        when(securityContextManager.checkSameUser(transferTransactionDTO.payer())).thenReturn(Boolean.FALSE);

        Exception output = assertThrows(UserUnauthorizedException.class, () -> {
            service.createTranfer(transferTransactionDTO);
        });
        String expectedMessage = "The user is not authorized to access this resource.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testCreateTransferWithNotFoundPayer() {
        TransferTransactionDTO transferTransactionDTO 
            = new TransferTransactionDTO(1L, 2L, new BigDecimal("0.01"));

        when(securityContextManager.checkSameUser(transferTransactionDTO.payer())).thenReturn(Boolean.TRUE);
        when(userRepository.findById(transferTransactionDTO.payer())).thenReturn(Optional.ofNullable(null));

        Exception output = assertThrows(ResourceNotFoundException.class, () -> {
            service.createTranfer(transferTransactionDTO);
        });
        String expectedMessage = "The payer was not found with the given ID.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testCreateTransferWithNotFoundPayee() {
        TransferTransactionDTO transferTransactionDTO 
            = new TransferTransactionDTO(1L, 2L, new BigDecimal("0.01"));
        User mockedUser = userMock.mockEntity(1);

        when(securityContextManager.checkSameUser(transferTransactionDTO.payer())).thenReturn(Boolean.TRUE);
        when(userRepository.findById(transferTransactionDTO.payer())).thenReturn(Optional.of(mockedUser));
        when(userRepository.findById(transferTransactionDTO.payee())).thenReturn(Optional.ofNullable(null));

        Exception output = assertThrows(ResourceNotFoundException.class, () -> {
            service.createTranfer(transferTransactionDTO);
        });
        String expectedMessage = "The payee was not found with the given ID.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testCreateTransferWithInsuficientBalance() {
        TransferTransactionDTO transferTransactionDTO 
            = new TransferTransactionDTO(1L, 2L, new BigDecimal("10.00"));
        User mockedUser = userMock.mockEntity(1);

        when(securityContextManager.checkSameUser(transferTransactionDTO.payer())).thenReturn(Boolean.TRUE);
        when(userRepository.findById(transferTransactionDTO.payer())).thenReturn(Optional.of(mockedUser));

        Exception output = assertThrows(RequestValidationException.class, () -> {
            service.createTranfer(transferTransactionDTO);
        });
        String expectedMessage = "The payer does not have enough balance to carry out the transaction.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testCreateTransferWithSuficientBalance() {
        //Setup
        TransferTransactionDTO transferTransactionDTO 
            = new TransferTransactionDTO(1L, 2L, new BigDecimal("0.01"));
        User mockedPayer = userMock.mockEntity(1);
        User mockedPayee = userMock.mockEntity(2);
        Transaction mockedTransaction 
            = transactionMock.mockEntity(1, mockedPayer, mockedPayee, new BigDecimal("0.01"));

        // Mock the results
        when(securityContextManager.checkSameUser(transferTransactionDTO.payer())).thenReturn(Boolean.TRUE);
        when(userRepository.findById(transferTransactionDTO.payer())).thenReturn(Optional.of(mockedPayer));
        when(userRepository.findById(transferTransactionDTO.payee())).thenReturn(Optional.of(mockedPayee));
        when(repository.save(any(Transaction.class))).thenReturn(mockedTransaction);
        
        // Verify the return of function
        TransactionDTO output = service.createTranfer(transferTransactionDTO);
        assertTrue(output.id() > 0);
        assertEquals(TransactionType.TRANSFER.name(), output.type());
        assertEquals(new BigDecimal("0.01"), output.value());
        assertNotNull(output.datetime());

        // Verify the setup of entities send to save in database
        ArgumentCaptor<List<User>> userArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(userRepository).saveAll(userArgumentCaptor.capture());
        List<User> capturedUserList = userArgumentCaptor.getValue();
        assertEquals(2, capturedUserList.size());
        assertEquals(1L, capturedUserList.get(0).getId());
        assertEquals(new BigDecimal("1.98"), capturedUserList.get(0).getAccountBalance());
        assertEquals(2L, capturedUserList.get(1).getId());
        assertEquals(new BigDecimal("3.00"), capturedUserList.get(1).getAccountBalance());

        ArgumentCaptor<Transaction> transactionArgumentCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(repository).save(transactionArgumentCaptor.capture());
        Transaction transactionCapturedObject = transactionArgumentCaptor.getValue();
        assertNull(transactionCapturedObject.getId());
        assertEquals(1L, transactionCapturedObject.getUser().getId());
        assertEquals(2L, transactionCapturedObject.getCounterpartyUser().getId());
        assertEquals(TransactionType.TRANSFER, transactionCapturedObject.getType());
        assertEquals(new BigDecimal("0.01"), transactionCapturedObject.getValue());
        assertNotNull(transactionCapturedObject.getDatetime());
    }
}
