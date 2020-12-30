package zm.packets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Handles "get video key" responses by updating a PacketProcessor with new
 * PFRAME packet handlers that can decrypt using the new key.
 *
 * @author zm
 */
public class GetKeyHandler extends CommandHandler {

	public GetKeyHandler(PacketProcessor processor) {
		this.processor = processor;
	}

	private final PacketProcessor processor;

	@Override
	protected void handle(CommandHeader header, InputStream in, OutputStream out)
			throws IOException, InterruptedException {
		
		// The "get key" metadata response is 1024 bytes long, where the first 32 are
		// the key and the rest are zeroes
		StreamScanner s = new StreamScanner(in);

		byte key[] = new byte[32];
		s.next(key);

		if (processor != null)
			processor.setAesKey(key);

		// skip the rest of the response
		s.skip(header.getPayloadLength() - key.length);

	}
}
