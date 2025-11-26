import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

public class PomodoroController {

    // ================== Dependensi ==================
    private final MainMenuPanel view;
    private final KoneksiDatabase db;
    private final int userId;

    // ================== Durasi (detik) ==================
    private int workDurationSec;
    private int shortBreakDurationSec;
    private int longBreakDurationSec;

    // ================== State sesi ==================
    private PomodoroPhase currentPhase;
    private Thread timerThread;
    private volatile boolean timerThreadRunning;
    private boolean phaseCompletionHandled;

    private boolean sessionActive;      // apakah sesi pomodoro (1 nama sesi) sedang berjalan
    private String sessionName;         // nama sesi (diisi user di popup awal)
    private int cycleCount;             // 1 cycle = Work + Short Break

    // Akumulasi waktu (detik) untuk history
    private int totalWorkSeconds;
    private int totalShortSeconds;
    private int totalLongSeconds;

    // ================== Konstruktor ==================
    public PomodoroController(MainMenuPanel view, KoneksiDatabase db, int userId) {
        this.view = view;
        this.db = db;
        this.userId = userId;

        this.view.setController(this);

        loadDurationsFromSettings();
        this.sessionActive = false;
        this.timerThreadRunning = false;
        this.cycleCount = 0;

        // Inisialisasi tampilan awal
        updateViewPhaseAndTime(null);
    }

    // ================== Load setting dari DB ==================
    private void loadDurationsFromSettings() {
        // ambil setting dari DB
        var settings = db.getUserSettings(userId);

        // fallback default kalau null
        int workMin  = settings.getOrDefault("work_duration", 25) instanceof Integer
                ? (Integer) settings.get("work_duration") : 25;
        int sbMin    = settings.getOrDefault("sb_duration", 5) instanceof Integer
                ? (Integer) settings.get("sb_duration") : 5;
        int lbMin    = settings.getOrDefault("lb_duration", 15) instanceof Integer
                ? (Integer) settings.get("lb_duration") : 15;

        this.workDurationSec       = workMin * 60;
        this.shortBreakDurationSec = sbMin * 60;
        this.longBreakDurationSec  = lbMin * 60;
    }

    // ================== Kontrol Sesi dari UI ==================

