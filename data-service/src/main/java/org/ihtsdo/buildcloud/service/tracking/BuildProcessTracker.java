package org.ihtsdo.buildcloud.service.tracking;

import org.ihtsdo.buildcloud.dao.helper.BuildS3PathHelper;
import org.ihtsdo.otf.dao.s3.S3Client;
import org.ihtsdo.otf.dao.s3.helper.FileHelper;
import org.ihtsdo.otf.dao.s3.helper.S3ClientHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BuildProcessTracker {

    private Map<String, BuildProcess> buildProcesses = new HashMap<>();

    @Autowired
    private BuildS3PathHelper s3PathHelper;

    private FileHelper fileHelper;

    private Logger LOGGER = LoggerFactory.getLogger(BuildProcessTracker.class);

    @Autowired
    public BuildProcessTracker(final String buildBucketName, final S3Client s3Client, final S3ClientHelper s3ClientHelper) {
        fileHelper = new FileHelper(buildBucketName, s3Client, s3ClientHelper);
    }

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

    public boolean cancelBuildProcess(String releaseCenter, String product, String buildId) {
        LOGGER.info("Start cancelling build {}", buildId);
        boolean processKilled = false;
        String key = BuildProcess.buildKey(releaseCenter, product, buildId);
        BuildProcess buildProcess = buildProcesses.get(key);
        if(buildProcess != null && buildProcess.getStatus().equals(BuildProcessStatus.STARTED)) {
            Long threadId = buildProcess.getThreadId();
            Set<Thread> threads = Thread.getAllStackTraces().keySet();
            for (Thread thread : threads) {
                if(thread.getId() == threadId) {
                    thread.interrupt();
                    LOGGER.info("Build {} is cancelled", buildId);
                    processKilled = true;
                    break;
                }
            }
            //Delete output directory
            String outputPath = s3PathHelper.getOutputFilesPath(buildProcess.getBuild());
            List<String> files = fileHelper.listFiles(outputPath);
            for (String file : files) {
                String outputFile = outputPath + file;
                LOGGER.info("Remove output file {}", outputFile);
                fileHelper.deleteFile(outputFile);

            }
        }
        return processKilled;
    }

    public Map<String, BuildProcess> getBuildProcesses() {
        return buildProcesses;
    }
}
