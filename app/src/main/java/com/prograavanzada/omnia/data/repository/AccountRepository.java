package com.prograavanzada.omnia.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prograavanzada.omnia.data.model.Account;

import java.util.List;

public class AccountRepository {
    private final FirebaseFirestore firestore;
    private final String currentUserId;

    public AccountRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            this.currentUserId = null;
        }
    }

    private CollectionReference getAccountsCollection() {
        if (currentUserId == null) throw new IllegalStateException("Usuario no autenticado");
        return firestore.collection("users").document(currentUserId).collection("accounts");
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public void createAccount(Account account, RepositoryCallback<Void> callback) {
        String newId = getAccountsCollection().document().getId();
        account.setId(newId);
        account.setUserId(currentUserId);

        getAccountsCollection().document(newId)
                .set(account)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void getAllAccounts(RepositoryCallback<List<Account>> callback) {
        getAccountsCollection()
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Account> accounts = queryDocumentSnapshots.toObjects(Account.class);
                    callback.onSuccess(accounts);
                })
                .addOnFailureListener(callback::onError);
    }

    public void updateAccount(Account account, RepositoryCallback<Void> callback) {
        account.setUpdatedAt(com.google.firebase.Timestamp.now());
        getAccountsCollection().document(account.getId())
                .set(account) // Sobrescribe el documento con los nuevos datos
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void deleteAccount(String accountId, RepositoryCallback<Void> callback) {
        getAccountsCollection().document(accountId)
                .update("active", false, "updatedAt", com.google.firebase.Timestamp.now())
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }
}