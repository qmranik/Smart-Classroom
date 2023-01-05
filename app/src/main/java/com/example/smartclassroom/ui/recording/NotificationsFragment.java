package com.example.smartclassroom.ui.recording;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartclassroom.R;
import com.example.smartclassroom.databinding.FragmentNotificationsBinding;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NotificationsFragment extends Fragment implements RecordingListAdapter.onItemListClick {

    private static final int PERMISSIONCODE = 21;
    private FragmentNotificationsBinding binding;
    private boolean isRecording=false;
    private String recordPermission =  Manifest.permission.RECORD_AUDIO;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;
    private Chronometer timer;
    private TextView recordFileName;
    private String recordFile;
    private RecyclerView audioList;
    private File[] allFiles;
    private File fileToPlay;
    String mFileName;
    private ImageButton playPause;
    private TextView PlayingFileName;
    private SeekBar playingControlBar;
    private Handler seekBarHandler;
    private Runnable updateSeekBar;
    private int lastPlayed;
    private int hasCompleted=0;
    private TextView noRecordingsTV;


    private RecordingListAdapter recordingListAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ImageButton recordBtn = binding.recordPlayPauseButton;
        timer = binding.recordTimer;
//        recordFileName = binding.recordFilename;
        audioList = binding.audioListView;
        playPause = binding.recordingPPbtn;
        PlayingFileName = binding.playingFileName;
        playingControlBar = binding.seekBarRecordP;
        noRecordingsTV = binding.noRecordingsTV;
        lastPlayed=0;

        refreshList();
        mediaPlayer = new MediaPlayer();

        playingControlBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                playAudio(fileToPlay,lastPlayed);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        mediaPlayer.setOnCompletionListener(mediaPlayer1 -> {
            stopAudio();
            hasCompleted=1;
        });

        playPause.setOnClickListener(view -> {
            if(isPlaying){
                mediaPlayer.pause();
                isPlaying=false;
                playPause.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_baseline_slow_motion_video_24,null));

            }else{
                mediaPlayer.start();
                isPlaying=true;
                playPause.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_baseline_motion_photos_paused_24,null));

            }
            if(hasCompleted==1){
                playAudio(fileToPlay,lastPlayed);
            }
        });

        recordBtn.setOnClickListener(view -> {
            if(isRecording){
                stopRecording();
                recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_mic_24, null));
                isRecording=false;
            }else{
                if(checkPermission()) {
                    startRecording();
                    recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_stop_circle_24,null));
                    isRecording = true;
                }
            }
        });

        return root;
    }

    void refreshList(){

        String path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS).toString()+"/class_recordings";
        File directory = new File(path);
        allFiles = directory.listFiles();

        if(allFiles==null) {
            noRecordingsTV.setText("No Recording Found");
            return;
        }
        noRecordingsTV.setText("");
        recordingListAdapter = new RecordingListAdapter(allFiles, this);
        audioList.setHasFixedSize(false);
        audioList.setLayoutManager(new LinearLayoutManager(getContext()));
        audioList.setAdapter(recordingListAdapter);

    }

    private void stopRecording() {
        timer.stop();
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();
        mediaRecorder  = null;
        Toast.makeText(getContext(),"File Saved: "+recordFile,Toast.LENGTH_LONG).show();
        refreshList();
    }

    private void startRecording() {

        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();

        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String strDate = dateFormat.format(date);
        strDate = strDate.replaceAll("\\s", "").replaceAll(":", ".");

//        String recordPath = getActivity().getExternalFilesDir("/").getAbsolutePath();
        String recordPath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS).toString();

        File myDir = new File(recordPath);
        if(!myDir.exists())
            myDir.mkdirs();


        recordFile = "Class_Record_"+strDate.trim()+".3gp";

        myDir = new File(recordPath + "/class_recordings");
        if(!myDir.exists())
            myDir.mkdirs();

        myDir.mkdirs();

        File file = new File (myDir,recordFile);
        if (file.exists()) file.delete();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(file);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        MediaScannerConnection.scanFile(getContext(), new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaRecorder.start();
        Toast.makeText(getContext(),"Started!... File Name: "+recordFile,Toast.LENGTH_LONG).show();

    }

    private boolean checkPermission() {

        if(ActivityCompat.checkSelfPermission(getContext(), recordPermission) == PackageManager.PERMISSION_GRANTED){
            return true;
        } else{
            ActivityCompat.requestPermissions(getActivity(), new String[]{recordPermission},PERMISSIONCODE);
            return false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClickListener(File file, int position) {
        fileToPlay = file;
        if(isPlaying && position==lastPlayed){
            stopAudio();
            return;
        }else if(!isPlaying && position==lastPlayed){
            mediaPlayer.start();
            isPlaying=true;
            hasCompleted=0;
            playPause.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_baseline_motion_photos_paused_24,null));

        }
        lastPlayed=position;

        if(isPlaying){
            stopAudio();
            isPlaying=false;
            playAudio(fileToPlay,position);
        }else{
            playAudio(fileToPlay,position);
            isPlaying=true;
        }
    }

    private void stopAudio() {
        isPlaying=false;
        playPause.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_baseline_slow_motion_video_24,null));
        mediaPlayer.stop();
    }

    private void playAudio(File fileToPlay,int pos) {
        isPlaying=true;
        playPause.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_baseline_motion_photos_paused_24,null));
        PlayingFileName.setText(allFiles[pos].getName());
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(allFiles[pos].getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
        hasCompleted=0;
        playingControlBar.setMax(mediaPlayer.getDuration());
        seekBarHandler = new Handler();
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                playingControlBar.setProgress(mediaPlayer.getCurrentPosition());
                seekBarHandler.postDelayed(this,500);
            }
        };
        seekBarHandler.postDelayed(updateSeekBar,0);
    }
}