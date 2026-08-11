package com.prograavanzada.omnia.ui.accounts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.prograavanzada.omnia.databinding.BottomSheetAddAccountBinding;
import com.prograavanzada.omnia.viewmodel.AccountViewModel;

public class AddAccountBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetAddAccountBinding binding;
    private AccountViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAddAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtenemos el ViewModel asociado a la Actividad principal para compartir datos
        viewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnSaveAccount.setText("");
                binding.btnSaveAccount.setEnabled(false);
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSaveAccount.setText("Crear Cuenta");
                binding.btnSaveAccount.setEnabled(true);
            }
        });

        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(getContext(), "Cuenta creada con éxito", Toast.LENGTH_SHORT).show();
                viewModel.resetSuccessState();
                dismiss(); // Cierra el panel deslizable
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupListeners() {
        binding.btnSaveAccount.setOnClickListener(v -> {
            String name = binding.etAccountName.getText().toString();
            String balanceStr = binding.etInitialBalance.getText().toString();
            double balance = balanceStr.isEmpty() ? 0.0 : Double.parseDouble(balanceStr);

            // Por simplicidad en esta fase, asignamos "Banco" por defecto. Luego añadiremos un selector.
            viewModel.createAccount(name, "Banco", balance);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}