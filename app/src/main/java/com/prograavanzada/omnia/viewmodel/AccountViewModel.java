package com.prograavanzada.omnia.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.prograavanzada.omnia.data.model.Account;
import com.prograavanzada.omnia.data.repository.AccountRepository;

import java.util.List;

public class AccountViewModel extends ViewModel {
    private final AccountRepository repository;

    private final MutableLiveData<List<Account>> accounts = new MutableLiveData<>();
    private final MutableLiveData<Double> totalBalance = new MutableLiveData<>(0.0);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();

    public AccountViewModel() {
        this.repository = new AccountRepository();
        loadAccounts();
    }

    public LiveData<List<Account>> getAccounts() { return accounts; }
    public LiveData<Double> getTotalBalance() { return totalBalance; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getOperationSuccess() { return operationSuccess; }

    public void loadAccounts() {
        isLoading.setValue(true);
        repository.getAllAccounts(new AccountRepository.RepositoryCallback<List<Account>>() {
            @Override
            public void onSuccess(List<Account> result) {
                accounts.setValue(result);
                calculateTotalBalance(result);
                isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                errorMessage.setValue("Error al cargar cuentas: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    private void calculateTotalBalance(List<Account> accountList) {
        double total = 0.0;
        if (accountList != null) {
            for (Account account : accountList) {
                total += account.getCurrentBalance();
            }
        }
        totalBalance.setValue(total);
    }

    public void createAccount(String name, String type, double initialBalance) {
        if (name == null || name.trim().isEmpty()) {
            errorMessage.setValue("El nombre de la cuenta es obligatorio.");
            return;
        }

        isLoading.setValue(true);

        // 1. Paleta de colores Flat Design vibrantes
        String[] palette = {
                "#3B5BFF", // Azul original
                "#F59E0B", // Naranja
                "#10B981", // Verde esmeralda
                "#8B5CF6", // Morado
                "#EF4444", // Rojo
                "#06B6D4"  // Cian
        };

        // 2. Selección aleatoria
        int randomIndex = new java.util.Random().nextInt(palette.length);
        String randomColor = palette[randomIndex];

        // 3. Ícono predeterminado
        String icon = type.equals("Efectivo") ? "ic_money" : "ic_bank";

        // Asignamos el color aleatorio a la cuenta
        Account newAccount = new Account(null, null, name, type, initialBalance, "USD", icon, randomColor);

        repository.createAccount(newAccount, new AccountRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                operationSuccess.setValue(true);
                loadAccounts();
            }

            @Override
            public void onError(Exception e) {
                errorMessage.setValue("No se pudo crear la cuenta: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    public void resetSuccessState() {
        operationSuccess.setValue(false);
    }
}