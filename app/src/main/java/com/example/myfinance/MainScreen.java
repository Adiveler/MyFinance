package com.example.myfinance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.myfinance.models.Payment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainScreen extends AppCompatActivity {
    ArrayList barArraylist;
    private RecyclerView recyclerView;
    MyAdapter myAdapter;
    final static String TAG = "My finance";
    DatabaseReference dbRef;
    FirebaseAuth auth;
    ArrayList<Payment> listPayments;
    String currentUserUid;
    String currentDateString;
    int totalHome = 0;
    int totalShop = 0;
    int totalFood = 0;
    int totalOther = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("payments").child("newPayment");
        currentUserUid = auth.getCurrentUser().getUid().toString();
        Button moveToPaymentScreen = (Button) findViewById(R.id.createNewPayment);
        Button moveToCalenderScreen = (Button) findViewById(R.id.calender);
        Date currentDate = new Date();
        currentDateString = new SimpleDateFormat("dd/MM/yyyy").format(currentDate);

        //date = currentDateString;
        Log.d(TAG, "onCreate: " + currentDateString);
        getUpdatedDate();
        Log.d(TAG, "onUpdate: " + currentDateString);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listPayments = new ArrayList<>();
        myAdapter = new MyAdapter(this, listPayments);
        recyclerView.setAdapter(myAdapter);

        getDataByDate(currentDateString);

        moveToPaymentScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainScreen.this, NewPaymentScreen.class);
                startActivity(intent);

            }
        });

        moveToCalenderScreen.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainScreen.this, CalendarActivity.class);
                startActivity(intent);

            }
        });

        barArraylist = new ArrayList<>();
        barArraylist.add(new BarEntry(1, totalFood));
        barArraylist.add(new BarEntry(2, totalHome));
        barArraylist.add(new BarEntry(3, totalShop));
        barArraylist.add(new BarEntry(4, totalOther));
        BarChart barChart = findViewById(R.id.chart);
        BarDataSet barDataSet = new BarDataSet(barArraylist, "my spendings");
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);

    }

    private void getDataByDate(String date) {
        dbRef.child(currentUserUid).orderByChild("date").equalTo(date).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Payment payment = dataSnapshot.getValue(Payment.class);
                    listPayments.add(payment);
                    String currentCategory = dataSnapshot.child("category").getValue().toString();

                    String costStr = dataSnapshot.child("cost").getValue().toString();
                    int cost = Integer.parseInt(costStr);

                    //Testing
                    Log.d(TAG, "Payment from Firebase:\n" + payment);
                    Log.d(TAG, "currentCategory child from Firebase:\n" + currentCategory);
                    Log.d(TAG, "cost child from Firebase:\n" + cost);

                    switch (currentCategory) {
                        case "food":
                            totalFood += cost;
                            break;
                        case "home":
                            totalHome += cost;
                            break;
                        case "shopping":
                            totalShop += cost;
                            break;
                        case "other":
                            totalOther += cost;
                            break;
                    }
                }
                Log.d(TAG, "onDataChange: "+ totalFood);
                barArraylist = new ArrayList<>();
                barArraylist.add(new BarEntry(1, totalFood));
                barArraylist.add(new BarEntry(2, totalHome));
                barArraylist.add(new BarEntry(3, totalShop));
                barArraylist.add(new BarEntry(4, totalOther));
                BarChart barChart = findViewById(R.id.chart);
                BarDataSet barDataSet = new BarDataSet(barArraylist, "my spendings");
                BarData barData = new BarData(barDataSet);
                barChart.setData(barData);
                barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                barDataSet.setValueTextColor(Color.BLACK);
//                barChart.getDescription().setEnabled(true);
                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Log.d(TAG, "new: "+ totalFood);
    }

    public void getUpdatedDate() {
        String newDate = CalendarActivity.getSelectedDate();
        if (newDate != currentDateString && newDate != null)
            currentDateString = newDate;
    }

}