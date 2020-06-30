package com.example.ex7;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ex7.data.SetUserPrettyNameRequest;
import com.example.ex7.data.Ticket;
import com.example.ex7.data.TokenResponse;
import com.example.ex7.data.User;
import com.example.ex7.data.UserResponse;
import com.example.ex7.server.MyOfficeServerInterface;
import com.example.ex7.server.ServerHolder;
import com.example.ex7.work.ConnectivityCheckWorker;
import com.example.ex7.work.CreateNewTicketWorker;
import com.example.ex7.work.GetUserWorker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.io.IOException;
import java.util.List;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String USER_ID = "3";
    private static String TAG = "MainActivity";
    public static String token;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handleUserName();


        checkConnectivityAndSetUI();
        getUser();
        createSampleTicket();

    }

    private void handleUserName() {
        if (checkIfFirstTimeInApp()) {
            setFirstTimeToFalse();
            askForUserName();
        }
    }



    private void askForUserName() {
        startActivity(new Intent(MainActivity.this, UserNameDialog.class));
    }

    private boolean checkIfFirstTimeInApp() {
        final SharedPreferences reader = getApplicationContext().getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        return reader.getBoolean("isFirstRun", true);
    }

    private void setFirstTimeToFalse() {
        final SharedPreferences reader = getApplicationContext().getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        final SharedPreferences.Editor preferencesEditor = reader.edit();
        preferencesEditor.putBoolean("isFirstRun", false);
        preferencesEditor.apply();
    }

    private String getUserNameFromPreferences() {
        final SharedPreferences reader = getApplicationContext().getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        return reader.getString("userName", "");
    }


    private void checkConnectivityAndSetUI() {

        OneTimeWorkRequest checkConnectivityWork = new OneTimeWorkRequest.Builder(ConnectivityCheckWorker.class)

                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                // if we will remove the constraints - then the connectivity check will happen immediately
                // if we will add the constraints - then the connectivity check will happen only after we have access to the internet

                .build();

        Operation runningWork = WorkManager.getInstance().enqueue(checkConnectivityWork);

        runningWork.getState().observe(this, new Observer<Operation.State>() {
            @Override
            public void onChanged(Operation.State state) {
                if (state == null) return;

                if (state instanceof Operation.State.SUCCESS) {
                    // update UI - connected
                } else {
                    // update UI - not connected :(
                }
            }
        });
    }

    private void getUser() {
        UUID workTagUniqueId = UUID.randomUUID();
        OneTimeWorkRequest checkConnectivityWork = new OneTimeWorkRequest.Builder(GetUserWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(new Data.Builder().putString("key_user_id", getUserNameFromPreferences()).putString("token", this.token).build())
                .addTag(workTagUniqueId.toString())
                .build();

        WorkManager.getInstance().enqueue(checkConnectivityWork);

        WorkManager.getInstance().getWorkInfosByTagLiveData(workTagUniqueId.toString()).observe(this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                // we know there will be only 1 work info in this list - the 1 work with that specific tag!
                // there might be some time until this worker is finished to work (in the mean team we will get an empty list
                // so check for that
                if (workInfos == null || workInfos.isEmpty())
                    return;

                WorkInfo info = workInfos.get(0);

                // now we can use it
                String userAsJson = info.getOutputData().getString("key_output_user");
                Log.d(TAG, "got user: " + userAsJson);

                User user = new Gson().fromJson(userAsJson, User.class);
                // update UI with the user we got
                TextView welcomeTextView = findViewById(R.id.tv_welcome);
                if (user.pretty_name != "" && user.pretty_name != null) {
                    welcomeTextView.setText("Welcome again, " + user.pretty_name);
                } else {
                    welcomeTextView.setText("Welcome, " + getUserNameFromPreferences());
                }
            }
        });
    }

    private void getAllTicketsForUser() {
        UUID workTagUniqueId = UUID.randomUUID();
        OneTimeWorkRequest checkConnectivityWork = new OneTimeWorkRequest.Builder(GetUserWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(new Data.Builder().putString("key_user_id", USER_ID).build())
                .addTag(workTagUniqueId.toString())
                .build();

        WorkManager.getInstance().enqueue(checkConnectivityWork);

        WorkManager.getInstance().getWorkInfosByTagLiveData(workTagUniqueId.toString()).observe(this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                // we know there will be only 1 work info in this list - the 1 work with that specific tag!
                // there might be some time until this worker is finished to work (in the mean team we will get an empty list
                // so check for that
                if (workInfos == null || workInfos.isEmpty())
                    return;

                WorkInfo info = workInfos.get(0);

                // now we can use it
                String ticketsAsJson = info.getOutputData().getString("key_output_tickets");
                List<Ticket> allTickets = new Gson().fromJson(ticketsAsJson, new TypeToken<List<Ticket>>() {
                }.getType());

                Log.d(TAG, "got tickets list with size " + allTickets.size());


                // update UI with the list we got
            }
        });
    }


    private void createSampleTicket() {
        Ticket ticket = new Ticket();
        ticket.id = 0;
        ticket.user_id = Integer.valueOf(USER_ID);
        ticket.title = "mock ticket";
        ticket.completed = false;

        String ticketAsJson = new Gson().toJson(ticket);

        UUID workTagUniqueId = UUID.randomUUID();
        OneTimeWorkRequest checkConnectivityWork = new OneTimeWorkRequest.Builder(CreateNewTicketWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(new Data.Builder().putString("key_input_ticket", ticketAsJson).build())
                .addTag(workTagUniqueId.toString())
                .build();

        WorkManager.getInstance().enqueue(checkConnectivityWork);

        WorkManager.getInstance().getWorkInfosByTagLiveData(workTagUniqueId.toString()).observe(this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                // we know there will be only 1 work info in this list - the 1 work with that specific tag!
                // there might be some time until this worker is finished to work (in the mean team we will get an empty list
                // so check for that
                if (workInfos == null || workInfos.isEmpty())
                    return;

                WorkInfo info = workInfos.get(0);

                // now we can use it
                String ticketAsJson = info.getOutputData().getString("key_output");
                Log.d(TAG, "got created ticket: " + ticketAsJson);
                Ticket ticketResponse = new Gson().fromJson(ticketAsJson, Ticket.class);

                // update UI with the ticket response.
            }
        });
    }

    public void onClickSetPrettyNameButton(View view) {
        startActivity(new Intent(MainActivity.this, EditPrettyName.class));
    }
}