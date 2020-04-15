package smartThingSwitch;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import com.digi.xbee.api.ZigBeeDevice;
import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.ExplicitXBeeMessage;
import com.digi.xbee.api.utils.HexUtils;

public class ActiveEndpointsResponse extends ZigbeeMessage {

	byte frameId;
	byte status;
	byte addr16[];
	byte endpoints;
	byte endpointAddress;

	ExplicitXBeeMessage request;

	private ActiveEndpointsResponse(ExplicitXBeeMessage request, byte status, byte endpoints, byte endpointAddress) {
		super();

		this.request = request;

		this.frameId = request.getData()[0];
		this.status = status;
		this.addr16 = Arrays.copyOfRange(request.getData(), 1, 3);

		this.endpoints = endpoints;
		this.endpointAddress = endpointAddress;
	}

	public static ActiveEndpointsResponse create(ExplicitXBeeMessage request, byte status, byte endpoints,
			byte endpointAddress) {

		return new ActiveEndpointsResponse(request, status, endpoints, endpointAddress);
	}

	private byte[] getBytes() throws IOException {
		final ByteBuffer data = ByteBuffer.allocate(6).order(ByteOrder.BIG_ENDIAN);
		data.put(this.frameId);
		data.put(this.status);
		data.put(this.addr16);
		data.put(this.endpoints);
		data.put(this.endpointAddress);

		System.out.println("ActiveEndpointsResponse: " + HexUtils.prettyHexString(data.array()));
		return data.array();
	}

	@Override
	public void send(ZigBeeDevice myDevice) throws TimeoutException, XBeeException, IOException {
		System.out.println("Sending ActiveEndpointsResponse " + this.toString());

		this.myDevice = myDevice;

		myDevice.sendExplicitData(this.request.getDevice(), 0x00, 0x00, ACTIVE_ENDPOINT_REQUEST | ZIGBEE_RESPONSE,
				0x0000, this.getBytes());
	}

	@Override
	public String toString() {
		return "ActiveEndpointsResponse [frameId=" + this.frameId + ", status=" + this.status + ", addr16="
				+ HexUtils.prettyHexString(this.addr16) + ", endpointAddress=" + this.endpointAddress + "]";
	}

}
