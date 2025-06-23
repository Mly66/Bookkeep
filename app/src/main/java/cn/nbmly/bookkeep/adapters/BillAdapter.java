package cn.nbmly.bookkeep.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import cn.nbmly.bookkeep.R;
import cn.nbmly.bookkeep.models.Bill;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.ViewHolder> {

    private List<Bill> billList;
    private Context context;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private SimpleDateFormat dateFormat;

    public BillAdapter(Context context, List<Bill> billList) {
        this.context = context;
        this.billList = billList;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    }

    public void setBillList(List<Bill> billList) {
        this.billList = billList;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bill, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bill bill = billList.get(position);
        holder.tvAmount.setText(String.format("金额: ¥%.2f", bill.getAmount()));
        holder.tvType.setText(String.format("类型: %s", getTypeText(bill.getType())));
        holder.tvCategory.setText(String.format("分类: %s", getCategoryText(bill.getType(), bill.getCategory())));
        holder.tvNote.setText(String.format("备注: %s", bill.getNote()));
        holder.tvTime.setText(String.format("时间: %s", dateFormat.format(bill.getCreateTime())));

        // 设置金额颜色：支出为红色，收入为绿色
        if (bill.getType() == 0) { // 支出
            holder.tvAmount.setTextColor(context.getResources().getColor(R.color.expenseColor));
        } else { // 收入
            holder.tvAmount.setTextColor(context.getResources().getColor(R.color.incomeColor));
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(bill);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                onItemLongClickListener.onItemLongClick(bill);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return billList != null ? billList.size() : 0;
    }

    private String getTypeText(int type) {
        String[] types = context.getResources().getStringArray(R.array.bill_types);
        return type >= 0 && type < types.length ? types[type] : "未知";
    }

    private String getCategoryText(int type, int category) {
        String[] categories;
        if (type == 0) { // 支出
            categories = context.getResources().getStringArray(R.array.expense_categories);
        } else { // 收入
            categories = context.getResources().getStringArray(R.array.income_categories);
        }
        return category >= 0 && category < categories.length ? categories[category] : "未知";
    }

    public interface OnItemClickListener {
        void onItemClick(Bill bill);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Bill bill);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAmount;
        TextView tvType;
        TextView tvCategory;
        TextView tvNote;
        TextView tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvType = itemView.findViewById(R.id.tv_type);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvNote = itemView.findViewById(R.id.tv_note);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}
