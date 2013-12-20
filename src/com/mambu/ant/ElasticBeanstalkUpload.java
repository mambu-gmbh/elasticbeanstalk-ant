package com.mambu.ant;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest;
import com.amazonaws.services.elasticbeanstalk.model.S3Location;
import com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest;
import com.amazonaws.services.s3.AmazonS3Client;

import org.apache.tools.ant.Task;

import java.io.File;

/**
 * Ant target for uploading war files to S3 and connect them to a elastic beanstalk environment
 */
public class ElasticBeanstalkUpload extends Task {

	private String endpoint = "elasticbeanstalk.us-east-1.amazonaws.com";
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

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

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

		AWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
		ClientConfiguration configuration = new ClientConfiguration();
		
		// Configure proxy if the properties are set, use HTTPS namespace since
		// all connections to AWS are over HTTPS
		String proxyHost = System.getProperty("https.proxyHost");
		String proxyPort = System.getProperty("https.proxyPort");
		String proxyUser = System.getProperty("https.proxyUser");
		String proxyPass = System.getProperty("https.proxyPass");

		// If the default Java variables weren't set, try to use environment variables
		if(proxyHost == null && proxyPort == null) {
			String proxyDef = System.getProperty("https_proxy");
			if (proxyDef != null) {
				String[] split = proxyDef.split(":");
				proxyHost = split[0];
				proxyPort = split[1];
			}
		}

		if (proxyHost != null) {
			configuration.setProxyHost(proxyHost);
		}
		if (proxyPort != null) {
			configuration.setProxyPort(Integer.parseInt(proxyPort));
		}
		if (proxyUser != null) {
			configuration.setProxyUsername(proxyUser);
		}
		if (proxyPass != null) {
			configuration.setProxyPassword(proxyPass);
		}

		System.out.printf("Deploying '%s' as version '%s' to environment '%s' for application '%s' at endpoint '%s' with access key ID '%s'\n",
				warFileLocation, versionLabel, environmentName, applicationName, endpoint, awsAccessKey);

		// upload war file to S3
		AmazonS3Client s3Client = new AmazonS3Client(credentials, configuration);
		File warFile = new File(warFileLocation);
		s3Client.putObject(s3Bucket, s3Key, warFile);

		AWSElasticBeanstalkClient client = new AWSElasticBeanstalkClient(credentials, configuration);
		client.setEndpoint(endpoint);

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

	}
}
