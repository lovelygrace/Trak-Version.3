package subai.trak2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

//this contains the log in activity
public class LoginActivity extends AppCompatActivity{
    private static EditText busNumber;
    private Button login;
    private static DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

    UserSessionManager sessionManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);
        sessionManager = new UserSessionManager(this);
        busNumber = (EditText) findViewById(R.id.busNumText);
        login = (Button) findViewById(R.id.btnLogin);

        //checks if the user is logged in
        if (sessionManager.isUserLoggedIn()) {
            sessionManager.setPosition(sessionManager.getPosition());
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        //on click listener of the log in button
        //checks the validity of the inputted bus number
        login.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "CHECKING FOR VALIDITY", Toast.LENGTH_SHORT).show();
                checkBusNumber();

            }

        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void sendMessage() {
        DatabaseReference statRef = ref.child("Bus_Accounts").child(sessionManager.getBusNum());
        statRef.child("status").setValue("Online");
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    //checks if the user inputted a bus number, if the bus number is valid, etc
    public void checkBusNumber(){
        DatabaseReference busRef = ref.child("Bus_Accounts");
        busRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String bNum= busNumber.getText().toString();
                if(!bNum.isEmpty()){
                    if (!dataSnapshot.hasChild(bNum)) {
                        busNumber.setText("");
                        DialogFragment newFragment = AlertDialogFragment.newInstance("Please input a valid bus number.");
                        newFragment.show(getFragmentManager(), "dialog");
                    } else {
                        if(dataSnapshot.child(bNum).child("status").getValue().equals("Offline")) {
                            valid();
                        } else {
                            Toast.makeText(getApplicationContext(), "Invalid Login", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    DialogFragment newFragment = AlertDialogFragment.newInstance("Please input bus number.");
                    newFragment.show(getFragmentManager(), "dialog");
                    //Toast.makeText(getApplicationContext(), "PLEASE INPUT BUS NUMBER", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    //checks if the bus number is a registered/valid bus number in the database
    public void valid(){
        ref.child("Bus_Accounts").child(busNumber.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> d = dataSnapshot.getChildren();
                for (DataSnapshot data: d){
                    if (data.getKey().equals("busCompany")){
                        setBusCompany(data.getValue().toString());
                    }
                    if (data.getKey().equals("accommodation")){
                        setAccommodation(data.getValue().toString());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        sessionManager.setLoggedIn(true);
        sessionManager.setBusNumber(busNumber.getText().toString());
        sendMessage();
    }

    //sets the bus company based on the bus number
    public void setBusCompany(String company){
        sessionManager.setBusCompany(company);
    }

    //sets the accommodation of the bus
    public void setAccommodation(String acc) {
        sessionManager.setAccomodation(acc);
    }

    //returns the bus number inputted by the user
    public static String getBusNumber(){
        return busNumber.getText().toString();
    }
}