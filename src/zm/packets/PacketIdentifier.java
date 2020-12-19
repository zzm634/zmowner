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

public enum PacketIdentifier implements Header {

	COMMAND(0x5555aaaa),

	// Video stream P-frames, per channel
	PFRAME_0("00dc"),
	PFRAME_1("10dc"),
	PFRAME_2("20dc"),
	PFRAME_3("30dc"),
	PFRAME_4("40dc"),
	PFRAME_5("50dc"),
	PFRAME_6("60dc"),
	PFRAME_7("70dc"),

	// Video stream I-frames, per channel
	IFRAME_0("01dc"),
	IFRAME_1("11dc"),
	IFRAME_2("21dc"),
	IFRAME_3("31dc"),
	IFRAME_4("41dc"),
	IFRAME_5("51dc"),
	IFRAME_6("61dc"),
	IFRAME_7("71dc"),

	AUDIO_0("01wb"),
	AUDIO_1("11wb"),
	AUDIO_2("21wb"),
	AUDIO_3("31wb"),
	AUDIO_4("41wb"),
	AUDIO_5("51wb"),
	AUDIO_6("61wb"),
	AUDIO_7("71wb");

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
	public byte[] getIdentifier() {
		return this.identifier;
	}

	@Override
	public Integer getMetadataLength() {
		return this.headerLength;
	}

	private PacketIdentifier(byte[] identifier) {
		assert identifier.length == 4;
		this.identifier = identifier;
		this.headerLength = null;
	}

	private PacketIdentifier(String idString) {
		assert idString.length() == 4;
		this.identifier = idString.getBytes();
		assert this.identifier.length == 4;
		this.headerLength = null;
	}

	private PacketIdentifier(long id) {

		this.identifier = new byte[4];
		ByteBuffer bb = ByteBuffer.wrap(this.identifier);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.putInt((int) id);

		this.headerLength = null;
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

	private final byte[] identifier;

	/**
	 * The size of the header metadata content, in bytes, if known. Does not include
	 * the 4-byte header itself.
	 */
	private final Integer headerLength;
}
