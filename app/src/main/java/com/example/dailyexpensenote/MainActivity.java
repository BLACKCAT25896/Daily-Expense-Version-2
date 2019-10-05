package com.example.dailyexpensenote;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.dailyexpensenote.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FloatingActionButton addExpenseFAB;
    private Spinner eTypeSpinner;
    String[] typeExpense;
    private String typeOfExpense;
    private int count = 0;
    private List<Expense> expenseList;
    private ExpenseAdapter adapterExpense;
    private DatabaseHelper helper;
    private Calendar calendar;
    private long  fdate,tdate;
    private String type;
    private int totAmount=0;
    int currentPosition=0;
    private int year, month, fromDay, toDay, hour, minute;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        init();

        addExpenseFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddExpenseActivity.class));
            }
        });

        try {
            initFiltardata();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            getdata();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            pullData();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void initFiltardata() throws ParseException{

        //get data from spinner and calender

        //set Type into spinner
        typeExpense = new String[]{"All", "Rent", "Food", "Utility bills", "Medicine", "Cloathing", "Transport", "Health", "Gift"};
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_activated_1, typeExpense);
        eTypeSpinner.setAdapter(arrayAdapter);

        typeOfExpense = typeExpense[0];


        eTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                typeOfExpense = typeExpense[position];
                type=typeExpense[currentPosition];
                if (count > 0) {                     //set listener to pull modyifiying data when second time data set
                    try {
                        getdata();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    try {
                        pullData();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        //customize date range within calander


        //date picker part
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        int indexMonth = month + 1;
        fromDay = 1;
        toDay = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR);
        minute = calendar.get(Calendar.MINUTE);


        //fromDate

        String nFromDate = year + "/" + indexMonth + "/" + fromDay;
        binding.fromDate.setText(nFromDate);


        final DatePickerDialog.OnDateSetListener fromDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int fromDay) {
                calendar.set(year, month, fromDay);
                String userFromDate = dateFormat.format(calendar.getTime());
                // Toast.makeText(context,userFromDate+" is selected",Toast.LENGTH_LONG).show();
                binding.fromDate.setText(userFromDate);

                if (count > 0) {                     //set listener to pull modyifiying data when second time data set
                    try {
                        getdata();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    try {
                        pullData();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }


            }
        };

        binding.fromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set Date as begining of the month
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, fromDateListener, year, month, fromDay);
                datePickerDialog.show();


            }
        });


        //toDate

        String nToDate = year + "/" + indexMonth + "/" + toDay;
        binding.toDate.setText(nToDate);


        final DatePickerDialog.OnDateSetListener toDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int toDay) {
                calendar.set(year, month, toDay);
                String userFromDate = dateFormat.format(calendar.getTime());
                binding.toDate.setText(userFromDate);

                if (count > 0) {                     //set listener to pull modyifiying data when second time data set
                    try {
                        getdata();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    try {
                        pullData();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }


            }
        };

        binding.toDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set Date as begining of the month

                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, toDateListener, year, month, toDay);
                datePickerDialog.show();


            }
        });


    }

    private void getdata() throws ParseException {

        //pull data from database and add to ArrayList using cursor
        Date d1 = dateFormat.parse(binding.toDate.getText().toString());
        Date d2 = dateFormat.parse(binding.toDate.getText().toString());
        long fromDate = d1.getTime();
        long toDate = d2.getTime();


        expenseList.clear();                    //clear previous data
        adapterExpense.notifyDataSetChanged();

        Cursor cursor = helper.showAllData();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(helper.COL_ID));
            String timeTo = cursor.getString(cursor.getColumnIndex(helper.COL_TIME));
            int amount = cursor.getInt(cursor.getColumnIndex(helper.COL_Amount));
            String type = cursor.getString(cursor.getColumnIndex(helper.COL_TYPE));
            long dateFromDB = cursor.getLong(cursor.getColumnIndex(helper.COL_Date));
            String dateTo = dateFormat.format(dateFromDB);
            count++;

            // Toast.makeText(context,"Check date : "+dateFromDB,Toast.LENGTH_LONG).show();

            if (typeOfExpense.equals(typeExpense[0]) && dateFromDB >= fromDate && dateFromDB <= toDate) {   //when selected All in types
                expenseList.add(new Expense(amount, type, dateTo, timeTo, id, null));  //add image documents next
                adapterExpense.notifyDataSetChanged();
            } else if (typeOfExpense.equals(type) && dateFromDB >= fromDate && dateFromDB <= toDate) {            //when selected specific in types
                expenseList.add(new Expense(amount, type, dateTo, timeTo, id, null));  //add image documents next
                adapterExpense.notifyDataSetChanged();
            } else {
                // expenseList.clear();               //when not found data according to filtering
                //adapterExpense.notifyDataSetChanged();
            }


        }


    }
    private void pullData() throws ParseException {
        //getTotal expense by  type from DataBase and set Total Expense
        Date d1=dateFormat.parse(binding.fromDate.getText().toString());
        Date d2=dateFormat.parse(binding.toDate.getText().toString());
        fdate=d1.getTime();
        tdate=d2.getTime();



        int dbAmount=0;
        Cursor cursor=helper.showAllData();
        while (cursor.moveToNext()){
            long dateFromDB=cursor.getLong(cursor.getColumnIndex(helper.COL_Date));
            String dbtype=cursor.getString(cursor.getColumnIndex(helper.COL_TYPE));
            count++;
            if(typeOfExpense.equals(typeExpense[0])&&dateFromDB>=fdate&&dateFromDB<=tdate){       //when selected All in types
                dbAmount=cursor.getInt(cursor.getColumnIndex(helper.COL_Amount));
                totAmount=totAmount+dbAmount;
            }
            else if(typeOfExpense.equals(dbtype)&&dateFromDB>=fdate&&dateFromDB<=tdate){          //when selected specific in types
                dbAmount=cursor.getInt(cursor.getColumnIndex(helper.COL_Amount));
                totAmount=totAmount+dbAmount;
            }

        }


        binding.totalAmount.setText(totAmount+"Tk");
        totAmount=0;
    }

    private void init() {
        expenseList = new ArrayList<>();
        addExpenseFAB = findViewById(R.id.addExpenseFAB);
        helper = new DatabaseHelper(this);
        adapterExpense = new ExpenseAdapter(this, expenseList, helper);
        eTypeSpinner = findViewById(R.id.showActivityTypeSpinnerID);

        binding.expenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.expenseRecyclerView.setAdapter(adapterExpense);


    }
}
