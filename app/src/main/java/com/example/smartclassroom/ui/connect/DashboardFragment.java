package com.example.smartclassroom.ui.connect;

import static android.os.Looper.getMainLooper;

import static com.example.smartclassroom.ui.drawings.HomeFragment.paintView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartclassroom.R;
import com.example.smartclassroom.databinding.FragmentDashboardBinding;

//after marge

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import android.graphics.PorterDuff;
import android.media.AudioAttributes;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.BassBoost;
import android.media.audiofx.NoiseSuppressor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;

    // after marge

    static TextView tvStatus;
    Button btnWifiState;
    static Button btnStart;
    static Button btnStop;
    Button btnDiscover;
    EditText etMessage;
    static ListView listView;

    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel channel;
    BroadcastReceiver receiver;
    IntentFilter intentFilter;

    static List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    static String[] devicesNames;
    static WifiP2pDevice[] deviceArray;
    Set<Integer> st;
    List<InetAddress> allListeners = new ArrayList<InetAddress>();
    WifiManager wifi;
    String hostAddress;

    public static boolean isHost;

    private Button startButton, stopButton;

    public byte[] buffer;
    //    public DatagramSocket socket;
    private boolean status = true;
    int PORT = 8080;


    //Audio
    private Button mOn;
    private boolean isOn;
    private static boolean isRecording;
    private AudioRecord record;
    private static AudioTrack player;
    private AudioManager manager;
    private int recordState;
    private static int playerState;
    private static int minBuffer;
//    MulticastSocket serverSocket;
//    StartStreaming startStrem;

    //Audio Settings
    private final int source = MediaRecorder.AudioSource.CAMCORDER;
    private final int channel_in = AudioFormat.CHANNEL_IN_MONO;
    private final int channel_out = AudioFormat.CHANNEL_OUT_MONO;
    private final int format = AudioFormat.ENCODING_PCM_16BIT;
    int dev;
//    CustServer cstServer;

    private final static int REQUEST_ENABLE_BT = 1;
    private boolean IS_HEADPHONE_AVAILBLE = false;
    static Socket socket;
    static ClientClass clientClass;
    public static ServerClass serverClass;
    public View root;
    public static ArrayList<OutputStream> os;
    public static ArrayList<DataOutputStream> dos;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        //start


        initializeAll();
        exqListeners();
        requestUserPermission();

        //Reduce latancy
        getActivity().setVolumeControlStream(AudioManager.MODE_IN_COMMUNICATION);


        mOn = (Button) root.findViewById(R.id.button);
        isOn = false;
        isRecording = false;

        manager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        manager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        // end


//        final TextView textView = binding.textDashboard;
//        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    public void initAudio() {
        //Tests all sample rates before selecting one that works
        int sample_rate = getSampleRate();
        minBuffer = AudioRecord.getMinBufferSize(sample_rate, channel_in, format);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        record = new AudioRecord(source, sample_rate, channel_in, format, minBuffer);
        recordState = record.getState();
        int id = record.getAudioSessionId();
        Log.d("Record", "ID: " + id);
        playerState = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            player = new AudioTrack(
                    new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build(),
                    new AudioFormat.Builder().setEncoding(format).setSampleRate(sample_rate).setChannelMask(channel_out).build(),
                    minBuffer,
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE);
            playerState = player.getState();
            // Formatting Audio
            if (AcousticEchoCanceler.isAvailable()) {
                AcousticEchoCanceler echo = AcousticEchoCanceler.create(id);
                echo.setEnabled(true);
                Log.d("Echo", "Off");
            }
            if (NoiseSuppressor.isAvailable()) {
                NoiseSuppressor noise = NoiseSuppressor.create(id);
                noise.setEnabled(true);
                Log.d("Noise", "Off");
            }
            if (AutomaticGainControl.isAvailable()) {
                AutomaticGainControl gain = AutomaticGainControl.create(id);
                gain.setEnabled(false);
                Log.d("Gain", "Off");
            }
            BassBoost base = new BassBoost(1, player.getAudioSessionId());
            base.setStrength((short) 1000);
        }
    }


    public int getSampleRate() {
        //Find a sample rate that works with the device
        for (int rate : new int[]{8000, 11025, 16000, 22050, 44100, 48000}) {
            int buffer = AudioRecord.getMinBufferSize(rate, channel_in, format);
            if (buffer > 0)
                return rate;
        }
        return -1;
    }

    public void requestUserPermission() {

        ArrayList<String> permissions = new ArrayList<String>();

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission_group.NEARBY_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission_group.NEARBY_DEVICES);
            }
        }
        String[] str = new String[permissions.size()];

        for (int i = 0; i < permissions.size(); i++) {
            str[i] = permissions.get(i);
        }

        ActivityCompat.requestPermissions(getActivity(), str, 1);

    }

    private void exqListeners() {

        btnWifiState.setOnClickListener(view -> {
            Intent intent = new Intent(Settings.Panel.ACTION_WIFI);
            startActivityForResult(intent, 1);

        });

        btnDiscover.setOnClickListener(view -> {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestUserPermission();
                return;
            }

            wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    tvStatus.setText("Discovery Started");
                }

                @Override
                public void onFailure(int i) {
                    final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
        });

        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            final WifiP2pDevice device = deviceArray[i];
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
//                    tvStatus.setText("Connected device : " + device.deviceAddress);
                }

                @Override
                public void onFailure(int i) {
                    tvStatus.setText("not connected");
                }
            });
        });

        btnStop.setOnClickListener(view -> {
//            ExecutorService executor = Executors.newSingleThreadExecutor();
//            String msg = etMessage.getText().toString();
//            executor.execute(()->{
//                if(msg!=null && isHost){
//
//                }else if(msg != null && !isHost){
//
//                }
//            });

            isRecording = false;

        });

        btnStart.setOnClickListener(view -> {

            ExecutorService executor = Executors.newSingleThreadExecutor();

            executor.execute(() -> {
                if (isHost) {

                    int read = 0, write = 0;
                    if (recordState == AudioRecord.STATE_INITIALIZED) {
                        record.startRecording();
                        isRecording = true;
                        Log.d("Record", "Recording...");
                    }

                    while (isRecording) {

                        byte[] audioData = new byte[minBuffer];
                        if (record != null)
                            read = record.read(audioData, 0, minBuffer);
                        else
                            break;
                        Log.d("Record", "Read: " + read);
                        if (read > 0)
                            serverClass.write(audioData);
                        Log.d("Record", "Write: " + write);

                    }

                } else if (!isHost) {

//                    clientClass.write(msg.getBytes());
                    isRecording = true;

                }
            });

        });

        root.findViewById(R.id.mictospkr).setOnClickListener(view -> {
            Intent i = new Intent(getContext(), taptospeak.class);
            startActivity(i);
        });

