package com.prograavanzada.omnia.ui.transactions;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prograavanzada.omnia.R;
import com.prograavanzada.omnia.data.model.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    // La lista ahora contiene Objetos genéricos (Strings para fechas, Transactions para datos)
    private List<Object> items = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES"));

    public void setTransactions(List<Transaction> transactions) {
        items.clear();
        String currentDateStr = "";

        for (Transaction tx : transactions) {
            if (tx.getDate() != null) {
                String txDateStr = sdf.format(tx.getDate().toDate());

                // Lógica simple para agrupar por fecha
                if (!txDateStr.equals(currentDateStr)) {
                    currentDateStr = txDateStr;
                    items.add(currentDateStr); // Añadimos el encabezado (String)
                }
            }
            items.add(tx); // Añadimos el ítem (Transaction)
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof String) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_HEADER) {
            String date = (String) items.get(position);
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.tvHeaderDate.setText(date);
            // El cálculo del total por día lo dejaremos pendiente para la próxima actualización del motor
            headerHolder.tvHeaderTotal.setText("");

        } else {
            Transaction tx = (Transaction) items.get(position);
            ItemViewHolder itemHolder = (ItemViewHolder) holder;

            String desc = (tx.getDescription() != null && !tx.getDescription().isEmpty()) ? tx.getDescription() : "Movimiento";
            itemHolder.tvTransactionDesc.setText(desc);

            // Mapeo temporal (En el próximo paso actualizaremos Firebase para guardar los colores reales)
            itemHolder.tvTransactionCategory.setText("Categoría");
            itemHolder.tvTransactionAccount.setText("• Cuenta");

            if ("INCOME".equals(tx.getType())) {
                itemHolder.tvTransactionAmount.setText(String.format("+ $%,.2f", tx.getAmount()));
                itemHolder.tvTransactionAmount.setTextColor(Color.parseColor("#00C47C"));
            } else {
                itemHolder.tvTransactionAmount.setText(String.format("- $%,.2f", tx.getAmount()));
                itemHolder.tvTransactionAmount.setTextColor(Color.parseColor("#FF4B4B"));
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder para la Fecha
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeaderDate, tvHeaderTotal;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeaderDate = itemView.findViewById(R.id.tvHeaderDate);
            tvHeaderTotal = itemView.findViewById(R.id.tvHeaderTotal);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvTransactionDesc, tvTransactionAmount, tvTransactionCategory, tvTransactionAccount;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTransactionDesc = itemView.findViewById(R.id.tvTransactionDesc);
            tvTransactionAmount = itemView.findViewById(R.id.tvTransactionAmount);
            tvTransactionCategory = itemView.findViewById(R.id.tvTransactionCategory);
            tvTransactionAccount = itemView.findViewById(R.id.tvTransactionAccount);
        }
    }
}