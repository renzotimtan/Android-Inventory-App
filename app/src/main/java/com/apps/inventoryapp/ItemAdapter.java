package com.apps.inventoryapp;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import javax.annotation.Nullable;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class ItemAdapter extends RealmRecyclerViewAdapter<Item, ItemAdapter.ViewHolder> {

    ItemInventory activity;

    public ItemAdapter(ItemInventory activity, @Nullable OrderedRealmCollection<Item> data, boolean autoUpdate) {
        super(data, autoUpdate);
        this.activity = activity;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView item_imageView;
        TextView Name;
        TextView Quantity;
        ImageButton item_addButton;
        ImageButton item_minusButton;
        Button item_detailsButton;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            item_imageView = itemView.findViewById(R.id.item_ImageView);
            Name = itemView.findViewById(R.id.Name);
            Quantity = itemView.findViewById(R.id.Quantity);
            item_addButton = itemView.findViewById(R.id.item_addButton);
            item_minusButton = itemView.findViewById(R.id.item_minusButton);
            item_detailsButton = itemView.findViewById(R.id.item_detailsButton);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        // Layout for ViewHolder is in item_row_layout.xml
        View v = activity.getLayoutInflater()
                .inflate(R.layout.item_row_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        // Get position, then set text in ViewHolder depending on position
        Item i = getItem(position);
        holder.Name.setText(i.getName());
        holder.Quantity.setText(String.valueOf(i.getQuantity()));

        //Picasso
        File savedImage = new File(i.getImage());
        Picasso.get()
                .load(savedImage)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(holder.item_imageView);

        // Set Tags
        holder.item_addButton.setTag(i);
        holder.item_minusButton.setTag(i);
        holder.item_detailsButton.setTag(i);

        //Listeners for image buttons
        holder.item_addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.add(view);
            }
        });

        holder.item_minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.subtract(view);
            }
        });

        holder.item_detailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.item_edit(view);
            }
        });

    }
}
