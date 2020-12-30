package zm.packets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Optional;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class VideoFrameHandler implements Handler {

	private final Optional<PacketProcessor> parent;

	/**
	 * No decryption. Used for I-frames
	 */
	public VideoFrameHandler(Integer channel) {
		assert channel == null || (channel >= 0 && channel < 10);
		this.parent = Optional.empty();
		this.assumeEncrypted = false;
		this.decryptor = null;
		this.channel = channel;
	}

	/**
	 * Assumes frames are encrypted, Used for P-frames.
	 */
	public VideoFrameHandler(Integer channel, PacketProcessor parent) {
		assert channel == null || (channel >= 0 && channel < 10);
		this.parent = Optional.ofNullable(parent);
		this.assumeEncrypted = true;
		try {
			this.decryptor = Cipher.getInstance("AES/CBC/NOPADDING");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new AssertionError(e);
		}
		this.channel = channel;
	}

	// Video channel to output, if containerized. Null if raw stream output
	private final Integer channel;

	private final boolean assumeEncrypted;
	private final Cipher decryptor;

	// frame encryption always uses 0 IVs
	private final byte[] ivBytes = new byte[16];
	private static final int ENCRYPTED_BYTES = 256;
	private final byte[] encryptedDataBuffer = new byte[ENCRYPTED_BYTES];

	private final byte[] chunkSizeBytes = new byte[4];
	private final ByteBuffer chunkSizeBB = ByteBuffer.wrap(chunkSizeBytes).order(ByteOrder.LITTLE_ENDIAN);

	private IvParameterSpec getIVs() {
		Arrays.fill(ivBytes, (byte) 0);
		return new IvParameterSpec(ivBytes);
	}
	
	@Override
	public void handle(InputStream in, OutputStream out) throws IOException, InterruptedException {
		StreamScanner s = new StreamScanner(in);
		s.setByteIndex(4); // offset to include the header identifier

		// Parse header
		final int frameLength = s.nextInt32();
		final byte hour = s.nextByte();
		final byte minute = s.nextByte();
		final byte second = s.nextByte();
		final byte uPad0 = s.nextByte(); // ?
		final int iframeOffset = s.nextInt32(); // ?
		final long timestamp = s.nextInt64();
		final int flags = s.nextInt32();

		final boolean alarm = ((flags >> 0) & 0b1) != 0;
		final int frameType = ((flags >> 1) & 0b1111);
		final boolean lost = ((flags >> 5) & 0b1) != 0;
		final int frameRate = ((flags >> 6) & 0b11111);

		final int nReserved = s.nextInt32();

		// I'm fairly certain one of these header bytes indicates whether the frame is
		// encrypted, but I don't know which one.
		final boolean encrypted = false;

		SecretKeySpec key = parent.map(PacketProcessor::getAesKey).orElse(null);

		if ((encrypted || assumeEncrypted) && key == null) {
			System.err.println("encountered encrypted frame but no AES key provided. skipping...");
			out = StreamUtils.NULL;
		}

		// Handle content

		// 1) output channel
		if (channel != null) {
			out.write(String.format("%d0dc", channel).getBytes(Charset.forName("UTF-8")));
			chunkSizeBB.rewind();
			chunkSizeBB.putInt(frameLength);

			out.write(chunkSizeBytes);

		}

		// 2) copy frame content
		int chunkSize = frameLength;

		if ((encrypted || assumeEncrypted) && key != null) {
			// decrypt first ENCRYPTED_BYTES bytes only.

			try {
				decryptor.init(Cipher.DECRYPT_MODE, key, getIVs());
			} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
				throw new AssertionError(e);
			}

			// read encrypted data
			s.next(encryptedDataBuffer);

			// decrypt and write data
			try {
				out.write(decryptor.doFinal(encryptedDataBuffer));
			} catch (IllegalBlockSizeException | BadPaddingException e) {
				throw new AssertionError(e);
			}

			chunkSize -= encryptedDataBuffer.length;
		}

		// copy the remaining data exactly
		s.copy(out, chunkSize);
	}

// from BufferManage.h:
//
//	typedef struct
//	{
//		unsigned int		m_nVHeaderFlag; // frame id 00dc, 01dc, 01wb
//		unsigned int 		m_nVFrameLen;  // frame length
//		unsigned char		m_u8Hour;
//		unsigned char		m_u8Minute;
//		unsigned char		m_u8Sec;
//		unsigned char		m_u8Pad0;// 	Represents the type of additional message, and determines its information structure according to this type. 0 represents no 1.2.3 each represents its information
//		unsigned int		m_nILastOffset;// The offset of this frame relative to the previous I FRAME is only useful for Iframe
//		long long			m_lVPts;		//Timestamp
//		unsigned int		m_bMdAlarm:1;/*bit0 Motion detection alarm 1: alarm, 0: no alarm*/
//		unsigned int		m_FrameType:4;
//		unsigned int 		m_Lost:1;
//		unsigned int 		m_FrameRate:5;
//		unsigned int		m_Res:21;	/*bit11-bit31 Temporarily reserved*/
//		unsigned int		m_nReserved;
//	}VideoFrameHeader;

// from ModuleFuncInterface.cpp
//
//int AesEncrypt(unsigned char *input, unsigned char *output)
//{
//	mbedtls_aes_context ctx;
//	int ret;
//	unsigned char key_str[100]={0};
//	unsigned char iv_str[100]={0};
//	strcpy((char*)key_str,(const char*)GetAesKey());
//
//	mbedtls_aes_init( &ctx );
//
//	mbedtls_aes_setkey_enc( &ctx, key_str, 256 );
//	ret = mbedtls_aes_crypt_cbc(&ctx, MBEDTLS_AES_ENCRYPT, 256, iv_str, input, output);
//	//printf("enc ret %d\n",ret);
//	mbedtls_aes_free( &ctx );
//	return 0;
//}

}
