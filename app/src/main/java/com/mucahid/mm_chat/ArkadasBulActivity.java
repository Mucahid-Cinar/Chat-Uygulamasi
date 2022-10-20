package com.mucahid.mm_chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mucahid.mm_chat.Model.User;
import com.mucahid.mm_chat.adapter.ArkadasBulAdapter;

import java.util.ArrayList;
import java.util.Iterator;

public class ArkadasBulActivity extends AppCompatActivity {

    private Toolbar mmToolBar;
    private RecyclerView arkadasbulRecyclerView;

    private DatabaseReference kullaniciYolu;

    private ArrayList<User> userList;
    private ArkadasBulAdapter arkadasBulAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arkadas_bul);

        userList = new ArrayList<>();
        arkadasBulAdapter = new  ArkadasBulAdapter(getApplicationContext(), userList);
        kullaniciYolu = FirebaseDatabase.getInstance().getReference("Kullanicilar");

        arkadasbulRecyclerView = findViewById(R.id.arkadasbul_recyclerview);
        arkadasbulRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        arkadasbulRecyclerView.setAdapter(arkadasBulAdapter);

        mmToolBar = findViewById(R.id.arkadasbul_toolbar);
        setSupportActionBar(mmToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Arkadaş Bul");

        KullanicilariGetir();
    }

    private void KullanicilariGetir(){

        kullaniciYolu.addChildEventListener(new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Iterator iteretor = snapshot.getChildren().iterator();

                while (iteretor.hasNext()){
                    User user = new User(
                            (((DataSnapshot) iteretor.next()).getValue()).toString(),
                            (((DataSnapshot) iteretor.next()).getValue()).toString(),
                            (((DataSnapshot) iteretor.next()).getValue()).toString(),
                            (((DataSnapshot) iteretor.next()).getValue().toString()),
                            (((DataSnapshot) iteretor.next()).getValue().toString()),
                            (((DataSnapshot) iteretor.next()).getValue().toString()),
                            (((DataSnapshot) iteretor.next()).getValue().toString())
                    );
                    userList.add(user);
                    arkadasBulAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}