package com.OneConnect.microService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import com.pingan.radosgw.sdk.config.ObsClientConfig;
import com.pingan.radosgw.sdk.service.RadosgwService;
import com.pingan.radosgw.sdk.service.RadosgwServiceFactory;
import com.pingan.radosgw.sdk.service.request.ListObjectsRequest;

import repkg.com.amazonaws.AmazonClientException;
import repkg.com.amazonaws.services.s3.model.ObjectListing;
import repkg.com.amazonaws.services.s3.model.PutObjectResult;
import repkg.com.amazonaws.services.s3.model.S3Object;
import repkg.com.amazonaws.services.s3.model.S3ObjectInputStream;

@Service
public class ObsService {

	private static RadosgwService service;
	private static String bucketName = "lab-obs-test";
	
    //initialize property	
	static {
		ObsClientConfig oc = new ObsClientConfig() {
			
			public String getUserAgent() {
			    return "";
			  }
			  public String getObsUrl() {
			    return "https://obs-sg-singapore.yun.pingan.com";
			  }
			  public String getObsAccessKey() {
			    return "JZX8EYKfNNADJaD0-ZAJCupX3nzfG--GwLakBk4jS5Z2DlOMkRmZZlh7nephYgm1MMw_UufOiln7Y93RIZB_3g";
			  }
			  public String getObsSecret() {
			    return "-96IpTLaXey3VStvnB6kx4uCR_Er2jsPsxUlLuT0SGCwe_qE2jAIL7k8BWc0QkKPaJyanJ5CurORUP4ZVEgXjA";
			  }
		};		
		try {
			service = RadosgwServiceFactory.getFromConfigObject(oc);
				 
		} catch (AmazonClientException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Client config success!");
	}
	
	/*
	 * @Description: Upload file by input file to cloud
	 * @Params: String key  // file name
	 *          File file   // file to upload
	 */
	public void uploadFile(String key,File file) throws AmazonClientException, FileNotFoundException {
		PutObjectResult result = service.putObject(bucketName, key, file);
		System.out.println(result.getObjectKey());
	}
	
	/*
	 * @Description: Upload file by input stream to cloud
	 * @Params: String key  // file name
	 *          InputStream is   // stream to upload
	 */
	public void uploadFileByStream(InputStream is,String key) throws AmazonClientException {
		PutObjectResult result = service.putObject(bucketName, key, is);
	}
	
	/*
	 * @Description: get file from cloud
	 * @Params: String key  // file name
	 * @Return: S3Object
	 */
	public S3Object getFile(String key) throws AmazonClientException, IOException {
		System.out.println(key);
		S3Object object = service.getObject(bucketName,key);
		S3ObjectInputStream s3is  = object.getObjectContent();
		FileOutputStream fos = new FileOutputStream(key);
		byte[] read_buf = new byte[1024];
		int read_len = 0;
	    while ((read_len = s3is.read(read_buf)) > 0) {
	        fos.write(read_buf, 0, read_len);
	    }
		System.out.println(s3is.toString());
		return object;
	}
	
	/*
	 * @Description: get the url of specific file
	 * @Params: String key  // file name
	 * @Reutrn: String   
	 */
	public String getFileUrl(String key) throws AmazonClientException {
		return service.getSignedUrl(bucketName, key);
	}
	
	/*
	 * @Description: Delete specific file on cloud
	 * @Params: String key  // file name
	 */
	public void deleteFile(String key) throws AmazonClientException {
		service.deleteObject("bucketName", key);
	}	
	
	
	/*
	 * @Description: get the file list of specific directory of bucket
	 * @Params: String prefix  // directory of bucket
	 * @Reutrn: List<String>  //key name of the bucket  
	 */
    public List<String> getFileList(String prefix) throws AmazonClientException {
    	ListObjectsRequest listObjectRequest = new ListObjectsRequest();
    	listObjectRequest.setPrefix(prefix);
    	listObjectRequest.setBucketName(bucketName);
    	listObjectRequest.setMaxKeys(1000);
    	ObjectListing objectList = service.listObjects(listObjectRequest);
    	System.out.println(objectList.getObjectSummaries().size());
    	List<String>resultList = new ArrayList<String>();
    	for (S3Object obj :objectList.getObjectSummaries()) {
    		resultList.add(obj.getKey());
    	}
    	return resultList;
    }
	
}
