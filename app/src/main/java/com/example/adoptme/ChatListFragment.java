package com.example.adoptme;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChatListFragment extends Fragment {

    public ChatListFragment() {
        super(R.layout.fragment_chat_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rvChats);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        ArrayList<ChatItem> demo = new ArrayList<>();
        demo.add(new ChatItem("Pie", "Hello Tomer 👋", "12:30"));
        demo.add(new ChatItem("Yuki", "I’m interested in adopting...", "11:05"));

        rv.setAdapter(new ChatAdapter(demo, item -> {
            // TODO: open chat details fragment
        }));


    }
}

