import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioPlayer {
    private static AudioPlayer instance;
    private Clip clip;
    private String currentPath;
    private boolean isMuted = false;

    private AudioPlayer() {}

    public static AudioPlayer getInstance() {
        if (instance == null) {
            instance = new AudioPlayer();
        }
        return instance;
    }

    public void play(String filePath) {
        // Jika file sama dan sedang main, jangan restart
        if (filePath.equals(currentPath) && clip != null && clip.isRunning()) {
            return;
        }
        
        stop(); // Stop lagu 

        try {
            File audioFile = new File(filePath);
            if (!audioFile.exists()) {
                System.err.println("File audio tidak ditemukan: " + filePath);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            
            if (!isMuted) {
                clip.loop(Clip.LOOP_CONTINUOUSLY); // Loop selamanya
                clip.start();
            }
            
            currentPath = filePath;

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Gagal memutar audio: " + e.getMessage());
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }

    public void mute() {
        isMuted = true;
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    public void unmute() {
        isMuted = false;
        if (clip != null && !clip.isRunning()) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        } else if (currentPath != null) {
            play(currentPath); // Re-play jika clip sudah closed
        }
    }
    
    public boolean isMuted() {
        return isMuted;
    }
}