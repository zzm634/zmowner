package zm.packets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Handler {

	/**
	 * Handles a single packet from the given input stream, writing to the given
	 * output stream if necessary. The input stream should not be closed when the
	 * handler exits. The given input stream is positioned immediately after the
	 * header bytes.
	 *
	 * @param in
	 * @param out
	 */
	default void handle(byte header[], InputStream in, OutputStream out) throws IOException, InterruptedException {
		handle(in, out);
	}

	/**
	 * Handles a single packet from the given input stream, writing to the output
	 * stream as necessary.
	 *
	 * @param in
	 * @param out
	 * @throws IOException
	 * @throws InterruptedException
	 */
	void handle(InputStream in, OutputStream out) throws IOException, InterruptedException;

}
