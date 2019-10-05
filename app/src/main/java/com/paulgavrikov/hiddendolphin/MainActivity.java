package com.paulgavrikov.hiddendolphin;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.speech.tts.Voice;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements VoiceBruteForcer.VoiceBruteForcerListener {

    private EditText mActionInput;
    private EditText mCommandInput;
    private TextView mVoiceNameTextView;
    private VoiceBruteForcer mVoiceBruteForcer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        /**
         * Here we can assume that the app has been left due to a start of the voice assistant
         * (assuming the user hasn't close the app) so we can speak the command.
         */
        String command = mCommandInput.getText().toString();
        mVoiceBruteForcer.speakCommand(command);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVoiceBruteForcer.destroy(); // release TTS
    }

    /**
     * Prepares the UI.
     */
    private void setupUI() {
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mActionInput = findViewById(R.id.inputText);
        mCommandInput = findViewById(R.id.commandText);
        mVoiceNameTextView = findViewById(R.id.voiceName);
        mVoiceBruteForcer = new VoiceBruteForcer(this);
        mVoiceBruteForcer.setVoiceBruteForcerListener(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mVoiceBruteForcer.bruteforceAction(mActionInput.getText().toString());
            }
        });
    }

    @Override
    public void onStartNewVoice(final Voice voice) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVoiceNameTextView.setText(String.format("%s (%s)\n", voice.getLocale().getDisplayName(), voice.getName()));
            }
        });
    }
}
