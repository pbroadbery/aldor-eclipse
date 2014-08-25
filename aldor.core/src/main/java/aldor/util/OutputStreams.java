package aldor.util;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreams {

	public static void forceClose(OutputStream outputStream) {
		try {
			// Close the input of the Process explicitly.
			// We will never write to it.
			outputStream.close();
		} catch (IOException e) {
		}
	}

	public static void writeSafely(OutputStream outputStream,
			String commandString) {

		try {
			outputStream.write(commandString.getBytes());
		}
		catch (IOException e) {
			throw new RuntimeException("Failed to write to outputStream");
		}
	
	}

}
