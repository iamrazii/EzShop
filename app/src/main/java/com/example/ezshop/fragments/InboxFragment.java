package com.example.ezshop.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezshop.adapters.InboxAdapter;
import com.example.ezshop.models.InboxItem;
import com.example.ezshop.R;
import com.example.ezshop.utilities.SessionManager; // 🔥 IMPORTED SESSION MANAGER

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class InboxFragment extends Fragment {

    private RecyclerView rvInbox;
    private List<InboxItem> inboxList;
    private InboxAdapter adapter;

    private FirebaseFirestore db;
    private String currentUserId;
    private SessionManager sessionManager; // Added variable

    public InboxFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inbox, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvInbox = view.findViewById(R.id.rvInbox);
        rvInbox.setLayoutManager(new LinearLayoutManager(getContext()));

        inboxList = new ArrayList<>();
        adapter = new InboxAdapter(getContext(), inboxList);
        rvInbox.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        sessionManager = new SessionManager(requireContext());
        currentUserId = sessionManager.getUserId();

        if (currentUserId != null && !currentUserId.isEmpty()) {
            loadInbox();
        } else {
            Toast.makeText(getContext(), "Error: Not logged in.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadInbox() {
        db.collection("UserChats")
                .document(currentUserId)
                .collection("inbox")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Failed to load inbox.", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    if (snapshot != null) {
                        inboxList.clear();

                        // Loop through all inbox items returned by Firestore
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            InboxItem item = doc.toObject(InboxItem.class);
                            if (item != null) {
                                inboxList.add(item);
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }
                });
    }
}