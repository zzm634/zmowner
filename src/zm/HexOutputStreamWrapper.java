package zm;

import java.io.IOException;
import java.io.OutputStream;

/**
 * HexOutputStreamWrapper is an OutputStream that converts all incoming data to 2-character hex digits before writing to the wrapped OutputStream.
 *
 * Useful for debugging.
 *
 * @author zm
 */
public class HexOutputStreamWrapper extends OutputStream {

	public HexOutputStreamWrapper(OutputStream wrapped) {
		assert wrapped != null;

		this.wrapped = wrapped;
	}

	private final OutputStream wrapped;

	@Override
	public void write(int b) throws IOException {
		wrapped.write(String.format("%02X ", b & 0xFF).getBytes());
	}

}
