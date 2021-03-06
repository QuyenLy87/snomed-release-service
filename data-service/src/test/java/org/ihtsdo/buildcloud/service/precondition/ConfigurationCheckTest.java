package org.ihtsdo.buildcloud.service.precondition;

import org.ihtsdo.buildcloud.entity.BuildConfiguration;
import org.ihtsdo.buildcloud.entity.PreConditionCheckReport;
import org.ihtsdo.buildcloud.entity.PreConditionCheckReport.State;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;

public class ConfigurationCheckTest extends PreconditionCheckTest {
	
	private static final String README_HEADER = "readmeHeader";
	private static final String PUBLISHED_PACKAGE_IN_JAN = "SnomedCT_Release_INT_20140131.zip";
	private static final String PUBLISHED_PACKAGE_IN_JULY = "SnomedCT_Release_INT_20140731.zip";
	private static final String INVALID_PUBLISHED_PAKCAGE_NAME = "Invalid_201407.zip";

	@Override
	@Before
	public void setup() throws Exception {
		super.setup();
		manager = new PreconditionManager().preconditionChecks(new ConfigurationCheck());
	}

	@Test
	public void testFirstReleaseConfiguredCorrectly() throws InstantiationException, IllegalAccessException, ParseException {
		BuildConfiguration buildConfiguration = product.getBuildConfiguration();
		buildConfiguration.setFirstTimeRelease(true);
		buildConfiguration.setPreviousPublishedPackage(null);
		buildConfiguration.setReadmeHeader(README_HEADER);
		buildConfiguration.setReadmeEndDate(JULY_RELEASE);

		PreConditionCheckReport report = runPreConditionCheck(ConfigurationCheck.class);
		State actualResult = report.getResult();
		Assert.assertEquals(State.PASS, actualResult);	
		Assert.assertEquals("", report.getMessage());
	}
	
	@Test
	public void testSubsequentReleaseConfiguredCorrectly() throws InstantiationException, IllegalAccessException, ParseException {
		BuildConfiguration buildConfiguration = product.getBuildConfiguration();
		buildConfiguration.setFirstTimeRelease(false);
		buildConfiguration.setPreviousPublishedPackage(PUBLISHED_PACKAGE_IN_JAN);
		buildConfiguration.setReadmeHeader(README_HEADER);
		buildConfiguration.setReadmeEndDate(JULY_RELEASE);

		PreConditionCheckReport report = runPreConditionCheck(ConfigurationCheck.class);
		State actualResult = report.getResult();
		Assert.assertEquals(State.PASS, actualResult);	
		Assert.assertEquals("", report.getMessage());

	}
	
	@Test
	public void testFirstReleaseConfiguredIncorrectly() throws InstantiationException, IllegalAccessException {
		BuildConfiguration buildConfiguration = product.getBuildConfiguration();
		buildConfiguration.setFirstTimeRelease(true);
		buildConfiguration.setPreviousPublishedPackage(PUBLISHED_PACKAGE_IN_JAN);

		PreConditionCheckReport report = runPreConditionCheck(ConfigurationCheck.class);
		Assert.assertEquals( State.FAIL, report.getResult());
	}
	
	
	@Test
	public void testSubsequentConfiguredIncorrectly() throws InstantiationException, IllegalAccessException {
		BuildConfiguration buildConfiguration = product.getBuildConfiguration();
		buildConfiguration.setFirstTimeRelease(false);
		buildConfiguration.setPreviousPublishedPackage(null);
		buildConfiguration.setReadmeHeader(README_HEADER);
		buildConfiguration.setReadmeEndDate(JULY_RELEASE);

		PreConditionCheckReport report = runPreConditionCheck(ConfigurationCheck.class);
		Assert.assertEquals(State.FAIL, report.getResult());	
	}
	
	
	@Test
	public void testMissingEffectiveTime() throws InstantiationException, IllegalAccessException {
		BuildConfiguration buildConfiguration = product.getBuildConfiguration();
		buildConfiguration.setEffectiveTime(null);
		buildConfiguration.setFirstTimeRelease(true);
		buildConfiguration.setPreviousPublishedPackage(null);
		buildConfiguration.setReadmeHeader(README_HEADER);
		buildConfiguration.setReadmeEndDate(JULY_RELEASE);

		PreConditionCheckReport report = runPreConditionCheck(ConfigurationCheck.class);
		Assert.assertEquals(State.FAIL, report.getResult());	
	}
	
