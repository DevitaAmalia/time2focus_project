public class LongBreakPhase extends PomodoroPhase {

    public LongBreakPhase(int durationSeconds) {
        super(durationSeconds);
    }

    @Override
    public String getPhaseName() {
        return "long break";
    }

    @Override
    public void onPhaseFinished(PomodoroController controller) {
        controller.handleLongBreakPhaseFinished(this);
    }
}
