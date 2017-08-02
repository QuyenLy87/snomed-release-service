package org.ihtsdo.buildcloud.controller;

import org.ihtsdo.buildcloud.controller.helper.IntegrationTestHelper;
import org.ihtsdo.buildcloud.service.tracking.BuildProcessTracker;
import org.ihtsdo.otf.dao.s3.S3Client;
import org.ihtsdo.otf.dao.s3.TestS3Client;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class BuildCancellationTest extends AbstractControllerTest{

    @Autowired
    private S3Client s3Client;

    private IntegrationTestHelper integrationTestHelper;
    
    private String buildURL;

    @Autowired
    private BuildProcessTracker buildProcessTracker;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        integrationTestHelper = new IntegrationTestHelper(mockMvc,"just_package_test");
        ((TestS3Client) s3Client).freshBucketStore();
        integrationTestHelper.loginAsManager();
        integrationTestHelper.createTestProductStructure();
        integrationTestHelper.uploadDeltaInputFile("/der2_Refset_SimpleDelta_INT_20140131.txt", getClass());
        integrationTestHelper.uploadManifest("/just_package_manifest_20140131.xml", getClass());
        integrationTestHelper.setEffectiveTime("20140131");
        integrationTestHelper.setJustPackage(true);
        integrationTestHelper.setFirstTimeRelease(true);
        integrationTestHelper.setReadmeHeader("Header");
        buildURL = integrationTestHelper.createBuild();
    }

    @Test
    public void testCancelBuild() throws Exception {
        System.out.println("Main: " + Thread.currentThread().getId());
        Thread thread = new Thread(triggerBuild());
        thread.start();
        String buildId = buildURL.substring(buildURL.lastIndexOf("/"));
        //buildProcessTracker.cancelBuildProcess("international","just_package_test_Product", buildId);
    }


    private Runnable triggerBuild() throws Exception {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    integrationTestHelper.triggerBuild(buildURL);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        return runnable;
    }
}
