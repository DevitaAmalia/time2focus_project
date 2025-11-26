public class ShortBreakPhase extends PomodoroPhase {

    public ShortBreakPhase(int durationSeconds) {
        super(durationSeconds);
    }

    @Override
    public String getPhaseName() {
        return "Short Break";
    }

    @Override
    public void onPhaseFinished(PomodoroController controller) {
    }
}

