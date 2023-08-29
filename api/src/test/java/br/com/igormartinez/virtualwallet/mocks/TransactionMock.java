package br.com.igormartinez.virtualwallet.mocks;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import br.com.igormartinez.virtualwallet.enums.TransactionType;
import br.com.igormartinez.virtualwallet.models.Transaction;
import br.com.igormartinez.virtualwallet.models.User;

public class TransactionMock {

    public Transaction mockEntity(int number, User user, TransactionType type, BigDecimal value) {
        Transaction transaction = new Transaction();
        transaction.setId(Long.valueOf(number));
        transaction.setUser(user);
        transaction.setType(type);
        transaction.setValue(value);
        transaction.setDatetime(ZonedDateTime.now());

        return transaction;
    }

    public Transaction mockEntity(int number, User user, User counterpartyUser, BigDecimal value) {
        Transaction transaction = new Transaction();
        transaction.setId(Long.valueOf(number));
        transaction.setUser(user);
        transaction.setCounterpartyUser(counterpartyUser);
        transaction.setType(TransactionType.TRANSFER);
        transaction.setValue(value);
        transaction.setDatetime(ZonedDateTime.now());

        return transaction;
    }
}
