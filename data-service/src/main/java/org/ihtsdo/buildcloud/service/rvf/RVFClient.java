package org.ihtsdo.buildcloud.service.rvf;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.ihtsdo.buildcloud.dao.io.AsyncPipedStreamBean;
import org.ihtsdo.buildcloud.service.execution.RF2Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.util.concurrent.ExecutionException;

public class RVFClient implements Closeable {

	private final String releaseValidationFrameworkUrl;
	private final CloseableHttpClient httpClient;
	private static final String ERROR_NO_LINES_RECEIVED_FROM_RVF = "Error - No lines received from RVF!";
	private static final String SUCCESS = "Success";

	private static final Logger LOGGER = LoggerFactory.getLogger(RVFClient.class);

	public RVFClient(String releaseValidationFrameworkUrl) {
		this.releaseValidationFrameworkUrl = releaseValidationFrameworkUrl;
		httpClient = HttpClients.createDefault();
	}

	public String checkInputFile(InputStream inputFileStream, String inputFileName, AsyncPipedStreamBean logFileOutputStream) {
		HttpPost post = new HttpPost(releaseValidationFrameworkUrl + "/test-file");
		post.setEntity(MultipartEntityBuilder.create().addPart("file", new InputStreamBody(inputFileStream, inputFileName)).build());

		LOGGER.info("Posting input file {} to RVF for precondition check.", inputFileName);

		try (CloseableHttpResponse response = httpClient.execute(post)) {
			int statusCode = response.getStatusLine().getStatusCode();
			long failureCount = 0;

			try (InputStream content = response.getEntity().getContent();
				 BufferedReader responseReader = new BufferedReader(new InputStreamReader(content));
				 BufferedWriter logWriter = new BufferedWriter(new OutputStreamWriter(logFileOutputStream.getOutputStream()))) {

				String line = responseReader.readLine(); // read header
				if (line != null) {
					while ((line = responseReader.readLine()) != null) {
						if (!line.startsWith(SUCCESS)) {
							failureCount++;
						}
						logWriter.write(line);
						logWriter.write(RF2Constants.LINE_ENDING);
					}
				} else {
					logWriter.write(ERROR_NO_LINES_RECEIVED_FROM_RVF);
					logWriter.write(RF2Constants.LINE_ENDING);
				}
			}
			logFileOutputStream.waitForFinish();

			if (200 == statusCode) {
				if (failureCount == 0) {
					return null;
				} else {
					return "There were " + failureCount + " RVF test failures.";
				}
			} else {
				String errorMessage = "Response HTTP status code " + statusCode;
				LOGGER.info("RVF Service failure: {}", errorMessage);
				return errorMessage;
			}
		} catch (InterruptedException | ExecutionException | IOException e) {
			String errorMessage = "Failed to check input file against RVF: " + inputFileName + " due to " + e.getMessage();
			LOGGER.error(errorMessage, e);
			try (OutputStream logOutputStream = logFileOutputStream.getOutputStream()) {
				StreamUtils.copy(errorMessage.getBytes(), logOutputStream);
				logFileOutputStream.waitForFinish();
			} catch (Exception e2) {
				LOGGER.error("Failed to write exception to log.", e);
			}
			return "RVF Client error. See logs for details.";
		} finally {
			LOGGER.info("RVF precondition check of {} complete.", inputFileName);
		}
	}

	@Override
	public void close() throws IOException {
		httpClient.close();
	}

}