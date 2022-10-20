package com.mucahid.mm_chat.adapter;


import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mucahid.mm_chat.Mesaj;
import com.mucahid.mm_chat.OzelMesaj;
import com.mucahid.mm_chat.databinding.KullaniciMesajBinding;
import com.mucahid.mm_chat.databinding.MesajBinding;
import com.mucahid.mm_chat.databinding.OzelMesajAlanBinding;
import com.mucahid.mm_chat.databinding.OzelMesajGonderenBinding;

import java.util.ArrayList;

public class OzelMesajAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        ArrayList<OzelMesaj> ozelMesajList;
        String myId ="";

        public OzelMesajAdapter(ArrayList<OzelMesaj> ozelMesajList, String myId) {
            this.ozelMesajList = ozelMesajList;
            this.myId = myId;
        }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        OzelMesajAlanBinding aliciBinding = OzelMesajAlanBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        OzelMesajGonderenBinding gonderenBinding = OzelMesajGonderenBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        if(viewType==1){

            return new GonderenHolder(gonderenBinding);

        }
        else {
            return new AlanHolder(aliciBinding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        OzelMesaj ozelMesaj = ozelMesajList.get(position);
        if(myId.equals(ozelMesaj.getKimden())){

            ((GonderenHolder)holder).bind(ozelMesaj);

        }
        else {
            ((AlanHolder)holder).bind(ozelMesaj);
        }

    }

    @Override
    public int getItemViewType(int position) {
        OzelMesaj ozelMesaj = ozelMesajList.get(position);
        if(ozelMesaj.getKimden().equals(myId)){

            return 1;

        }
        else {

            return 2;
        }

    }

    @Override
        public int getItemCount() {
            return ozelMesajList.size();
        }

    private static class AlanHolder extends RecyclerView.ViewHolder {

        private final OzelMesajAlanBinding alanBinding;

        public AlanHolder(OzelMesajAlanBinding alanBinding) {
            super(alanBinding.getRoot());
            this.alanBinding = alanBinding;
        }
        public void bind(OzelMesaj ozelMesaj){

            alanBinding.mesajAlici.setText(ozelMesaj.getMesaj());
            alanBinding.zaman.setText(ozelMesaj.getZaman());

        }
    }

    private static class GonderenHolder extends RecyclerView.ViewHolder {

        private final OzelMesajGonderenBinding gonderenBinding;

        public GonderenHolder(OzelMesajGonderenBinding gonderenBinding) {
        super(gonderenBinding.getRoot());
        this.gonderenBinding = gonderenBinding;
        }
        public void bind(OzelMesaj ozelMesaj){

            gonderenBinding.mesajGonderen.setText(ozelMesaj.getMesaj());
            gonderenBinding.zaman.setText(ozelMesaj.getZaman());

        }
    }
}

