package br.com.igormartinez.virtualwallet.mocks;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.igormartinez.virtualwallet.enums.TransactionType;
import br.com.igormartinez.virtualwallet.models.Transaction;
import br.com.igormartinez.virtualwallet.models.User;

public class TransactionMock {

    public Transaction mockEntity(int number) {
        User user = new User();
        user.setId(Long.valueOf(number));

        Transaction transaction = new Transaction();
        transaction.setId(Long.valueOf(number));
        transaction.setUser(user);
        transaction.setValue(new BigDecimal(number + ".99"));
        transaction.setDatetime(ZonedDateTime.now());

        switch(number%3) {
            case 0:
                transaction.setType(TransactionType.DEPOSIT);
                break;
            case 1:
                transaction.setType(TransactionType.WITHDRAWAL);
                break;
            case 2:
                transaction.setType(TransactionType.TRANSFER);

                User counterpartyUser = new User();
                counterpartyUser.setId(Long.valueOf(number + 1));
                break;
        }

        return transaction;
    }

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

    public List<Transaction> mockEntityList(int number) {
        List<Transaction> list = new ArrayList<>();
        for (int i=1; i <= number; i++) {
            list.add(mockEntity(i));
        }
        return list;
    }
}
