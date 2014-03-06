package org.ihtsdo.buildcloud.service;

import org.ihtsdo.buildcloud.dao.BuildDAO;
import org.ihtsdo.buildcloud.dao.ExecutionDAO;
import org.ihtsdo.buildcloud.entity.Build;
import org.ihtsdo.buildcloud.entity.Execution;
import org.ihtsdo.buildcloud.service.helper.CompositeKeyHelper;
import org.ihtsdo.buildcloud.service.mapping.ExecutionConfigurationJsonGenerator;
import org.ihtsdo.buildcloud.service.maven.MavenExecutor;
import org.ihtsdo.buildcloud.service.maven.MavenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ExecutionServiceImpl implements ExecutionService {

	@Autowired
	private ExecutionDAO dao;

	@Autowired
	private BuildDAO buildDAO;

	@Autowired
	private ExecutionConfigurationJsonGenerator executionConfigurationJsonGenerator;

	@Autowired
	private MavenGenerator mavenGenerator;

	@Autowired
	private MavenExecutor mavenExecutor;

	@Override
	public Execution create(String buildCompositeKey, String authenticatedId) throws IOException {
		Long buildId = CompositeKeyHelper.getId(buildCompositeKey);
		Build build = buildDAO.find(buildId, authenticatedId);

		Date creationDate = new Date();

		Execution execution = new Execution(creationDate, build);

		// Create Build config export
		String jsonConfig = executionConfigurationJsonGenerator.getJsonConfig(execution);

		// Persist export
		dao.save(execution, jsonConfig);

		return execution;
	}

	@Override
	public List<Execution> findAll(String buildCompositeKey, String authenticatedId) {
		Long buildId = CompositeKeyHelper.getId(buildCompositeKey);
		Build build = buildDAO.find(buildId, authenticatedId);
		return dao.findAll(build);
	}

	@Override
	public Execution find(String buildCompositeKey, String executionId, String authenticatedId) {
		Long buildId = CompositeKeyHelper.getId(buildCompositeKey);
		Build build = buildDAO.find(buildId, authenticatedId);
		return dao.find(build, executionId);
	}

	@Override
	public String loadConfiguration(String buildCompositeKey, String executionId, String authenticatedId) throws IOException {
		Long buildId = CompositeKeyHelper.getId(buildCompositeKey);
		Build build = buildDAO.find(buildId, authenticatedId);
		return dao.loadConfiguration(build, executionId);
	}

	@Override
	public void triggerBuild(String buildCompositeKey, String executionId, String authenticatedId) throws IOException {
		Date triggerDate = new Date();

		String executionConfiguration = loadConfiguration(buildCompositeKey, executionId, authenticatedId);

		// Generate poms from config export
		File buildScriptsTmpDirectory = mavenGenerator.generateBuildScripts(executionConfiguration);

		// todo: store generated poms in permanent storage

		// todo: trigger build
	}

}