package com.example.smartclassroom.ui.drawings;

import static android.os.Environment.DIRECTORY_PICTURES;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static com.example.smartclassroom.ui.connect.DashboardFragment.dos;
import static com.example.smartclassroom.ui.connect.DashboardFragment.isHost;
import static com.example.smartclassroom.ui.connect.DashboardFragment.serverClass;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartclassroom.R;
import com.example.smartclassroom.databinding.FragmentHomeBinding;
import com.tolunaykan.drawinglibrary.DrawingChangeListener;
import com.tolunaykan.drawinglibrary.PaintView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    public static PaintView paintView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        verifystoragepermissions(getActivity());

        paintView = root.findViewById(R.id.drawingView);
        Button redBtn = root.findViewById(R.id.redBtn);
        Button greenBtn = root.findViewById(R.id.grnBtn);
        Button blackBtn = root.findViewById(R.id.blcBtn);
        Button blueBtn = root.findViewById(R.id.bluBtn);
        SeekBar seekBar = root.findViewById(R.id.seekBarClr);

        ImageButton undoBtn = binding.undoBtn;
        ImageButton redoBtn = root.findViewById(R.id.redoBtn);
        ImageButton clearBtn = root.findViewById(R.id.clearBtn);
        ImageButton eraseBtn = root.findViewById(R.id.eraseBtn);
        ImageButton saveBtn = root.findViewById(R.id.saveBtn);

        redBtn.setOnClickListener(view -> redCLR(view));
        greenBtn.setOnClickListener(view -> greenCLR(view));
        blackBtn.setOnClickListener(view -> blackCLR(view));
        blueBtn.setOnClickListener(view -> blueCLR(view));
        undoBtn.setOnClickListener(view -> paintView.undoDrawing());
        redoBtn.setOnClickListener(view -> paintView.redoDrawing());
        clearBtn.setOnClickListener(view -> paintView.clearCanvas());
        eraseBtn.setOnClickListener(view -> paintView.enableEraser());

        paintView.addDrawingChangeListener(new DrawingChangeListener() {
            @Override
            public void onTouchStart(float x, float y) {
                Log.d("TAG", "onTouchStart: "+x+" "+y);
//                paintView.startTouch(x+10,y+10);
                if(isHost){
                    String cordinate = "OTS "+Float.toString(x)+" "+Float.toString(y);
//                    sendCoordinate(cordinate);
                }
            }

            @Override
            public void onDrawingChange(float x, float y) {
                Log.d("TAG", "onDrawingChange: "+x+" "+y);
//                paintView.drawToCanvas(x+10,y+10);
                if(isHost){
                    String cordinate = "ODS "+Float.toString(x)+" "+Float.toString(y);
//                    serverClass.write(cordinate);
//                    sendCoordinate(cordinate);
                }
            }
        });

        saveBtn.setOnClickListener(view -> {

            Date date = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
            String strDate = dateFormat.format(date);
            strDate = strDate.replaceAll("\\s", "");

            String fname = "Notes-"+strDate.trim()+".jpg";

            fname = fname.replaceAll(":", ".");
            String rootDirPath = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES).toString();
                    File myDir = new File(rootDirPath + "/saved_images");
                    myDir.mkdirs();

                    File file = new File (myDir, fname);
                    if (file.exists ()) file.delete ();
                    try {
                        FileOutputStream out = new FileOutputStream(file);
                        Bitmap finalBitmap = paintView.getCanvasBitmap();
                        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        // sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                        //     Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
                        out.flush();
                        out.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
// Tell the media scanner about the new file so that it is
// immediately available to the user.
                    MediaScannerConnection.scanFile(getContext(), new String[]{file.toString()}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("ExternalStorage", "Scanned " + path + ":");
                                    Log.i("ExternalStorage", "-> uri=" + uri);
                                }
                            });



//            try {
//                // Initialising the directory of storage
////                String dirpath = Environment.getExternalStorageDirectory() + "";
//                String dirpath = String.valueOf(getActivity().getExternalFilesDir(DIRECTORY_PICTURES));
//
//                String path = "notes-" + fname + "-" + ".jpeg";
//
//                view.setDrawingCacheEnabled(true);
//                Bitmap bitmap = paintView.getCanvasBitmap();
//                view.setDrawingCacheEnabled(false);
//                File imageurl = new File(dirpath,path);
//                FileOutputStream outputStream = new FileOutputStream(imageurl);
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
//                outputStream.flush();
//                outputStream.close();
//
//            } catch (FileNotFoundException io) {
//                io.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            Toast.makeText(getContext(),"Saved as "+fname,Toast.LENGTH_LONG).show();
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                paintView.setBrushSize(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

//        final TextView textView = binding.textHome;
//        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    public static void sendCoordinate(String msg){

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                DataOutputStream dataOutputStream;
                for (int i = 0; i < dos.size(); i++) {
                    dataOutputStream = new DataOutputStream(dos.get(i));
                    dataOutputStream.writeUTF(msg);
                    dataOutputStream.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    public static void verifystoragepermissions(Activity activity) {

        int permissions = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // If storage permission is not given then request for External Storage Permission
        if (permissions != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(activity, permissionstorage, REQUEST_EXTERNAL_STORAGE);

            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void blackCLR(View view) {
        paintView.setBrushColor(Color.BLACK);
    }

    public void redCLR(View view) {
        paintView.setBrushColor(Color.RED);
    }

    public void greenCLR(View view) {
        paintView.setBrushColor(Color.GREEN);
    }

    public void blueCLR(View view) {
        paintView.setBrushColor(Color.BLUE);
    }
}