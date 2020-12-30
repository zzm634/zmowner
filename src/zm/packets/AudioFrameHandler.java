package zm.packets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Copies audio frames "00wb"-etc.
 * @author zm
 *
 */
public class AudioFrameHandler implements Handler {

	private final Integer channel;
	private final byte[] chunkSizeBytes = new byte[4];
	private final ByteBuffer chunkSizeBB = ByteBuffer.wrap(chunkSizeBytes).order(ByteOrder.LITTLE_ENDIAN);

	public AudioFrameHandler(Integer channel) {
		assert channel == null || (channel >= 0 && channel < 10);
		this.channel = channel;
	}

	@Override
	public void handle(InputStream in, OutputStream out) throws IOException, InterruptedException {
		// An audio frame has a length and a timestamp, but this doesn't handle channels
		// yet so just discard everything.

		StreamScanner s = new StreamScanner(in);
		int length = s.nextInt32();
		long timestamp = s.nextInt64();

//		// discard the rest
//		s.skip(length);

		if(channel != null) {
			out.write(String.format("%1d1wb", channel).getBytes(Charset.forName("UTF-8")));
			chunkSizeBB.rewind();
			chunkSizeBB.putInt(length);
			out.write(chunkSizeBytes);
		}

		s.copy(out, length);
	}
}

// from BufferManage.h
//
//typedef struct
//{
//	unsigned int		m_nAHeaderFlag; // frame id 00dc, 01dc, 01wb
//	unsigned int 		m_nAFrameLen;  // frame length
//	long long			m_lAPts;		// Timestamp
//}AudioFrameHeader;