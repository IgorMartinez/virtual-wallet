package br.com.igormartinez.virtualwallet.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.igormartinez.virtualwallet.models.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {}
