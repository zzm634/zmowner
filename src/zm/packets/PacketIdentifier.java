package zm.packets;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public enum PacketIdentifier implements Identifier {

	COMMAND(StreamUtils.parseHex("5555aaaa"), null),

	// Video stream P-frames, per channel
	PFRAME_0("00dc", new VideoFrameHandler(0)),
	PFRAME_1("10dc", new VideoFrameHandler(1)),
	PFRAME_2("20dc", new VideoFrameHandler(2)),
	PFRAME_3("30dc", new VideoFrameHandler(3)),
	PFRAME_4("40dc", new VideoFrameHandler(4)),
	PFRAME_5("50dc", new VideoFrameHandler(5)),
	PFRAME_6("60dc", new VideoFrameHandler(6)),
	PFRAME_7("70dc", new VideoFrameHandler(7)),

	// Video stream I-frames, per channel
	IFRAME_0("01dc", new VideoFrameHandler(0)),
	IFRAME_1("11dc", new VideoFrameHandler(1)),
	IFRAME_2("21dc", new VideoFrameHandler(2)),
	IFRAME_3("31dc", new VideoFrameHandler(3)),
	IFRAME_4("41dc", new VideoFrameHandler(4)),
	IFRAME_5("51dc", new VideoFrameHandler(5)),
	IFRAME_6("61dc", new VideoFrameHandler(6)),
	IFRAME_7("71dc", new VideoFrameHandler(7)),

	AUDIO_0("01wb", new AudioFrameHandler(0)),
	AUDIO_1("11wb", new AudioFrameHandler(1)),
	AUDIO_2("21wb", new AudioFrameHandler(2)),
	AUDIO_3("31wb", new AudioFrameHandler(3)),
	AUDIO_4("41wb", new AudioFrameHandler(4)),
	AUDIO_5("51wb", new AudioFrameHandler(5)),
	AUDIO_6("61wb", new AudioFrameHandler(6)),
	AUDIO_7("71wb", new AudioFrameHandler(7)),

	// Contains encryption key, see FileHeader264Handler
	FILE_HEADER_264(StreamUtils.parseHex("78563412"), new DiscardHandler(0x200));

	public static final List<PacketIdentifier> IFRAME_IDS = Arrays.asList(
			IFRAME_0,
			IFRAME_1,
			IFRAME_2,
			IFRAME_3,
			IFRAME_4,
			IFRAME_5,
			IFRAME_6,
			IFRAME_7);

	public static final List<PacketIdentifier> PFRAME_IDS = Arrays.asList(
			PFRAME_0,
			PFRAME_1,
			PFRAME_2,
			PFRAME_3,
			PFRAME_4,
			PFRAME_5,
			PFRAME_6,
			PFRAME_7);

	public static final List<PacketIdentifier> AUDIO_IDS = Arrays.asList(
			AUDIO_0,
			AUDIO_1,
			AUDIO_2,
			AUDIO_3,
			AUDIO_4,
			AUDIO_5,
			AUDIO_6,
			AUDIO_7);

	@Override
	public byte[] getBytes() {
		return this.identifier;
	}

	private PacketIdentifier(byte[] identifier, Handler defaultHandler) {
		assert identifier.length == 4;
		this.identifier = identifier;
		this.headerLength = null;
		this.defaultHandler = defaultHandler;
	}

	private PacketIdentifier(String idString, Handler defaultHandler) {
		assert idString.length() == 4;
		this.identifier = idString.getBytes();
		assert this.identifier.length == 4;
		this.headerLength = null;
		this.defaultHandler = defaultHandler;
	}

	public static PacketIdentifier get(byte headerValue[]) {
		assert headerValue.length == 4;

		ByteBuffer bb = ByteBuffer.wrap(headerValue);
		return byIds.get(bb.getInt());
	}

	/**
	 * Reads the next four bytes from the input stream and returns the decoded
	 * packet header.
	 */
	public static PacketIdentifier readAndGet(InputStream in) throws IOException, EOFException, InterruptedException {
		byte header[] = new byte[4];
		StreamUtils.blockingReadExact(in, header, 0, 4);
		return get(header);
	}

	private static final Map<Integer, PacketIdentifier> byIds = new TreeMap<>();
	static {
		for (PacketIdentifier header : PacketIdentifier.values()) {
			ByteBuffer bb = ByteBuffer.wrap(header.identifier);
			int idInt = bb.getInt();
			byIds.put(idInt, header);
		}
	}

	@Override
	public String getName() {
		return this.name();
	}

	@Override
	public Handler getDefaultHandler() {
		return defaultHandler;
	}

	private final byte[] identifier;
	private final Handler defaultHandler;

	/**
	 * The size of the header metadata content, in bytes, if known. Does not include
	 * the 4-byte header itself.
	 */
	private final Integer headerLength;
}
