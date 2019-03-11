package com.ocsg.apiservice;

import com.ocsg.util.FileUtil;
import com.pingan.radosgw.sdk.config.ObsClientConfig;
import com.pingan.radosgw.sdk.service.RadosgwService;
import com.pingan.radosgw.sdk.service.RadosgwServiceFactory;
import com.pingan.radosgw.sdk.service.request.ListObjectsRequest;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import repkg.com.amazonaws.AmazonClientException;
import repkg.com.amazonaws.services.s3.model.ObjectListing;
import repkg.com.amazonaws.services.s3.model.PutObjectResult;
import repkg.com.amazonaws.services.s3.model.S3Object;
import repkg.com.amazonaws.services.s3.model.S3ObjectInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

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

	public String getDefaultDirLocation(){
		return "";
	}
	public String getDeleteSecret() {return FileUtil.getCurrentDate(); }


    public String createFileDir(String key) throws AmazonClientException, UnsupportedEncodingException, IOException {
        key = URLDecoder.decode(key, "UTF-8");
        System.out.println(key);
        File file = new File(key);
        file.createNewFile();
        //System.out.println(request.getMethod());
	    PutObjectResult result = service.putObject(bucketName, key.concat("/open"), file);
	    service.close();
	    return result.getETag();

    }


	public void uploadFile(String key,String filename, HttpServletRequest request) throws AmazonClientException, IOException {
		filename = this.getDefaultDirLocation().concat("/").concat(filename);
		uploadFile(key, filename);
		service.close();
	}


    /*
	 * @Description: Upload file by input file to cloud
	 * @Params: String key  // file name
	 *          File file   // file to upload
	 */
	public String uploadFile(String key, String fileName)throws IOException{
		String out = "";
		InputStream is = null;
		try{
			is = new DataInputStream(new FileInputStream(new File(fileName)));
			PutObjectResult result = service.putObject(bucketName, key, is);
			out.concat(fileName).concat(": ").concat(result.getETag());
		}catch (Exception e){
			out.concat("Error: ").concat(fileName).concat("\n").concat(e.getMessage().concat("\n"));
		}finally {
			if(is!=null)
				is.close();
		}


		return out;
	}

	
	/*
	 * @Description: Upload file by input stream to cloud
	 * @Params: String key  // file name
	 *          InputStream is   // stream to upload
	 */
	public void uploadDir(String key) throws AmazonClientException, IOException {
		try {
			List<String> files = FileUtil.listFiles(this.getDefaultDirLocation().concat("/").concat(key));
			for(String file : files){
				key = file.replaceAll(this.getDefaultDirLocation().concat("/"), "");
				uploadFile(key, file);
			}

		} catch (IOException e){
			e.getMessage();
			e.printStackTrace();
		}
		service.close();
	}
	
	/*
	 * @Description: get file from cloud
	 * @Params: String key  // file name
	 * @Return: S3Object
	 */
	public void getFile(String key, HttpServletResponse response) throws AmazonClientException, IOException {
		key = URLDecoder.decode(key, "UTF-8");
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=\""+key+"\"");
		System.out.println(key);
		S3Object object = service.getObject(bucketName,key);
		S3ObjectInputStream s3is  = object.getObjectContent();

		int nRead;
		while ((nRead = s3is.read()) != -1) {
			response.getWriter().write(nRead);
		}
		s3is.close();
	}
	
	/*
	 * @Description: get the url of specific file
	 * @Params: String key  // file name
	 * @Return: String
	 */
	public String getFileUrl(String key) throws AmazonClientException {
		String url = service.getSignedUrl(bucketName, key);
		return url;
	}
	
	/*
	 * @Description: Delete specific file on cloud
	 * @Params: String key  // file name
	 */
	public String deleteFile(String secret, String key, HttpServletRequest request) throws AmazonClientException {
		if (secret.equals(this.getDeleteSecret())) {
			service.deleteObject(bucketName, key);
		}
		service.close();
		return "";
	}

	public String deleteDir(String secret, String key, HttpServletRequest request)throws AmazonClientException, IOException{
		String listString = "Failed!";
		if (secret.equals(this.getDeleteSecret())) {
			List<String>resultList = getFileList(key);
			listString = "";
			for (String s : resultList)
			{
				service.deleteObject(bucketName, s);
				listString += "Deleted: " + s + "\n";
			}
		}
		return listString;
	}

	public String downloadDir(String prefix, HttpServletResponse response, HttpServletRequest request) throws AmazonClientException, IOException {
		List<String>resultList = getFileList(prefix);
		String listString = "";
		for (String s : resultList)
		{
			saveFile(service.getObject(bucketName, s), s);
			listString += s + "\n";
		}
		service.close();
		return listString;
	}



	public void saveFile(S3Object object, String key) throws  IOException {
		S3ObjectInputStream s3is  = object.getObjectContent();
		try{
			File targetFile = new File("storage/".concat(key));
			if(!targetFile.exists()){
				targetFile.getParentFile().mkdirs();
				targetFile.createNewFile();
			}
			Files.copy(
					s3is,
					targetFile.toPath(),
					StandardCopyOption.REPLACE_EXISTING);

			IOUtils.closeQuietly(s3is);
		}catch (Exception e){
			System.out.println("Error: "+key);
			//e.printStackTrace();
			System.out.println(e.getMessage());
		}finally {
			if(s3is != null)
			s3is.close();
		}

	}
	
	
	/*
	 * @Description: get the file list of specific directory of bucket
	 * @Params: String prefix  // directory of bucket
	 * @Reutrn: List<String>  //key name of the bucket  
	 */

	public String getFileList(String prefix, HttpServletResponse response, HttpServletRequest request) throws AmazonClientException, IOException {

		List<String>resultList = getFileList(prefix);

		String listString = "";
		System.out.println(request.getRequestURI());
		//String hostUrl= request.getServerName().concat(":")+(request.getServerPort());
		System.out.println(response.toString());
		for (String s : resultList)
		{
			listString += s + "\n";
		}
		service.close();
		return listString;
	}

    public List<String> getFileList(String prefix) throws AmazonClientException, IOException {

    	ListObjectsRequest listObjectRequest = new ListObjectsRequest();
    	listObjectRequest.setPrefix(prefix);
    	listObjectRequest.setBucketName(bucketName);
    	listObjectRequest.setMaxKeys(5000);
    	System.out.println(listObjectRequest.toString());
    	ObjectListing objectList = service.listObjects(listObjectRequest);
    	List<String>resultList = new ArrayList<String>();
    	for (S3Object obj :objectList.getObjectSummaries()) {
    		resultList.add(obj.getKey());
    	}

    	return resultList;
    }




	
}
