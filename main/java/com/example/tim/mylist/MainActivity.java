package com.example.tim.mylist;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String COMMAND = "getprop /sdcard/Download/output.txt";
    private ListView myListView;
    private String items[];
    private BufferedReader reader;
    private String tempData;

    private Timer getpropTimer;
    private TimerTask getpropTask;

    private Timer displayTimer;
    private TimerTask displayTask;
    private String[] tempDataArray;
    private ItemAdapter itemAdapter;
    private int lineCounter = 6;

    public Context thisContext;

    // user permission
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Variables
        verifyStoragePermissions(this);
        myListView = (ListView)findViewById(R.id.myListView);
        thisContext = this;




        // Initialize TimerTasks
        init();

        /*
        getpropTimer = new Timer();
        getpropTimer.schedule(getpropTask,0);
        */

        displayTimer = new Timer();
        displayTimer.schedule(displayTask,1000, 1000);


        //----------------------------- Start Read From File -----------------------------






    }// end onCreate

    public void init(){
        //-------------------------------- Start ----------------------------------
        getpropTask = new TimerTask() {
            @Override
            public void run() {
                try {

                    int counter = 0;
                    Process process = Runtime.getRuntime().exec("getprop");
                    DataOutputStream os = new DataOutputStream(process.getOutputStream());

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    // Grab the results
                    StringBuilder log = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        log.append(line + "\n");
                    }

                    Log.d("GETPROP_INFO: ", log.toString());
                    writeToFile(log.toString(),getApplicationContext());

                    while(counter < 5) {

                        os.flush();
                        os.writeBytes("getprop");

                        // Grab the results
                        log = new StringBuilder();
                        line = "";
                        while ((line = bufferedReader.readLine()) != null) {
                            log.append(line + "\n");
                        }

                        Log.d("GETPROP_INFO: ", log.toString());
                        writeToFile(log.toString(),getApplicationContext());

                        Thread.sleep(1000);
                        ++counter;
                    }
                    os.flush();
                    os.close();

                }catch(IOException ioe){
                    ioe.printStackTrace();
                    Log.d("IO_EXCEPTION", ioe.getMessage());
                }catch(InterruptedException ie){
                    ie.printStackTrace();
                    Log.d("I_EXCEPTION", ie.getMessage());
                }
            }// end run
        };// end getprop task

        displayTask = new TimerTask(){
            @Override
            public void run(){
                // Read from file 'output.txt'
                try {
                    File dumpedFile = new File("/sdcard/Download/output.txt");

                    boolean fileExists = true;
                    if(!dumpedFile.exists()){
                        Toast.makeText(getApplicationContext(), "'output.txt' does not exist", Toast.LENGTH_SHORT).show();
                    }
                    reader = new BufferedReader(new FileReader(dumpedFile));
                    String temp;


                    tempData = "";
                    while ((temp = reader.readLine())!= null) {
                        Log.d("READ PKT:", temp);
                        tempData += temp;
                        tempData += "\n";
                        //updateDisplay(temp);
                    }

                } catch(IOException io){
                    Log.d("IOEX",io.getMessage());
                    Toast.makeText(getApplicationContext(),io.getMessage(),Toast.LENGTH_SHORT).show();
                }

                //String temp = buffer.toString();
                updateDisplay(tempData, thisContext);

                 /*
                 myListView = (ListView)findViewById(R.id.myListView);
                String[] tempDataArray = tempData.toString().split("\\n");

                ItemAdapter itemAdapter = new ItemAdapter(this,tempDataArray);
                myListView.setAdapter(itemAdapter);
                */
                try { reader.close(); } catch(IOException io) { Toast.makeText(getApplicationContext(),io.getMessage(),Toast.LENGTH_SHORT).show(); }
                //Log.d("Display Thread : ",temp);


            }// end run()
        };// end displayTask()


    }// end init()


    public void updateDisplay(String content, final Context context){
        final String data = content;
        runOnUiThread(new Runnable(){
            @Override
            public void run(){
                //myListView = (ListView)findViewById(R.id.myListView);

                tempData += "This is Text is added " + lineCounter + "\n";

                myListView = (ListView)findViewById(R.id.myListView);
                tempDataArray = tempData.split("\\n");

                itemAdapter = new ItemAdapter(thisContext,tempDataArray);

                myListView.setAdapter(itemAdapter);

                //myListView.setAdapter(itemAdapter);
                //itemAdapter.notifyDataSetChanged();
                //myListView.invalidateViews();
                //myListView.refreshDrawableState();

                ++lineCounter;


            }
        });
    }


    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("output.txt", Context.MODE_APPEND));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }


}// end class
