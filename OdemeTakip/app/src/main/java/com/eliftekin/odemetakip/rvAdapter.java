package com.eliftekin.odemetakip;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class rvAdapter extends RecyclerView.Adapter<rvAdapter.MyViewHolder> {

    ArrayList <data> dataArrayList;
    FirebaseFirestore firestore;

    String Tarih, VadeTarihi, Acıklama;

    data newdata;

    public rvAdapter(ArrayList<data> dataArrayList, FirebaseFirestore firestore) {
        this.dataArrayList = dataArrayList;
        this.firestore = firestore;
    }

    @NonNull
    @Override
    public rvAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.eklenen, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull rvAdapter.MyViewHolder holder, int position) {
        newdata = dataArrayList.get(position);
        holder.setData(newdata);
    }

    @Override
    public int getItemCount() {
        return dataArrayList != null ? dataArrayList.size() : 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tarih_c;
        TextView aciklama_c;
        TextView vadetarihi_c;
        TextView tutar_c;
        CheckBox durum_c;
        long Tutar;
        String id;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tarih_c = itemView.findViewById(R.id.tarih_c);
            aciklama_c = itemView.findViewById(R.id.aciklama_c);
            vadetarihi_c = itemView.findViewById(R.id.vadetarihi_c);
            tutar_c= itemView.findViewById(R.id.tutar_c);
            durum_c = itemView.findViewById(R.id.durum_c);

            durum_c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    newdata.setChecked(isChecked);
                    if (isChecked){
                        Tarih = tarih_c.getText().toString();
                        Acıklama = aciklama_c.getText().toString();
                        VadeTarihi = vadetarihi_c.getText().toString();

                        HashMap<String, Object> odenmisler = new HashMap<>();

                        odenmisler.put("Eklenildiği Tarih", Tarih);
                        odenmisler.put("Açıklama", Acıklama);
                        odenmisler.put("Vade Tarihi", VadeTarihi);
                        odenmisler.put("Tutar", Tutar);
                        odenmisler.put("Durum", true);

                        firestore.collection("ödenmişler").add(odenmisler).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });

                        firestore.collection("data").document(id).delete();

                    }

                }
            });
        }

        public void setData(data newdata) {
            this.tarih_c.setText(newdata.tarih);
            this.aciklama_c.setText(newdata.aciklama);
            this.vadetarihi_c.setText(newdata.vadetarihi);
            this.tutar_c.setText(newdata.tutar + " TL");
            this.durum_c.setChecked(newdata.isChecked());
            id = newdata.getDocumentId();
            Tutar = newdata.tutar;
        }

    }

}