	@Test
	public void testMissingReadmeHeader() throws InstantiationException, IllegalAccessException {
		BuildConfiguration buildConfiguration = product.getBuildConfiguration();
		buildConfiguration.setFirstTimeRelease(true);
		buildConfiguration.setPreviousPublishedPackage(null);
		buildConfiguration.setReadmeEndDate(JULY_RELEASE);

		PreConditionCheckReport report = runPreConditionCheck(ConfigurationCheck.class);
		Assert.assertEquals(State.FAIL, report.getResult());	
	}
	
	@Test
	public void testMissingReadmeEndDate() throws InstantiationException, IllegalAccessException {
		BuildConfiguration buildConfiguration = product.getBuildConfiguration();
		buildConfiguration.setFirstTimeRelease(true);
		buildConfiguration.setPreviousPublishedPackage(null);
		buildConfiguration.setReadmeHeader(README_HEADER);

		PreConditionCheckReport report = runPreConditionCheck(ConfigurationCheck.class);
		Assert.assertEquals(State.FAIL, report.getResult());	
	}
	
	@Test
	public void testPreviousPublishedReleaseDateIsNotBeforeCurrentReleaseDate() throws InstantiationException, IllegalAccessException {
		BuildConfiguration buildConfiguration = product.getBuildConfiguration();
		buildConfiguration.setFirstTimeRelease(false);
		buildConfiguration.setPreviousPublishedPackage(PUBLISHED_PACKAGE_IN_JULY);
		buildConfiguration.setReadmeHeader(README_HEADER);
		buildConfiguration.setReadmeEndDate(JULY_RELEASE);

		PreConditionCheckReport report = runPreConditionCheck(ConfigurationCheck.class);
		Assert.assertEquals(State.FAIL, report.getResult());	
	}
	
	
	@Test
	public void testInvalidPublishedPackageName() throws InstantiationException, IllegalAccessException {
		BuildConfiguration buildConfiguration = product.getBuildConfiguration();
		buildConfiguration.setFirstTimeRelease(false);
		buildConfiguration.setPreviousPublishedPackage(INVALID_PUBLISHED_PAKCAGE_NAME);
		buildConfiguration.setReadmeHeader(README_HEADER);
		buildConfiguration.setReadmeEndDate(JULY_RELEASE);

		PreConditionCheckReport report = runPreConditionCheck(ConfigurationCheck.class);
		Assert.assertEquals(State.FAIL, report.getResult());	
	}

	
	@Test
	public void testAllMissingForSubsequentRelease() throws InstantiationException, IllegalAccessException {
		BuildConfiguration buildConfiguration = product.getBuildConfiguration();
		buildConfiguration.setFirstTimeRelease(false);

		PreConditionCheckReport report = runPreConditionCheck(ConfigurationCheck.class);
		Assert.assertEquals(State.FAIL, report.getResult());	
		Assert.assertEquals("Subsequent releases must have a previous published package specified. The copyright end date is not set. No Readme Header detected.",
				report.getMessage());
	}

	@Test
	public void testAllMissingForFirstTimeRelease() throws InstantiationException, IllegalAccessException {
		BuildConfiguration buildConfiguration = product.getBuildConfiguration();
		buildConfiguration.setFirstTimeRelease(true);

		PreConditionCheckReport report = runPreConditionCheck(ConfigurationCheck.class);
		Assert.assertEquals(State.FAIL, report.getResult());
		Assert.assertEquals("The copyright end date is not set. No Readme Header detected.", report.getMessage());
	}

}
