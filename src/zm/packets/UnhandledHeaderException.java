package zm.packets;

public class UnhandledHeaderException extends RuntimeException {
	public UnhandledHeaderException(Identifier header) {
		super(String.format("Encountered header %s but no PacketHandler was registered to "
				+ "handle it.", header.getName()));
	}
}
