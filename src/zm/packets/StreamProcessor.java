package zm.packets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumMap;
import java.util.Map;

public class StreamProcessor {

	public StreamProcessor() {
		// init discard handlers for any headers of known size
		for (PacketHeader h : PacketHeader.values()) {
			if (h.headerLength != null) {
				packetHandlers.putIfAbsent(h, new DiscardHandler(h));
			}
		}
	}

	public void registerHandler(PacketHeader header, PacketHandler handler) {
		assert header != null;
		synchronized (packetHandlers) {
			this.packetHandlers.put(header, handler);
		}
	}

	public void process(InputStream in, OutputStream out)
			throws InterruptedException, UnknownHeaderException, UnhandledHeaderException, IOException {

		final StreamScanner s = new StreamScanner(in);

		final byte[] headerBuf = new byte[4];
		while (true) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}

			// Read the header
			s.next(headerBuf);
			PacketHeader header = PacketHeader.get(headerBuf);
			if (header == null) {
				throw new UnknownHeaderException(headerBuf);
			}

			// find an appropriate handler
			final PacketHandler handler;
			synchronized (packetHandlers) {
				handler = packetHandlers.get(header);
				if (handler == null) {
					throw new UnhandledHeaderException(header);
				}
			}

			// handle the packet data
			handler.handle(in, out);
		}
	}

	private static class DiscardHandler implements PacketHandler {
		private final int discardBytes;

		public DiscardHandler(PacketHeader h) {
			assert h.headerLength != null;

			this.discardBytes = h.headerLength;
		}

		public DiscardHandler(int bytesToDiscard) {
			assert bytesToDiscard >= 0;

			this.discardBytes = bytesToDiscard;
		}

		@Override
		public void handle(InputStream in, OutputStream out) throws IOException, InterruptedException {
			StreamUtils.blockingDiscardExact(in, discardBytes);
		}
	}

	private final Map<PacketHeader, PacketHandler> packetHandlers = new EnumMap<>(PacketHeader.class);
}
