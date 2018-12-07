package player.likai.com.myapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private List<String> musics;
    private List<String> simpleMusics;
    private ArrayAdapter<String> adapter;
    private ListView musicLv;
    private Button btnPlayOrPause;
    private Button btnStop;
    private Button btnPre;
    private Button btnNext;
    private SeekBar sb;
    private TextView textCur;
    private TextView textTotal;
    private MyBinder binder;
    private MusicService musicService;
    private Handler handler;
    private ExecutorService executorService;
    private BroadcastReceiver receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPrem();

        initVarible();

        initView();
        startBindService();
        setListener();


        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                String playTime = msg.getData().getString("playTime");
                String totalTime = msg.getData().getString("totalTime");
                textCur.setText(playTime);
                textTotal.setText(totalTime);
            }
        };
    }


    public void requestPrem(){
           if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
                   !=PackageManager.PERMISSION_GRANTED){
               ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
           }
    }
    public void initVarible(){
        executorService = Executors.newCachedThreadPool();
        IntentFilter  intentFilter= new IntentFilter();
        intentFilter.addAction("player.likai.com.myapplication.RECEIVE");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                  musicLv.getChildAt(binder.getCurMusicIndex()).setBackgroundColor(Color.WHITE);
                  musicService.next();
                  musicLv.getChildAt(binder.getCurMusicIndex()).setBackgroundColor(Color.GREEN);
            }
        };
        registerReceiver(receiver,intentFilter);
    }

    public void initView(){
        musicLv = findViewById(R.id.musicLv);
        musicLv = findViewById(R.id.musicLv);
        sb = findViewById(R.id.sb);
        btnPlayOrPause = findViewById(R.id.btn_play_pause);
        btnStop = findViewById(R.id.btn_stop);
        btnPre = findViewById(R.id.btn_pre);
        btnNext = findViewById(R.id.btn_next);
        textCur = findViewById(R.id.text_current);
        textTotal = findViewById(R.id.text_total);

        sb.setProgress(0);
        textCur.setText("00:00");
        textTotal.setText("00:00");
    }
    public void startBindService(){
        Intent intent = new Intent();
        intent.setClass(this,MusicService.class);
        startService(intent);
        bindService(intent,serviceConnection,Context.BIND_AUTO_CREATE);

    }
    private ServiceConnection serviceConnection = new ServiceConnection(){
        public void onServiceConnected(ComponentName name , IBinder service){
            binder = (MyBinder) service;
            musicService = binder.getMusicService();
            initListView();
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        try {
                            Thread.sleep(200);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                        sb.setMax(binder.getTotalMusicTime());
                        sb.setProgress(binder.getPlayPosition());
                        String playTime = getTime(binder.getPlayPosition());
                        String totalTime = getTime(binder.getTotalMusicTime());
                        Message msg = Message.obtain();
                        msg.getData().putString("playTime", playTime);
                        msg.getData().putString("totalTime", totalTime);
                        handler.sendMessage(msg);
                    }
                }
            });

            }

        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
        }
    };
    private void initListView() {
        musics = binder.getMusics();
        simpleMusics = binder.getSimpleMusics();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, simpleMusics);
        musicLv.setAdapter(adapter);
    }
    private String getTime(int time) {
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        return sdf.format(time);
    }
    private void setListener(){
        InnerOnClickListerer listerer = new InnerOnClickListerer();
        btnPlayOrPause.setOnClickListener(listerer);
        btnStop.setOnClickListener(listerer);
        btnPre.setOnClickListener(listerer);
        btnNext.setOnClickListener(listerer);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b){
                    binder.setPausePosition(seekBar.getProgress());
                    musicService.player.seekTo(binder.getPausePosition());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        InnerItemOnCLickListener listener2 = new InnerItemOnCLickListener();
        musicLv.setOnItemClickListener(listener2);
    }
    private class InnerItemOnCLickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            binder.setCurMusicIndex(position);
            binder.setPausePosition(0);
        }
    }
    private class InnerOnClickListerer implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_play_pause:
                    musicLv.getChildAt(binder.getCurMusicIndex()).setBackgroundColor(Color.GREEN);
                    if (musicService.player.isPlaying()) {
                        musicService.pause();
                        btnPlayOrPause.setText("播放");
                    } else {
                        musicService.play();
                        sb.setProgress(binder.getPausePosition());
                        sb.setMax(binder.getTotalMusicTime());
                        btnPlayOrPause.setText("暂停");
                    }
                    break;
                case R.id.btn_next:
                    musicLv.getChildAt(binder.getCurMusicIndex()).setBackgroundColor(Color.WHITE);
                    musicService.next();
                    musicLv.getChildAt(binder.getCurMusicIndex()).setBackgroundColor(Color.GREEN);
                    sb.setMax(binder.getTotalMusicTime());
                    btnPlayOrPause.setText("暂停");
                    break;
                case R.id.btn_pre:
                    musicLv.getChildAt(binder.getCurMusicIndex()).setBackgroundColor(Color.WHITE);
                    musicService.pre();
                    musicLv.getChildAt(binder.getCurMusicIndex()).setBackgroundColor(Color.GREEN);
                    sb.setMax(binder.getTotalMusicTime());
                    btnPlayOrPause.setText("暂停");
                    break;
                case R.id.btn_stop:
                    musicLv.getChildAt(binder.getCurMusicIndex()).setBackgroundColor(Color.WHITE);
                    musicService.stop();
                    btnPlayOrPause.setText("播放");
                    sb.setProgress(0);
                    textCur.setText("00:00");
                    textTotal.setText("00:00");
                    break;

            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        musicService.player.stop();
        musicService.player.release();
        unbindService(serviceConnection);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
