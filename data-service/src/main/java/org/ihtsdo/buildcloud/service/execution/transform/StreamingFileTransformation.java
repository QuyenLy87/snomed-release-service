package org.ihtsdo.buildcloud.service.execution.transform;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.ihtsdo.buildcloud.service.execution.RF2Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamingFileTransformation {

	private final List<LineTransformation> lineTransformations;

	private static final Logger LOGGER = LoggerFactory.getLogger(StreamingFileTransformation.class);

	public StreamingFileTransformation() {
		lineTransformations = new ArrayList<>();
	}

	public void transformFile(final InputStream inputStream, final OutputStream outputStream, final String fileName) throws IOException,
			TransformationException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, RF2Constants.UTF_8));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, RF2Constants.UTF_8));
		try {
			LOGGER.info("Start: Transform file {}.", fileName);
			// Iterate input lines
			String line;
			StringBuilder stringBuilder = new StringBuilder();
			boolean firstLine = true;
			while ((line = reader.readLine()) != null) {
				if (firstLine) {
					firstLine = false;
					writer.write(line);
					writer.write(RF2Constants.LINE_ENDING);
				} else {

					// Split column values
					String[] columnValues = line.split(RF2Constants.COLUMN_SEPARATOR);

					// Pass line through all transformers
					for (LineTransformation lineTransformation : lineTransformations) {
						lineTransformation.transformLine(columnValues);
					}

					// Write transformed line to temp file
					stringBuilder.setLength(0);// reuse StringBuilder
					for (int a = 0; a < columnValues.length; a++) {
						if (a > 0) {
							stringBuilder.append(RF2Constants.COLUMN_SEPARATOR);
						}
						stringBuilder.append(columnValues[a]);
					}
					stringBuilder.append(RF2Constants.LINE_ENDING);
					writer.write(stringBuilder.toString());
				}
			}
			LOGGER.info("Finish: Transform file {}.", fileName);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				LOGGER.error("Failed to close writer.", e);
			}
			try {
				reader.close();
			} catch (IOException e) {
				LOGGER.error("Failed to close reader.", e);
			}
		}
	}

	public StreamingFileTransformation addLineTransformation(final LineTransformation transformation) {
		lineTransformations.add(transformation);
		return this;
	}

	public List<LineTransformation> getLineTransformations() {
		return lineTransformations;
	}

}
