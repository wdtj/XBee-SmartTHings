package smartThingSwitch;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

import com.digi.xbee.api.ZigBeeDevice;
import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.ExplicitXBeeMessage;
import com.digi.xbee.api.utils.HexUtils;

public class SimpleDescriptorResponse extends ZigbeeMessage {

	byte frameId;
	byte status;
	byte addr16[];
	byte endpoint;
	short endpointType;
	short endpointDevice;
	byte version;
	ArrayList<Short> clusterTypeIn = new ArrayList<>();
	ArrayList<Short> clusterTypeOut = new ArrayList<>();

	ExplicitXBeeMessage request;

	private SimpleDescriptorResponse(ExplicitXBeeMessage request, int status, int endpoint, int endpointType,
			int endpointDevice, int version) {
		super();

		this.request = request;

		this.frameId = request.getData()[0];
		this.status = (byte) status;
		this.addr16 = Arrays.copyOfRange(request.getData(), 1, 3);

		this.endpoint = (byte) endpoint;
		this.endpointType = (short) endpointType;
		this.endpointDevice = (short) endpointDevice;
		this.version = (byte) version;
	}

	public static SimpleDescriptorResponse create(ExplicitXBeeMessage request, byte status, int endpoint,
			int endpointType, int endpointDevice, byte version) {

		return new SimpleDescriptorResponse(request, status, endpoint, endpointType, endpointDevice, version);
	}

	public SimpleDescriptorResponse clusterIn(int i) {
		this.clusterTypeIn.add((short) i);
		return this;
	}

	public SimpleDescriptorResponse clusterOut(int i) {
		this.clusterTypeOut.add((short) i);
		return this;
	}

	private byte[] getBytes() throws IOException {
		final int length = 1 + 2 + 2 + 1 + 1 + this.clusterTypeIn.size() * 2 + 1 + this.clusterTypeOut.size() * 2;

		ByteBuffer data;
		data = ByteBuffer.allocate(5 + length).order(ByteOrder.LITTLE_ENDIAN);
		try {
			data.put(this.frameId);
			data.put(this.status);
			data.put(this.addr16);
			data.put((byte) length);
			data.put(this.endpoint);
			data.putShort(this.endpointType);
			data.putShort(this.endpointDevice);
			data.put(this.version);
			data.put((byte) this.clusterTypeIn.size());
			for (final Short cluster : this.clusterTypeIn) {
				data.putShort(cluster);
			}
			data.put((byte) this.clusterTypeOut.size());
			for (final Short cluster : this.clusterTypeOut) {
				data.putShort(cluster);
			}
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("SimpleDescriptorResponse: " + HexUtils.prettyHexString(data.array()));
		return data.array();

	}

	@Override
	public void send(ZigBeeDevice myDevice) throws TimeoutException, XBeeException, IOException {
		System.out.println("Sending SimpleDescriptorResponse " + this.toString());

		this.myDevice = myDevice;

		myDevice.sendExplicitData(this.request.getDevice(), 0x00, 0x00, SIMPLE_DESCRIPTOR_REQUEST | ZIGBEE_RESPONSE,
				0x0000, this.getBytes());
	}

	@Override
	public String toString() {
		return "SimpleDescriptorResponse [frameId=" + this.frameId + ", status=" + this.status + ", addr16="
				+ HexUtils.prettyHexString(this.addr16) + ", endpoint=" + this.endpoint + ", endpointType="
				+ this.endpointType + ", endpointDevice=" + this.endpointDevice + ", version=" + this.version
				+ ", clusterTypeIn=" + this.clusterTypeIn + ", clusterTypeOut=" + this.clusterTypeOut
				+ ", request addr=" + this.request.getDevice().get64BitAddress() + "]";
	}
}
