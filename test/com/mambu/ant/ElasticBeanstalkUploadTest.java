package com.mambu.ant;

/**
 * Simple locally executable system test
 */
public class ElasticBeanstalkUploadTest {

	public static void main(String[] args) {

		ElasticBeanstalkUpload elasticBeanstalkUpload = new ElasticBeanstalkUpload();

		elasticBeanstalkUpload.setApplicationName("");
		elasticBeanstalkUpload.setAwsAccessKey("");
		elasticBeanstalkUpload.setAwsSecretKey("");
		elasticBeanstalkUpload.setS3Endpoint("s3.amazonaws.com");
		elasticBeanstalkUpload.setCreateApplicationVersion("true");
		elasticBeanstalkUpload.setDeployApplicationVersion("true");
		elasticBeanstalkUpload.setEndpoint("elasticbeanstalk.us-east-1.amazonaws.com");
		elasticBeanstalkUpload.setEnvironmentName("");
		elasticBeanstalkUpload.setS3Bucket("");
		elasticBeanstalkUpload.setS3Key("");
		elasticBeanstalkUpload.setUploadToS3("true");
		elasticBeanstalkUpload.setVersionLabel("");
		elasticBeanstalkUpload.setWarFileLocation("");

		elasticBeanstalkUpload.execute();

	}

}
