package zm.packets;

/**
 * Identifier describes a byte signature present in a packet yeader
 *
 * @author zm
 *
 */
public interface Identifier {

	default String getName() {
		if(this.getClass().isEnum()) {
			return ((Enum)this).name();
		}

		return StreamUtils.byteToHex(this.getBytes());
	}

	/**
	 * Returns the unique byte signature for this identifier. All identifiers of the
	 * same type should have the same signature length.
	 *
	 * @return
	 */
	byte[] getBytes();

	/**
	 * Returns the default Handler for dealing with packets identified by this
	 * Identitifer, or null if no default exists.
	 */
	default Handler getDefaultHandler() {
		return null;
	}

}
