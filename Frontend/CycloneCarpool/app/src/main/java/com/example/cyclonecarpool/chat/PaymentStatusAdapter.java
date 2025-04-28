package com.example.cyclonecarpool.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cyclonecarpool.R;
import java.util.List;

public class PaymentStatusAdapter extends RecyclerView.Adapter<PaymentStatusAdapter.ViewHolder> {

    private final List<PaymentStatusItem> paymentStatusList;

    public PaymentStatusAdapter(List<PaymentStatusItem> paymentStatusList) {
        this.paymentStatusList = paymentStatusList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_status_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentStatusItem item = paymentStatusList.get(position);
        holder.passengerName.setText(item.getPassengerName());
        holder.paymentStatus.setText(item.getPaymentStatus());
    }

    @Override
    public int getItemCount() {
        return paymentStatusList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView passengerName, paymentStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            passengerName = itemView.findViewById(R.id.passenger_name);
            paymentStatus = itemView.findViewById(R.id.payment_status);
        }
    }
}

