package com.midas.app.workflows;

import com.midas.app.activities.UpdateAccountActivity;
import com.midas.app.models.Account;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import org.slf4j.Logger;

@WorkflowImpl(taskQueues = UpdateAccountWorkflow.QUEUE_NAME)
public class UpdateAccountWorkflowImpl implements UpdateAccountWorkflow {
  private final Logger log = Workflow.getLogger(UpdateAccountWorkflowImpl.class);
  private final UpdateAccountActivity updateAccountActivity;

  public UpdateAccountWorkflowImpl() {
    this.updateAccountActivity =
        Workflow.newActivityStub(UpdateAccountActivity.class, getOptions());
  }

  /**
   * getOptions returns the activity options.
   *
   * @return ActivityOptions
   */
  private ActivityOptions getOptions() {
    return ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofSeconds(10))
        .setRetryOptions(
            RetryOptions.newBuilder()
                .setInitialInterval(Duration.ofSeconds(2))
                .setMaximumAttempts(1)
                .build())
        .build();
  }

  @Override
  public Account updateAccount(Account details) {
    Account existingAccount = updateAccountActivity.findAccount(details);
    Account updatedAccount = updateAccountFields(existingAccount, details);
    updateAccountActivity.updatePaymentAccount(updatedAccount);
    Account account = updateAccountActivity.updateAccount(updatedAccount);
    log.info("Account updated: {}", account.getId());
    return account;
  }

  /**
   * updateAccountFields updates the fields of an account.
   *
   * @param existingAccount is the existing account.
   * @param newAccount is the new account.
   * @return Account
   */
  private Account updateAccountFields(Account existingAccount, Account newAccount) {
    existingAccount.setEmail(getUpdatedField(newAccount.getEmail(), existingAccount.getEmail()));
    existingAccount.setFirstName(
        getUpdatedField(newAccount.getFirstName(), existingAccount.getFirstName()));
    existingAccount.setLastName(
        getUpdatedField(newAccount.getLastName(), existingAccount.getLastName()));
    return existingAccount;
  }

  /**
   * getUpdatedField returns the updated field.
   *
   * @param newField is the new field.
   * @param existingField is the existing field.
   * @return String
   */
  private String getUpdatedField(String newField, String existingField) {
    return newField != null && !newField.isBlank() ? newField : existingField;
  }
}
