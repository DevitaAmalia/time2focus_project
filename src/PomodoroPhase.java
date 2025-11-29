public abstract class PomodoroPhase {

    // ================== Field ==================
    private final int totalSeconds;   // durasi total fase (detik)
    private int remainingSeconds;     // sisa waktu
    private int elapsedSeconds;       // waktu yang sudah berjalan
    private boolean running;
    private boolean finished;

    // ================== Konstruktor ==================
    public PomodoroPhase(int totalSeconds) {
        if (totalSeconds <= 0) {
            throw new IllegalArgumentException("Durasi fase harus > 0 detik");
        }
        this.totalSeconds = totalSeconds;
        this.remainingSeconds = totalSeconds;
        this.elapsedSeconds = 0;
        this.running = false;
        this.finished = false;
    }

    // ================== Method Umum ==================

    /** Mulai fase (dipanggil saat user klik Start di awal). */
    public void start() {
        if (!finished) {
            this.running = true;
        }
    }

    /** Pause fase (dipanggil saat user klik Pause). */
    public void pause() {
        this.running = false;
    }

    /** Lanjutkan fase setelah pause. */
    public void resume() {
        if (!finished) {
            this.running = true;
        }
    }

    /** Reset kembali ke awal durasi. */
    public void reset() {
        this.running = false;
        this.finished = false;
        this.remainingSeconds = totalSeconds;
        this.elapsedSeconds = 0;
    }

    public void tick() {
        if (!running || finished) {
            return;
        }

        if (remainingSeconds > 0) {
            remainingSeconds--;
            elapsedSeconds++;
        }

        if (remainingSeconds <= 0) {
            remainingSeconds = 0;
            finished = true;
            running = false;
        }
    }

    // ================== Getter ==================

    public int getTotalSeconds() {
        return totalSeconds;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isFinished() {
        return finished;
    }

    public abstract String getPhaseName();

    public abstract void onPhaseFinished(PomodoroController controller);

    public String getRemainingTimeFormatted() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
