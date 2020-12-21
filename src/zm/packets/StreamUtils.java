package zm.packets;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Various utilities methods for working with streams.
 *
 * @author zm
 */
public class StreamUtils {

	/**
	 * Reads the desired number of bytes from the given input stream into the given
	 * buffer, repeating as necessary until the buffer is filled or EOF has been
	 * reached.
	 *
	 * @param in     the InputStream to read from
	 * @param buffer the buffer to write to
	 * @param offset an offset into the buffer to start writing
	 * @param length the number of bytes to read
	 *
	 * @return the number of bytes read. If the returned value is not equal to
	 *         {@code length}, then EOF was reached
	 *
	 * @pre in != null
	 * @pre buffer != null
	 * @pre offset >= 0
	 * @pre length >= 0
	 * @pre buffer.length >= offset + length
	 */
	public static final int blockingRead(InputStream in, byte[] buffer, int offset, int length)
			throws IOException, InterruptedException {

		assert in != null;
		assert buffer != null;
		assert offset >= 0;
		assert length >= 0;
		assert buffer.length >= offset + length;

		if (length == 0) {
			return 0;
		}

		int bytesRead = 0;
		while (bytesRead < length) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}

			int r = in.read(buffer, bytesRead, length - bytesRead);
			if (r == -1)
				break;

			bytesRead += r;
		}

		return bytesRead;
	}

	/**
	 * Same as {@link #blockingRead(InputStream, byte[], int, int)}, except an
	 * {@link EOFException} is thrown if fewer than the desired number of bytes were
	 * read.
	 *
	 * @see {@link #blockingRead(InputStream, byte[], int, int)}
	 */
	public static final void blockingReadExact(InputStream in, byte[] buffer, int offset, int length)
			throws IOException, InterruptedException {
		if (blockingRead(in, buffer, offset, length) != length)
			throw new EOFException();
	}

	/**
	 * Consumes and discards the given number of bytes from the given input stream.
	 *
	 * @param in    the InputStream to read from
	 * @param bytes the number of bytes to discard
	 *
	 * @return the number of bytes discarded. If the returned value is not equal to
	 *         {@code bytes} then EOF was reached.
	 * @throws InterruptedException
	 *
	 */
	public static final int blockingDiscard(InputStream in, int bytes) throws IOException, InterruptedException {
		assert in != null;
		assert bytes >= 0;

		if (bytes == 0)
			return 0;

		int bytesDiscarded = 0;
		while (bytesDiscarded < bytes) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}

			int b = in.read();

			if (b == -1)
				break;
			bytesDiscarded++;
		}
		return bytesDiscarded;
	}

	/**
	 * Same as {@link #blockingDiscard(InputStream, int)}, except an
	 * {@link EOFException} is thrown if fewer than the desired number of bytes were
	 * discarded.
	 * @throws InterruptedException
	 *
	 * @see {@link #blockingDiscard(InputStream, int)}
	 */
	public static final void blockingDiscardExact(InputStream in, int bytes) throws IOException, InterruptedException {
		if (blockingDiscard(in, bytes) != bytes)
			throw new EOFException();
	}

	/**
	 * Copies the given number of bytes from the input stream to the output stream.
	 *
	 * Not thread-safe, uses static buffer. For a thread-safe version, use bl
	 *
	 * @param in
	 * @param out
	 * @param bytes
	 *
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static final int blockingCopy(InputStream in, OutputStream out, int bytes) throws IOException, InterruptedException {
		return blockingCopy(in,out,bytes,COPY_BUFFER);
	}

	/**
	 * Copies the given number of bytes from the input stream to the output stream.
	 *
	 * Thread-safe version: provide your own buffer.
	 *
	 * @param in
	 * @param out
	 * @param bytes
	 *
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static final int blockingCopy(InputStream in, OutputStream out, int bytes, byte[] buffer) throws IOException, InterruptedException {
		assert in != null;
		assert out != null;
		assert buffer.length > 0;
		assert bytes >= 0;

		final int bufferSize = buffer.length;
		int bytesCopied = 0;
		while(bytes > 0) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}

			int toRead = Math.min(bytes, bufferSize);
			int read = blockingRead(in, buffer, 0, toRead);
			out.write(buffer,0,read);

			bytesCopied += read;
			bytes -= read;

			if(toRead != read) {
				break;
			}
		}

		return bytesCopied;
	}

	public static final void blockingCopyExact(InputStream in, OutputStream out, int bytes) throws IOException, InterruptedException {
		if(blockingCopy(in, out, bytes) != bytes) throw new EOFException();
	}

	private static final byte[] COPY_BUFFER = new byte[65536];
	static final OutputStream NULL = new OutputStream() {
		@Override
		public void write(int b) throws IOException {
			// do nothing
		}
	};
	public static byte[] parseHex(String hex) {
		byte bytes[] = new byte[hex.length() / 2];
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		while (!hex.isEmpty()) {
			String digit = hex.substring(0, 2);
			int value = Integer.parseInt(digit, 16);
			bb.put((byte) value);
			hex = hex.substring(2);
		}

		return bytes;
	}

	public static int fromUnsigned(byte b) {
		return ((int)b) & 0xFF;
	}

	public static String byteToHex(byte[] data) {
		return byteToHex(data, 0, data.length);
	}

	public static String byteToHex(byte[] data, int offset, int length) {
		StringBuilder sb = new StringBuilder(data.length * 2);
		for (int i = offset; i < offset + length; ++i) {
			sb.append(Integer.toHexString(data[i]));
		}
		return sb.toString();
	}
}
