package com.apps.inventoryapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class UserAdapter extends RealmRecyclerViewAdapter<User, UserAdapter.ViewHolder> {
    UserAdmin activity;

    public UserAdapter(UserAdmin activity, @Nullable OrderedRealmCollection<User> data, boolean autoUpdate) {
        super(data, autoUpdate);
        this.activity = activity;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        TextView pass;
        ImageButton edit;
        ImageButton delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.admin_username);
            pass = itemView.findViewById(R.id.admin_password);
            edit = itemView.findViewById(R.id.admin_edit_button);
            delete = itemView.findViewById(R.id.admin_remove_button);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Layout for ViewHolder is in row_layout.xml
        View v = activity.getLayoutInflater()
                .inflate(R.layout.row_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get position, then set text in ViewHolder depending on position
        User u = getItem(position);
        holder.username.setText(u.getUsername());
        holder.pass.setText(u.getPassword());

        // Set tags
        holder.edit.setTag(u);
        holder.delete.setTag(u);

        // Add listener to edit ImageButton
        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Do edit function of UserAdmin
                activity.edit(view);

                // Go to EditUser activity on click
                Intent intent = new Intent(view.getContext(), UserEdit_.class);
                view.getContext().startActivity(intent);
            }
        });

        // Add listener to delete ImageButton
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                // Create dialog interface (Let's user confirm delete)
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){

                            // If yes is pressed
                            case DialogInterface.BUTTON_POSITIVE:
                                // Use delete function
                                activity.delete(view);
                                break;

                            // If no is pressed
                            case DialogInterface.BUTTON_NEGATIVE:
                                // Nothing happens
                                break;
                        }
                    }
                };

                // Create alert dialog builder
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage("Are you sure?")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });
    }
}
