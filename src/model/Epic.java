package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();
    private Duration duration = Duration.ZERO;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Epic(int id, String name, String description) {
        super(id, name, description, TaskStatus.NEW, Duration.ZERO, null);
    }

    public List<Integer> getSubtaskIds() { return subtaskIds; }
    public void addSubtaskId(int id) { subtaskIds.add(id); }
    public void removeSubtaskId(int id) { subtaskIds.remove(Integer.valueOf(id)); }

    public void updateTimeAndDuration(List<Subtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) {
            duration = Duration.ZERO;
            startTime = null;
            endTime = null;
            return;
        }
        Duration total = Duration.ZERO;
        LocalDateTime earliest = null;
        LocalDateTime latest = null;
        for (Subtask s : subtasks) {
            if (s.getStartTime() == null || s.getDuration() == null) continue;
            total = total.plus(s.getDuration());
            if (earliest == null || s.getStartTime().isBefore(earliest)) earliest = s.getStartTime();
            LocalDateTime end = s.getEndTime();
            if (latest == null || (end != null && end.isAfter(latest))) latest = end;
        }
        duration = total;
        startTime = earliest;
        endTime = latest;
    }

    @Override public Duration getDuration() { return duration; }
    @Override public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
}

