package org.ihtsdo.buildcloud.controller;

import org.ihtsdo.buildcloud.controller.helper.HypermediaGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/version")
@Api(value = "Version", position = 5)
public class VersionController {

	public static final String VERSION_FILE_PATH = "/var/opt/snomed-release-service-api/version.txt";

	@Autowired
	private HypermediaGenerator hypermediaGenerator;

	private String versionString;

	@RequestMapping( method = RequestMethod.GET )
	@ApiOperation( value = "Returns version of current deployment",
		notes = "Returns the software-build version as captured during installation (deployment using ansible)" )
	@ResponseBody
	public Map<String, Object> getVersion(HttpServletRequest request) throws IOException {
		Map<String, String> entity = new HashMap<>();
		entity.put("package_version", getVersionString());
		return hypermediaGenerator.getEntityHypermedia(entity, true, request, new String[] {});
	}

	private String getVersionString() throws IOException {
		if (this.versionString == null) {
			String versionString = "";
			File file = new File(VERSION_FILE_PATH);
			if (file.isFile()) {
				try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
					versionString = bufferedReader.readLine();
				}
			} else {
				versionString = "Version information not found.";
			}
			this.versionString = versionString;
		}
		return versionString;
	}

}
