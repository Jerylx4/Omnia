package com.prograavanzada.omnia.ui.dashboard;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.prograavanzada.omnia.R;
import com.prograavanzada.omnia.data.model.Account;

import java.util.ArrayList;
import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private List<Account> accountList = new ArrayList<>();

    public void setAccounts(List<Account> accounts) {
        this.accountList = accounts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account account = accountList.get(position);

        holder.tvAccountName.setText(account.getName());
        // Formateo de moneda estricto
        holder.tvAccountBalance.setText(String.format("$%,.2f", account.getCurrentBalance()));
        holder.tvAccountType.setText(account.getType());

        try {
            holder.cardBackground.setBackgroundColor(Color.parseColor(account.getColor()));
        } catch (IllegalArgumentException e) {
            holder.cardBackground.setBackgroundColor(Color.parseColor("#3B5BFF")); // Color fallback
        }
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView tvAccountName, tvAccountBalance, tvAccountType;
        ConstraintLayout cardBackground;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAccountName = itemView.findViewById(R.id.tvAccountName);
            tvAccountBalance = itemView.findViewById(R.id.tvAccountBalance);
            tvAccountType = itemView.findViewById(R.id.tvAccountType);
            cardBackground = itemView.findViewById(R.id.cardBackground);
        }
    }
}