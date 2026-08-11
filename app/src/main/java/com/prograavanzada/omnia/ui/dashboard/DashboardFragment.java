package com.prograavanzada.omnia.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.prograavanzada.omnia.R;
import com.prograavanzada.omnia.databinding.FragmentDashboardBinding;
import com.prograavanzada.omnia.ui.transactions.TransactionAdapter;
import com.prograavanzada.omnia.viewmodel.AccountViewModel;
import com.prograavanzada.omnia.viewmodel.TransactionViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private AccountViewModel accountViewModel;
    private TransactionViewModel transactionViewModel;
    private AccountAdapter adapter;
    private TransactionAdapter recentAdapter;

    private boolean isBalanceVisible = true;
    private double currentTotalBalance = 0.0;

    public DashboardFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        setupUI();
        setupRecyclerView();
        setupObservers();
        setupClickListeners();

        accountViewModel.loadAccounts();
        transactionViewModel.loadCurrentMonthMetrics();
        transactionViewModel.loadAllTransactions();
    }

    private void setupUI() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        String currentMonthStr = sdf.format(new Date());
        currentMonthStr = currentMonthStr.substring(0, 1).toUpperCase() + currentMonthStr.substring(1);
        binding.tvMonthLabel.setText("Saldo total - " + currentMonthStr);
    }

    private void setupRecyclerView() {
        adapter = new AccountAdapter();
        binding.rvAccounts.setAdapter(adapter);

        recentAdapter = new TransactionAdapter();
        binding.rvRecentDashboard.setAdapter(recentAdapter);
    }

    private void setupClickListeners() {
        binding.ivToggleBalance.setOnClickListener(v -> {
            isBalanceVisible = !isBalanceVisible;
            updateBalanceVisibility();
        });

        binding.tvSeeAllRecent.setOnClickListener(v -> {
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                    requireActivity().findViewById(R.id.bottomNavigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_transactions);
            }
        });
    }

    private void setupObservers() {
        accountViewModel.getAccounts().observe(getViewLifecycleOwner(), accounts -> {
            if (accounts != null) adapter.setAccounts(accounts);
        });

        accountViewModel.getTotalBalance().observe(getViewLifecycleOwner(), totalBalance -> {
            currentTotalBalance = totalBalance;
            updateBalanceVisibility();
        });

        accountViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.pbDashboardLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.rvAccounts.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        });

        transactionViewModel.getCurrentMonthIncome().observe(getViewLifecycleOwner(), income -> {
            if (income >= 1000) {
                binding.tvTotalIncome.setText(String.format("$%.1fk", income / 1000));
            } else {
                binding.tvTotalIncome.setText(String.format("$%.0f", income));
            }
        });

        transactionViewModel.getCurrentMonthExpense().observe(getViewLifecycleOwner(), expense -> {
            if (expense >= 1000) {
                binding.tvTotalExpenses.setText(String.format("$%.1fk", expense / 1000));
            } else {
                binding.tvTotalExpenses.setText(String.format("$%.0f", expense));
            }
        });

        transactionViewModel.getSavingsRate().observe(getViewLifecycleOwner(), rate -> {
            binding.tvSavingsRate.setText(String.format("%.0f%%", rate));
        });

        transactionViewModel.getRecentTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                int limit = Math.min(transactions.size(), 4);
                recentAdapter.setTransactions(transactions.subList(0, limit));
                binding.rvRecentDashboard.setVisibility(View.VISIBLE);
            } else {
                binding.rvRecentDashboard.setVisibility(View.GONE);
            }
        });
    }

    private void updateBalanceVisibility() {
        if (isBalanceVisible) {
            binding.tvTotalBalance.setText(String.format("$%,.2f", currentTotalBalance));
            binding.ivToggleBalance.setImageResource(android.R.drawable.ic_menu_view);
            binding.ivToggleBalance.setAlpha(1.0f);
        } else {
            binding.tvTotalBalance.setText("$ ••••••");
            binding.ivToggleBalance.setImageResource(android.R.drawable.ic_secure);
            binding.ivToggleBalance.setAlpha(0.5f);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}