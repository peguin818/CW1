package com.comp1786.cw1.hikeDetails;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.comp1786.cw1.Entity.Hike;
import com.comp1786.cw1.HikeList.HikeListActivity;
import com.comp1786.cw1.R;
import com.comp1786.cw1.constant.Difficulty;
import com.comp1786.cw1.constant.TrailType;
import com.comp1786.cw1.dbHelper.HikeDbHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HikeEditForm extends AppCompatActivity {

    final Calendar myCalendar = Calendar.getInstance();
    EditText editHikeName;
    EditText editLocation;
    EditText editDate;
    EditText editLength;
    EditText editEContact;
    EditText editDescription;
    RadioGroup groupParking;
    RadioButton radioButtonPark;
    RadioGroup groupDifficulty;
    RadioButton radioButtonDifficulty;
    Spinner spinnerType;
    Button saveButton;
    Long parsedLength;
    private HikeDbHelper hikeDbHelper;
    private Hike hike;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hike_edit_form);

        editHikeName = findViewById(R.id.editHikeName);
        editLocation = findViewById(R.id.editLocation);
        editDate = findViewById(R.id.editDate);
        groupParking = findViewById(R.id.rGroupParking);
        editLength = findViewById(R.id.editLength);
        spinnerType = findViewById(R.id.spinType);
        groupDifficulty = findViewById(R.id.rGroupDifficulty);
        editEContact = findViewById(R.id.editEContact);
        editDescription = findViewById(R.id.editDescription);
        btnBack= findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //extract data form list
        Bundle extras = getIntent().getExtras();
        long id = 0;
        if (extras != null) {
            id = extras.getLong("DATA");
        }

        //get Hike from database
        hikeDbHelper = new HikeDbHelper(getApplicationContext());
        try {
            hike = hikeDbHelper.getHikeById(id);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        editHikeName.setText(hike.getHikeName());
        editLocation.setText(hike.getLocation());
        editDate.setText(hike.getDate());
        editLength.setText(String.valueOf(hike.getLength()));
        editEContact.setText(hike.getContact());
        editDescription.setText(hike.getDescription());

        //set Parking
        if(hike.isParking() == true){
            radioButtonPark = findViewById(R.id.radioParkYes);
        }else if(hike.isParking() == false){
            radioButtonPark = findViewById(R.id.radioParkNo);
        }
        radioButtonPark.setChecked(true);

        //set difficulty
        if(hike.getDifficulty() == Difficulty.EASY){
            radioButtonDifficulty = findViewById(R.id.radioEasy);
        }else if(hike.getDifficulty() == Difficulty.NORMAL){
            radioButtonDifficulty = findViewById(R.id.radioNormal);
        }else if(hike.getDifficulty() == Difficulty.HARD){
            radioButtonDifficulty = findViewById(R.id.radioHard);
        }

        radioButtonDifficulty.setChecked(true);


        //gets current date
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, day);
                updateDateLabel();
            }
        };
        editDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(HikeEditForm.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // set Trail Type
        spinnerType = findViewById(R.id.spinType);
        TrailType[] items = TrailType.values();
        String[] typeStrings = new String[items.length];
        for (int i = 0; i < items.length; i++) {
            typeStrings[i] = items[i].name().replace("_", " ");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, typeStrings);
        spinnerType.setAdapter(adapter);
        int spinnerPosition = adapter.getPosition(hike.getType().toString().replace("_", " "));
        spinnerType.setSelection(spinnerPosition);


        saveButton = findViewById(R.id.btnDeleteHike);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    saveDetails();
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    private void saveDetails() throws ParseException, IllegalAccessException {

        HikeDbHelper hikeDbHelper = new HikeDbHelper(getApplicationContext());
        Hike updatedHike = new Hike();

        updatedHike.setId(this.hike.getId());
        updatedHike.setHikeName(editHikeName.getText().toString());
        updatedHike.setLocation(editLocation.getText().toString());
        updatedHike.setDate(editDate.getText().toString());
        updatedHike.setDescription(editDescription.getText().toString());
        updatedHike.setContact(editEContact.getText().toString());

        boolean hasError = false;

        if (!verifyBlankEditText(editHikeName, editLocation, editDate, editLength, editDescription, editEContact)) {
            hasError = true;
        }

        int selectedParkId = groupParking.getCheckedRadioButtonId();
        radioButtonPark = findViewById(selectedParkId);
        if (radioButtonPark != null) {
            if (radioButtonPark.getId() == R.id.radioParkYes) {
                updatedHike.setParking(true);
            } else if (radioButtonPark.getId() == R.id.radioParkNo) {
                updatedHike.setParking(false);
            }
        } else {
            Toast.makeText(this, "Please select at least an option for Parking Availability", Toast.LENGTH_LONG).show();
            hasError = true;
        }

        if (spinnerType.getSelectedItem().toString().equals(TrailType.RETURN.toString())) {
            updatedHike.setType(TrailType.RETURN);
        } else if (spinnerType.getSelectedItem().toString().equals(TrailType.LOOP.toString())) {
            updatedHike.setType(TrailType.LOOP);
        } else if (spinnerType.getSelectedItem().toString().equals(TrailType.PACK_CARRY.toString().replace("_", " "))) {
            updatedHike.setType(TrailType.PACK_CARRY);
        } else if (spinnerType.getSelectedItem().toString().equals(TrailType.STAGE.toString())) {
            updatedHike.setType(TrailType.STAGE);
        } else if (spinnerType.getSelectedItem().toString().equals(TrailType.POINT_TO_POINT.toString().replace("_", " "))) {
            updatedHike.setType(TrailType.POINT_TO_POINT);
        } else {
            Toast.makeText(this, "Please select at least an option for Trail Type", Toast.LENGTH_LONG).show();
            hasError = true;
        }

        int selectedDifficultyId = groupDifficulty.getCheckedRadioButtonId();
        radioButtonDifficulty = findViewById(selectedDifficultyId);
        if (radioButtonDifficulty != null) {
            if (radioButtonDifficulty.getId() == R.id.radioHard) {
                updatedHike.setDifficulty(Difficulty.HARD);
            } else if (radioButtonDifficulty.getId() == R.id.radioNormal) {
                updatedHike.setDifficulty(Difficulty.NORMAL);
            } else if (radioButtonDifficulty.getId() == R.id.radioEasy) {
                updatedHike.setDifficulty(Difficulty.EASY);
            }
        } else {
            Toast.makeText(this, "Please select at least an option for Difficulty", Toast.LENGTH_LONG).show();
            hasError = true;
        }

        try {
            parsedLength = Long.parseUnsignedLong(editLength.getText().toString());
            updatedHike.setLength(parsedLength);
        } catch (Exception e) {
            editLength.setError("Please enter a number higher than 0");
        }

        if (!hasError) {
            boolean updateStatus = hikeDbHelper.updateHike(updatedHike);
            if(updateStatus){
                Toast.makeText(this, "Updated successfully with id: " + hike.getId(), Toast.LENGTH_LONG).show();
                Intent i = new Intent(this, HikeListActivity.class);
                startActivity(i);
            }

        }
    }

    private void updateDateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.ROOT);
        editDate.setText(dateFormat.format(myCalendar.getTime()).toString());
    }

    private boolean verifyBlankEditText(EditText... editText) {
        boolean result = true;

        for (EditText text : editText) {
            if (TextUtils.isEmpty(text.getText().toString().trim())) {
                text.setError("Field cannot be empty");
                result = false;
            }
        }
        return result;
    }
}