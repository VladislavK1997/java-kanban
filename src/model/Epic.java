package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();

    private LocalDateTime endTime;

    public Epic(int id, String name, String description) {
        super(id, name, description, TaskStatus.NEW, Duration.ZERO, null);
    }

    public Epic(Epic other) {
        super(other.getId(), other.getName(), other.getDescription(), other.getStatus(), other.getDuration(), other.getStartTime());
        this.subtaskIds.addAll(other.getSubtaskIds());
        this.endTime = other.getEndTime();
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int id) {
        subtaskIds.add(id);
    }

    public void removeSubtaskId(int id) {
        subtaskIds.remove(Integer.valueOf(id));
    }

    public void updateTimeAndDuration(List<Subtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) {
            super.setDuration(Duration.ZERO);
            super.setStartTime(null);
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
        super.setDuration(total);
        super.setStartTime(earliest);
        endTime = latest;
    }

    @Override
    public Duration getDuration() {
        return super.getDuration();
    }

    @Override
    public LocalDateTime getStartTime() {
        return super.getStartTime();
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}

