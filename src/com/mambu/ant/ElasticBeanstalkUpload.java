package com.mambu.ant;

import java.io.File;

import org.apache.tools.ant.Task;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest;
import com.amazonaws.services.elasticbeanstalk.model.S3Location;
import com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest;
import com.amazonaws.services.s3.AmazonS3Client;

/**
 * Ant target for uploading war files to S3 and connect them to a elastic beanstalk environment
 */
public class ElasticBeanstalkUpload extends Task {

	private String s3Endpoint = "s3.amazonaws.com";
	private String beanstalkEndpoint = "elasticbeanstalk.us-east-1.amazonaws.com";
	private String s3Bucket;
	private String applicationName;
	private String versionLabel;
	private String s3Key;
	private String warFileLocation;
	private String awsAccessKey;
	private String awsSecretKey;
	private boolean uploadToS3 = true;
	private boolean createApplicationVersion = true;
	private boolean deployApplicationVersion = true;
	private String environmentName;

	/**
	 * Endpoint for Elastic Beanstalk, defaults to elasticbeanstalk.us-east-1.amazonaws.com
	 * 
	 * @param beanstalkEndpoint
	 *            the beanstalkEndpoint to set
	 */
	public void setEndpoint(String beanstalkEndpoint) {
		this.beanstalkEndpoint = beanstalkEndpoint;
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

	public void setUploadToS3(String uploadToS3) {
		this.uploadToS3 = Boolean.parseBoolean(uploadToS3);
	}

	/**
	 * Endpoint for S3, defaults to s3.amazonaws.com
	 * 
	 * @param s3Endpoint
	 *            the s3Endpoint to set
	 */
	public void setS3Endpoint(String s3Endpoint) {
		this.s3Endpoint = s3Endpoint;
	}

	public void execute() throws org.apache.tools.ant.BuildException {

		System.out.println("Beanstalk Ant Task Started");

		AWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
		ClientConfiguration configuration = new ClientConfiguration();

		// Configure proxy if the properties are set, use HTTPS namespace since
		// all connections to AWS are over HTTPS
		String proxyHost = System.getProperty("https.proxyHost");
		String proxyPort = System.getProperty("https.proxyPort");
		String proxyUser = System.getProperty("https.proxyUser");
		String proxyPass = System.getProperty("https.proxyPass");

		// If the default Java variables weren't set, try to use environment variables
		if (proxyHost == null && proxyPort == null) {
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

		// upload war file to S3
		if (uploadToS3) {

			System.out.printf(
					"- Uploading '%s' to S3 bucket '%s' under '%s' using S3 endpoint '%s' with access key ID '%s'\n",
					warFileLocation, s3Bucket, s3Key, s3Endpoint, awsAccessKey);

			AmazonS3Client s3Client = new AmazonS3Client(credentials, configuration);
			s3Client.setEndpoint(s3Endpoint);
			File warFile = new File(warFileLocation);
			s3Client.putObject(s3Bucket, s3Key, warFile);
		}

		AWSElasticBeanstalkClient client = new AWSElasticBeanstalkClient(credentials, configuration);
		client.setEndpoint(beanstalkEndpoint);

		// use war file in S3 to create new application version
		if (createApplicationVersion) {

			try {

				System.out
						.printf("- Creating Application Version for S3 Object '%s/%s' as version '%s' to environment '%s' for application '%s' via Beanstalk endpoint '%s' with access key ID '%s'\n",
								s3Bucket, s3Key, versionLabel, environmentName, applicationName, beanstalkEndpoint,
								awsAccessKey);

				S3Location sourceBundle = new S3Location(s3Bucket, s3Key);
				CreateApplicationVersionRequest createApplicationVersionRequest = new CreateApplicationVersionRequest(
						applicationName, versionLabel);
				createApplicationVersionRequest.withSourceBundle(sourceBundle);
				client.createApplicationVersion(createApplicationVersionRequest);

			} catch (AmazonServiceException e) {
				System.out.println("\nAn error occurred during creation of the application version: ");
				e.printStackTrace();

				// for some reason stack trace log output is mixed up otherwise
				try {
					Thread.sleep(200);
				} catch (InterruptedException e1) {
					// ignore
				}

				System.out.println("\nContinuing with next step ...\n");
			}
		}

		// deploy new version
		if (deployApplicationVersion) {

			System.out
					.printf("- Deploying version '%s' to environment '%s' for application '%s' via Beanstalk endpoint '%s' with access key ID '%s'\n",
							versionLabel, environmentName, applicationName, beanstalkEndpoint, awsAccessKey);

			UpdateEnvironmentRequest updateEnvironmentRequest = new UpdateEnvironmentRequest();
			updateEnvironmentRequest.setVersionLabel(versionLabel);
			updateEnvironmentRequest.setEnvironmentName(environmentName);
			client.updateEnvironment(updateEnvironmentRequest);
		}

		System.out.println("Beanstalk Ant Task Finished");

	}
}
