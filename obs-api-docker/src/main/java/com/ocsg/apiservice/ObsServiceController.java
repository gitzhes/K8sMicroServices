package com.ocsg.apiservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import repkg.com.amazonaws.AmazonClientException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;


@RestController
public class ObsServiceController {
	@Autowired
	ObsService obsService;

	@RequestMapping("/")
	public String home() {
		return "Docker Bucket API";
	}

	@RequestMapping(value = "/service/createFileDir/**")
	public void createFileDir(HttpServletRequest request) throws AmazonClientException, IOException {
		String key = request.getRequestURI().replaceAll("/service/createFileDir/", "");
		obsService.createFileDir(key);
	}
/*
	@RequestMapping(value ="/service/uploadDir/**", method = RequestMethod.GET)
	public void uploadDir(HttpServletRequest request) throws AmazonClientException, IOException {
		String key = request.getRequestURI().replaceAll("/service/upload/", "");

		obsService.uploadDir(key);
	}
	
	@RequestMapping(value = "/service/uploadFile/**", method = RequestMethod.GET)
	public void uploadFile(HttpServletRequest request) throws AmazonClientException, IOException {
		String pathVar = request.getRequestURI().replaceAll("/service/uploadFile/", "");
		String fileName	= pathVar.substring(pathVar.lastIndexOf("/"), pathVar.length()-1);
		String key = pathVar.replaceAll("/".concat(fileName), "");
		obsService.uploadFile(key, fileName, request);
	}
*/
	@RequestMapping(value = "/service/getFile/**", method = RequestMethod.GET)
	public void getFile(HttpServletResponse response, HttpServletRequest request) throws AmazonClientException, IOException {
		System.out.println(request.getRequestURI());
		String key = request.getRequestURI().replaceAll("/service/getFile/", "");
		obsService.getFile(key, response);
	}
/*
    @RequestMapping(value = {"/service/downloadDir/{prefix}","/service/getFileDir"}, method = RequestMethod.GET)
	public String downloadDir(@PathVariable (value="prefix",required = false) String prefix, HttpServletResponse response, HttpServletRequest request) throws AmazonClientException, IOException {
		return obsService.downloadDir(prefix, response, request);
	}

	@RequestMapping("/service/getFileUrl/{key:.*}")
	public String getFileUrl(@PathVariable String key) throws AmazonClientException {
		return obsService.getFileUrl(key);
	}
*/
	@RequestMapping("/service/deleteFile/{secret}/{key}")
	public void deleteFile(@PathVariable String key, @PathVariable String secret, HttpServletRequest request) throws AmazonClientException {
		obsService.deleteFile(secret, key, request);
	}

	@RequestMapping("/service/deleteDir/{secret}/{key}")
	public void deleteDir(@PathVariable String key, @PathVariable String secret, HttpServletRequest request) throws AmazonClientException, IOException{
		obsService.deleteDir(secret, key, request);
	}
	
	@RequestMapping(value = {"/service/getFileList/{prefix}","/service/getFileList"}, method = RequestMethod.GET)
	public String getFileList(@PathVariable (value="prefix",required = false) String prefix, HttpServletResponse response, HttpServletRequest request) throws AmazonClientException, IOException {
		return obsService.getFileList(prefix, response, request);
	}


	
}
