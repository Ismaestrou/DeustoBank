package com.example.deustobank.scheduler;

import com.example.deustobank.model.ScheduledTransfer;
import com.example.deustobank.repository.ScheduledTransferRepository;
import com.example.deustobank.service.AccountService;
import com.example.deustobank.service.ScheduledTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class ScheduledTransferJob {

    @Autowired private ScheduledTransferRepository repo;
    @Autowired private AccountService accountService;
    @Autowired private ScheduledTransferService scheduledTransferService;

    // Se ejecuta todos los días a las 00:05
    @Scheduled(cron = "0 5 0 * * *")
    public void ejecutarTransferencias() {
        List<ScheduledTransfer> pendientes = repo.findByActiveTrueAndNextExecutionLessThanEqual(LocalDate.now());

        for (ScheduledTransfer st : pendientes) {
            try {
                accountService.transfer(
                    st.getFromAccount().getId(),
                    st.getToAccount().getId(),
                    st.getAmount(),
                    st.getFromAccount().getUser().getId()
                );
                // Actualizar próxima ejecución
                st.setNextExecution(scheduledTransferService.calcularProximaEjecucion(
                    st.getNextExecution(), st.getFrequency()
                ));
                repo.save(st);
            } catch (Exception e) {
                // Si falla (saldo insuficiente, etc.) se desactiva
                st.setActive(false);
                repo.save(st);
            }
        }
    }
}