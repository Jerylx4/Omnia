package com.prograavanzada.omnia.data.repository;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prograavanzada.omnia.data.model.User;

import java.util.Date;


public class AuthRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    private static final String USERS_COLLECTION = "users";

    public AuthRepository(){
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public interface AuthCallback{
        void onSuccess(FirebaseUser user);
        void onError(String errorMessage);
    }

    public void login(String email, String password, AuthCallback callback){
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if(firebaseUser != null){
                        saveUserToFirestore(firebaseUser, callback);
                    } else {
                        callback.onError("Error fata: Usuario creado, pero no devuelto por el servidor");
                    }
                })
                .addOnFailureListener(e -> callback.onError(getFriendlyErrorMessage(e)));
    }

    public void register(String email, String password, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        saveUserToFirestore(firebaseUser, callback);
                    } else {
                        callback.onError("Error fatal: Usuario creado pero no devuelto por el servidor.");
                    }
                })
                .addOnFailureListener(e -> callback.onError(getFriendlyErrorMessage(e)));
    }

    private void saveUserToFirestore(FirebaseUser firebaseUser, AuthCallback callback) {
        User newUser = new User(
                firebaseUser.getUid(),
                firebaseUser.getEmail(),
                new Timestamp(new Date())
        );

        firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.getUid())
                .set(newUser)
                .addOnSuccessListener(aVoid -> callback.onSuccess(firebaseUser))
                .addOnFailureListener(e -> {
                    callback.onError("Se creó la cuenta, pero falló la inicialización de la base de datos: " + e.getMessage());
                });
    }

    public void logout(){
        firebaseAuth.signOut();
    }

    public FirebaseUser getCurrentUser(){
        return firebaseAuth.getCurrentUser();
    }

    private String getFriendlyErrorMessage (Exception e){
        String error = e.getMessage();
        if (error == null) return "Ha ocurrido un error desconocido.";

        if (error.contains("invalid-email") || error.contains("badly formatted")) {
            return "El formato del correo electrónico no es válido.";
        } else if (error.contains("email-already-in-use")) {
            return "Este correo electrónico ya está registrado.";
        } else if (error.contains("weak-password")) {
            return "La contraseña es demasiado débil. Usa al menos 6 caracteres.";
        } else if (error.contains("INVALID_LOGIN_CREDENTIALS") || error.contains("user-not-found") || error.contains("wrong-password")) {
            return "Correo o contraseña incorrectos.";
        } else if (error.contains("network-request-failed")) {
            return "Error de conexión. Verifica tu internet.";
        }
        return "Fallo en la autenticación. Inténtalo más tarde.";
    }

}
