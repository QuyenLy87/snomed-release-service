package org.ihtsdo.buildcloud.controller;

import org.ihtsdo.buildcloud.controller.helper.HypermediaGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping
public class RootController {

	@Autowired
	private HypermediaGenerator hypermediaGenerator;

	private static final String[] ROOT_LINK = {"centres"};

	@RequestMapping
	@ResponseBody
	public Map getRoot(HttpServletRequest request) {
		return hypermediaGenerator.getEntityHypermedia(new HashMap<String, String>(), request, ROOT_LINK);
	}

}