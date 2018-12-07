package player.likai.com.myapplication;

import android.os.Binder;

import java.util.List;

public class MyBinder extends Binder {
    private List<String> musics;
    private List<String> simpleMusics;
    private int curMusicIndex=0;
    private int pausePosition=0;
    private int totalMusicTime=0;
    private int playPosition=0;
    private MusicService musicService;
    private boolean isPause = false;

    public int getPlayPosition() {
        return playPosition;
    }

    public void setPlayPosition(int playPosition) {
        this.playPosition = playPosition;
    }

    public MyBinder(MusicService musicService) {
        this.musicService = musicService;
    }

    public List<String> getSimpleMusics() {
        return simpleMusics;
    }

    public void setSimpleMusics(List<String> simpleMusics) {
        this.simpleMusics = simpleMusics;
    }

    public void setMusicService(MusicService musicService) {
        this.musicService = musicService;
    }

    public boolean isPause() {
        return isPause;
    }

    public void setPause(boolean pause) {
        isPause = pause;
    }

    public MusicService getMusicService() {
        return musicService;
    }

    public List<String> getMusics() {
        return musics;
    }

    public void setMusics(List<String> musics) {
        this.musics = musics;
    }

    public int getCurMusicIndex() {
        return curMusicIndex;
    }

    public void setCurMusicIndex(int curMusicIndex) {
        this.curMusicIndex = curMusicIndex;
    }

    public int getPausePosition() {
        return pausePosition;
    }

    public void setPausePosition(int pausePosition) {
        this.pausePosition = pausePosition;
    }

    public int getTotalMusicTime() {
        return totalMusicTime;
    }

    public void setTotalMusicTime(int totalMusicTime) {
        this.totalMusicTime = totalMusicTime;
    }
}

