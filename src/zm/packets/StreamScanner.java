package zm.packets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A utility class that wraps an {@code InputStream} and provides methods for
 * reading values.
 *
 * Unless otherwise noted, all integers are read little-endian.
 *
 * @author zm
 */
public class StreamScanner {

	private long bytesRead = 0;

	public StreamScanner(InputStream in) {
		assert in != null;
		this.in = in;
	}

	public int nextInt32() throws IOException, InterruptedException {
		StreamUtils.blockingReadExact(in, buf, 0, 4);
		bytesRead += 4;
		bbuf.rewind();
		return bbuf.getInt();
	}

	public long nextUInt32() throws IOException, InterruptedException {
		return Integer.toUnsignedLong(nextInt32());
	}

	public short nextInt16() throws IOException, InterruptedException {
		StreamUtils.blockingReadExact(in, buf, 0, 2);
		bytesRead += 2;
		bbuf.rewind();
		return bbuf.getShort();
	}

	public int nextUInt16() throws IOException, InterruptedException {
		return Short.toUnsignedInt(nextInt16());
	}

	public char nextInt8() throws IOException, InterruptedException {
		StreamUtils.blockingReadExact(in, buf, 0, 1);
		bytesRead += 1;
		bbuf.rewind();
		return bbuf.getChar();
	}

	public byte nextByte() throws IOException, InterruptedException {
		return (byte) nextInt8();
	}

	public long nextInt64() throws IOException, InterruptedException {
		StreamUtils.blockingReadExact(in, buf, 0, 8);
		bytesRead += 8;
		bbuf.rewind();
		return bbuf.getLong();
	}

	public void next(byte[] buffer) throws IOException, InterruptedException {
		StreamUtils.blockingReadExact(in, buffer, 0, buffer.length);
		bytesRead += buffer.length;
	}

	public void skip(int bytes) throws IOException, InterruptedException {
		StreamUtils.blockingDiscardExact(in, bytes);
		bytesRead += bytes;
	}

	public void skip(long bytes) throws IOException, InterruptedException {
		while (bytes > 0) {
			int toSkip = (int) Math.min(Integer.MAX_VALUE, bytes);
			this.skip(toSkip);
			bytes -= toSkip;
		}
	}

	public void skipTo(int byteIndex) throws IOException, InterruptedException {
		if (byteIndex < bytesRead) {
			throw new RuntimeException("Cannot skip to byte index, already head past it.");
		}

		this.skip(byteIndex - bytesRead);
	}

	/**
	 * Resets the current "byte index" position, which is a count of bytes read.
	 * This is normally 0 when the StreamScanner is created, but can be useful if
	 * the stream scanner has been created after a portion of the stream has already
	 * been read.
	 * 
	 * @param byteIndex
	 */
	public void setByteIndex(long byteIndex) {
		this.bytesRead = byteIndex;
	}	

	public void copy(OutputStream out, int bytes) throws IOException, InterruptedException {
		StreamUtils.blockingCopyExact(in, out, bytes);
	}

	// buffer for small values
	private final byte buf[] = new byte[8];
	private final ByteBuffer bbuf = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);

	private final InputStream in;

}
