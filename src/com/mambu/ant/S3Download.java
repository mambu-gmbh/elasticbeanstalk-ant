package com.mambu.ant;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.tools.ant.Task;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;

/**
 * Ant target for downloading .properties files from S3
 */
public class S3Download extends Task {

	private String s3Bucket;
	private String s3Key;
	private String destination;
	private String awsAccessKey;
	private String awsSecretKey;

	public void setS3Key(String s3Key) {
		this.s3Key = s3Key;
	}

	public void setS3Bucket(String s3Bucket) {
		this.s3Bucket = s3Bucket;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public void setAwsAccessKey(String awsAccessKey) {
		this.awsAccessKey = awsAccessKey;
	}

	public void setAwsSecretKey(String awsSecretKey) {
		this.awsSecretKey = awsSecretKey;
	}

	public void execute() throws org.apache.tools.ant.BuildException {

		// imitating .properties file with AWS credentials
		String awsAccessKeyLine = "accessKey = " + awsAccessKey;
		String awsSecretKeyLine = "secretKey = " + awsSecretKey;
		ByteArrayInputStream awsInputStream = new ByteArrayInputStream(
				(awsAccessKeyLine + "\n" + awsSecretKeyLine).getBytes());

		System.out.println("using " + awsAccessKeyLine + ", " + awsSecretKeyLine);

		try {

			PropertiesCredentials credentials = new PropertiesCredentials(awsInputStream);

			// download file from S3
			AmazonS3Client s3Client = new AmazonS3Client(credentials);
			File propertiesFile = new File(destination);
			S3Object propertiesFileObj = s3Client.getObject(s3Bucket, s3Key);
			// write content to disk
			write(propertiesFileObj.getObjectContent(), propertiesFile);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void write(InputStream is, File file) throws IOException {
		OutputStream os = new FileOutputStream(file);
		int numRead;
		while ((numRead = is.read()) >= 0) {
			os.write(numRead);
		}
		os.close();
		is.close();
	}

}
