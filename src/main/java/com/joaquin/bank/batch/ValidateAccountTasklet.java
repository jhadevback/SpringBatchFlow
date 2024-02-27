package com.joaquin.bank.batch;

import com.joaquin.bank.entities.TransferPaymentEntity;
import com.joaquin.bank.repositories.TransferPaymentRepository;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

public class ValidateAccountTasklet implements Tasklet {

    @Autowired
    private TransferPaymentRepository transferPaymentRepository;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {

        Boolean filterIsApproved = true;

        String transactionId = (String) chunkContext
                .getStepContext()
                .getJobParameters()
                .get("transactionId");

        TransferPaymentEntity transferPayment = transferPaymentRepository.findById(transactionId).orElseThrow();

        if (!transferPayment.getIsEnabled()){
            //Error cuenta inactiva
            chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .put("message", "Error, cuenta inactiva");

            filterIsApproved = false;
        }

        if (transferPayment.getAmountPaid() > transferPayment.getAvailableBalance()){
            //Error saldo insuficiente

            chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .put("message", "Error, saldo insuficiente");

            filterIsApproved = false;
        }

        ExitStatus exitStatus = null;

        if (filterIsApproved){
            exitStatus = new ExitStatus("VALID");
            stepContribution.setExitStatus(exitStatus);
        } else {
            exitStatus = new ExitStatus("INVALID");
            stepContribution.setExitStatus(exitStatus);
        }

        return RepeatStatus.FINISHED;
    }

}
