package com.mambu.ant;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.Task;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest;
import com.amazonaws.services.elasticbeanstalk.model.S3Location;
import com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest;
import com.amazonaws.services.s3.AmazonS3Client;

/**
 * Ant target for uploading war files to S3 and connect them to a elastic beanstalk environment
 */
public class ElasticBeanstalkUpload extends Task {

	private String s3Bucket;
	private String applicationName;
	private String versionLabel;
	private String s3Key;
	private String warFileLocation;
	private String awsAccessKey;
	private String awsSecretKey;
	private boolean createApplicationVersion = true;
	private boolean deployApplicationVersion = true;
	private String environmentName;

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public void setVersionLabel(String versionLabel) {
		this.versionLabel = versionLabel;
	}

	public void setS3Key(String s3Key) {
		this.s3Key = s3Key;
	}

	public void setS3Bucket(String s3Bucket) {
		this.s3Bucket = s3Bucket;
	}

	public void setWarFileLocation(String warFileLocation) {
		this.warFileLocation = warFileLocation;
	}

	public void setAwsAccessKey(String awsAccessKey) {
		this.awsAccessKey = awsAccessKey;
	}

	public void setAwsSecretKey(String awsSecretKey) {
		this.awsSecretKey = awsSecretKey;
	}

	public void setCreateApplicationVersion(String createApplicationVersion) {
		this.createApplicationVersion = Boolean.parseBoolean(createApplicationVersion);
	}

	public void setDeployApplicationVersion(String deployApplicationVersion) {
		this.deployApplicationVersion = Boolean.parseBoolean(deployApplicationVersion);
	}

	public void setEnvironmentName(String environmentName) {
		this.environmentName = environmentName;
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

			// upload war file to S3
			AmazonS3Client s3Client = new AmazonS3Client(credentials);
			File warFile = new File(warFileLocation);
			s3Client.putObject(s3Bucket, s3Key, warFile);

			AWSElasticBeanstalkClient client = new AWSElasticBeanstalkClient(credentials);

			// use war file in S3 to create new application version
			if (createApplicationVersion) {
				S3Location sourceBundle = new S3Location(s3Bucket, s3Key);
				CreateApplicationVersionRequest createApplicationVersionRequest = new CreateApplicationVersionRequest(
						applicationName, versionLabel);
				createApplicationVersionRequest.withSourceBundle(sourceBundle);
				client.createApplicationVersion(createApplicationVersionRequest);
			}

			// deploy new version
			if (deployApplicationVersion) {
				UpdateEnvironmentRequest updateEnvironmentRequest = new UpdateEnvironmentRequest();
				updateEnvironmentRequest.setVersionLabel(versionLabel);
				updateEnvironmentRequest.setEnvironmentName(environmentName);
				client.updateEnvironment(updateEnvironmentRequest);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
