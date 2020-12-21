package zmodo_stream_fixer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import zm.packets.StreamUtils;

public class StreamFixer {

	private static final int BUFFER_SIZE = 4096;

	private static final int SIZE_OFFSET = 24;

	private static final byte[] OODC_HEADER = { 0x30, 0x30, 0x64, 0x63 };
	private static final byte[] o1dc_header = { 0x30, 0x31, 0x64, 0x63 };

	private static final String KEY = "D52FF00F33CA48D98AB5C3D038C5A2EA";

	private static final int ENCRYPTED_BYTES = 256;

	private static final boolean PRINT_HEX = false;

	public static void main(String[] args)
			throws IOException, InterruptedException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

		final String key;
		if(args.length == 1) {
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

		try {
			byte header[] = new byte[4];
			byte size[] = new byte[4];
			ByteBuffer sizebb = ByteBuffer.wrap(size);
			sizebb.order(ByteOrder.LITTLE_ENDIAN);
			byte buffer[] = new byte[BUFFER_SIZE];
			Cipher cipher = Cipher.getInstance("AES/CBC/NOPADDING");

			while (!Thread.interrupted()) {
				// read the header

				StreamUtils.blockingReadExact(in, header, 0, 4);

				boolean encrypted = Arrays.equals(header, OODC_HEADER);

				// read the size
				StreamUtils.blockingReadExact(in, size, 0, 4);

				sizebb.rewind();
				int chunkSize = sizebb.getInt();

				// skip the rest of the header and get to the data

				// read extra data and throw it away
				StreamUtils.blockingDiscardExact(in, SIZE_OFFSET);

				// copy the chunk data

				if (encrypted) {
					// first 256 bytes are possibly encoded with aes-cbc

					// decrypt 256 bytes, then continue with the rest
					byte iv[] = new byte[16];
					Arrays.fill(iv, (byte) 0);

					IvParameterSpec ivS = new IvParameterSpec(iv);
					SecretKeySpec skeySpec = new SecretKeySpec(KEY.getBytes(), "AES");

					cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivS);

					byte encryptedData[] = new byte[ENCRYPTED_BYTES];
					StreamUtils.blockingReadExact(in, encryptedData, 0, ENCRYPTED_BYTES);

					byte decryptedData[] = cipher.doFinal(encryptedData);

					if (PRINT_HEX) {
						System.out.print(byteToHex(decryptedData));
					} else {
						System.out.write(decryptedData, 0, ENCRYPTED_BYTES);
					}
					chunkSize -= ENCRYPTED_BYTES;
				}

				while (chunkSize > 0) {
					int toRead = Math.min(BUFFER_SIZE, chunkSize);

					StreamUtils.blockingReadExact(in, buffer, 0, toRead);
					if (PRINT_HEX) {
						System.out.print(StreamUtils.byteToHex(buffer, 0, toRead));
					} else {
						System.out.write(buffer, 0, toRead);
					}
					chunkSize -= toRead;
				}

			}

		} finally {
			in.close();
		}

	}

	private static String byteToHex(byte[] data) {
		return StreamUtils.byteToHex(data, 0, data.length);
	}

}
