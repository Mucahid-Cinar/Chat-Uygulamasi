package com.mucahid.mm_chat;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mucahid.mm_chat.databinding.KullaniciMesajBinding;
import com.mucahid.mm_chat.databinding.MesajBinding;

import java.util.ArrayList;

public class MesajAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        ArrayList<Mesaj> mesajList;
        String kullaniciId ="";

        public MesajAdapter(ArrayList<Mesaj> mesajList) {
            this.mesajList = mesajList;
        }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MesajBinding mesajBinding = MesajBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        KullaniciMesajBinding kullaniciMesajBinding = KullaniciMesajBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        if(viewType==1){

            return new KullaniciMesajHolder(kullaniciMesajBinding);

        }
        else {
            return new MesajHolder(mesajBinding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Mesaj mesaj = mesajList.get(position);
        if(kullaniciId.equals(mesaj.kullaniciId)){

            ((KullaniciMesajHolder)holder).bind(mesaj);

        }
        else {
            ((MesajHolder)holder).bind(mesaj);
        }

    }

    @Override
    public int getItemViewType(int position) {
        Mesaj mesaj = mesajList.get(position);
        if(mesaj.kullaniciId.equals(kullaniciId)){

            return 1;

        }
        else{

            return 2;

        }
    }

    @Override
        public int getItemCount() {
            return mesajList.size();
        }

        private static class MesajHolder extends RecyclerView.ViewHolder {

            private final MesajBinding binding;

            public MesajHolder(MesajBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
            public void bind(Mesaj mesaj){

                binding.kullaniciAdi.setText(mesaj.kullaniciAdi);
                binding.mesaj.setText(mesaj.mesaj);
                binding.tarih.setText(mesaj.zaman);

            }
        }


    private static class KullaniciMesajHolder extends RecyclerView.ViewHolder {

        private final KullaniciMesajBinding bindingKullanici;

        public KullaniciMesajHolder(KullaniciMesajBinding bindingKullanici) {
        super(bindingKullanici.getRoot());
        this.bindingKullanici = bindingKullanici;
        }
        public void bind(Mesaj mesaj){

            bindingKullanici.mesaj.setText(mesaj.mesaj);
            bindingKullanici.tarih.setText(mesaj.zaman);

        }
    }
}

