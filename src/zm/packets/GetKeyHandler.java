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
public class GetKeyHandler implements Handler {

	public GetKeyHandler(PacketProcessor processor) {
		this.processor = processor;
	}

	private final PacketProcessor processor;

	@Override
	public void handle(InputStream in, OutputStream out) throws IOException, InterruptedException {
		// The "get key" metadata response is 1024 bytes long, where the first 32 are the key and the rest are zeroes
		StreamScanner s = new StreamScanner(in);

		byte key[] = new byte[32];
		s.next(key);

		int channel = 0;
		for(PacketIdentifier p : PacketIdentifier.PFRAME_IDS) {
			processor.registerHandler(p, new VideoFrameHandler(channel++, key));
		}

		// skip the rest of the response
		s.skip(1024 - 32);
	}
}
