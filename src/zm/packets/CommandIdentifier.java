package zm.packets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

enum CommandIdentifier implements Header {
	// write these "big-endian" because they're easier to read
	// actual values are read little-endian.
	GET_KEY(0x3639);

	private CommandIdentifier(long id) {
		short sId = (short) id;
		ByteBuffer bb = ByteBuffer.wrap(this.identifier);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.putShort(sId);
	}

	public static CommandIdentifier get(byte identifier[]) {
		return byId.get(ByteBuffer.wrap(identifier).getShort());
	}

	private static final Map<Short, CommandIdentifier> byId = new HashMap<>();
	static {
		for(CommandIdentifier c : CommandIdentifier.values()) {
			byId.put(ByteBuffer.wrap(c.identifier).getShort(), c);
		}
	}

	private final byte identifier[] = new byte[2];

	@Override
	public String getName() {
		return this.name();
	}

	@Override
	public byte[] getIdentifier() {
		return this.identifier;
	}

	@Override
	public Integer getMetadataLength() {
		return null;
	}
}