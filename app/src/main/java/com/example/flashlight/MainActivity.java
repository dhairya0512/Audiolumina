package com.example.flashlight;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int SAMPLE_RATE = 44100; // Sample rate (Hz)
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO; // Mono channel
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT; // 16-bit PCM encoding
    private double threshold = 100.0; // Adjust the threshold value as needed
    private CameraManager cameraManager;
    private String cameraId;
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private boolean isFlashlightOn = false;
    ConstraintLayout CNT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startButton = findViewById(R.id.mainbtn1);
        CNT= (ConstraintLayout) findViewById(R.id.constrainlayout);

//        if(isRecording){
//            Toast.makeText(this, "OOOOOOOOOOOO", Toast.LENGTH_SHORT).show();
//}
//        else {
//            startButton.setBackgroundResource(R.drawable.neonglow);
//        }

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isRecording)
                {   CNT.setBackgroundResource(R.drawable.yellowgradient);
                    startButton.setBackgroundResource(R.drawable.onxml);
                    startRecording();


                }

                else{
                    CNT.setBackgroundResource(R.drawable.defaultbg);
                    startButton.setBackgroundResource(R.drawable.neonglow);
                    stopRecording();
                    toggleFlashlight(false);
                }
            }
        });

//        Button stopButton = findViewById(R.id.mainstopbtn2);
//        stopButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                stopRecording();
//                toggleFlashlight(false);
//            }
//        });
    }

    private void startRecording() {
        // Check for the RECORD_AUDIO permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        } else {
            // Permission already granted, start recording
            initializeAudioRecord();
            audioRecord.startRecording();
            isRecording = true;
            startRecordingThread();
        }
    }

    private void initializeAudioRecord() {
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);
    }

    private void startRecordingThread() {
        Thread recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                processAudioData();
            }
        });
        recordingThread.start();
    }

    private void processAudioData() {
        short[] buffer = new short[1024]; // Buffer to hold audio data
        double sumSquaredSamples = 0.0;
        while (isRecording) {
            int bytesRead = audioRecord.read(buffer, 0, buffer.length);
            // Process the audio data here
            // You can calculate the amplitude, perform frequency analysis, etc.
            // Example: Calculate the average amplitude
//            double averageAmplitude = calculateAverageAmplitude(buffer, bytesRead);
//            // Use the averageAmplitude value as needed
//            Log.d("LLLLLLLLLLLLLLL","Average amplitude value: "+averageAmplitude);


            for (int i = 0; i < bytesRead; i++) {
                double normalizedSample = buffer[i] / 32768.0; // Normalize the sample to a value between -1.0 and 1.0
                sumSquaredSamples += normalizedSample * normalizedSample;
            }

            // Calculate the RMS value
            double rms = Math.sqrt(sumSquaredSamples / bytesRead);
            // Update the threshold based on the current noise level
            updateThreshold(rms);

            // Check if the RMS value exceeds the threshold
            if (rms > threshold && !isFlashlightOn) {
                // Turn on the flashlight
                toggleFlashlight(true);
            } else if (rms <= threshold && isFlashlightOn) {
                // Turn off the flashlight
                toggleFlashlight(false);
            }


            // Use the calculated rms value as needed
            // e.g., display in a TextView, log to console, etc.
            Log.e("Bhai","Value is : "+rms);
            sumSquaredSamples = 0.0;
        }
    }

    private void toggleFlashlight(boolean enable) {
        try {
            cameraManager.setTorchMode(cameraId, enable);
            isFlashlightOn = enable;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updateThreshold(double currentRms) {
        threshold = 0.7 * threshold + 0.3 * currentRms; // Update threshold using a weighted average
    }

    private double calculateAverageAmplitude(short[] audioData, int bytesRead) {
        double sum = 0;
        for (int i = 0; i < bytesRead; i++) {
            sum += Math.abs(audioData[i]);
        }
        return sum / bytesRead;
    }

    private void stopRecording() {
        isRecording = false;
        try {
            Thread.sleep(100); // Wait for the recording thread to finish
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        audioRecord.stop();
        audioRecord.release();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecording();
        }
    }
}