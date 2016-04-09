package com.htdevs.samaritan;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ListView messageList;
    private MessageAdapter messageAdapter;
    private EditText messageBodyField;
    private String messageBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate(), Inside method");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate(), setting the ListView with the messageAdapter");
        messageList = (ListView) findViewById(R.id.listMessages);
        messageAdapter = new MessageAdapter(this);
        messageList.setAdapter(messageAdapter);

        messageBodyField = (EditText) findViewById(R.id.messageBodyField);

        //Onclick Listener for the send button
        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }


    //Populate message history
    private void populateDummyData(){
        Log.d(TAG, "populateDummyData(), adding dummy data and calling addMessage()");
        //Data stub till database is added
        String dummyData[] = {"Hey","Test data", "Working", "Yes its, Working", "Add more data", "And more data. Let's make this data long", "Okay"};
        for (int i = 0; i < dummyData.length; i++) {
            String message = dummyData[i];
            if (i%2==0) {
                messageAdapter.addMessage(message, MessageAdapter.DIRECTION_INCOMING);
            } else {
                messageAdapter.addMessage(message, MessageAdapter.DIRECTION_OUTGOING);
            }
        }
    }

    //Function to add a message to the UI
    private void sendMessage(){
        Log.d(TAG,"sendMessage(), send messages to the UI");
        messageBody = messageBodyField.getText().toString();
        if(messageBody.isEmpty()){
            Toast.makeText(this, "Please enter a message!", Toast.LENGTH_LONG).show();
            return;
        }
        Log.d(TAG, "sendMessage(), using addMessage to add it to the UI");
        messageAdapter.addMessage(messageBody, MessageAdapter.DIRECTION_INCOMING);
        messageBodyField.setText("");
        handleResponse();
    }

    //Function to handle the response based on the user request
    private void handleResponse(){
        Log.d(TAG, "handleResponse() handle the response of the user's request");
        String message;
        List<String> messageList = Arrays.asList(messageBody.split(" "));
        String messageCore = messageList.get(0);
        if(messageCore.equals("#help")){
            message = "You can access the following features:\n#twitter\n#weather\n#movies";
        }  if(messageBody.equals("#help weather")){
            message = "You can get the weather of any city in the world by entering\n\n#weather <city-name> <F/C> <No. of days>";
        }
        else if(messageCore.equals("#movies")){
            message = "#movies will allow you to search latest movies from the online repository.";
        }else if(messageCore.equals("#weather")){
            FetchWeatherTask weather = new FetchWeatherTask(messageAdapter);
            String location = "Chennai";
            String unitsPref = "metric";
            String numDays = "1";
            if(messageList.size()==1){
                message = "Uh oh! You just got 404'D.\nEnter message in proper format. Enter '#help weather' for more options.";
                messageAdapter.addMessage(message,MessageAdapter.DIRECTION_OUTGOING);
                return;
            }
            else if(messageList.size()==2){
                location = messageList.get(1);
            } else if(messageList.size()==3){
                location = messageList.get(1);
                if(messageList.get(2).equals("F"))
                    unitsPref = "imperial";
            } else if(messageList.size()==4) {
                location = messageList.get(1);
                if (messageList.get(2).equals("F"))
                    unitsPref = "imperial";
                numDays = messageList.get(3);
            }
            weather.execute(location, unitsPref, numDays);
            return;
        }else if(messageCore.equals("#twitter")){
            message = "#twitter will give you the latest tweets from a an user account.";
        }else if(messageCore.equals("#test")){
            message = "";
            messageAdapter.addMessage(message, MessageAdapter.DIRECTION_RESPONSE);
            return;
        }else {
            message = "You've entered a wrong command, send #help for more information";
        }
        Log.d(TAG, "handleResponse(), sending the outgoing message");
        messageAdapter.addMessage(message, MessageAdapter.DIRECTION_OUTGOING);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
