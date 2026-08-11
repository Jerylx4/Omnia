package com.prograavanzada.omnia.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prograavanzada.omnia.data.model.Category;

import java.util.Arrays;
import java.util.List;

public class CategoryRepository {
    private final FirebaseFirestore firestore;
    private final String currentUserId;

    public CategoryRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }

    private CollectionReference getCategoriesCollection() {
        return firestore.collection("users").document(currentUserId).collection("categories");
    }

    public interface CategoryCallback {
        void onSuccess(List<Category> categories);
        void onError(Exception e);
    }

    public void getCategoriesByType(String type, CategoryCallback callback) {
        getCategoriesCollection()
                .whereEqualTo("type", type)
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    List<Category> categories = querySnapshots.toObjects(Category.class);
                    if (categories.isEmpty()) {
                        // Generar categorías por defecto si es la primera vez
                        generateDefaultCategories(type, callback);
                    } else {
                        callback.onSuccess(categories);
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    private void generateDefaultCategories(String requestedType, CategoryCallback callback) {
        List<Category> defaults = Arrays.asList(
                new Category(null, currentUserId, "Alimentación", "EXPENSE", "ic_food", "#F59E0B", true),
                new Category(null, currentUserId, "Transporte", "EXPENSE", "ic_bus", "#06B6D4", true),
                new Category(null, currentUserId, "Hogar", "EXPENSE", "ic_home", "#059669", true),
                new Category(null, currentUserId, "Educación", "EXPENSE", "ic_book", "#3B5BFF", true),
                new Category(null, currentUserId, "Salud", "EXPENSE", "ic_health", "#EF4444", true),
                new Category(null, currentUserId, "Ocio", "EXPENSE", "ic_movie", "#8B5CF6", true),
                new Category(null, currentUserId, "Salario", "INCOME", "ic_money", "#10B981", true),
                new Category(null, currentUserId, "Freelance", "INCOME", "ic_laptop", "#3B5BFF", true)
        );

        firestore.runTransaction(transaction -> {
            for (Category cat : defaults) {
                String id = getCategoriesCollection().document().getId();
                cat.setId(id);
                transaction.set(getCategoriesCollection().document(id), cat);
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            getCategoriesByType(requestedType, callback);
        }).addOnFailureListener(callback::onError);
    }
}