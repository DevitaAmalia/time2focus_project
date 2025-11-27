public class WorkPhase extends PomodoroPhase {

    public WorkPhase(int durationSeconds) {
        super(durationSeconds);
    }

    @Override
    public String getPhaseName() {
        return "Work";
    }

    @Override
    public void onPhaseFinished(PomodoroController controller) {
        controller.handleWorkPhaseFinished(this);
    }
}
