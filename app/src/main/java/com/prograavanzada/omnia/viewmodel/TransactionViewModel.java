package com.prograavanzada.omnia.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.Timestamp;
import com.prograavanzada.omnia.data.model.Account;
import com.prograavanzada.omnia.data.model.Category;
import com.prograavanzada.omnia.data.model.Transaction;
import com.prograavanzada.omnia.data.repository.AccountRepository;
import com.prograavanzada.omnia.data.repository.CategoryRepository;
import com.prograavanzada.omnia.data.repository.TransactionRepository;

import java.util.Date;
import java.util.List;

import java.util.Calendar;
import java.util.Date;

public class TransactionViewModel extends ViewModel {
    private final TransactionRepository transactionRepo;
    private final AccountRepository accountRepo;
    private final CategoryRepository categoryRepo;

    private final MutableLiveData<List<Account>> accounts = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
    private final MutableLiveData<Double> currentMonthIncome = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> currentMonthExpense = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> savingsRate = new MutableLiveData<>(0.0);
    private final MutableLiveData<List<Transaction>> recentTransactions = new MutableLiveData<>();
    public LiveData<List<Transaction>> getRecentTransactions() { return recentTransactions; }

    public TransactionViewModel() {
        this.transactionRepo = new TransactionRepository();
        this.accountRepo = new AccountRepository();
        this.categoryRepo = new CategoryRepository();
    }

    public LiveData<List<Account>> getAccounts() { return accounts; }
    public LiveData<List<Category>> getCategories() { return categories; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getOperationSuccess() { return operationSuccess; }
    public LiveData<Double> getCurrentMonthIncome() { return currentMonthIncome; }
    public LiveData<Double> getCurrentMonthExpense() { return currentMonthExpense; }
    public LiveData<Double> getSavingsRate() { return savingsRate; }

    public void loadAllTransactions(){
        isLoading.setValue(true);

        Calendar cal = Calendar .getInstance();
        cal.set(2000, Calendar.JANUARY, 1);
        Date startDate = cal.getTime();

        cal.set(2100, Calendar.DECEMBER, 31);
        Date endDate = cal.getTime();

        transactionRepo.getTransactionsByDateRange(startDate, endDate, new TransactionRepository.TransactionListCallback() {
            @Override
            public void onSuccess(List<Transaction> transactions) {
                recentTransactions.setValue(transactions);
                isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                errorMessage.setValue("Error al calrgar el historial: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    public void loadAccounts() {
        accountRepo.getAllAccounts(new AccountRepository.RepositoryCallback<List<Account>>() {
            @Override
            public void onSuccess(List<Account> result) { accounts.setValue(result); }
            @Override
            public void onError(Exception e) { errorMessage.setValue("Error cargando cuentas."); }
        });
    }

    public void loadCategories(String type) {
        // Carga categorías dependiendo de si es "INCOME" (Ingreso) o "EXPENSE" (Gasto)
        categoryRepo.getCategoriesByType(type, new CategoryRepository.CategoryCallback() {
            @Override
            public void onSuccess(List<Category> result) { categories.setValue(result); }
            @Override
            public void onError(Exception e) { errorMessage.setValue("Error cargando categorías."); }
        });
    }

    public void registerTransaction(String type, String amountStr, String description, Account selectedAccount, Category selectedCategory) {
        if (amountStr == null || amountStr.isEmpty() || selectedAccount == null || selectedCategory == null) {
            errorMessage.setValue("Monto, Cuenta y Categoría son obligatorios.");
            return;
        }

        double amount = Double.parseDouble(amountStr);
        if (amount <= 0) {
            errorMessage.setValue("El monto debe ser mayor a cero.");
            return;
        }

        isLoading.setValue(true);
        Transaction transaction = new Transaction(
                null, null, selectedAccount.getId(), selectedCategory.getId(),
                type, amount, description, new Timestamp(new Date()), new Timestamp(new Date())
        );

        transactionRepo.registerTransaction(transaction, new TransactionRepository.TransactionCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                operationSuccess.setValue(true);
            }

            @Override
            public void onError(Exception e) {
                isLoading.setValue(false);
                errorMessage.setValue("Error al guardar: " + e.getMessage());
            }
        });
    }

    public void loadCurrentMonthMetrics() {
        Calendar cal = Calendar.getInstance();

        // Configurar al primer milisegundo del mes actual
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfMonth = cal.getTime();

        // Configurar al último milisegundo del mes actual
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date endOfMonth = cal.getTime();

        transactionRepo.getTransactionsByDateRange(startOfMonth, endOfMonth, new TransactionRepository.TransactionListCallback() {
            @Override
            public void onSuccess(List<Transaction> transactions) {
                double income = 0;
                double expense = 0;

                for (Transaction t : transactions) {
                    if ("INCOME".equals(t.getType())) {
                        income += t.getAmount();
                    } else if ("EXPENSE".equals(t.getType())) {
                        expense += t.getAmount();
                    }
                }

                currentMonthIncome.setValue(income);
                currentMonthExpense.setValue(expense);

                // Calcular la tasa de ahorro
                if (income > 0) {
                    double saved = income - expense;
                    double rate = (saved / income) * 100;
                    savingsRate.setValue(Math.max(0, rate)); // Evita porcentajes negativos en la UI
                } else {
                    savingsRate.setValue(0.0);
                }
            }

            @Override
            public void onError(Exception e) {
                errorMessage.setValue("Error al calcular métricas: " + e.getMessage());
            }
        });
    }

    public void resetSuccessState() {
        operationSuccess.setValue(false);
    }
}