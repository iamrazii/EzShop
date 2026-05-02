package com.example.ezshop.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezshop.Activities.ChatActivity;
import com.example.ezshop.models.InboxItem;
import com.example.ezshop.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.InboxViewHolder> {

    private Context context;
    private List<InboxItem> inboxList;

    public InboxAdapter(Context context, List<InboxItem> inboxList) {
        this.context = context;
        this.inboxList = inboxList;
    }

    @NonNull
    @Override
    public InboxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_inbox, parent, false);
        return new InboxViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InboxViewHolder holder, int position) {
        InboxItem currentItem = inboxList.get(position);

        holder.tvPartnerId.setText("Loading...");
        holder.tvLastMessage.setText(currentItem.lastMessage);

        FirebaseFirestore.getInstance().collection("users").document(currentItem.partnerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Assuming your User collection has a field called "name"
                        String realName = documentSnapshot.getString("name");
                        if (realName != null && !realName.isEmpty()) {
                            holder.tvPartnerId.setText(realName);
                        } else {
                            holder.tvPartnerId.setText("User");
                        }
                    } else {
                        holder.tvPartnerId.setText("Unknown User");
                    }
                });

        // 3. When the row is clicked, open the ChatActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("CHAT_PARTNER_ID", currentItem.partnerId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return inboxList.size();
    }

    public static class InboxViewHolder extends RecyclerView.ViewHolder {
        TextView tvPartnerId, tvLastMessage;

        public InboxViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPartnerId = itemView.findViewById(R.id.tvInboxPartnerId);
            tvLastMessage = itemView.findViewById(R.id.tvInboxLastMessage);
        }
    }
}