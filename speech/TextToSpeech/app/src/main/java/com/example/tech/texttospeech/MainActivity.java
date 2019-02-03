package com.example.tech.texttospeech;


import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    private TextToSpeech mTTS;
    private EditText mEditText;
    private SeekBar mPitchSeekBar;
    private SeekBar mSpeedSeekBar;
    private Button mBtnSpeak;

    private final String errorMessage = "Sorry I have not learnt that word yet. Can you help me and use only yes or no?";
    private final String unconst = "That is not an option you must do what I say. Chose the other option.";

    private enum Stages{
        NONE, FIRST, SECOND, THIRD
    }

    public enum State{
        WAITING, BEGINNING, SHOPPING, TUTORIAL, LIMBO
    }

    private State currentState;
    private Stages currentStage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentState = State.WAITING;
        currentStage = Stages.NONE;

        mBtnSpeak = findViewById(R.id.say_it_btn);

        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    int result = mTTS.setLanguage(Locale.ENGLISH);
                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("TTS", "Language not supported");
                    } else{
                        mBtnSpeak.setEnabled(true);
                    }
                } else {
                    Log.e("TTS", "Initialistaion failed");
                }
            }
        });

        mEditText = findViewById(R.id.edittext);
        mSpeedSeekBar = findViewById(R.id.speed_seek_bar);
        mPitchSeekBar = findViewById(R.id.pitch_seek_bar);

        mBtnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });
    }

    private void speak(){
//        String text = mEditText.getText().toString();
        String text = getResponse();
        float pitch = (float) mPitchSeekBar.getProgress() / 50;
        if(pitch < 0.1){ pitch = 0.1f; }
        float speed = (float) mSpeedSeekBar.getProgress() / 50;
        if(speed < 0.1){ speed = 0.1f; }

        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);

        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    protected void onDestroy() {
        if(mTTS != null){
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }

    private String getResponse(){
        String text =  mEditText.getText().toString();
        if(currentState == State.WAITING){
            return stateWaiting(text);
        } else if(currentState == State.BEGINNING){
            return stateBeginning(text);
        } else if (currentState == State.TUTORIAL){
            return stateTutorial(text);
        } else if(currentState == State.SHOPPING){
            return stateShopping();
        } else if(currentState == State.LIMBO){
            return stateLimbo(text);
        }
        return "I dont know where I am";
    }

    private String stateWaiting(String text){
        if (text.equals("begin")){
            currentState = State.BEGINNING;
            return "Hello my name is Iona Trolley. Have you ever used good boy before?";
        }
        return "Hello there, in order to use good boy please say begin.";
    }

    private String stateBeginning(String text){
        if (currentStage == Stages.NONE)
        {
            if (text.equals("yes"))
            {
                currentStage = Stages.FIRST;
                return "Would you like to go shopping?";
            }
            else if (text.equals("no"))
            {
                currentStage = Stages.SECOND;
                return "Would you like to do a quick tutorial";
            }
            else {
                return errorMessage;
            }
        }
        else if (currentStage == Stages.FIRST)
        {
            if (text.equals("yes"))
            {
                currentState = State.SHOPPING;
                return "Here we go?";
            }
            else if(text.equals("no"))
            {
                currentStage = Stages.SECOND;
                return "Would you like to do a quick tutorial";
            } else{
                return errorMessage;
            }
        }
        else if (currentStage == Stages.SECOND)
        {
            if(text.equals("yes")){
                currentState = State.TUTORIAL;
                currentStage = Stages.NONE;
                return "Perfect. Today I will be guiding you around the shop. You can give me a list of items or simply browse the shop. " +
                        "Would you like more information about this service?";
            }
            else if (text.equals("no"))
            {
                // need to add a new State here.
                currentState = State.LIMBO;
                currentStage = Stages.NONE;
                return "Okay. When you are ready to start shopping say start. and to begin the tutorial say tutorial";
            }
            else {
                return errorMessage;
            }
        }
        return errorMessage;
    }

    private String stateTutorial(String text){
        if(currentStage == Stages.NONE){
            String returnText = "I will tell you when we have arrived at an item. In this shop the items will always be on your right." +
                    " I will then tell you what item you have arrived at and what shelf the item is on. Would you like an example?";
            if (text.equals("no"))
            {
                currentStage = Stages.FIRST;
                return returnText;
            }
            else if (text.equals("yes"))
            {
                currentStage = Stages.FIRST;
                return "I am a shopping trolley that follows a route around the shop to help you pick up items. To give me a list simply attach your" +
                        "smartphone and send me the list, I know that we might not be attaching the phone but use your imagination. " + returnText;
            }
            else {
                return errorMessage;
            }
        }
        else if (currentStage == Stages.FIRST)
        {
            String output = "Once you have selected an item you can scan it's barcode using the camera on your phone. Don't worry I will help you with this." +
                    "ONce you have scanned the item I can tell you information about the item you have picked up. If you pick up the wrong item you can ty again." +
                    "Does this make sense?";
            if (text.equals("yes"))
            {
                currentStage = Stages.SECOND;
                return "The Garlic Bread is on the middle shelf... " + output;
            }
            else if (text.equals("no"))
            {
                currentStage = Stages.SECOND;
                return output;
            }
            else {
                return errorMessage;
            }
        }
        else if(currentStage == Stages.SECOND)
        {
            if (text.equals("yes"))
            {
                currentStage = Stages.THIRD;
                return "Once we have finished your list you can continue to Browse or you may go to the checkout. Shall we start Shopping?";
            }
            else if (text.equals("no"))
            {
                return unconst;
            }
            else {
                return errorMessage;
            }
        }
        else if(currentStage == Stages.THIRD)
        {
            if(text.equals("yes"))
            {
                currentStage = Stages.NONE;
                currentState = State.SHOPPING;
                return "Here we go?";
            }
            else if (text.equals("no")){
                // need to add a new State here.
                currentState = State.LIMBO;
                currentStage = Stages.NONE;
                return "Okay. When you are ready to start shopping say start. and to begin the tutorial say tutorial";
            }
            else {
                return errorMessage;
            }
        }
        return errorMessage;
    }

    private String stateKeywordTesting(String text){
        if(text.equals("next")){
            return "Good Job. When you say next I will take you to the next item. Now try and say repeat.";
        } else if(text.equals("repeat")){
            return "Repeat. That was easy right. When we are shopping and you say repeat I will repeat exactly what I have just said." +
                    "Okay lets try and say price";
        } else if (text.equals("price")){
            return "Perfect.";
        }
        return errorMessage;
    }

    private String stateLimbo(String text){
        if (text.equals("shopping"))
        {
            currentState = State.BEGINNING;
            currentStage = Stages.FIRST;
            return "Would you like to go shopping?";
        }
        else if(text.equals("tutorial"))
        {
            currentState = State.BEGINNING;
            currentStage = Stages.SECOND;
            return "Would you like to do a quick tutorial?";
        }
        else {
            return errorMessage;
        }
    }

    private String stateShopping(){
        return "I am shopping here";
    }


}