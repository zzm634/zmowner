package zm.packets;

public class UnhandledHeaderException extends Exception {
	public UnhandledHeaderException(PacketHeader header) {
		super(String.format("Encountered header %s but no PacketHandler was registered to "
				+ "handle it.", header.name()));
	}
}
