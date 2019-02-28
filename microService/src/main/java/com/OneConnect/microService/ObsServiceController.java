package com.OneConnect.microService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import repkg.com.amazonaws.AmazonClientException;
import repkg.com.amazonaws.services.s3.model.S3Object;



@RestController
public class ObsServiceController {
	@Autowired
	ObsService obsService;
	
	@RequestMapping("/service/uploadFile/{key}/{file}")
	public void uploadFile(@PathVariable String key,@PathVariable File file) throws AmazonClientException, FileNotFoundException {
		System.out.println(key);
		obsService.uploadFile(key,file);
	}
	
	@RequestMapping("/service/uploadFile/{key}/{fileStream}")
	public void uploadFileByStream(@PathVariable String key,@PathVariable InputStream fileStream) throws AmazonClientException {
		obsService.uploadFileByStream(fileStream, key);
	}
	
	@RequestMapping("/service/getFile/{key:.*}")
	public S3Object getFile(@PathVariable String key) throws AmazonClientException, IOException {
		System.out.println(key);
		return obsService.getFile(key);
	}
	
	@RequestMapping("/service/getFileUrl/{key:.*}")
	public String getFileUrl(@PathVariable String key) throws AmazonClientException {
		return obsService.getFileUrl(key);
	}
	
	@RequestMapping("/service/deleteFile/{key}")
	public void deleteFile(@PathVariable String key) throws AmazonClientException {
		obsService.deleteFile(key);
	}
	
	@RequestMapping(value = {"/service/getFileList/{prefix}","/service/getFileList"})
	public List<String> getFileList(@PathVariable (value="prefix",required = false) String prefix) throws AmazonClientException {
		return obsService.getFileList(prefix);
	}
	
}
