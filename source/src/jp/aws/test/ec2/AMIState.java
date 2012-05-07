/**
 * @file
 * @brief AMI State
 * @author tmor
 * @licence Apache License, Version 2.0 http://www.apache.org/licenses/LICENSE-2.0
 *
 * $Revision: 266 $
 */
package jp.aws.test.ec2;

import java.util.HashMap;

import jp.aws.test.R;

/**
 * AMI Stateクラス
 *
 */
public class AMIState {
	public static final HashMap<String, Integer> states_resource = new HashMap<String, Integer>() {
		{
			put("available", R.drawable.ic_ec2_state_running);
			put("deregistered", R.drawable.ic_ec2_state_stopped);
		}
	};

	public static Integer getResource(String key) {
		return states_resource.get(key);
	}
}
