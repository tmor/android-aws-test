/**
 * @file
 * @brief EC2インスタンス
 * @author tmor
 * @licence Apache License, Version 2.0 http://www.apache.org/licenses/LICENSE-2.0
 *
 * $Revision: 266 $
 */

package jp.aws.test.ec2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import jp.aws.test.AmazonClientManager;

import android.util.Log;
import android.util.Pair;

import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesRequest;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.GroupIdentifier;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.KeyPairInfo;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

public class EC2Instance {
	private AmazonClientManager clientManager = null;

	public EC2Instance(AmazonClientManager clientManager) {
		this.clientManager = clientManager;
	}

	/**
	 * インスタンス一覧を取得
	 *
	 * @note - 時間がかかるため非同期取得推奨
	 * @return
	 * @throws Exception
	 */
	public List<EC2Item> list() {

		List<EC2Item> ec2ItemList = new ArrayList<EC2Item>();

		// リージョン指定
		this.clientManager.changeRegion();

		// ReservationのListを取得
		List<Reservation> reservationList = this.clientManager.ec2()
				.describeInstances().getReservations();

		for (Reservation reservation : reservationList) {
			// EC2のInstanceのListを取得
			List<Instance> instanceList = reservation.getInstances();

			for (Instance instance : instanceList) {
				EC2Item ec2Item = new EC2Item();

				// com.amazonaws.services.ec2.model.Instance
				// http://docs.amazonwebservices.com/AWSAndroidSDK/latest/javadoc/com/amazonaws/services/ec2/model/Instance.html
				ec2Item.architecture = instance.getArchitecture(); // i386,
																	// x86_64
				ec2Item.instanceId = instance.getInstanceId(); // i-xxxxxxxx
				ec2Item.instanceType = instance.getInstanceType(); // t1.micro
				ec2Item.keyName = instance.getKeyName(); //
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				ec2Item.launchTime = sdf.format(instance.getLaunchTime()); // 2012-01-01
																			// 01:02:03
																			// +9:00
				ec2Item.privateDnsName = instance.getPrivateDnsName(); // domU-xxx.compute-1.internal
				ec2Item.privateIpAddress = instance.getPrivateIpAddress(); // 10.xxx.xxx.xxx
				ec2Item.publicDnsName = instance.getPublicDnsName(); // ec2-xxx.compute-1.amazonaws.com
				ec2Item.publicIpAddress = instance.getPublicIpAddress(); // 201.xxx.xxx.xxx
				ec2Item.rootDeviceType = instance.getRootDeviceType(); // ebs /
																		// s3
				ec2Item.rootDeviceName = instance.getRootDeviceName(); // /dev/sda1)
				StringBuilder builder = new StringBuilder();
				List<GroupIdentifier> sgs = instance.getSecurityGroups(); // default
				for (GroupIdentifier sg : sgs) {
					builder.append(sg.getGroupName());
					builder.append(", ");
				}
				ec2Item.securityGroups = builder.toString();
				ec2Item.stateName = instance.getState().getName(); // Runnning
				ec2Item.imageId = instance.getImageId(); // ami-xxxxxxxx
				ec2Item.platform = instance.getPlatform(); // Windows
				builder.setLength(0); // 初期化
				List<Tag> tags = instance.getTags(); // tag
				for (Tag tag : tags) {
					builder.append(tag.getValue());
					builder.append(", ");
				}
				ec2Item.availabilityZone = instance.getPlacement()
						.getAvailabilityZone(); // AvailabilityZone

				// アイテムに追加
				ec2ItemList.add(ec2Item);
			}
		}

		return ec2ItemList;
	}

	/**
	 * インスタンスの再起動
	 *
	 * @throws Exception
	 */
	public void reboot(String instanceId) {
		ArrayList<String> instanceIds = new ArrayList<String>();
		instanceIds.add(instanceId);

		RebootInstancesRequest rebootInstancesRequest = new RebootInstancesRequest();
		// rebootInstancesRequest.withInstanceIds(instanceId); // 一つの場合
		rebootInstancesRequest.setInstanceIds(instanceIds); // 複数の場合
		clientManager.ec2().rebootInstances(rebootInstancesRequest);
	}

	/*
	 * インスタンスの終了
	 *
	 * @throws Exception
	 */
	public void terminate(String instanceId) {
		ArrayList<String> instanceIds = new ArrayList<String>();
		instanceIds.add(instanceId);

		TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
		terminateInstancesRequest.setInstanceIds(instanceIds); // 複数の場合
		clientManager.ec2().terminateInstances(terminateInstancesRequest);
	}

