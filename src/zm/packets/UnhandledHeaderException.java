package zm.packets;

public class UnhandledHeaderException extends RuntimeException {
	public UnhandledHeaderException(Header header) {
		super(String.format("Encountered header %s but no PacketHandler was registered to "
				+ "handle it.", header.getName()));
	}
}
