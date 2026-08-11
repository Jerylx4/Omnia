package com.prograavanzada.omnia.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.prograavanzada.omnia.data.repository.AuthRepository;

public class AuthViewModel extends ViewModel {
    private final AuthRepository authRepository;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<FirebaseUser> authenticatedUser = new MutableLiveData<>();

    public AuthViewModel() {
        this.authRepository = new AuthRepository();
        checkCurrentSession();
    }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<FirebaseUser> getAuthenticatedUser() { return authenticatedUser; }

    public void checkCurrentSession() {
        FirebaseUser user = authRepository.getCurrentUser();
        if (user != null) {
            authenticatedUser.setValue(user);
        }
    }

    public void login(String email, String password) {
        if (!validateInputs(email, password)) return;

        isLoading.setValue(true);
        authRepository.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                isLoading.setValue(false);
                authenticatedUser.setValue(user);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void register(String email, String password) {
        if (!validateInputs(email, password)) return;

        isLoading.setValue(true);
        authRepository.register(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                isLoading.setValue(false);
                authenticatedUser.setValue(user);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void logout() {
        authRepository.logout();
        authenticatedUser.setValue(null);
    }

    private boolean validateInputs(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            errorMessage.setValue("El correo no puede estar vacío.");
            return false;
        }
        if (password == null || password.trim().isEmpty()) {
            errorMessage.setValue("La contraseña no puede estar vacía.");
            return false;
        }
        return true;
    }
}