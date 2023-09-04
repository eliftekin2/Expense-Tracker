package com.eliftekin.odemetakip;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class odenmisler_rvAdapter extends RecyclerView.Adapter<odenmisler_rvAdapter.MyViewHolder> {

    private ArrayList<data> checkedItems;

    public odenmisler_rvAdapter(ArrayList<data> checkedItems) {
        this.checkedItems = checkedItems;
    }

    @NonNull
    @Override
    public odenmisler_rvAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.odenmisler,parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull odenmisler_rvAdapter.MyViewHolder holder, int position) {
        data checkedData = checkedItems.get(position);
        holder.setData(checkedData);

    }

    @Override
    public int getItemCount() {
        return checkedItems != null ? checkedItems.size() : 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tarih_o;
        TextView aciklama_o;
        TextView vadetarihi_o;
        TextView tutar_o;

        public MyViewHolder(View itemView) {
            super(itemView);
            tarih_o = itemView.findViewById(R.id.tarih_o);
            aciklama_o = itemView.findViewById(R.id.aciklama_o);
            aciklama_o.setPaintFlags(aciklama_o.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            vadetarihi_o = itemView.findViewById(R.id.vadetarihi_o);
            tutar_o = itemView.findViewById(R.id.tutar_o);
        }

        public void setData(data checkedData) {
            this.tarih_o.setText(checkedData.tarih);
            this.aciklama_o.setText(checkedData.aciklama);
            this.vadetarihi_o.setText(checkedData.vadetarihi);
            this.tutar_o.setText(checkedData.tutar + " TL");
        }
    }
}
