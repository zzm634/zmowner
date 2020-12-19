package zm.packets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AudioFrameHandler implements Handler {

	public AudioFrameHandler(int channel) {
		// do nothing
	}

	@Override
	public void handle(InputStream in, OutputStream out) throws IOException, InterruptedException {
		// An audio frame has a length and a timestamp, but this doesn't handle channels
		// yet so just discard everything.

		StreamScanner s = new StreamScanner(in);
		int length = s.nextInt32();
		long timestamp = s.nextInt64();

		// discard the rest
		s.skip(length);
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