package zm.packets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class Processor<HEADER extends Header> implements Handler {

	private final byte headerBuf[];

	protected abstract HEADER decodeHeader(byte header[]);

	protected abstract int getHeaderLength();

	public Processor(Collection<? extends HEADER> headers) {
		assert !headers.isEmpty();

		this.headerBuf = new byte[this.getHeaderLength()];

		// optimize this later to use enum map.
		this.handlers = new HashMap<>();

		for(HEADER h : headers) {
			Handler handler = h.getDefaultHandler();
			if(handler != null) {
				registerHandler(h, handler);
			}
		}
	}

	@Override
	public void handle(InputStream in, OutputStream out) throws IOException, InterruptedException {
		final StreamScanner s = new StreamScanner(in);

		while (true) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}

			// Read the header
			s.next(headerBuf);
			HEADER header = decodeHeader(headerBuf);
			if (header == null) {
				throw new UnknownHeaderException(headerBuf);
			}

			// find an appropriate handler
			final Handler handler;
			synchronized (handlers) {
				handler = handlers.get(header);
				if (handler == null) {
					throw new UnhandledHeaderException(header);
				}
			}

			// handle the packet data
			handler.handle(in, out);
		}

	}

	public void registerHandler(HEADER header, Handler handler) {
		handlers.put(header, handler);
	}

	private final Map<HEADER, Handler> handlers;
}
