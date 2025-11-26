public class LongBreakPhase extends PomodoroPhase {

    public LongBreakPhase(int durationSeconds) {
        super(durationSeconds);
    }

    @Override
    public String getPhaseName() {
        return "Long Break";
    }

    @Override
    public void onPhaseFinished(PomodoroController controller) {
    }
}

