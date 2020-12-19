package zm.packets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Processes incoming "command" echoes or responses (0x55 0x55 0xAA 0xAA)
 *
 * Does various things: - Update the parent stream handler with new
 * VideoFrameHandlers if a new encryption key is encountered.
 *
 * @author zm
 *
 */
public class CommandProcessor extends Processor<CommandIdentifier> implements Handler {

	public CommandProcessor() {
		super(Arrays.asList(CommandIdentifier.values()));
	}

	@Override
	protected CommandIdentifier decodeHeader(byte[] header) {
		// commands have a 12-byte header, but this processor will be skipping the first
		// 4 bytes (5555aaaa), so really it's a 8 byte header we're dealing with here.

		// the last two bytes are the ones we care about
		byte commandId[] = new byte[2];

		commandId[0] = header[6];
		commandId[1] = header[7];
		return CommandIdentifier.get(commandId);
	}

	@Override
	protected int getHeaderLength() {
		return 8;
	}

	public static CommandProcessor getDefaultProcessor(PacketProcessor parent) {
		CommandProcessor processor = new CommandProcessor();
		processor.registerHandler(CommandIdentifier.GET_KEY, new GetKeyHandler(parent));
		return processor;
	}
}
