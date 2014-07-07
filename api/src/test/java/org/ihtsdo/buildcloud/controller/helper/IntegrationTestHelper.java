package org.ihtsdo.buildcloud.controller.helper;

import com.jayway.jsonpath.JsonPath;

import org.apache.commons.codec.binary.Base64;
import org.ihtsdo.buildcloud.controller.AbstractControllerTest;
import org.ihtsdo.buildcloud.service.BuildService;
import org.ihtsdo.buildcloud.service.PackageService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class IntegrationTestHelper {

	public static final String PRODUCT_URL = "/centers/international/extensions/snomed_ct_international_edition/products/nlm_example_refset";
	public static final String TEST_PACKAGE = "testpackage";
	public static final String BUILD_NAME = "test-build";
	public static final String BUILD_ID = "10_testbuild";
	public static final String PACKAGE_URL = "/builds/" + BUILD_ID + "/packages/" + TEST_PACKAGE;

	private MockMvc mockMvc;
	private String basicDigestHeaderValue;

	public IntegrationTestHelper(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
		basicDigestHeaderValue = "NOT_YET_AUTHENTICATED"; // initial value only
	}

	public void loginAsManager() throws Exception {
		MvcResult loginResult = mockMvc.perform(
				post("/login")
						.param("username", "manager")
						.param("password", "test123")
		)
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(AbstractControllerTest.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.authenticationToken", notNullValue()))
				.andReturn();

		String authenticationToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.authenticationToken");
		basicDigestHeaderValue = "Basic " + new String(Base64.encodeBase64((authenticationToken + ":").getBytes()));
	}

	public void createTestBuildStructure() throws Exception {
		// Create Build
		mockMvc.perform(
				post(PRODUCT_URL + "/builds")
						.header("Authorization", getBasicDigestHeaderValue())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{ \"name\" : \"" + BUILD_NAME + "\" }")
		)
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(content().contentType(AbstractControllerTest.APPLICATION_JSON_UTF8))
				.andReturn();

		// Create Package
		mockMvc.perform(
				post("/builds/" + BUILD_ID + "/packages")
						.header("Authorization", getBasicDigestHeaderValue())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{ \"name\" : \"" + TEST_PACKAGE + "\" }")
		)
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(AbstractControllerTest.APPLICATION_JSON_UTF8));
	}

	public void uploadDeltaInputFile(String deltaFileName, Class classpathResourceOwner) throws Exception {
		MockMultipartFile deltaFile = new MockMultipartFile("file", deltaFileName, "text/plain", classpathResourceOwner.getResourceAsStream(deltaFileName));
		mockMvc.perform(
				fileUpload(PACKAGE_URL + "/inputfiles")
						.file(deltaFile)
						.header("Authorization", getBasicDigestHeaderValue())
		)
				.andDo(print())
				.andExpect(status().isOk());
	}
	
	public void publishFile(String publishFileName, Class classpathResourceOwner, HttpStatus expectedStatus) throws Exception {
		MockMultipartFile publishFile = new MockMultipartFile("file", publishFileName, "text/plain", classpathResourceOwner.getResourceAsStream(publishFileName));
		mockMvc.perform(
				fileUpload(PRODUCT_URL + "/published")
						.file(publishFile)
						.header("Authorization", getBasicDigestHeaderValue())
		)
				.andDo(print())
				.andExpect(status().is(expectedStatus.value()));
	}

	public void deletePreviousTxtInputFiles() throws Exception {
		mockMvc.perform(
				request(HttpMethod.DELETE, PACKAGE_URL + "/inputfiles/*.txt")
						.header("Authorization", getBasicDigestHeaderValue())
		)
				.andDo(print())
				.andExpect(status().isNoContent());
	}

	public void uploadManifest(String manifestFileName, Class classpathResourceOwner) throws Exception {
		MockMultipartFile manifestFile = new MockMultipartFile("file", manifestFileName, "text/plain", classpathResourceOwner.getResourceAsStream(manifestFileName));
		mockMvc.perform(
				fileUpload(PACKAGE_URL + "/manifest")
						.file(manifestFile)
						.header("Authorization", getBasicDigestHeaderValue())
		)
				.andDo(print())
				.andExpect(status().isOk());
	}

	public void setEffectiveTime(String effectiveDate) throws Exception {
		String jsonContent = "{ " + jsonPair(BuildService.EFFECTIVE_TIME, getEffectiveDateWithSeparators(effectiveDate)) + " }";
		mockMvc.perform(
				request(HttpMethod.PATCH, "/builds/" + BUILD_ID)
						.header("Authorization", getBasicDigestHeaderValue())
						.contentType(MediaType.APPLICATION_JSON)
						.content(jsonContent)
		)
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(AbstractControllerTest.APPLICATION_JSON_UTF8));
	}

	private String getEffectiveDateWithSeparators(String effectiveDate) {
		return effectiveDate.substring(0, 4) + "-" + effectiveDate.substring(4, 6) + "-" + effectiveDate.substring(6, 8);
	}

	/*
		 * @return a string formatted for use as a JSON key/value pair eg 	"\"effectiveTime\" : \""+ effectiveDate + "\","
		 * with a trailing comma just in case you want more than one and json is OK with that if there's only one
		 */
	public String jsonPair(String key, String value) {
		return "  \"" + key + "\" : \"" + value + "\" ";
	}

	public void setFirstTimeRelease(boolean isFirstTime) throws Exception {
		String jsonContent = "{ " + jsonPair(PackageService.FIRST_TIME_RELEASE, Boolean.toString(isFirstTime)) + " }";
		mockMvc.perform(
				request(HttpMethod.PATCH, PACKAGE_URL)
						.header("Authorization", getBasicDigestHeaderValue())
						.contentType(MediaType.APPLICATION_JSON)
						.content(jsonContent)
		)
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(AbstractControllerTest.APPLICATION_JSON_UTF8));
	}

	public void setPreviousPublishedPackage(String previousPublishedFile) throws Exception {
		String jsonContent = "{ " + jsonPair(PackageService.PREVIOUS_PUBLISHED_PACKAGE, previousPublishedFile) + " }";
		mockMvc.perform(
				request(HttpMethod.PATCH, PACKAGE_URL)
						.header("Authorization", getBasicDigestHeaderValue())
						.contentType(MediaType.APPLICATION_JSON)
						.content(jsonContent)
		)
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(AbstractControllerTest.APPLICATION_JSON_UTF8));

	}

	public void setReadmeHeader(String readmeHeader) throws Exception {
		mockMvc.perform(
				request(HttpMethod.PATCH, PACKAGE_URL)
						.header("Authorization", getBasicDigestHeaderValue())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{ \"readmeHeader\" : \"" + readmeHeader + "\" }")
		)
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(AbstractControllerTest.APPLICATION_JSON_UTF8));
	}

	public String createExecution() throws Exception {
		MvcResult createExecutionResult = mockMvc.perform(
				post("/builds/" + BUILD_ID + "/executions")
						.header("Authorization", getBasicDigestHeaderValue())
						.contentType(MediaType.APPLICATION_JSON)
		)
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(AbstractControllerTest.APPLICATION_JSON_UTF8))
				.andReturn();

		String executionId = JsonPath.read(createExecutionResult.getResponse().getContentAsString(), "$.id");
		return "/builds/" + BUILD_ID + "/executions/" + executionId;
	}

	public void triggerExecution(String executionURL) throws Exception {
		mockMvc.perform(
				post(executionURL + "/trigger")
						.header("Authorization", getBasicDigestHeaderValue())
						.contentType(MediaType.APPLICATION_JSON)
		)
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(AbstractControllerTest.APPLICATION_JSON_UTF8));
	}

	public void publishOutput(String executionURL) throws Exception {
		mockMvc.perform(
				post(executionURL + "/output/publish")
						.header("Authorization", getBasicDigestHeaderValue())
						.contentType(MediaType.APPLICATION_JSON)
		)
				.andDo(print())
				.andExpect(status().isOk());
	}

	public List<String> getZipEntryPaths(ZipFile zipFile) {
		List<String> entryPaths = new ArrayList<>();
		Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
		while (zipEntries.hasMoreElements()) {
			entryPaths.add(zipEntries.nextElement().getName());
		}
		return entryPaths;
	}

	public String getPreviousPublishedPackage() throws Exception {

		//Recover URL of published things from Product
		MvcResult productResult = mockMvc.perform(
				post(PRODUCT_URL)
						.header("Authorization", getBasicDigestHeaderValue())
						.contentType(MediaType.APPLICATION_JSON)
		)
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(AbstractControllerTest.APPLICATION_JSON_UTF8))
				.andReturn();

		String publishedURL = JsonPath.read(productResult.getResponse().getContentAsString(), "$.published_url");
		String expectedURL = "http://localhost:80/centers/international/extensions/snomed_ct_international_edition/products/nlm_example_refset/published";

		Assert.assertEquals(expectedURL, publishedURL);

		//Recover list of published packages
		MvcResult publishedResult = mockMvc.perform(
				post(publishedURL)
						.header("Authorization", getBasicDigestHeaderValue())
						.contentType(MediaType.APPLICATION_JSON)
		)
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(AbstractControllerTest.APPLICATION_JSON_UTF8))
				.andReturn();

		return JsonPath.read(publishedResult.getResponse().getContentAsString(), "$.publishedPackages[0]");
	}

	public ZipFile testZipNameAndEntryNames(String executionURL, int expectedOutputFileCount, String expectedZipFilename, String expectedZipEntries, Class classpathResourceOwner) throws Exception {
		MvcResult outputFileListResult = mockMvc.perform(
				get(executionURL + "/packages/" + TEST_PACKAGE + "/outputfiles")
						.header("Authorization", getBasicDigestHeaderValue())
						.contentType(MediaType.APPLICATION_JSON)
		)
				.andDo(print())
				.andExpect(status().isOk())
				.andReturn();

		String outputFileListJson = outputFileListResult.getResponse().getContentAsString();
		JSONArray jsonArray = new JSONArray(outputFileListJson);
		Assert.assertEquals(expectedOutputFileCount, jsonArray.length());

		String zipFilePath = null;
		for (int a = 0; a < jsonArray.length(); a++) {
			JSONObject file = (JSONObject) jsonArray.get(a);
			String filePath = file.getString("id");
			if (filePath.endsWith(".zip")) {
				zipFilePath = filePath;
			}
		}

		Assert.assertEquals(expectedZipFilename, zipFilePath);

		ZipFile zipFile = new ZipFile(downloadToTempFile(executionURL, zipFilePath, classpathResourceOwner));
		List<String> entryPaths = getZipEntryPaths(zipFile);
		Assert.assertEquals("Zip entries expected.",
				expectedZipEntries,
				entryPaths.toString().replace(", ", "\n").replace("[", "").replace("]", ""));
		return zipFile;
	}

	private File downloadToTempFile(String executionURL, String zipFilePath, Class classpathResourceOwner) throws Exception {
		MvcResult outputFileResult = mockMvc.perform(
				get(executionURL + "/packages/" + TEST_PACKAGE + "/outputfiles/" + zipFilePath)
						.header("Authorization", getBasicDigestHeaderValue())
						.contentType(MediaType.APPLICATION_JSON)
		)
				.andExpect(status().isOk())
				.andReturn();

		Path tempFile = Files.createTempFile(classpathResourceOwner.getCanonicalName(), "output-file");
		try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile.toFile())) {
			fileOutputStream.write(outputFileResult.getResponse().getContentAsByteArray());
		}
		return tempFile.toFile();
	}

	public String getBasicDigestHeaderValue() {
		return basicDigestHeaderValue;
	}

}