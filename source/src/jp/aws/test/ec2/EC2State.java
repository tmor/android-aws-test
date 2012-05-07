/**
 * @file
 * @brief EC2 State
 * @author tmor
 * @licence Apache License, Version 2.0 http://www.apache.org/licenses/LICENSE-2.0
 *
 * $Revision: 266 $
 */
package jp.aws.test.ec2;

import java.util.HashMap;

import jp.aws.test.R;

/**
 * EC2 Stateクラス
 *
 */
public class EC2State {
	public static final HashMap<String, Integer> states_resource = new HashMap<String, Integer>() {
		{
			put("pending", R.drawable.ic_ec2_state_pending);
			put("running", R.drawable.ic_ec2_state_running);
			put("shutting-down", R.drawable.ic_ec2_state_shutting_down);
			put("terminated", R.drawable.ic_ec2_state_terminated);
			put("stopping", R.drawable.ic_ec2_state_stopping);
			put("stopped", R.drawable.ic_ec2_state_stopped);
		}
	};

	public static Integer getResource(String key) {
		return states_resource.get(key);
	}
}
