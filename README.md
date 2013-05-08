elasticbeanstalk-ant
====================

Ant target to upload a file to S3, add it as a version to an AWS ElasticBeanstalk environment and deploy that version.

Sample Usage
============
```xml
<target name="deployToAwsElBT">
<!-- upload to s3 and update beanstalk-->
<taskdef name="elasticbeanstalk" classname="com.mambu.ant.ElasticBeanstalkUpload" classpath="lib/elasticbeanstalk-1.1.jar:lib/httpclient-4.1.1.jar:lib/httpcore-4.1.jar:lib/commons-codec-1.3.jar:lib/commons-logging-1.1.1.jar:lib/aws-java-sdk-1.2.7.jar" />
<elasticbeanstalk applicationname="${BEANSTALK_APPLICATION_NAME}" versionlabel="r${revisionnumber}-${current.time}" warfilelocation="${ABSOLUTE_PATH_TO_WAR_FILE}" s3bucket="${S3_BUCKET_NAME}" s3key="r${revisionnumber}-${current.time}.war" awsaccesskey="${AWS_ACCESS_KEY_ID}" awssecretkey="${AWS_SECRET_KEY}" environmentname="${BEANSTALK_ENV_NAME}" />
</target>
```
