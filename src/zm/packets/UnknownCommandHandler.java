package zm.packets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Since command packets have a known header format which includes the size, we can safely discard bytes from the stream.
 * 
 * @author zm
 */
public class UnknownCommandHandler extends CommandHandler {

	@Override
	protected void handle(CommandHeader header, InputStream in, OutputStream out)
			throws IOException, InterruptedException {
		StreamUtils.blockingDiscardExact(in, header.getPayloadLength());
	}

}
