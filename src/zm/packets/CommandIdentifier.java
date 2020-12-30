package zm.packets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

/**
 * A 2 byte sequence that identifies the type of command in a command packet's 8-byte body
 * @author zm
 *
 */
enum CommandIdentifier implements Identifier {
	// write these "big-endian" because they're easier to read
	// actual values are read little-endian.
	GET_KEY(0x3696),
	START_VGA(0xa290 , new DiscardHandler(4)),
	START_720P(0x0050, new DiscardHandler(4)),
	G_TALK_SETTING(0x6090, new DiscardHandler(8)),
	SET_AUDIOSWITCH(0x6690, new DiscardHandler(4));

	private CommandIdentifier(long id) {
		this(id, null);
	}

	private CommandIdentifier(long id, Handler defaultHandler) {
		short sId = (short) id;
		ByteBuffer bb = ByteBuffer.wrap(this.identifier);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.putShort(sId);
		this.defaultHandler = defaultHandler;
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
	public byte[] getBytes() {
		return this.identifier;
	}

	@Override
	public Handler getDefaultHandler() {
		return this.defaultHandler;
	}

	private final Handler defaultHandler;
}