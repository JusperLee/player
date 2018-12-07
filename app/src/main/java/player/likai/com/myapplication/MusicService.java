package player.likai.com.myapplication;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.IBinder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusicService extends Service {
    private List<String> musics;
    private List<String> simpleMusics;
    private MyBinder binder;
    public MediaPlayer player;
    private ExecutorService executorService = Executors.newCachedThreadPool();
    public MusicService(){

    }
    public void onCreate(){
        super.onCreate();
    }
    private void initLiseView(){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/music/";
        File mp3dir = new File(path);
        if (mp3dir.isDirectory()){
            File[] files = mp3dir.listFiles();
            for(File f:files){
                musics.add(f.getAbsolutePath());
                simpleMusics.add(f.getName());
            }
        }
        binder.setMusics(musics);
        binder.setSimpleMusics(simpleMusics);
    }
    public void play(){
        player.reset();
        try{
            player.setDataSource(musics.get(binder.getCurMusicIndex()));
            player.prepare();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    player.start();
                }
            });
            player.seekTo(binder.getPausePosition());
            binder.setTotalMusicTime(player.getDuration());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void pause(){
        if (player.isPlaying()){
            player.pause();
            binder.setPause(true);
            binder.setPausePosition(player.getCurrentPosition());
        }
    }

    public void next(){
        int curMusicIndex = binder.getCurMusicIndex();
        curMusicIndex+=1;
        if (curMusicIndex >=musics.size()){
            binder.setCurMusicIndex(0);
            binder.setPausePosition(0);
            play();
        }
        else{
            binder.setCurMusicIndex(curMusicIndex);
            binder.setPausePosition(0);
            play();
        }
    }
    public void pre(){
        int curMusicIndex = binder.getCurMusicIndex();
        curMusicIndex-=1;
        if (curMusicIndex<0) {
            binder.setCurMusicIndex(musics.size() - 1);
            binder.setPausePosition(0);
            play();
        }else{
            binder.setCurMusicIndex(curMusicIndex);
            binder.setPausePosition(0);
            play();
        }
    }
    public void stop(){
        if (player!=null){
            player.pause();
            player.stop();
            binder.setPause(false);
        }
        binder.setPausePosition(0);
        binder.setCurMusicIndex(0);
        binder.setPlayPosition(0);
    }
    public void init(){
        player = new MediaPlayer();
        musics = new ArrayList<>();
        simpleMusics =new ArrayList<>();
        binder = new MyBinder(this);
    }
    public void onDestroy(){
        if (player!=null&&player.isPlaying()){
            player.stop();
            player.release();
            player=null;
        }
        super.onDestroy();
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        init();
        // player.reset();
        // 自动播放下一首
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Intent intent = new Intent();
                intent.setAction("player.likai.com.myapplication.RECEIVER");
                sendBroadcast(intent);
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }
    public IBinder onBind(Intent intent){
        initLiseView();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(200);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    binder.setPlayPosition(player.getCurrentPosition());
                }
            }
        });
        return binder;
    }
}
