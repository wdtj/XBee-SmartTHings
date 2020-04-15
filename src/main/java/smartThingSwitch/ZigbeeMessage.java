package smartThingSwitch;

import java.io.IOException;

import com.digi.xbee.api.ZigBeeDevice;
import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;

public abstract class ZigbeeMessage {

	public static final int ZIGBEE_REQUEST = 0x0000;
	public static final int ZIGBEE_RESPONSE = 0x8000;

	public static final int DEVICE_ANNOUNCE = 0x0013;
	public static final int ACTIVE_ENDPOINT_REQUEST = 0x005;
	public static final int SIMPLE_DESCRIPTOR_REQUEST = 0x0004;

	protected ZigBeeDevice myDevice;

	public ZigbeeMessage() {
		super();
	}

	protected byte[] bigEndian(byte[] data) {
		final byte[] be = new byte[data.length];
		for (int i = 0; i < data.length; ++i) {
			be[i] = data[data.length - i - 1];
		}
		return be;
	}

	abstract public void send(ZigBeeDevice myDevice) throws TimeoutException, XBeeException, IOException;

}