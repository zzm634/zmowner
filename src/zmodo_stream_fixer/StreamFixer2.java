package zmodo_stream_fixer;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import zm.packets.PacketHeader;
import zm.packets.StreamProcessor;
import zm.packets.UnhandledHeaderException;
import zm.packets.UnknownHeaderException;
import zm.packets.VideoFrameHandler;

// Exactly as implemented with StreamFixer, but using packet library
public class StreamFixer2 {

	private static final String KEY = "D52FF00F33CA48D98AB5C3D038C5A2EA";

	public static void main(String[] args)
			throws InterruptedException, UnknownHeaderException, UnhandledHeaderException, IOException {

		final String key;
		if (args.length == 1) {
			key = args[0];
		} else {
			key = KEY;
		}

		final InputStream in;

		if (args.length == 1) {
			String filename = args[0];
			in = new FileInputStream(filename);
		} else if (args.length > 1) {
			return;
		} else {
			in = System.in;
		}

		StreamProcessor processor = new StreamProcessor();
		processor.registerHandler(PacketHeader.PFRAME_0, new VideoFrameHandler(key));
		processor.registerHandler(PacketHeader.IFRAME_0, new VideoFrameHandler());

		try {
			processor.process(in, System.out);
		} catch (EOFException e) {
			if (in.read() == -1) {
				// no problem
				return;
			} else {
				throw e;
			}
		}
	}

}
