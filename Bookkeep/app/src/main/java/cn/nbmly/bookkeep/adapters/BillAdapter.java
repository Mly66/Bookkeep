package cn.nbmly.bookkeep.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.nbmly.bookkeep.R;
import cn.nbmly.bookkeep.models.Bill;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.BillViewHolder> {

    private List<Bill> billList;
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(Bill bill);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Bill bill);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public BillAdapter(List<Bill> billList) {
        this.billList = billList;
    }

    public void setBillList(List<Bill> billList) {
        this.billList = billList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bill, parent, false);
        return new BillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        Bill bill = billList.get(position);
        holder.tvAmount.setText(String.format("金额: %.2f", bill.getAmount()));
        holder.tvTypeCategory.setText(String.format("类型: %s - %s", bill.getType(), bill.getCategory()));
        holder.tvDate.setText(String.format("日期: %s", bill.getDate()));
        holder.tvDescription.setText(String.format("描述: %s", bill.getDescription()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(bill);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(bill);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return billList.size();
    }

    static class BillViewHolder extends RecyclerView.ViewHolder {
        TextView tvAmount;
        TextView tvTypeCategory;
        TextView tvDate;
        TextView tvDescription;

        public BillViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tv_bill_amount);
            tvTypeCategory = itemView.findViewById(R.id.tv_bill_type_category);
            tvDate = itemView.findViewById(R.id.tv_bill_date);
            tvDescription = itemView.findViewById(R.id.tv_bill_description);
        }
    }
}