	/**
	 * インスタンスの開始
	 *
	 * @throws Exception
	 */
	public void start(String instanceId) {
		ArrayList<String> instanceIds = new ArrayList<String>();
		instanceIds.add(instanceId);

		StartInstancesRequest startInstancesRequest = new StartInstancesRequest();
		startInstancesRequest.setInstanceIds(instanceIds);
		clientManager.ec2().startInstances(startInstancesRequest);
	}

	/**
	 * インスタンスの停止
	 *
	 * @throws Exception
	 */
	public void stop(String instanceId) {
		ArrayList<String> instanceIds = new ArrayList<String>();
		instanceIds.add(instanceId);

		StopInstancesRequest stopInstancesRequest = new StopInstancesRequest();
		stopInstancesRequest.setInstanceIds(instanceIds);
		clientManager.ec2().stopInstances(stopInstancesRequest);
	}

	/**
	 * EC2インスタンス起動
	 *
	 * @param imageId
	 * @param min
	 * @param max
	 * @param instanceType
	 * @param keyPairName
	 * @param availabilityZone
	 * @param securityGroups
	 * @return Vector<Instance>
	 * @throws Exception
	 */
	public Vector<Instance> launchEC2Instances(String imageId, int min,
			int max, String instanceType, String keyPairName,
			String availabilityZone, Collection<String> securityGroups,
			String additionalInfo, String userData) throws Exception {

		Vector<Instance> newInstances = new Vector<Instance>();

		if (min <= 0 || max <= 0 || min > max) {
			return newInstances;
		}

		RunInstancesRequest request = new RunInstancesRequest();
		request.setImageId(imageId);
		request.setInstanceType(instanceType);
		request.setMinCount(min);
		request.setMaxCount(max);
		Placement p = new Placement();
		if (availabilityZone.toLowerCase().equals("any"))
			availabilityZone = ""; // どこでも良い場合は空白
		p.setAvailabilityZone(availabilityZone);
		request.setPlacement(p);
		request.setSecurityGroups(securityGroups);
		request.setKeyName(keyPairName);// assign Keypair name for this request
		request.setUserData(userData);
		request.setAdditionalInfo(additionalInfo);

		// インスタンス起動
		RunInstancesResult runInstancesRes = clientManager.ec2().runInstances(
				request);
		String reservationId = runInstancesRes.getReservation()
				.getReservationId();

		List<Instance> instances = runInstancesRes.getReservation()
				.getInstances();
		if (runInstancesRes != null) {
			for (Instance instance : instances) {
				// EC2InstanceObject newInstanceObject = new
				// EC2InstanceObject();
				// newInstanceObject.setDnsName(i.getPublicDnsName());
				// newInstanceObject.setInstanceId(i.getInstanceId());
				// instances.add(newInstanceObject);
				// newInstances.add(newInstanceObject);
				newInstances.add(instance);
			}
		}

		return newInstances;
	}

