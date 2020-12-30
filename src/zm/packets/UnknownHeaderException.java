package zm.packets;

public class UnknownHeaderException extends RuntimeException {
	public UnknownHeaderException(byte header[]) {
		super(String.format("Encountered unknown packet header: %02X %02X %02X %02X",
				(int)(header[0]),
				(int)(header[1]),
				(int)(header[2]),
				(int)(header[3])));
	}
}
