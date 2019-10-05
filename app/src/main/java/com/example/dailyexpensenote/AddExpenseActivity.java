package com.example.dailyexpensenote;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.dailyexpensenote.databinding.ActivityAddExpenseBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

public class AddExpenseActivity extends AppCompatActivity {
    private ActivityAddExpenseBinding binding;
    private ImageView backIV;
    private Spinner typeSpinner;
    private EditText amountET, dateET, timeET;
    private Button addDocument, addExpense, cancelAddBtn;
    private ImageView datePickBtn, timePickBtn;
    private ImageView documentImage;
    private Bitmap bitmap = null;
    private String documentURL = "";
    private Context context;

    private Calendar calendar;
    private int hour, minute;


    private LinearLayout cameraGalleryBtnField, camera, gallery, cancle;
    private String typeOfExpense;
    private DatabaseHelper helper;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_expense);
        init();
        back();
        process();


    }

    private void process() {


        final String[] typeExpense = {"Rent", "Food", "Utility bills", "Medicine", "Cloathing", "Transport", "Health", "Gift"};
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_activated_1, typeExpense);
        typeSpinner.setAdapter(arrayAdapter);

        typeOfExpense = typeExpense[0];
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                typeOfExpense = typeExpense[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //get date and time into dateET and Time ET

        binding.fDateTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                View view = getLayoutInflater().inflate(R.layout.custom_date_picker, null);

                Button done = view.findViewById(R.id.doneButton);
                final DatePicker datePicker = view.findViewById(R.id.datePickerDialogue);
                builder.setView(view);
                final Dialog dialog = builder.create();
                dialog.show();
                done.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int day = datePicker.getDayOfMonth();
                        int month = datePicker.getMonth();
                        month = month + 1;
                        int year = datePicker.getYear();

                        String cDate = year + "/" + month + "/" + day;
                        Date d = null;
                        try {
                            d = dateFormat.parse(cDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        String fdate = dateFormat.format(d);
                        binding.fromDateTV.setText(fdate);
                        dialog.dismiss();

                    }
                });


            }
        });
        //add Document  section
        binding.addImageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //request for permission on amera or gallery
                binding.cameraOrGallery.setVisibility(View.VISIBLE);

                binding.addImageLayout.setVisibility(View.GONE);

            }
        });

        //add value into database
        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getdate = binding.fromDateTV.getText().toString();
                DateValidate dv = new DateValidate();
                dv.matcher = Pattern.compile(dv.DATE_PATTERN).matcher(getdate);           //check valid Date from ValidDate class
                if (binding.amountET.getText().toString().equals("") || binding.fromDateTV.getText().toString().equals("")) {
                    if (binding.amountET.getText().toString().equals("")) {
                        binding.amountET.setError("please enter amount");
                        binding.amountET.requestFocus();
                    } else if (binding.fromDateTV.getText().toString().equals("")) {
                        binding.fromDateTV.setError("please enter date from date picker");
                        binding.fromDateTV.requestFocus();
                    }


                } else if (!dv.matcher.matches()) {
                    binding.fromDateTV.setError("Enter a valid Date format : yyyy/MM/dd");
                    binding.fromDateTV.requestFocus();
                    Toast.makeText(AddExpenseActivity.this, "Wrong Date format according to : yyyy/MM/dd", Toast.LENGTH_LONG).show();
                } else {
                    //add type,date,time,amount value into database
                    int uamount = Integer.valueOf(binding.amountET.getText().toString());
                    String userDate = binding.fromDateTV.getText().toString();
                    Date d = null;
                    try {
                        d = dateFormat.parse(userDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    long mdate = d.getTime();

                    if (bitmap != null) {
                        documentURL = encodeToBase64(bitmap, Bitmap.CompressFormat.JPEG, 100);
                    }


                    long id = helper.insertData(typeOfExpense, uamount, mdate, documentURL);

                    if (id == -1) {
                        Toast.makeText(AddExpenseActivity.this, "Error : Data  can not Inserted.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AddExpenseActivity.this, "Expense Data : " + id + " is Inserted.", Toast.LENGTH_SHORT).show();

                        //back to fragment when add expense complete
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));


                        //finish(); //need to add fragment for real time data change view


                    }


                }
            }
        });


       binding.camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,0);
            }
        });

        binding.gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent,1);
            }
        });

        binding.cancelImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.showImageLayout.setVisibility(View.GONE);
                binding.addImageLayout.setVisibility(View.VISIBLE);

            }
        });


    }


//get Result from camera gallery image

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == 0) {
                Bundle bundle = data.getExtras();
                bitmap = (Bitmap) bundle.get("data");
                binding.showImage.setImageBitmap(bitmap);
                binding.addImageLayout.setVisibility(View.GONE);


            } else if (requestCode == 1) {

                Uri uri = data.getData();
                bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                binding.showImage.setImageBitmap(bitmap);
                binding.showImageLayout.setVisibility(View.VISIBLE);
                binding.addImageLayout.setVisibility(View.GONE);
                binding.cameraOrGallery.setVisibility(View.GONE);
                //set Image into ImageView


            }

        }
    }


    private String encodeToBase64(Bitmap image, Bitmap.CompressFormat jpeg, int i) {

        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(jpeg, i, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }


    private void back() {
        binding.backIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }


    private void init() {
        calendar = Calendar.getInstance();
        hour = calendar.get(Calendar.HOUR);
        minute = calendar.get(Calendar.MINUTE);


        typeSpinner = findViewById(R.id.addActivityTypeSpinnerID);
//        amountET = findViewById(R.id.addActtivityexpenseAmountET);
//        dateET = findViewById(R.id.addActivityexpenseDateET);
//        timeET = findViewById(R.id.addActivityexpenseTimeET);
//        addDocument = findViewById(R.id.addActivityaAddDocumentButton);
//        addExpense = findViewById(R.id.addActivityAddExpenseBtn);
//        datePickBtn = findViewById(R.id.addActivityDatePickerBtn);
//        timePickBtn = findViewById(R.id.addActiviTimePickerBtn);
//        documentImage = findViewById(R.id.fileIV);
//        cameraGalleryBtnField = findViewById(R.id.cameraGalleryLLfield);
//        camera = findViewById(R.id.cameraBtnID);
//        gallery = findViewById(R.id.galleryBtnID);
//        cancle = findViewById(R.id.cencleButtonID);
//        cancelAddBtn = findViewById(R.id.addActivityCancelExpenseBtn);

        helper = new DatabaseHelper(this);

    }


}
