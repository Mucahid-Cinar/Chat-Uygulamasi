package com.mucahid.mm_chat.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mucahid.mm_chat.ArkadasBulActivity;
import com.mucahid.mm_chat.Mesaj;
import com.mucahid.mm_chat.Model.User;
import com.mucahid.mm_chat.ProfileActivity;
import com.mucahid.mm_chat.R;
import com.mucahid.mm_chat.databinding.ActivityProfileBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ArkadasBulAdapter extends RecyclerView.Adapter<ArkadasBulAdapter.MyViewHolder> {

    ArrayList<User> arkadasList;
    Context context;

    public ArkadasBulAdapter(Context context, ArrayList<User> userList) {
        this.arkadasList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.kullanici_ekle_layout,parent,false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        User user = arkadasList.get(position);

        holder.kullaniciAdi.setText(user.getKullaniciAdi());
        holder.kullaniciDurum.setText(user.getHakkimda());

        if (!user.getResim().isEmpty() && user.getResim() != null)
            Picasso.get().load(user.getResim()).into(holder.kullaniciProfilResim);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("kullaniciId", user.getUid());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arkadasList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView kullaniciAdi;
        public TextView kullaniciDurum;
        public ImageView kullaniciProfilResim;

        public MyViewHolder(View itemView) {
            super(itemView);

            kullaniciAdi = itemView.findViewById(R.id.kullanici_adi);
            kullaniciDurum = itemView.findViewById(R.id.kullanici_durumu);
            kullaniciProfilResim = itemView.findViewById(R.id.kullanici_profilresmi);
        }
    }
}
