package zm.packets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class DiscardHandler implements Handler {
	private final int discardBytes;

	public DiscardHandler(int bytesToDiscard) {
		assert bytesToDiscard >= 0;

		this.discardBytes = bytesToDiscard;
	}

	@Override
	public void handle(InputStream in, OutputStream out) throws IOException, InterruptedException {
		StreamUtils.blockingDiscardExact(in, discardBytes);
	}
}