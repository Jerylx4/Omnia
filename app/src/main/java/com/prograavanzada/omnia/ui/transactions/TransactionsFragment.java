package com.prograavanzada.omnia.ui.transactions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.prograavanzada.omnia.databinding.FragmentTransactionsBinding;
import com.prograavanzada.omnia.viewmodel.TransactionViewModel;

public class TransactionsFragment extends Fragment {

    private FragmentTransactionsBinding binding;
    private TransactionViewModel transactionViewModel;
    private TransactionAdapter adapter;

    public TransactionsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        setupRecyclerView();
        setupObservers();

        transactionViewModel.loadAllTransactions();
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter();
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvTransactions.setAdapter(adapter);
    }

    private void setupObservers() {
        transactionViewModel.getRecentTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                adapter.setTransactions(transactions);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}