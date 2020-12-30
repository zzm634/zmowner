package zm.packets;

import java.nio.charset.Charset;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

/**
 * PacketProcessor handles the incoming data feed directly from the camera.
 * <p>
 * Data coming back from the camera is a stream of data broken up into "packets"
 * (not actual TCP packets, mind you), that start with a 4-byte identifier
 * specifying the type of packet to follow. Each packet type has a different
 * format (including header length, etc), so after decoding the header, a
 * suitable packet handler is chosen to process the data afterward.
 *
 * @author zm
 */
public class PacketProcessor extends Processor<PacketIdentifier> {

	private SecretKeySpec aesKey = null;

	public SecretKeySpec getAesKey() {
		return aesKey;
	}

	public void setAesKey(SecretKeySpec aesKey) {
		this.aesKey = aesKey;
	}

	public void setAesKey(byte[] aesKey) {
		this.setAesKey(new SecretKeySpec(aesKey, "AES"));
	}

	public void setAesKey(String aesKey) {
		this.setAesKey(aesKey.getBytes(Charset.forName("UTF-8")));
	}

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

	public static PacketProcessor getDefaultProcessor(boolean singleStream) {
		PacketProcessor pp = new PacketProcessor();

		for (int channel = 0; channel < PacketIdentifier.IFRAME_IDS.size(); channel++) {
			Integer ch = singleStream ? null : channel;

			Handler iFrameHandler = new VideoFrameHandler(ch);
			Handler pFrameHandler = new VideoFrameHandler(ch, pp);
			Handler aFrameHandler = new AudioFrameHandler(ch);

			if (singleStream && channel != 0) {
				iFrameHandler = new Nullify(iFrameHandler);
				pFrameHandler = new Nullify(pFrameHandler);
				aFrameHandler = new Nullify(aFrameHandler);
			}

			pp.registerHandler(PacketIdentifier.IFRAME_IDS.get(channel), iFrameHandler);
			pp.registerHandler(PacketIdentifier.PFRAME_IDS.get(channel), pFrameHandler);
			pp.registerHandler(PacketIdentifier.AUDIO_IDS.get(channel), aFrameHandler);
		}

		pp.registerHandler(PacketIdentifier.COMMAND, CommandProcessor.getDefaultProcessor(pp));
		pp.registerHandler(PacketIdentifier.FILE_HEADER_264, new FileHeader264Handler(pp));

		return pp;

	}
}
