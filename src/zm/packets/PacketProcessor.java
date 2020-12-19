package zm.packets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/**
 * PacketProcessor handles the incoming data feed directly from the camera.
 *
 * @author zm
 */
public class PacketProcessor extends Processor<PacketIdentifier> {

	public PacketProcessor() {
		super(Arrays.asList(PacketIdentifier.values()));
	}

	protected final int getHeaderLength() {
		return 4;
	}

	@Override
	protected PacketIdentifier decodeHeader(byte[] header) {
		return PacketIdentifier.get(header);
	}

	public static PacketProcessor getDefaultProcessor(String aesKey) {
		PacketProcessor pp = new PacketProcessor();

		int channel = 0;
		for(PacketIdentifier i : PacketIdentifier.IFRAME_IDS) {
			pp.registerHandler(i, new VideoFrameHandler(channel++));
		}

		channel = 0;
		for(PacketIdentifier p : PacketIdentifier.PFRAME_IDS) {

			Handler h = aesKey != null ?
					new VideoFrameHandler(channel,aesKey) :
						new VideoFrameHandler(channel);

			channel++;

			pp.registerHandler(p, h);
		}

		pp.registerHandler(PacketIdentifier.COMMAND, CommandProcessor.getDefaultProcessor(pp));

		// do audio frame handlers?

		return pp;

	}
}
