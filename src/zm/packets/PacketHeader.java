package zm.packets;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.TreeMap;

public enum PacketHeader {

	COMMAND(0x55, 0x55, 0xaa, 0xaa),

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

	private PacketHeader(byte[] identifier) {
		assert identifier.length == 4;
		this.identifier = identifier;
		this.headerLength = null;
	}

	private PacketHeader(String idString) {
		assert idString.length() == 4;
		this.identifier = idString.getBytes();
		assert this.identifier.length == 4;
		this.headerLength = null;
	}

	private PacketHeader(int... idBytes) {
		assert idBytes.length == 4;

		this.identifier = new byte[4];
		this.identifier[0] = (byte) (idBytes[0]);
		this.identifier[1] = (byte) (idBytes[1]);
		this.identifier[2] = (byte) (idBytes[2]);
		this.identifier[3] = (byte) (idBytes[3]);

		this.headerLength = null;
	}

	public static PacketHeader get(byte headerValue[]) {
		assert headerValue.length == 4;

		ByteBuffer bb = ByteBuffer.wrap(headerValue);
		return byIds.get(bb.getInt());
	}

	/**
	 * Reads the next four bytes from the input stream and returns the decoded
	 * packet header.
	 */
	public static PacketHeader readAndGet(InputStream in) throws IOException, EOFException, InterruptedException {
		byte header[] = new byte[4];
		StreamUtils.blockingReadExact(in, header, 0, 4);
		return get(header);
	}

	private static final Map<Integer, PacketHeader> byIds = new TreeMap<>();
	static {
		for (PacketHeader header : PacketHeader.values()) {
			ByteBuffer bb = ByteBuffer.wrap(header.identifier);
			int idInt = bb.getInt();
			byIds.put(idInt, header);
		}
	}

	private final byte[] identifier;

	/**
	 * The size of the header metadata content, in bytes, if known. Does not include
	 * the 4-byte header itself.
	 */
	public final Integer headerLength;
}
