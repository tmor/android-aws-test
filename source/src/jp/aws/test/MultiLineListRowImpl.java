/**
 * @file
 * @brief 複数行レイアウトの実装
 * @author tmor
 * @licence Apache License, Version 2.0 http://www.apache.org/licenses/LICENSE-2.0
 *
 * $Revision:$
 */

package jp.aws.test;

import java.util.ArrayList;
import java.util.List;

import jp.aws.test.ec2.AMIItem;
import jp.aws.test.ec2.AMIState;
import jp.aws.test.ec2.EC2Item;
import jp.aws.test.ec2.EC2State;

public class MultiLineListRowImpl implements MultiLineListRow {

	private Integer prefixImage;
	private Integer suffixImage;

	List<String> texts;
	List<Float> size;

	public static MultiLineListRowImpl create() {

		MultiLineListRowImpl m = new MultiLineListRowImpl();
		m.texts = new ArrayList<String>();
		m.size = new ArrayList<Float>();
		return m;
	}

	public MultiLineListRowImpl prefixImage(Integer id) {
		this.prefixImage = id;
		return this;
	}

	public MultiLineListRowImpl suffixImage(Integer id) {
		this.suffixImage = id;
		return this;
	}

	public MultiLineListRowImpl addText(String text, float size) {
		this.texts.add(text);
		this.size.add(size);
		return this;
	}

	public MultiLineListRowImpl addText(String text) {
		this.texts.add(text);
		this.size.add(0f);
		return this;
	}

	public Integer getPrefixImageId() {
		return prefixImage;
	}

	public Integer getSuffixImageId() {
		return suffixImage;
	}

	public String getText(int position) {
		return texts.get(position);
	}

	public float getTextSize(int position) {
		return size.get(position);
	}

	public int sieze() {
		return texts.size();
	}

	public MultiLineListRowImpl addEC2Item(EC2Item ec2Item) {
		this.prefixImage(EC2State.getResource(ec2Item.stateName));
		this.addText(ec2Item.instanceId, 18);
		this.addText("State: " + ec2Item.stateName + " / Type: "
				+ ec2Item.instanceType);
		this.addText("Launch Time:  " + ec2Item.launchTime);
		return this;
	}

	public MultiLineListRowImpl addAMIItem(AMIItem amiItem) {
		this.prefixImage(AMIState.getResource(amiItem.state));
		this.addText(amiItem.imageId, 18);
		this.addText("Arch: " + amiItem.architecture + " / Root Device: "
				+ amiItem.rootDeviceType);
		this.addText("Manifest: " + amiItem.imageLocation);
		return this;
	}
}
