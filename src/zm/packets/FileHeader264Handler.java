package zm.packets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileHeader264Handler implements Handler {

	private final PacketProcessor parent;

	public FileHeader264Handler(PacketProcessor parent) {
		super();
		this.parent = parent;
	}

	@Override
	public void handle(InputStream in, OutputStream out) throws IOException, InterruptedException {
		// total of 512 bytes before video data starts.
		// header has 4 bytes, (xV4something)

		// note that header already gets skipped
		StreamScanner s = new StreamScanner(in);
		s.setByteIndex(4);

		// aes key starts at 0x70
		s.skipTo(0x70);

		byte aesKey[] = new byte[32];
		s.next(aesKey);
		parent.setAesKey(aesKey);
		
		// data starts at 0x200
		s.skipTo(0x200);
	}

}
