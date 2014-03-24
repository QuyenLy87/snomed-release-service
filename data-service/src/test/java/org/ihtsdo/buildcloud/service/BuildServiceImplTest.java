package org.ihtsdo.buildcloud.service;

import java.util.EnumSet;
import java.util.List;

import org.ihtsdo.buildcloud.entity.Build;
import org.ihtsdo.buildcloud.entity.helper.EntityHelper;
import org.ihtsdo.buildcloud.entity.helper.TestEntityGenerator;
import org.ihtsdo.buildcloud.service.helper.FilterOption;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext.xml"})
@Transactional
public class BuildServiceImplTest extends TestEntityGenerator {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BuildServiceImplTest.class);
	
	public static final String AUTHENTICATED_ID = "test";
	
	@Autowired
	private BuildService bs;
	
	@Test
	public void testFindForExtension() {
		EnumSet<FilterOption> filterOff = EnumSet.noneOf(FilterOption.class);
		List<Build> builds = bs.findForExtension(EntityHelper.formatAsBusinessKey(releaseCentreShortNames[0]), 
												 EntityHelper.formatAsBusinessKey(extensionNames[0]), 
												 filterOff, 
												 AUTHENTICATED_ID);
		Assert.assertEquals(TestEntityGenerator.buildCount[0], builds.size());
	}
	
	@Test
	public void testFindForExtension_Filtered() {
		EnumSet<FilterOption> filterOn = EnumSet.of(FilterOption.STARRED_ONLY);
		List<Build> builds = bs.findForExtension(EntityHelper.formatAsBusinessKey(releaseCentreShortNames[0]), 
												 EntityHelper.formatAsBusinessKey(extensionNames[0]), 
												 filterOn, 
												 AUTHENTICATED_ID);
		Assert.assertEquals(TestEntityGenerator.starredCount[0], builds.size());
	}

	@Test
	public void testCreate() throws Exception{

		Assert.assertNotNull(bs);
		EnumSet<FilterOption> filterOptions = EnumSet.of(FilterOption.INCLUDE_REMOVED);
		List<Build> builds = bs.findAll(filterOptions, AUTHENTICATED_ID);
		int before = builds.size();
		//LOGGER.warn("Found " + before + " builds");
		Assert.assertTrue(before > 0);  //Check our test data is in there.
		bs.create(EntityHelper.formatAsBusinessKey(releaseCentreShortNames[0]), 
				  EntityHelper.formatAsBusinessKey(extensionNames[0]), 
				  EntityHelper.formatAsBusinessKey(productNames[0][0]), 
				  "my test build name", 
				  AUTHENTICATED_ID);
		int after = bs.findAll(filterOptions, AUTHENTICATED_ID).size();
		Assert.assertEquals(before + 1, after);
		
		//TODO Could add further tests to ensure that the new item was created at the correct point in the hierarchy
	}
	
	@Test
	public void testStarredFilter() throws Exception{

		EnumSet<FilterOption> filterOff = EnumSet.noneOf(FilterOption.class);
		EnumSet<FilterOption> filterOn = EnumSet.of(FilterOption.STARRED_ONLY);
		List<Build> builds = bs.findAll(filterOff, AUTHENTICATED_ID);
		int allBuildCount = builds.size();
		
		Assert.assertEquals(TestEntityGenerator.totalBuildCount, allBuildCount);  //Check our test data is in there.
		int starredBuildCount = bs.findAll(filterOn, AUTHENTICATED_ID).size();
		Assert.assertEquals(TestEntityGenerator.totalStarredBuilds, starredBuildCount);
	}	
	
	@Test
	public void testRemovedFilter() throws Exception{

		EnumSet<FilterOption> filterOff = EnumSet.noneOf(FilterOption.class);
		EnumSet<FilterOption> filterOn = EnumSet.of(FilterOption.INCLUDE_REMOVED);
		List<Build> builds = bs.findAll(filterOff, AUTHENTICATED_ID);
		int visibleBuildCount = builds.size();
		Assert.assertTrue(visibleBuildCount > 0);
		
		int includeRemovedCount = bs.findAll(filterOn, AUTHENTICATED_ID).size();
		Assert.assertTrue(includeRemovedCount > 0);
		
		//TODO When remove functionality is built, use it to remove a build and check
		//that our build count goes up if we inclue removed builds.
	}		

}