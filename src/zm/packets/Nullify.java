package zm.packets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Nullifies the output of another handler (prevents it from writing to the output stream).
 *
 * @author zm
 */
public class Nullify implements Handler {

	public Nullify(Handler wrapped) {
		this.wrapped = wrapped;
	}

	private static final OutputStream NULL = new OutputStream() {
		@Override
		public void write(int b) throws IOException {
			// do nothing
		}
	};

	private final Handler wrapped;

	@Override
	public void handle(InputStream in, OutputStream out) throws IOException, InterruptedException {
		wrapped.handle(in, NULL);
	}

}
