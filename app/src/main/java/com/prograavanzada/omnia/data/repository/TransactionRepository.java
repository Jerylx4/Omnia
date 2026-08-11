package com.prograavanzada.omnia.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.prograavanzada.omnia.data.model.Transaction;

import java.util.Date;
import java.util.List;

public class TransactionRepository {
    private final FirebaseFirestore firestore;
    private final String currentUserId;

    public TransactionRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }

    public interface TransactionCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface TransactionListCallback {
        void onSuccess(List<Transaction> transactions);
        void onError(Exception e);
    }

    public void registerTransaction(Transaction transaction, TransactionCallback callback) {
        if (currentUserId == null) {
            callback.onError(new Exception("Usuario no autenticado"));
            return;
        }

        transaction.setUserId(currentUserId);
        DocumentReference accountRef = firestore.collection("users").document(currentUserId)
                .collection("accounts").document(transaction.getAccountId());

        DocumentReference transactionRef = firestore.collection("users").document(currentUserId)
                .collection("transactions").document();
        transaction.setId(transactionRef.getId());

        firestore.runTransaction(firebaseTransaction -> {
                    com.google.firebase.firestore.DocumentSnapshot accountSnapshot = firebaseTransaction.get(accountRef);
                    if (!accountSnapshot.exists()) {
                        throw new FirebaseFirestoreException("La cuenta no existe", FirebaseFirestoreException.Code.NOT_FOUND);
                    }

                    Double currentBalance = accountSnapshot.getDouble("currentBalance");
                    if (currentBalance == null) currentBalance = 0.0;

                    double newBalance = currentBalance;
                    if (transaction.getType().equals("EXPENSE")) {
                        newBalance -= transaction.getAmount();
                    } else if (transaction.getType().equals("INCOME")) {
                        newBalance += transaction.getAmount();
                    }

                    firebaseTransaction.update(accountRef, "currentBalance", newBalance, "updatedAt", com.google.firebase.Timestamp.now());
                    firebaseTransaction.set(transactionRef, transaction);

                    return null;

                }).addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void getTransactionsByDateRange(Date startDate, Date endDate, TransactionListCallback callback) {
        if (currentUserId == null) return;

        firestore.collection("users").document(currentUserId)
                .collection("transactions")
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Transaction> transactions = queryDocumentSnapshots.toObjects(Transaction.class);
                    callback.onSuccess(transactions);
                })
                .addOnFailureListener(callback::onError);
    }
}