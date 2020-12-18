package zm.packets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface PacketHandler {

	/**
	 * Handles the incoming packet contents (minus the header) from the given input
	 * stream, writing to the given output stream if necessary. The input stream
	 * should not be closed when the handler exits.
	 *
	 * @param in
	 * @param out
	 */
	void handle(InputStream in, OutputStream out) throws IOException, InterruptedException;

}
