package zm;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.input.TeeInputStream;

import zm.packets.PacketProcessor;
import zm.packets.StreamUtils;

/**
 * Command line entry point.
 *
 * Args: -f,--file: read from input file -h,--host: IP address or hostname of
 * camera -p,--port: override default port (8000) -k,--key: provide AES key on
 * command line -x,--key-hex: provide aes key in hexadecimal -a,--audio: enable
 * audio stream (implies -m) -m,--mux: output multiple video or audio streams
 *
 * @author zm
 *
 */
public class Cli {

	public static void main(String[] args) throws IOException, InterruptedException {

		Options options = new Options();
		Option oFile = Option.builder("f")
				.longOpt("file")
				.hasArg()
				.argName("path")
				.desc(".264 file to read data from")
				.numberOfArgs(1)
				.build();

		Option oHost = Option.builder("h")
				.longOpt("host")
				.argName("hostname")
				.desc("host name or IP address of camera to connect to")
				.numberOfArgs(1)
				.hasArg()
				.build();

		OptionGroup ogInputSource = new OptionGroup();
		ogInputSource.addOption(oFile);
		ogInputSource.addOption(oHost);

		options.addOptionGroup(ogInputSource);

		Option oPort = Option.builder("p")
				.longOpt("port")
				.argName("port")
				.desc("port on camera to connect to (default: 8000)")
				.numberOfArgs(1)
				.hasArg()
				.build();
		options.addOption(oPort);

		Option oKey = Option.builder("k")
				.longOpt("key")
				.argName("aesKeyAscii")
				.numberOfArgs(1)
				.hasArg()
				.desc("ASCII values of AES key used to decrypt P-frames. Required when processing stream data from stdin.")
				.build();

		Option oKeyHex = Option.builder("x")
				.longOpt("key-hex")
				.argName("aesKeyHex")
				.numberOfArgs(1)
				.hasArg()
				.desc("Hexadecimal values of AES key used to decrypt P-frames. Required when processing stream data from stdin.")
				.build();

		OptionGroup ogKeySpec = new OptionGroup();
		ogKeySpec.addOption(oKey);
		ogKeySpec.addOption(oKeyHex);

		options.addOptionGroup(ogKeySpec);

// audio output might need some decrypting, I think
//		Option oMultiStream = Option.builder("m")
//				.longOpt("mux")
//				.desc("enable multiple stream output muxing using AVI chunk headers")
//				.build();
//		options.addOption(oMultiStream);
//
//		Option oAudio = Option.builder("a")
//				.longOpt("audio")
//				.desc("enable audio output (implies --mux)")
//				.build();
//		options.addOption(oAudio);

		Option oHelp = new Option("help", "prints usage info");
		options.addOption(oHelp);

		CommandLineParser parser = new DefaultParser();

		CommandLine line = null;
		try {
			line = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
		}

		if(line == null || line.hasOption("help")) {
			HelpFormatter hf = new HelpFormatter();

			// TODO
			return;
		}

		boolean audio = line.hasOption("audio");
		boolean mux = audio || line.hasOption("mux");

		// check for AES key
		byte key[];
		if(line.hasOption("key")) {
			key = line.getOptionValue("key").getBytes(Charset.forName("UTF-8"));
		} else if(line.hasOption("key-hex")) {
			key = StreamUtils.parseHex(line.getOptionValue("key-hex"));
		} else {
			key = null;
		}

		InputStream in;
		Socket socket = null;
		OutputStream socketOut = null;

		// Determine input stream
		if(line.hasOption("file")) {
			in = new FileInputStream(line.getOptionValue("file"));
		} else if(line.hasOption("host")) {
			int port = Integer.parseInt(line.getOptionValue("port","8000"));

			socket = new Socket(line.getOptionValue("host"),port);
			in = socket.getInputStream();

			socketOut = socket.getOutputStream();
			if(key == null) {
				socketOut.write(GET_KEY_COMMAND);
			}

			socketOut.write(START_VIDEO_720P_COMMAND);
			if(audio) {
				socketOut.write(START_AUDIO_COMMAND);
			}

			socketOut.flush();
		} else {
			in = System.in;
		}

		// for debuggin'
//		in = new TeeInputStream(in, new HexOutputStreamWrapper(System.err));

		// Determine output stream (right now, just stdout)
		OutputStream out = System.out;

		PacketProcessor p = PacketProcessor.getDefaultProcessor(!mux);
		if(key != null) {
			p.setAesKey(key);
		}

		try {
			p.process(in, out);
		} finally {
			if(socket != null) {
				socket.close();
			}
		}
	}

	private static final byte[] GET_KEY_COMMAND = StreamUtils.parseHex("5555aaaa0000000000003696");
	private static final byte[] START_VIDEO_720P_COMMAND = StreamUtils.parseHex("5555aaaa0000000000000050");
	private static final byte[] START_AUDIO_COMMAND = StreamUtils.parseHex("5555aaaa040000000000669000100000");

}
