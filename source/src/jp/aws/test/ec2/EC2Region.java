/**
 * @file
 * @brief EC2 Region
 * @note
 * - Regions and Endpoints http://docs.amazonwebservices.com/general/latest/gr/rande.html
 * @author tmor
 * @licence Apache License, Version 2.0 http://www.apache.org/licenses/LICENSE-2.0
 *
 * $Revision:$
 */

package jp.aws.test.ec2;

import java.util.HashMap;

/** リージョン */
public class EC2Region {
	public static String DEFAULT_REGION = "us-east-1";
	public static final HashMap<String, String> regions = new HashMap<String, String>() {
		{
			put("us-east-1", "US-East (Virginia)");
			put("us-west-1", "US-West (N.California)");
			put("us-west-2", "US-West (Oregon)");
			put("ap-northeast-1", "Asia Pacific (Tokyo)");
			put("ap-southeast-1", "Asia Pacific (Singapore)");
			put("eu-west-1", "EU-West (Ireland)");
			put("sa-east-1", "South America (Sao Paulo)");
		}
	};

	public static String getName(String key) {
		return regions.get(key);
	}
}
