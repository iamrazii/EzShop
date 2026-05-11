package com.example.ezshop.Activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ezshop.R;
import com.example.ezshop.models.InboxItem;
import com.example.ezshop.models.Message;
import com.example.ezshop.utilities.SessionManager; // 🔥 IMPORTED SESSION MANAGER
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ChatActivity extends AppCompatActivity {

    private LinearLayout llChatContainer;
    private ScrollView chatScrollView;
    private EditText etMessageInput;
    private ImageView btnSendMessage;

    private FirebaseFirestore db;
    private String currentUserId;
    private String targetSellerId;
    private String chatRoomId;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        llChatContainer = findViewById(R.id.llChatContainer);
        chatScrollView = findViewById(R.id.chatScrollView);
        etMessageInput = findViewById(R.id.etMessageInput);
        btnSendMessage = findViewById(R.id.btnSendMessage);

        db = FirebaseFirestore.getInstance();

        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();

        targetSellerId = getIntent().getStringExtra("CHAT_PARTNER_ID");

        if (targetSellerId == null || targetSellerId.isEmpty()) {
            targetSellerId = "admin_support";
        }

        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "Error: You must be logged in to chat.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Generate consistent room ID regardless of who starts the chat
        if (currentUserId.compareTo(targetSellerId) < 0) {
            chatRoomId = currentUserId + "_" + targetSellerId;
        } else {
            chatRoomId = targetSellerId + "_" + currentUserId;
        }

        listenForMessages();

        btnSendMessage.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String text = etMessageInput.getText().toString().trim();
        if (text.isEmpty()) return;

        long timestamp = System.currentTimeMillis();
        Message newMessage = new Message(currentUserId, targetSellerId, text, timestamp);

        db.collection("chats")
                .document(chatRoomId)
                .collection("messages")
                .add(newMessage)
                .addOnSuccessListener(documentReference -> {
                    etMessageInput.setText("");

                    InboxItem myInbox = new InboxItem(targetSellerId, text, timestamp);
                    InboxItem theirInbox = new InboxItem(currentUserId, text, timestamp);

                    db.collection("UserChats")
                            .document(currentUserId)
                            .collection("inbox")
                            .document(targetSellerId)
                            .set(myInbox);


                    db.collection("UserChats")
                            .document(targetSellerId)
                            .collection("inbox")
                            .document(currentUserId)
                            .set(theirInbox);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
                });
    }

    private void listenForMessages() {
        db.collection("chats")
                .document(chatRoomId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(ChatActivity.this, "Chat sync failed.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshot != null) {
                        llChatContainer.removeAllViews();
                        LayoutInflater inflater = LayoutInflater.from(ChatActivity.this);

                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Message msg = doc.toObject(Message.class);

                            if (msg != null) {
                                View bubbleView;

                                if (currentUserId.equals(msg.senderId)) {
                                    bubbleView = inflater.inflate(R.layout.item_chat_sent, llChatContainer, false);
                                    TextView tv = bubbleView.findViewById(R.id.tvSentMessage);
                                    tv.setText(msg.getMessageText());
                                } else {
                                    bubbleView = inflater.inflate(R.layout.item_chat_received, llChatContainer, false);
                                    TextView tv = bubbleView.findViewById(R.id.tvReceivedMessage);
                                    tv.setText(msg.getMessageText());
                                }

                                llChatContainer.addView(bubbleView);
                            }
                        }

                        chatScrollView.post(() -> chatScrollView.fullScroll(ScrollView.FOCUS_DOWN));
                    }
                });
    }
}