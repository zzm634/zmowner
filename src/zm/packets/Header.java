package zm.packets;

public interface Header {

	default String getName() {
		byte identifier[] = this.getIdentifier();
		StringBuilder sb = new StringBuilder(identifier.length * 2);
		for(byte b : identifier) {
			sb.append(String.format("%02X", b));
		}
		return sb.toString();
	}

	/**
	 * Returns the unique identifying bytes for a header. All headers of the same
	 * type should have the same identifier length.
	 *
	 * @return
	 */
	byte[] getIdentifier();

	/**
	 * Returns the length of the metadata of this header (minus the identifier), if
	 * the header always has a fixed length. Otherwise null.
	 */
	Integer getMetadataLength();

}