	/**
	 * AMI一覧を取得
	 *
	 * @param HashMap
	 *            <String,String> filterMap : オーナーID: ownerid => self, amazon,
	 *            redhat, 00000000 必須 root-device-type => ebs, instance-store
	 *            architecture => i386, x86_64 name => amzn-ami*
	 * @return
	 * @throws Exception
	 * @note - 時間がかかるため非同期取得推奨
	 */
	public List<AMIItem> ami_list(HashMap<String, String> filterMap) {

		List<AMIItem> amiItemList = new ArrayList<AMIItem>();

		// リージョン指定
		this.clientManager.changeRegion();

		// AMI抽出の条件
		DescribeImagesRequest describeImagesRequest = new DescribeImagesRequest();
		List<String> ownersList = new ArrayList<String>();

		// OwnerIDを指定する(self, amazon, redhat, 00000000)
		ownersList.add(filterMap.get("ownerid"));
		describeImagesRequest.setOwners(ownersList);

		// AMIのみ取得するために、Filterを定義する
		ArrayList<Filter> filters = new ArrayList<Filter>();

		Filter filter = new Filter();
		filter.setName("image-type");

		// machineを指定
		List<String> valueList = new ArrayList<String>();
		valueList.add("machine");
		filter.setValues(valueList);

		// Filterを定義
		filters.add(filter);

		// 追加フィルター
		for (Iterator<String> it = filterMap.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			Log.d("ami_list",
					String.format("key:%s, value:%s", key, filterMap.get(key)));
			if (key.toLowerCase().equals("ownerid"))
				continue;
			filters.add(new Filter().withName(key).withValues(
					filterMap.get(key)));
		}

		// Filterを設定
		describeImagesRequest.setFilters(filters);

		// 所持しているAMIのリストを取得
		DescribeImagesResult describeImagesResult = this.clientManager.ec2()
				.describeImages(describeImagesRequest);

		// AMIのListを取得(Imageはcom.amazonaws.services.ec2.modelのImageクラスのこと)
		List<Image> amiList = describeImagesResult.getImages();

		// 各AMIの中身を確認
		for (Image image : amiList) {
			// http://docs.amazonwebservices.com/AWSAndroidSDK/latest/javadoc/com/amazonaws/services/ec2/model/Image.html
			AMIItem amiItem = new AMIItem();

			StringBuilder builder = new StringBuilder();

			amiItem.imageId = image.getImageId();
			amiItem.imageType = image.getImageType();
			amiItem.imageLocation = image.getImageLocation();
			amiItem.name = image.getName();
			amiItem.architecture = image.getArchitecture();
			amiItem.platform = image.getPlatform();
			amiItem.state = image.getState();
			amiItem.ownerId = image.getOwnerId();
			amiItem.rootDeviceType = image.getRootDeviceType();
			amiItem.rootDeviceName = image.getRootDeviceName();
			amiItem.description = image.getDescription();
			builder.setLength(0); // 初期化
			List<Tag> tags = image.getTags(); // tag
			for (Tag tag : tags) {
				builder.append(tag.getValue());
				builder.append(", ");
			}

			// アイテムに追加
			amiItemList.add(amiItem);
		}

		return amiItemList;
	}

	/**
	 * キーペアを新規作成
	 *
	 * @param keyPairName
	 * @return
	 * @throws Exception
	 *
	 * @code Pair<String, String> sshAuthenticationKey = createKeyPairs("hoge");
	 *       Strinig fingerprint = sshAuthenticationKey.first; String material =
	 *       sshAuthenticationKey.second;
	 * @endcode
	 */
	public Pair<String, String> createKeyPairs(String keyPairName) {
		KeyPair keyPair;

		CreateKeyPairRequest kpReq = new CreateKeyPairRequest();
		kpReq.setKeyName(keyPairName);
		CreateKeyPairResult keyPairRes = clientManager.ec2().createKeyPair(
				kpReq);
		keyPair = keyPairRes.getKeyPair();

		Pair<String, String> sshAuthenticationKey = new Pair<String, String>(
				keyPair.getKeyFingerprint(), keyPair.getKeyMaterial());

		return sshAuthenticationKey;
	}

	/**
	 * キーペアを取得
	 *
	 * @return
	 */
	public List<String> getKeyPairs() {
		List<String> itemList = new ArrayList<String>();

		DescribeKeyPairsResult describeKeyPairsResult = this.clientManager
				.ec2().describeKeyPairs();
		List<KeyPairInfo> keyPairInfos = describeKeyPairsResult.getKeyPairs();
		for (KeyPairInfo keyPairInfo : keyPairInfos) {
			itemList.add(keyPairInfo.getKeyName());
		}

		return itemList;
	}

	/**
	 * アベイラビリティゾーンを取得
	 *
	 * @return
	 * @note - "any"=指定しない。を含むので使用側で不要ならば削除
	 */
	public List<String> getAvailabilityZones() {
		List<String> itemList = new ArrayList<String>();

		DescribeAvailabilityZonesRequest request = new DescribeAvailabilityZonesRequest();
		DescribeAvailabilityZonesResult describeAvailabilityZonesResult = this.clientManager
				.ec2().describeAvailabilityZones(request);
		List<AvailabilityZone> availabilityZones = describeAvailabilityZonesResult
				.getAvailabilityZones();

		itemList.add("any");
		for (AvailabilityZone az : availabilityZones) {
			itemList.add(az.getZoneName());
		}

		return itemList;
	}

	/**
	 * セキュリティグループを取得
	 *
	 * @return
	 */
	public List<String> getSecurityGroups() {
		List<String> itemList = new ArrayList<String>();

		DescribeSecurityGroupsRequest request = new DescribeSecurityGroupsRequest();
		DescribeSecurityGroupsResult describeSecurityGroupsResult = this.clientManager
				.ec2().describeSecurityGroups(request);
		List<SecurityGroup> securityGroups = describeSecurityGroupsResult
				.getSecurityGroups();
		for (SecurityGroup sg : securityGroups) {
			itemList.add(sg.getGroupName());
		}

		return itemList;
	}
}
