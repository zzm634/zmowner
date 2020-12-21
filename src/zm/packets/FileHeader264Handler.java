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
		// total of 512 bytes before data starts.
		// header has 4 bytes, then 12 bytes of nothing, then the key, then more zeroes.

		// note that header already gets skipped
		StreamScanner s = new StreamScanner(in);

		s.skip(12);

		byte aesKey[] = new byte[32];
		s.next(aesKey);
		parent.setAesKey(aesKey);

		s.skip(512-(4+12+32));
	}

}
