package smartThingSwitch;

import java.io.IOException;

import com.digi.xbee.api.ZigBeeDevice;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.listeners.IExplicitDataReceiveListener;
import com.digi.xbee.api.listeners.IModemStatusReceiveListener;
import com.digi.xbee.api.listeners.IPacketReceiveListener;
import com.digi.xbee.api.models.APIOutputMode;
import com.digi.xbee.api.models.AssociationIndicationStatus;
import com.digi.xbee.api.models.ExplicitXBeeMessage;
import com.digi.xbee.api.models.ModemStatusEvent;
import com.digi.xbee.api.models.XBeeMessage;
import com.digi.xbee.api.packet.XBeePacket;

public class SmartThingSwitch implements IModemStatusReceiveListener, IPacketReceiveListener, IDataReceiveListener,
		IExplicitDataReceiveListener {

	private final ZigBeeDevice myDevice;
	private byte tranSeq = 0;

	public SmartThingSwitch(String port, int baudrate) {
		this.myDevice = new ZigBeeDevice(port, baudrate);
	}

	public void open() throws XBeeException {
		this.myDevice.open();

		this.myDevice.readDeviceInfo();

		this.myDevice.setAPIOutputMode(APIOutputMode.MODE_EXPLICIT_ZDO_PASSTHRU);
		this.myDevice.setNodeID("Fred");
		this.myDevice.setParameter("ZS", new byte[] { (byte) 0x02 });
		this.myDevice.setParameter("JV", new byte[] { (byte) 0x01 });
		this.myDevice.setParameter("KY",
				new byte[] { (byte) 0x5A, (byte) 0x69, (byte) 0x67, (byte) 0x42, (byte) 0x65, (byte) 0x65, (byte) 0x41,
						(byte) 0x6C, (byte) 0x6C, (byte) 0x69, (byte) 0x61, (byte) 0x6E, (byte) 0x63, (byte) 0x65,
						(byte) 0x30, (byte) 0x39 });
		this.myDevice.setParameter("EE", new byte[] { (byte) 0x01 });
		this.myDevice.setParameter("EO", new byte[] { (byte) 0x01 });
		this.myDevice.setParameter("NO", new byte[] { (byte) 0x03 });
		this.myDevice.setParameter("SC", new byte[] { (byte) 0x07f, (byte) 0xff });

		this.myDevice.addModemStatusListener(this);
		this.myDevice.addPacketListener(this);
		this.myDevice.addDataListener(this);
		this.myDevice.addExplicitDataListener(this);

		if (this.myDevice.getAssociationIndicationStatus() == AssociationIndicationStatus.SUCCESSFULLY_JOINED) {
			System.out.println("Already joined");
			joined();
		}
	}

	@Override
	public void modemStatusEventReceived(ModemStatusEvent modemStatusEvent) {
		System.out.format("Modem Status event received: %s%n", modemStatusEvent.toString());

		try {
			if (this.myDevice.getAssociationIndicationStatus() == AssociationIndicationStatus.SUCCESSFULLY_JOINED) {
				joined();
			}
		} catch (final XBeeException e) {
			e.printStackTrace();
		}
	}

	private void joined() {
		try {
			this.myDevice.readDeviceInfo();
			System.out.println(" - 64-bit address:          " + this.myDevice.get64BitAddress());
			System.out.println(" - 16-bit address:          " + this.myDevice.get16BitAddress());

			DeviceAnnounce.create(this.tranSeq++, this.myDevice.get64BitAddress().getValue(),
					this.myDevice.get16BitAddress().getValue(), (byte) 0x04).send(this.myDevice);

		} catch (XBeeException | IOException e) {
			e.printStackTrace();
			return;
		}
	}

	@Override
	public void dataReceived(XBeeMessage xbeeMessage) {
//		System.out.println("xbeeMessage" + xbeeMessage);
	}

	@Override
	public void packetReceived(XBeePacket receivedPacket) {
		System.out.println("receivedPacket: " + receivedPacket);
		switch (receivedPacket.getParameters().get("AT Command")) {
		case "AI":
			System.out.println("Received Association Indicator " + receivedPacket.getParameters().get("Status"));
			break;
		}
	}

	@Override
	public synchronized void explicitDataReceived(ExplicitXBeeMessage explicitXBeeMessage) {
		synchronized (this.myDevice) {
//			this.myDevice.addExplicitDataListener(this);
			final int clusterID = explicitXBeeMessage.getClusterID();
			final int profileID = explicitXBeeMessage.getProfileID();
			System.out.println("explicitDataReceived Profile " + profileID + ", Cluster " + clusterID);

			try {
				switch (profileID) {
				case 0x0000:
					switch (clusterID) {
					case ZigbeeMessage.ACTIVE_ENDPOINT_REQUEST:
						System.out.println("Received ACTIVE_ENDPOINT_REQUEST");
						ActiveEndpointsResponse.create(explicitXBeeMessage, (byte) 0x00, (byte) 0x01, (byte) 0x08)
								.send(this.myDevice);
						break;
					case ZigbeeMessage.SIMPLE_DESCRIPTOR_REQUEST:
						System.out.println("Received SIMPLE_DESCRIPTOR_REQUEST");
						SimpleDescriptorResponse
								.create(explicitXBeeMessage, (byte) 0x00, (byte) 0x08, 0x0104, 0x0002, (byte) 0x30)
								.clusterIn(0x0000).clusterIn(0x0003).clusterIn(0x0006).send(this.myDevice);
						break;
					default:
						System.out.println("Undefined Profile " + profileID + ", Cluster " + clusterID);
						break;
					}
					break;
				default:
					System.out.println("Undefined Profile " + profileID + ", Cluster " + clusterID);
					break;
				}
			} catch (final Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
