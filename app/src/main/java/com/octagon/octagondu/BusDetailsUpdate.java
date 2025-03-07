package com.octagon.octagondu;

import android.app.TimePickerDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class BusDetailsUpdate extends AppCompatActivity {
    private Spinner spinnerBusName;
    private Spinner spinnerBusType;
    private TextView textViewBusId;
    private ImageView imageViewTime;
    private TextView textViewRouteSt;
    private TextView textViewRoute;
    private TextView viewtime;
    private String inputTime;
    private String pastId;
    private String busType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_details_update);
        /*Toolbar*/
        MaterialToolbar detailsBusToolbar = findViewById(R.id.toolbar);
        detailsBusToolbar.setTitle("Update Bus Details");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            Drawable blackArrow = ContextCompat.getDrawable(this, R.drawable.baseline_arrow_back_24);
            actionBar.setHomeAsUpIndicator(blackArrow);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        // Initialize your form fields with the received data
        String busId = getIntent().getStringExtra("busId");
        String busName = getIntent().getStringExtra("busName");
        busType = getIntent().getStringExtra("busType");
        String destinationLocation = getIntent().getStringExtra("destinationLocation");
        String startLocation = getIntent().getStringExtra("startLocation");
        String time = getIntent().getStringExtra("time");
        pastId = busId;

        spinnerBusName = findViewById(R.id.spinnerBusName);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                android.R.id.text1
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add(busName);
        spinnerBusName.setAdapter(adapter);

        spinnerBusName = findViewById(R.id.spinnerBusName);
        spinnerBusType = findViewById(R.id.spinnerBusType);
        textViewBusId = findViewById(R.id.textViewBusId);
        imageViewTime = findViewById(R.id.setTime);
        textViewRouteSt = findViewById(R.id.textViewRouteSt);
        textViewRoute = findViewById(R.id.textViewRoute);
        viewtime = findViewById(R.id.viewTime);

        // Set the received data to the form fields
        textViewBusId.setText(busId);
        textViewRouteSt.setText(startLocation);
        textViewRoute.setText(destinationLocation);
        viewtime.setText(time);
        if (busType.equals("Up")) spinnerBusType.setSelection(0);
        else spinnerBusType.setSelection(1);
        inputTime = time; // Set the received time as the default value

        // Set click listener for the Submit button
        Button buttonSubmit = findViewById(R.id.buttonSubmit);
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String busName = spinnerBusName.getSelectedItem().toString();
                String busType = spinnerBusType.getSelectedItem().toString();
                String busId = textViewBusId.getText().toString();
                String time = inputTime;
                String startLocation = textViewRouteSt.getText().toString();
                String destinationLocation = textViewRoute.getText().toString();

                if (!busId.isEmpty() && !time.isEmpty() && !startLocation.isEmpty() && !destinationLocation.isEmpty()) {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    deleteBus(busName, pastId, databaseReference);
                } else {
                    showToast("Please Fill Completely");
                }
            }
        });
    }

    public void showTimePickerDialog(View view) {
        // Get the current time
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Create a TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Update the selected time in the TextView
                        BusDetailsUpdate.this.inputTime = String.format("%02d:%02d", hourOfDay, minute);
                        viewtime.setText(BusDetailsUpdate.this.inputTime);
                    }
                },
                hour,
                minute,
                true
        );

        // Show the dialog
        timePickerDialog.show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void deleteBus(String busName, String pastId, DatabaseReference databaseReference) {
        String pathToDelete = "Bus Schedule/" + busName + "/" + busType + pastId;

        databaseReference.child(pathToDelete).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Data deleted successfully, now insert the new data
                            insertBus(busName, pastId);
                        } else {
                            showToast("Failed to Delete Data");
                        }
                    }
                });
    }

    private void insertBus(String busName, String pastId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        InfoBusDetails infoBusDetails = new InfoBusDetails(busName, spinnerBusType.getSelectedItem().toString(),
                textViewBusId.getText().toString(), textViewRouteSt.getText().toString(), textViewRoute.getText().toString(), viewtime.getText().toString());

        String pathToInsert = "Bus Schedule/" + busName + "/" + busType + pastId;

        databaseReference.child(pathToInsert).setValue(infoBusDetails)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Data inserted successfully
                            showToast("Successfully Submitted Response");
                            finish();
                        } else {
                            // Failed to insert data
                            showToast("Failed to Insert Data");
                        }
                    }
                });
    }
}
