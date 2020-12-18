package zm.packets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A utility class that wraps an {@code InputStream} and provides methods for reading values.
 *
 * Unless otherwise noted, all integers are read little-endian.
 *
 * @author zm
 */
public class StreamScanner {

	public StreamScanner(InputStream in) {
		assert in != null;
		this.in = in;
	}

	public int nextInt32() throws IOException, InterruptedException {
		StreamUtils.blockingReadExact(in, buf, 0, 4);
		bbuf.rewind();
		return bbuf.getInt();
	}

	public short nextInt16() throws IOException, InterruptedException {
		StreamUtils.blockingReadExact(in, buf, 0, 2);
		bbuf.rewind();
		return bbuf.getShort();
	}

	public char nextInt8() throws IOException, InterruptedException {
		StreamUtils.blockingReadExact(in, buf, 0, 1);
		bbuf.rewind();
		return bbuf.getChar();
	}

	public byte nextByte()  throws IOException, InterruptedException{
		return (byte)nextInt8();
	}

	public long nextInt64()  throws IOException, InterruptedException {
		StreamUtils.blockingReadExact(in, buf, 0, 8);
		bbuf.rewind();
		return bbuf.getLong();
	}

	public void next(byte[] buffer) throws IOException, InterruptedException {
		StreamUtils.blockingReadExact(in, buffer, 0, buffer.length);
	}

	public void skip(int bytes) throws IOException, InterruptedException {
		StreamUtils.blockingDiscardExact(in, bytes);
	}

	public void copy(OutputStream out, int bytes) throws IOException, InterruptedException {
		StreamUtils.blockingCopyExact(in, out, bytes);
	}

	// buffer for small values
	private final byte buf[] = new byte[8];
	private final ByteBuffer bbuf = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);

	private final InputStream in;

}
