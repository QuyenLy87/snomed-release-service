package org.ihtsdo.buildcloud.service.tracking;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BuildProcessTracker {

    private Map<String, BuildProcess> buildProcesses = new HashMap<>();

    public void trackBuildProcess(BuildProcess buildProcess) {
        buildProcess.setStatus(BuildProcessStatus.STARTED);
        buildProcesses.put(buildProcess.getKey(), buildProcess);
    }

    public void untrackBuildProcess(BuildProcess buildProcess) {
        String key = buildProcess.getKey();
        if(buildProcesses.containsKey(key)) {
            buildProcesses.remove(key);
        }
    }

    public void cancelBuildProcess(String releaseCenter, String product, String buildId) {
        String key = BuildProcess.buildKey(releaseCenter, product, buildId);
        BuildProcess buildProcess = buildProcesses.get(key);
        if(buildProcess != null && buildProcess.getStatus().equals(BuildProcessStatus.STARTED)) {
            Long threadId = buildProcess.getThreadId();
            Set<Thread> threads = Thread.getAllStackTraces().keySet();
            for (Thread thread : threads) {
                if(thread.getId() == threadId) {
                    thread.interrupt();
                }
            }
        }
    }

    public Map<String, BuildProcess> getBuildProcesses() {
        return buildProcesses;
    }
}
