package zm.packets;

import java.util.Arrays;

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

	public static PacketProcessor getDefaultProcessor(String aesKey, boolean singleStream) {
		PacketProcessor pp = new PacketProcessor();

		int channel = 0;
		for (PacketIdentifier i : PacketIdentifier.IFRAME_IDS) {
			Integer ch = singleStream ? null : channel;

			Handler h = new VideoFrameHandler(ch);

			if (singleStream && channel != 0) {
				h = new Nullify(h);
			}

			pp.registerHandler(i, h);

			channel++;
		}

		channel = 0;
		for (PacketIdentifier p : PacketIdentifier.PFRAME_IDS) {
			Integer ch = singleStream ? null : channel;

			Handler h = aesKey != null
					? new VideoFrameHandler(channel, aesKey)
					: new VideoFrameHandler(channel);

			if (singleStream && channel != 0) {
				h = new Nullify(h);
			}

			pp.registerHandler(p, h);

			channel++;
		}

		pp.registerHandler(PacketIdentifier.COMMAND, CommandProcessor.getDefaultProcessor(pp));

		// do audio frame handlers?

		return pp;

	}
}
