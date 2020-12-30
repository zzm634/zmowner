package zm.packets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class CommandHandler implements Handler {

	protected static class CommandHeader {

		public CommandHeader(byte headerData[]) {
			ByteBuffer bb = ByteBuffer.wrap(headerData).order(ByteOrder.LITTLE_ENDIAN);

			// may be 2 bytes or 4, but all captured packets so far have had zeroes at those
			// two extra bytes, and all integers so far have been little endian.
			this.payloadLength = Short.toUnsignedInt(bb.getShort());
			
			// unknown 4 bytes
			bb.getInt();
			
			// 2 byte command identifier
			bb.get(this.identifier);
		}

		public byte[] getIdentifier() {
			return identifier;
		}

		/**
		 * @return >= 0
		 */
		public int getPayloadLength() {
			return payloadLength;
		}

		public CommandIdentifier getIdentifierEnum() {
			return CommandIdentifier.get(getIdentifier());
		}

		private final int payloadLength;
		private final byte identifier[] = new byte[2];
	}

	@Override
	public void handle(byte headerBuf[], InputStream in, OutputStream out) throws IOException, InterruptedException {
		this.handle(new CommandHeader(headerBuf), in, out);
	}

	public void handle(InputStream in, OutputStream out) {
		throw new UnsupportedOperationException("must pass command header to command handler, unless overridden");
	}

	protected abstract void handle(CommandHeader header, InputStream in, OutputStream out)
			throws IOException, InterruptedException;

}
