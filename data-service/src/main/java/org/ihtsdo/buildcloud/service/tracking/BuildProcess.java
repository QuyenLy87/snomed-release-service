package org.ihtsdo.buildcloud.service.tracking;

public class BuildProcess {

    private String releaseCenterKey;
    private String productKey;
    private String buildId;
    private Long threadId;
    private BuildProcessStatus status;

    public BuildProcess(String releaseCenterKey, String productKey, String buildId, Long threadId) {
        this.releaseCenterKey = releaseCenterKey;
        this.productKey = productKey;
        this.buildId = buildId;
        this.threadId = threadId;
    }

    public String getReleaseCenterKey() {
        return releaseCenterKey;
    }

    public void setReleaseCenterKey(String releaseCenterKey) {
        this.releaseCenterKey = releaseCenterKey;
    }

    public String getProductKey() {
        return productKey;
    }

    public void setProductKey(String productKey) {
        this.productKey = productKey;
    }

    public String getBuildId() {
        return buildId;
    }

    public void setBuildId(String buildId) {
        this.buildId = buildId;
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public BuildProcessStatus getStatus() {
        return status;
    }

    public void setStatus(BuildProcessStatus status) {
        this.status = status;
    }

    public String getKey() {
        return buildKey(this.releaseCenterKey, this.productKey, this.buildId);
    }

    public static String buildKey(String releaseCenterKey, String productKey, String buildId) {
        return releaseCenterKey + "_" + productKey + "_" + buildId;
    }
}
