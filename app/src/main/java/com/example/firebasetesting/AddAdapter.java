package com.example.firebasetesting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AddAdapter extends RecyclerView.Adapter<AddAdapter.ViewHolder> {

    Context context;
    List<ShoppingItem> itemList;

    public AddAdapter(Context context, List<ShoppingItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, priceText, descriptionText;
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.name);
            priceText = itemView.findViewById(R.id.price);
            descriptionText = itemView.findViewById(R.id.description);
//            imageView = itemView.findViewById(R.id.img1);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.add_data, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ShoppingItem item = itemList.get(position);
        holder.nameText.setText(item.getName());
        holder.priceText.setText(item.getPrice());
        holder.descriptionText.setText(item.getDescription());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}

