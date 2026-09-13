package com.Lilith.Curtain.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanExecution {

    private final Map<Long, List<Executed>> executedMap;

    public PlanExecution() {
        this.executedMap = new HashMap<Long, List<Executed>>();
    }

    public void post(long time, Executed executed) {
        List<Executed> executedList = this.executedMap.get(time);
        if (executedList == null) {
            executedList = new ArrayList<Executed>();
            this.executedMap.put(time, executedList);
        }
        executedList.add(executed);
    }

    public void execute(long time) {
        List<Executed> executedList = this.executedMap.remove(time);
        if (executedList == null) return;
        for (Executed executed : executedList) {
            executed.execute(time);
        }
    }

    @FunctionalInterface
    public interface Executed {

        void execute(long time);
    }
}
