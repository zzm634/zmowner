package zm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import zm.packets.PacketProcessor;
import zm.packets.Processor;

/**
 * Command line entry point.
 *
 * Args:
 * -f,--file: read from input file
 * -h,--host: IP address or hostname of camera
 * -p,--port: override default port (8000)
 * -k,--key: provide AES key on command line
 * -x,--key-hex: provide aes key in hexadecimal
 * -a,--audio: enable audio stream (implies -m)
 * -m,--multi-stream: output multiple video or audio streams
 *
 * @author zm
 *
 */
public class Cli {

	public static void main(String[] args) throws IOException, InterruptedException {

		// Determine input stream
		InputStream in = System.in;

		// Determine output stream
		OutputStream out = System.out;

		// Find AES key
		String key = null;

		// If using direct connection:
		// 1) issue get key command if key was not provided on input
		// 2) issue start video command
		// 3) issue start audio command

		PacketProcessor p = PacketProcessor.getDefaultProcessor(key, true);

		p.handle(in, out);
	}

}