//        root.findViewById(R.id.createGroupBtn).setOnClickListener(view -> {
//
//            wifiP2pManager.createGroup(channel, new WifiP2pManager.ActionListener() {
//                @Override
//                public void onSuccess() {
//                    Toast.makeText(getContext(), "P2P group Created",
//                            Toast.LENGTH_SHORT).show();
//                }
//
//                @Override
//                public void onFailure(int i) {
//                    wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
//                        @Override
//                        public void onSuccess() {
//                            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                                return;
//                            }
//                            wifiP2pManager.createGroup(channel, new WifiP2pManager.ActionListener() {
//                                @Override
//                                public void onSuccess() {
//                                    Toast.makeText(getContext(), "P2P group Created",
//                                            Toast.LENGTH_SHORT).show();
//                                }
//
//                                @Override
//                                public void onFailure(int i) {
//                                    Toast.makeText(getContext(), "P2P group creation failed. Restart the app",
//                                            Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                        }
//
//                        @Override
//                        public void onFailure(int i) {
//                            Toast.makeText(getContext(), "P2P group creation failed. Restart the app",
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    });
//
//                }
//            });
//        });

    }

    private void initializeAll() {

        tvStatus = root.findViewById(R.id.tvConStatus);
        btnWifiState = root.findViewById(R.id.btnWifiState);
        btnDiscover = root.findViewById(R.id.btnDiscover);
        btnStart = root.findViewById(R.id.btnStart);
        btnStop = root.findViewById(R.id.btnStop);
        startStopBtn(false);
        listView = root.findViewById(R.id.listview);

        wifiP2pManager = (WifiP2pManager) getContext().getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(getContext(),getMainLooper(),null);
        receiver = new WifiDirectBroadcastRecever(wifiP2pManager,channel,getActivity());
        wifi = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        initAudio();
        st =  new HashSet<Integer>();

        Log.d("debug","1");

    }

    public static WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {

            if(!peers.equals(wifiP2pDeviceList.getDeviceList())) {
                peers.clear();
                peers.addAll(wifiP2pDeviceList.getDeviceList());

                devicesNames = new String[wifiP2pDeviceList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[wifiP2pDeviceList.getDeviceList().size()];

                int index = 0;
                for (WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
                    devicesNames[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }

                Log.d("debug", String.valueOf(devicesNames.length));
                ArrayAdapter<String> adapter = new ArrayAdapter<>(listView.getContext(), android.R.layout.simple_list_item_1, devicesNames);
                listView.setAdapter(adapter);

//                 adapter.addAll(wifiP2pDeviceList.getDeviceList().toString());
                adapter.notifyDataSetChanged();
                if (peers.size() == 0) {
                    tvStatus.setText("No devices found");
                    return;
                }

            }

        }
    };

    static WifiP2pManager.ConnectionInfoListener connectionInfoListener =  new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {

            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner && tvStatus.getText()!="HOST"){
                tvStatus.setText("HOST");
                isHost=true;
                startStopBtn(true);
                serverClass = new ServerClass();
                serverClass.start();
            }else if(!wifiP2pInfo.isGroupOwner){
                tvStatus.setText("CLIENT");
                isHost=false;
                startStopBtn(true);
                clientClass = new ClientClass(groupOwnerAddress);
                clientClass.start();
            }

        }
    };

    public static void startStopBtn(boolean clickableState){
        btnStart.setEnabled(clickableState);
        btnStop.setEnabled(clickableState);
    }


    public static class ServerClass extends Thread{

        ServerSocket serverSocket;
        private InputStream inputStream;
        private OutputStream outputStream;


        ServerClass(){

            os = new ArrayList<OutputStream>();
            dos = new ArrayList<DataOutputStream>();
        }

        public void write(byte[] bytes){
            ExecutorService executor = Executors.newSingleThreadExecutor();

            executor.execute(() -> {

                try {

                        for (int i = 0; i < os.size(); i++) {
                            os.get(i).write(bytes);
                        }

//                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            });
        }

//        public void write(String sendMsg){
//            ExecutorService executor = Executors.newSingleThreadExecutor();
//
//            executor.execute(() -> {
//
//                try {
//                        DataOutputStream dataOutputStream;
//                        for (int i = 0; i < os.size(); i++) {
//
//                            dataOutputStream = new DataOutputStream(os.get(i));
//                            dataOutputStream.writeUTF(sendMsg);
//                            dataOutputStream.flush();
//                        }
////                outputStream.write(bytes);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
//        }


        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8889);
//                socket = serverSocket.accept();
//                inputStream = socket.getInputStream();
//                outputStream = socket.getOutputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(getMainLooper());

            executor.execute(() -> {
//                byte[] buffer = new byte[1024];
//                int bytes;
//
//                while (socket!=null){
//                    try {
//                        bytes = inputStream.read(buffer);
//                        if(bytes > 0){
//                            int finalBytes = bytes;
//                            handler.post(() -> {
//                                String str = new String(buffer,0,finalBytes);
//
//                            });
//
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }

                while (true){
                    try {
                        socket = serverSocket.accept();
                        outputStream = socket.getOutputStream();
                        os.add(outputStream);
                        dos.add(new DataOutputStream(outputStream));
                    } catch (IOException e) {
                        System.out.println("I/O error: " + e);
                    }
                }
            });

        }
    }


    public static class ClientClass extends Thread{
        String hostAddress;
        private InputStream inputStream;
        private OutputStream outputStream;
//        private DataInputStream dataInputStream;

        public void write(byte[] bytes){

            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        public ClientClass(InetAddress hostAddress){
            this.hostAddress = hostAddress.getHostAddress();
            socket = new Socket();
        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAddress,8889),500);

                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
//                dataInputStream = new DataInputStream(inputStream);

            } catch (IOException e) {
                e.printStackTrace();
            }

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(getMainLooper());

            executorService.execute(() -> {

                while(!socket.isConnected()){
                    try {
                        socket.connect(new InetSocketAddress(hostAddress,8889),500);

                        inputStream = socket.getInputStream();
                        outputStream = socket.getOutputStream();
//                        dataInputStream = new DataInputStream(inputStream);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                int bytes;
                if(playerState == AudioTrack.STATE_INITIALIZED) {
                    player.play();
                    Log.d("Record", "Recording...");
                }
                while (socket != null){

                    byte[] buffer = new byte[minBuffer];

                    try {
                        bytes = inputStream.read(buffer);
                        Log.d("TAG", "Received bytes run: "+bytes);
                        if(bytes>0){

//                            if(bytes < minBuffer-100){
//
//                                Log.d("TAG", "Received bytes Picchi: "+bytes);
//                                // read the message from the socket
//                                String str = dataInputStream.readUTF();
//                                Log.d("TAG", "Received STR Picchi: "+str);
//                                if(str.charAt(1)=='T'){
//
//                                    String[] words = str.split("\\s");
//                                    Log.d("TAG", "onTouchChange: "+Float.valueOf(words[1])+" "+Float.valueOf(words[2]));
//                                    handler.post(() -> {
//                                        paintView.startTouch(Float.valueOf(words[1]).floatValue(), Float.valueOf(words[2]).floatValue());
//                                    });
//                                }else if(str.charAt(1)=='D'){
//
//                                    String[] words=str.split("\\s");
//
//                                    Log.d("TAG", "onDrawingChange: "+Float.valueOf(words[1])+" "+Float.valueOf(words[2]));
//
//                                       handler.post(()->{
////                                           paintView.
//                                           if(!words[1].isEmpty() && !words[2].isEmpty());
//                                            paintView.drawToCanvas(Float.valueOf(words[1]).floatValue(),Float.valueOf(words[2]).floatValue());
//
//                                       });
//
//                                }
//
//                            }else{

                                Log.d("TAG", "Received bytes Boro: "+bytes);

                                int finalBytes = bytes;

                                final int[] write = new int[1];
                                handler.post(() -> {

                                    if(player != null)
                                        if(isRecording)
                                            write[0] = player.write(buffer, 0, finalBytes);
                                    Log.d("Record", "Write: " + write[0]);

                                });
                            }
//                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                player.stop();
            });

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(receiver,intentFilter);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestUserPermission();
            return;
        }


        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                tvStatus.setText("Discovery Started");
            }

            @Override
            public void onFailure(int i) {
                tvStatus.setText("Discovery Failed");
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}