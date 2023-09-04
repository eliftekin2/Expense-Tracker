package com.eliftekin.odemetakip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //firebase
    private FirebaseFirestore firestore;
    private Timestamp firestoreTimestamp;

    Context context = this;

    //ödenecekler recyclerview
    ArrayList<data> datalist;
    private RecyclerView recyclerView;
    private rvAdapter rvAdapter;

    //ödenmişler recyclerview
    ArrayList<data> odenmisler_datalist;
    private RecyclerView odenmisler_recyclerView;
    private  odenmisler_rvAdapter odenmisler_rvA;

    //tarih
    int gun;
    int ay;
    int yıl;
    String yenigun;
    String yeniay;

    //activity_main
    TextView tutar_0_15;
    TextView tutar_15_30;
    TextView tutar_30_60;
    TextView toplam_tutar;
    Button button;

    //dialog_penceresi
    TextView tarih;
    EditText vadetarihi;
    ImageView takvim;
    EditText tutar;
    EditText aciklama;
    Button kaydet;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firestore = FirebaseFirestore.getInstance(); //firestore nesnesi

        //Adaptörün bağlanması
        datalist = new ArrayList<>();
        recyclerView = findViewById(R.id.odenecekler_rv);
        rvAdapter = new rvAdapter(datalist, firestore);
        recyclerView.setAdapter(rvAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Ödenmişler rv için adaptörün bağlanması
        odenmisler_datalist = new ArrayList<>();
        odenmisler_recyclerView = findViewById(R.id.odenmisler_rv);
        odenmisler_rvA = new odenmisler_rvAdapter(odenmisler_datalist);
        odenmisler_recyclerView.setAdapter(odenmisler_rvA);
        odenmisler_recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //veri tabanından verileri alıp gösteren fonksiyon.
        getData();

        //ödenmiş verileri alıp gösteren fonksiyon
        odenmis_getData();

        //toplamları gösteren fonksiyon
        toplam();

        button = findViewById(R.id.button); //artı butonunu
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowDialog();
            }
        });

        //tarih işlemleri
        Calendar bugun = Calendar.getInstance(); //geçerli tarih ve zamanı alır
        gun = bugun.get(Calendar.DAY_OF_MONTH); //gün
        ay = bugun.get(Calendar.MONTH); //ay
        yıl = bugun.get(Calendar.YEAR); //yıl

        yenigun = String.format("%02d", gun); //gun tek basamaksa soluna 0 ekler
        yeniay = String.format("%02d", ay + 1); // ay tek basamaksa soluna 0 ekler

        tutar_0_15 = findViewById(R.id.tutar_0_15);
        tutar_15_30 = findViewById(R.id.tutar_15_30);
        tutar_30_60 = findViewById(R.id.tutar_30_60);
        toplam_tutar = findViewById(R.id.toplam_tutar);

    }

    private void toplam() {

        Date bugun = new Date();
        Long timestampbugun = bugun.getTime();

        firestore.collection("data").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if(error != null){
                    Toast.makeText(context, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }

                if (value != null) {
                    long toplam15 = 0;
                    long toplam15_30 = 0;
                    long toplam30_60 = 0;
                    long toplamgenel = 0;

                    for (DocumentSnapshot snapshot : value.getDocuments()){
                        com.google.firebase.Timestamp vadeTarihi = (com.google.firebase.Timestamp) snapshot.getData().get("Vade Tarihi");

                        if (vadeTarihi != null) {
                            Long timestampVadeTarihi = vadeTarihi.toDate().getTime();
                            long gunFarki = (timestampVadeTarihi - timestampbugun) / (24 * 60 * 60 * 1000);

                            long tutar = snapshot.getLong("Tutar"); // "Tutar" alanını alın

                            // Tarih aralığına göre belgeyi sınıflandırın ve toplamı güncelleyin
                            if (gunFarki <= 15) {
                                toplam15 += tutar;
                            } else if (gunFarki <= 30 && gunFarki > 15) {
                                toplam15_30 += tutar;
                            } else if (gunFarki <= 60 && gunFarki > 30) {
                                toplam30_60 += tutar;
                            }

                            toplamgenel += tutar;
                        }
                    }
                    tutar_0_15.setText(toplam15 + "TL");
                    tutar_15_30.setText(toplam15_30 + "TL");
                    tutar_30_60.setText(toplam30_60 + "TL");
                    toplam_tutar.setText(toplamgenel + "TL");

                }
            }
        });
    }

    private void ShowDialog() {
        final Dialog dialog= new Dialog(this); //dialog nesnesi oluşturur

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //başlığı gizler
        dialog.setContentView(R.layout.dialog_penceresi); //layoutu tanımlar
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); //genişlik yükseklik
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //arkaplanı saydam yapar
        dialog.getWindow().getAttributes().windowAnimations =R.style.dialogAnimation; //animasyonu tanımlar
        dialog.getWindow().setGravity(Gravity.BOTTOM); //pencerenin yerini ayarlar

        dialog.show(); //pencereyi gösterir

        tarih = dialog.findViewById(R.id.tarih); //bugünün tarihini gösteren textview
        aciklama = dialog.findViewById(R.id.aciklama); //açıklamanın girildiği edittext
        tutar = dialog.findViewById(R.id.tutar); //tutarın girildiği edittext
        vadetarihi = dialog.findViewById(R.id.vadetarihi); //seçilen tarihin gösterileceği edittext
        takvim = dialog.findViewById(R.id.takvim); //takvim ikonu
        kaydet = dialog.findViewById(R.id.kaydet); //kaydet butonu

        tarih.setText(yenigun+"/"+yeniay+"/"+yıl); //bugünün tarihini textview'de gösterir

        //takvim ikonuna basıldığında çalışır
        takvim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tarih();
            }
        });

        kaydet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //tarihleri ve açıklamayı string'e dönüştürüp verileri değişkenlere aktarır
                String Tarih = tarih.getText().toString();
                String VadeTarihi = vadetarihi.getText().toString();
                String Acıklama = aciklama.getText().toString();

                if(Tarih.isEmpty() || VadeTarihi.isEmpty() || tutar.getText().toString().isEmpty() || Acıklama.isEmpty()){
                    Toast.makeText(context, "Lütfen Tüm Alanları Doldurun.", Toast.LENGTH_SHORT).show();
                }

                else {
                    int Tutar = Integer.parseInt(String.valueOf(tutar.getText()));

                    HashMap <String, Object> data = new HashMap<>();
                    data.put("Eklenildiği Tarih", Tarih);
                    data.put("Açıklama", Acıklama);
                    data.put("Vade Tarihi", firestoreTimestamp);
                    data.put("Tutar", Tutar);
                    data.put("Durum", false);

                    firestore.collection("data").add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(context, "Kaydedildi", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }
            }
        });
    }

    private void odenmis_getData() {
        firestore.collection("ödenmişler").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    Toast.makeText(context, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }

                if (value != null){
                    odenmisler_datalist.clear();

                    for (DocumentSnapshot snapshot : value.getDocuments()){
                        Map<String, Object> odenmislerData = snapshot.getData();

                        String aciklama = (String) odenmislerData.get("Açıklama");
                        String tarih = (String) odenmislerData.get("Eklenildiği Tarih");
                        long tutar = (Long) odenmislerData.get("Tutar");
                        String vadeTarihi = (String) odenmislerData.get("Vade Tarihi");
                        boolean durum = (boolean) odenmislerData.get("Durum");

                        String id = snapshot.getId();

                        data alınanveri = new data(aciklama, tarih, vadeTarihi, tutar, durum, id);
                        odenmisler_datalist.add(alınanveri);
                    }
                    odenmisler_rvA.notifyDataSetChanged();
                }
            }
        });
    }

    private void getData(){
        firestore.collection("data").orderBy("Vade Tarihi").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    Toast.makeText(context, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }

                if(value != null){
                    datalist.clear();

                    for(DocumentSnapshot snapshot : value.getDocuments()){
                        Map<String, Object> data = snapshot.getData();

                        com.google.firebase.Timestamp ts = (com.google.firebase.Timestamp) data.get("Vade Tarihi");
                        Date date = ts.toDate();

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        String dateString = sdf.format(date);

                        String aciklama = (String) data.get("Açıklama");
                        String tarih = (String) data.get("Eklenildiği Tarih");
                        long tutar = (long) data.get("Tutar");
                        boolean durum = (boolean) data.get("Durum");

                        String id = snapshot.getId();

                        //alınan verileri arrayliste aktarır.
                        data alınanveri = new data(aciklama, tarih, dateString , tutar, durum, id);
                        datalist.add(alınanveri);
                    }
                    rvAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    private void tarih() {
        DatePickerDialog tarihsec = new DatePickerDialog(context,
                new DatePickerDialog.OnDateSetListener() { //tarih seçildiğinde tetiklenecek olaylar
                    @Override
                    public void onDateSet(DatePicker datePicker, int yıl, int ay, int gun) {
                        //seçilen tarihi calendar cinsine dönüştürür
                        Calendar secilentarih = Calendar.getInstance();
                        secilentarih.set(yıl, ay, gun);

                        Calendar bugununtarihi = Calendar.getInstance();

                        if (secilentarih.before(bugununtarihi))
                        {
                            Toast.makeText(context, "Geçmiş bir tarih seçemezsiniz.", Toast.LENGTH_SHORT).show();
                        }

                        else {
                            String yenigun = String.format("%02d", gun);
                            String yeniay = String.format("%02d", ay + 1);

                            vadetarihi.setText(yenigun+"/"+yeniay+"/"+yıl);

                            Date date = secilentarih.getTime();
                            firestoreTimestamp = new Timestamp(date.getTime());
                        }
                    }
                },yıl, ay, gun);

        tarihsec.show();
    }
}