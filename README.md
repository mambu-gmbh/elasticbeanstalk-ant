elasticbeanstalk-ant
====================

Ant target to upload a file to S3, add it as a version to an AWS ElasticBeanstalk environment and deploy that version.

Sample Usage
============
```xml
<target name="deployToAwsElBT">
<!-- upload to s3 and update beanstalk-->
<taskdef name="elasticbeanstalk" classname="com.mambu.ant.ElasticBeanstalkUpload" classpath="lib/elasticbeanstalk-1.1.jar:lib/httpclient-4.1.1.jar:lib/httpcore-4.1.jar:lib/commons-codec-1.3.jar:lib/commons-logging-1.1.1.jar:lib/aws-java-sdk-1.2.7.jar" />
<elasticbeanstalk applicationname="${BEANSTALK_APPLICATION_NAME}" versionlabel="r${revisionnumber}-${current.time}" warfilelocation="${ABSOLUTE_PATH_TO_WAR_FILE}" s3bucket="${S3_BUCKET_NAME}" s3key="r${revisionnumber}-${current.time}.war" awsaccesskey="${AWS_ACCESS_KEY_ID}" awssecretkey="${AWS_SECRET_KEY}" environmentname="${BEANSTALK_ENV_NAME}" endpoint="${S3_ENDPOINT}" beanstalkEndpoint="${BEANSTALK_ENDPOINT}"/>
</target>
```

Sample Policy to allow Uploading and Deploying
==============================================
```
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "elasticbeanstalk:CreateApplicationVersion",
                "elasticbeanstalk:DescribeEnvironments",
                "elasticbeanstalk:DeleteApplicationVersion",
                "elasticbeanstalk:UpdateEnvironment",
                "elasticbeanstalk:CreateStorageLocation",
                "elasticbeanstalk:DescribeEvents"
            ],
            "Resource": [
                "arn:aws:elasticbeanstalk:us-east-1:<ACCOUNT_NUMBER>:applicationversion/<APPLICATION_NAME>/*",
                "arn:aws:elasticbeanstalk:us-east-1:<ACCOUNT_NUMBER>:environment/<APPLICATION_NAME>/*"
            ]
        },
        {
            "Effect": "Allow",
            "Action": [
                "sns:CreateTopic",
                "sns:GetTopicAttributes",
                "sns:ListSubscriptionsByTopic",
                "sns:Subscribe"
            ],
            "Resource": [
                "arn:aws:sns:us-east-1:<ACCOUNT_NUMBER>:ElasticBeanstalkNotifications-Environment-*"
            ]
        },
        {
            "Action": [
                "autoscaling:SuspendProcesses",
                "autoscaling:DescribeScalingActivities",
                "autoscaling:ResumeProcesses",
                "autoscaling:DescribeAutoScalingGroups",
                "autoscaling:DescribeLaunchConfigurations",
                "autoscaling:PutNotificationConfiguration"
            ],
            "Effect": "Allow",
            "Resource": "*"
        },
        {
            "Effect": "Allow",
            "Action": [
                "cloudformation:GetTemplate",
                "cloudformation:DescribeStackResources",
                "cloudformation:DescribeStackResource",
                "cloudformation:DescribeStackEvents",
                "cloudformation:DescribeStacks",
                "cloudformation:UpdateStack",
                "cloudformation:CancelUpdateStack"
            ],
            "Resource": [
                "arn:aws:cloudformation:us-east-1:<ACCOUNT_NUMBER>:stack/awseb*"
            ]
        },
        {
            "Effect": "Allow",
            "Action": [
                "ec2:DescribeImages",
                "ec2:DescribeKeyPairs",
                "ec2:DescribeSecurityGroups",
                "ec2:DescribeVpcs",
                "ec2:DescribeAddresses",
                "ec2:DescribeInstances",
                "ec2:RevokeSecurityGroupIngress",
                "ec2:AuthorizeSecurityGroupIngress"
            ],
            "Resource": [
                "*"
            ]
        },
        {
            "Effect": "Allow",
            "Action": [
                "s3:PutObject",
                "s3:GetObject",
                "s3:CreateBucket"
            ],
            "Resource": [
                "arn:aws:s3:::elasticbeanstalk-us-east-1-<ACCOUNT_NUMBER>",
                "arn:aws:s3:::elasticbeanstalk-us-east-1-<ACCOUNT_NUMBER>/*"
            ]
        },
        {
            "Effect": "Allow",
            "Action": [
                "s3:*"
            ],
            "Resource": [
                "*"
            ]
        },
        {
            "Effect": "Allow",
            "Action": [
                "elasticloadbalancing:DescribeLoadBalancers",
                "elasticloadbalancing:RegisterInstancesWithLoadBalancer",
                "elasticloadbalancing:DescribeInstanceHealth"
            ],
            "Resource": [
                "*"
            ]
        }
    ]
}
```
