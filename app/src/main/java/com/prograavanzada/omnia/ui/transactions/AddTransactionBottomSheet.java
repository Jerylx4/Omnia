package com.prograavanzada.omnia.ui.transactions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.prograavanzada.omnia.R;
import com.prograavanzada.omnia.data.model.Account;
import com.prograavanzada.omnia.data.model.Category;
import com.prograavanzada.omnia.databinding.BottomSheetAddTransactionBinding;
import com.prograavanzada.omnia.viewmodel.AccountViewModel;
import com.prograavanzada.omnia.viewmodel.TransactionViewModel;

import java.util.ArrayList;
import java.util.List;

public class AddTransactionBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetAddTransactionBinding binding;
    private TransactionViewModel transactionViewModel;
    private AccountViewModel accountViewModel; // Lo necesitamos para actualizar el Dashboard

    private List<Account> accountList = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();

    private Account selectedAccount = null;
    private Category selectedCategory = null;
    private String currentType = "EXPENSE"; // Gasto por defecto

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAddTransactionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializamos los ViewModels
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);

        setupUI();
        setupObservers();
        setupListeners();

        // Cargar las cuentas y las categorías iniciales (Gastos)
        transactionViewModel.loadAccounts();
        transactionViewModel.loadCategories(currentType);
    }

    private void setupUI() {
        // Aseguramos que el botón de Gasto esté presionado visualmente al abrir
        binding.toggleGroupType.check(R.id.btnExpense);
    }

    private void setupObservers() {
        // Llenar el menú desplegable de Cuentas
        transactionViewModel.getAccounts().observe(getViewLifecycleOwner(), accounts -> {
            this.accountList = accounts;
            List<String> accountNames = new ArrayList<>();
            for (Account a : accounts) accountNames.add(a.getName());

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, accountNames);
            binding.actvAccount.setAdapter(adapter);
        });

        // Llenar el menú desplegable de Categorías
        transactionViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            this.categoryList = categories;
            List<String> categoryNames = new ArrayList<>();
            for (Category c : categories) categoryNames.add(c.getName());

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryNames);
            binding.actvCategory.setAdapter(adapter);

            // Limpiar la selección anterior si el usuario cambió de Gasto a Ingreso
            binding.actvCategory.setText("", false);
            selectedCategory = null;
        });

        transactionViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnSaveTransaction.setText(isLoading ? "" : "Guardar Movimiento");
            binding.btnSaveTransaction.setEnabled(!isLoading);
        });

        transactionViewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(getContext(), "Movimiento registrado", Toast.LENGTH_SHORT).show();
                // ¡Magia! Le pedimos al Dashboard que recalcule todo el dinero
                accountViewModel.loadAccounts();
                transactionViewModel.resetSuccessState();
                dismiss(); // Cierra el panel
            }
        });

        transactionViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupListeners() {
        // Detectar si el usuario toca "Gasto" o "Ingreso" para cambiar las categorías
        binding.toggleGroupType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnExpense) {
                    currentType = "EXPENSE";
                } else if (checkedId == R.id.btnIncome) {
                    currentType = "INCOME";
                }
                // Pedimos a Firebase las categorías correspondientes
                transactionViewModel.loadCategories(currentType);
            }
        });

        binding.actvAccount.setOnItemClickListener((parent, view, position, id) -> {
            selectedAccount = accountList.get(position);
        });

        binding.actvCategory.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategory = categoryList.get(position);
        });

        binding.btnSaveTransaction.setOnClickListener(v -> {
            String amount = binding.etAmount.getText().toString();
            String description = binding.etDescription.getText().toString();
            transactionViewModel.registerTransaction(currentType, amount, description, selectedAccount, selectedCategory);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}