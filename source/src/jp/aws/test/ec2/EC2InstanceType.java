/**
 * @file
 * @brief EC2 InstanceType
 * @author tmor
 * @licence Apache License, Version 2.0 http://www.apache.org/licenses/LICENSE-2.0
 *
 * $Revision: 266 $
 */
package jp.aws.test.ec2;

/**
 * EC2 InstanceType
 *
 */
public class EC2InstanceType {
	public static final String[] ebs_i386 = { "t1.micro", "m1.small",
			"c1.medium", "m1.medium", };

	public static final String[] ebs_x86_64 = { "t1.micro", "m1.small",
			"c1.medium", "m1.medium", "m1.large", "m1.xlarge", "m2.xlarge",
			"m2.2xlarge", "m2.4xlarge", "c1.xlarge", };

	public static final String[] instance_store_i386 = { "m1.small",
			"c1.medium", "m1.medium", };

	public static final String[] instance_store_x86_64 = { "m1.small",
			"c1.medium", "m1.medium", "m1.large", "m1.xlarge", "m2.xlarge",
			"m2.2xlarge", "m2.4xlarge", "c1.xlarge", };

	public static String[] getInstanceTypes(String rootDeviceType, String arch) {
		if (rootDeviceType.toLowerCase().equals("ebs")) {
			if (arch.toLowerCase().equals("i386")) {
				return ebs_i386;
			} else if (arch.toLowerCase().equals("x86_64")) {
				return ebs_x86_64;
			}
		} else if (rootDeviceType.toLowerCase().equals("instance-store")) {
			if (arch.toLowerCase().equals("i386")) {
				return instance_store_i386;
			} else if (arch.toLowerCase().equals("x86_64")) {
				return instance_store_x86_64;
			}
		}
		return null;
	}
}