    public void startNewSession(String sessionName) {
        if (sessionActive) {
            return;
        }

        if (sessionName == null || sessionName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                view,
                "Nama sesi tidak boleh kosong.",
                "Peringatan",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        this.sessionName = sessionName.trim();
        this.sessionActive = true;
        this.cycleCount = 0;
        this.totalWorkSeconds = 0;
        this.totalShortSeconds = 0;
        this.totalLongSeconds = 0;

        // Mulai dari Work phase
        switchToNewPhase(new WorkPhase(workDurationSec));
        currentPhase.start();
        startTimerThreadIfNeeded();
    }

    /** Tombol Pause di UI. */
    public void pauseCurrentPhase() {
        if (currentPhase != null && sessionActive) {
            currentPhase.pause();
        }
    }

    /** Tombol Resume di UI. */
    public void resumeCurrentPhase() {
        if (currentPhase != null && sessionActive) {
            currentPhase.resume();
        }
    }

    /** Tombol Reset di UI: reset hanya phase saat ini. */
    public void resetCurrentPhase() {
        if (currentPhase != null && sessionActive) {
            currentPhase.reset();
            updateViewPhaseAndTime(currentPhase);
        }
    }

    /**
     * Tombol End Session di UI:
     * Sesi dianggap selesai, akumulasi waktu saat ini, lalu simpan ke DB.
     */
    public void endSession() {
        if (!sessionActive) {
            return;
        }

        sessionActive = false;
        timerThreadRunning = false;

        // Akumulasi waktu phase saat ini ke total
        if (currentPhase != null) {
            accumulatePhaseElapsed(currentPhase);
        }

        // Simpan ke DB dalam satuan menit (dibulatkan ke bawah)
        int workMinutes  = totalWorkSeconds / 60;
        int sbMinutes    = totalShortSeconds / 60;
        int lbMinutes    = totalLongSeconds / 60;

        boolean ok = db.saveSessionHistory(userId, sessionName, workMinutes, sbMinutes, lbMinutes);

        if (ok) {
            JOptionPane.showMessageDialog(
                view,
                "Sesi \"" + sessionName + "\" telah disimpan ke history.",
                "Sesi Berakhir",
                JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            JOptionPane.showMessageDialog(
                view,
                "Gagal menyimpan sesi ke history.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }

        // Bersihkan state
        currentPhase = null;
        updateViewPhaseAndTime(null);
    }

    // ================== Internal: Timer Thread ==================

    private void startTimerThreadIfNeeded() {
        if (timerThread != null && timerThread.isAlive()) {
            return;
        }

        timerThreadRunning = true;

        timerThread = new Thread(() -> {
            while (timerThreadRunning) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }

                if (!sessionActive || currentPhase == null) {
                    continue;
                }

                currentPhase.tick();

                // update UI (harus di EDT)
                SwingUtilities.invokeLater(() -> updateViewPhaseAndTime(currentPhase));

                // kalau fase selesai dan belum ditangani:
                if (currentPhase.isFinished() && !phaseCompletionHandled) {
                    phaseCompletionHandled = true;

                    // Jalankan onPhaseFinished di EDT (karena ada dialog / update UI)
                    SwingUtilities.invokeLater(() -> {
                        currentPhase.onPhaseFinished(this);
                    });
                }
            }
        });

        timerThread.setDaemon(true);
        timerThread.start();
    }

    // ================== Internal: Helper ==================

    private void switchToNewPhase(PomodoroPhase newPhase) {
        this.currentPhase = newPhase;
        this.phaseCompletionHandled = false;
        updateViewPhaseAndTime(currentPhase);
    }

    /** Update label fase & timer di panel. Sesuaikan dengan method yang ada di MainMenuPanel-mu. */
    private void updateViewPhaseAndTime(PomodoroPhase phase) {
        if (phase == null) {
            view.updatePhaseLabel("-");
            view.updateTimerLabel("00:00");
        } else {
            view.updatePhaseLabel(phase.getPhaseName());
            view.updateTimerLabel(phase.getRemainingTimeFormatted());
        }
    }

    /** Tambahkan elapsedSeconds fase ke akumulasi total untuk history. */
    private void accumulatePhaseElapsed(PomodoroPhase phase) {
        int elapsed = phase.getElapsedSeconds();
        String name = phase.getPhaseName();

        switch (name) {
            case "Work":
                totalWorkSeconds += elapsed;
                break;
            case "Short Break":
                totalShortSeconds += elapsed;
                break;
            case "Long Break":
                totalLongSeconds += elapsed;
                break;
            default:
                // do nothing
                break;
        }
    }

    // ================== Dipanggil dari Phase ==================
    // Metode ini di-trigger oleh onPhaseFinished(...) di masing-masing subclass.

    /** WorkPhase sudah selesai. */
    public void handleWorkPhaseFinished(WorkPhase phase) {
        if (!sessionActive) return;

        // tambahkan waktu fase ini
        accumulatePhaseElapsed(phase);

        // cycle++ sesuai permintaanmu
        cycleCount++;

        int result = JOptionPane.showConfirmDialog(
            view,
            "Work phase selesai.\nLanjut ke Short Break?",
            "Phase Selesai",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            // lanjut ke Short Break
            ShortBreakPhase next = new ShortBreakPhase(shortBreakDurationSec);
            switchToNewPhase(next);
            next.start();
        } else {
            // end session
            endSession();
        }
    }

    /** ShortBreakPhase sudah selesai. */
    public void handleShortBreakPhaseFinished(ShortBreakPhase phase) {
        if (!sessionActive) return;

        accumulatePhaseElapsed(phase);

        if (cycleCount < 4) {
            int result = JOptionPane.showConfirmDialog(
                view,
                "Short Break selesai.\nLanjut ke Work berikutnya?",
                "Phase Selesai",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {
                WorkPhase next = new WorkPhase(workDurationSec);
                switchToNewPhase(next);
                next.start();
            } else {
                endSession();
            }
        } else {
            // cycle == 4 → Long Break
            int result = JOptionPane.showConfirmDialog(
                view,
                "Kamu telah menyelesaikan 4 cycle.\nLanjut ke Long Break?",
                "Phase Selesai",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {
                LongBreakPhase next = new LongBreakPhase(longBreakDurationSec);
                switchToNewPhase(next);
                next.start();
            } else {
                endSession();
            }
        }
    }

    /** LongBreakPhase sudah selesai. */
    public void handleLongBreakPhaseFinished(LongBreakPhase phase) {
        if (!sessionActive) return;

        accumulatePhaseElapsed(phase);

        // reset cycle ke 0 sesuai permintaanmu
        cycleCount = 0;

        int result = JOptionPane.showConfirmDialog(
            view,
            "Long Break selesai.\nApakah ingin lanjut ke Work lagi?",
            "Phase Selesai",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            WorkPhase next = new WorkPhase(workDurationSec);
            switchToNewPhase(next);
            next.start();
        } else {
            endSession();
        }
    }

    // ================== Getter opsional ==================

    public boolean isSessionActive() {
        return sessionActive;
    }

    public int getCycleCount() {
        return cycleCount;
    }

    public String getSessionName() {
        return sessionName;
    }
}

