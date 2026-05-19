package com.example.deustobank.service;

import com.example.deustobank.model.Account;
import com.example.deustobank.model.ScheduledTransfer;
import com.example.deustobank.repository.AccountRepository;
import com.example.deustobank.repository.ScheduledTransferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ScheduledTransferService {

    @Autowired private ScheduledTransferRepository repo;
    @Autowired private AccountRepository accountRepo;
    @Autowired private AccountService accountService;

    public ScheduledTransfer create(Long fromId, Long toId, double amount,
                                    ScheduledTransfer.Frequency frequency,
                                    LocalDate startDate, Long requesterId) {
        Account from = accountRepo.findById(fromId)
            .orElseThrow(() -> new RuntimeException("Cuenta origen no encontrada"));
        Account to = accountRepo.findById(toId)
            .orElseThrow(() -> new RuntimeException("Cuenta destino no encontrada"));

        if (fromId.equals(toId)) throw new RuntimeException("Las cuentas no pueden ser iguales");
        if (amount <= 0) throw new RuntimeException("Importe inválido");
        if (from.getBalance() < amount) throw new RuntimeException("Saldo insuficiente para programar la transferencia");

        ScheduledTransfer st = new ScheduledTransfer();
        st.setFromAccount(from);
        st.setToAccount(to);
        st.setAmount(amount);
        st.setFrequency(frequency);
        st.setStartDate(startDate);
        st.setNextExecution(startDate);
        st.setActive(true);

        return repo.save(st);
    }

    public ScheduledTransfer edit(Long id, double amount,
                                   ScheduledTransfer.Frequency frequency,
                                   LocalDate nextExecution, Long requesterId) {
        ScheduledTransfer st = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Transferencia programada no encontrada"));

        if (amount <= 0) throw new RuntimeException("Importe inválido");

        st.setAmount(amount);
        st.setFrequency(frequency);
        st.setNextExecution(nextExecution);

        return repo.save(st);
    }

    public void cancel(Long id, Long requesterId) {
        ScheduledTransfer st = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Transferencia programada no encontrada"));
        st.setActive(false);
        repo.save(st);
    }

    public List<ScheduledTransfer> getByAccount(Long accountId) {
        return repo.findByFromAccount_IdAndActiveTrue(accountId);
    }

    public LocalDate calcularProximaEjecucion(LocalDate actual, ScheduledTransfer.Frequency frequency) {
        return switch (frequency) {
            case DAILY   -> actual.plusDays(1);
            case WEEKLY  -> actual.plusWeeks(1);
            case MONTHLY -> actual.plusMonths(1);
        };
    }
}