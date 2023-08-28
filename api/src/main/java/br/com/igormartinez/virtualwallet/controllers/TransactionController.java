package br.com.igormartinez.virtualwallet.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.igormartinez.virtualwallet.data.dto.PersonalTransactionDTO;
import br.com.igormartinez.virtualwallet.data.dto.TransactionDTO;
import br.com.igormartinez.virtualwallet.data.dto.TransferTransactionDTO;
import br.com.igormartinez.virtualwallet.enums.TransactionType;
import br.com.igormartinez.virtualwallet.services.TransactionService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1/transaction")
public class TransactionController {

    @Autowired
    TransactionService service;

    @PostMapping("/deposit")
    public TransactionDTO deposit(@RequestBody @Valid PersonalTransactionDTO transaction) {
        return service.createPersonalTransaction(transaction, TransactionType.DEPOSIT);
    }

    @PostMapping("/withdrawal")
    public TransactionDTO withdrawal(@RequestBody @Valid PersonalTransactionDTO transaction) {
        return service.createPersonalTransaction(transaction, TransactionType.WITHDRAWAL);
    }

    @PostMapping("/transfer")
    public TransactionDTO transfer(@RequestBody @Valid TransferTransactionDTO transaction) {
        return service.createTranfer(transaction);
    }
}
