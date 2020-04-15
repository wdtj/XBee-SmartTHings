package smartThingSwitch;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.digi.xbee.api.ZigBeeDevice;
import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.XBee16BitAddress;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.utils.HexUtils;

class DeviceAnnounce extends ZigbeeMessage {
	private final byte frameId;
	private final byte addr64[];
	private final byte addr16[];
	private final byte capability;

	private DeviceAnnounce(byte frameId, byte[] addr64, byte[] addr16, byte capability) {
		super();
		this.frameId = frameId;
		this.addr64 = addr64;
		this.addr16 = addr16;
		this.capability = capability;
	}

	public static ZigbeeMessage create(byte frameId, byte[] addr64, byte[] addr16, byte capability) {
		return new DeviceAnnounce(frameId, addr64, addr16, capability);
	}

	private byte[] getBytes() throws IOException {
		final ByteBuffer data = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN);
		data.put(this.frameId);
		data.put(bigEndian(this.addr16));
		data.put(bigEndian(this.addr64));
		data.put(this.capability);

		System.out.println("DeviceAnnounce: " + HexUtils.prettyHexString(data.array()));
		return data.array();
	}

	@Override
	public void send(ZigBeeDevice myDevice) throws TimeoutException, XBeeException, IOException {
		System.out
				.println("Sending DeviceAnnounce to " + myDevice.get64BitAddress() + ":" + myDevice.get16BitAddress());

		this.myDevice = myDevice;
		myDevice.sendExplicitData(XBee64BitAddress.COORDINATOR_ADDRESS, new XBee16BitAddress(0xff, 0xfc), 0x00, 0x00,
				DEVICE_ANNOUNCE, 0x0000, this.getBytes());
	}

	@Override
	public String toString() {
		return "DeviceAnnounce [frameId=" + this.frameId + ", addr64=" + HexUtils.prettyHexString(this.addr64)
				+ ", addr16=" + HexUtils.prettyHexString(this.addr16) + ", capability=" + this.capability + "]";
	}

}
