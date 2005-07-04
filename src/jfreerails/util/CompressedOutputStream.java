package jfreerails.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;

/**
 * A FilterOutputStream for sending compressed data over a network connection.
 * Note that standard ZipOutputStream and java.util.zip.GZipOutputStream don't
 * guarantee that flush sends out all the data written so far, which leads to
 * deadlocks in request-response-based protocols.
 * 
 * @author Patrice Espie Licensing: LGPL
 */
public class CompressedOutputStream extends FilterOutputStream {
	public CompressedOutputStream(OutputStream out) {
		super(out);
		buffer = new byte[0x7d000];
		compBuffer = new byte[(int) (buffer.length * 1.2D)];
		writeIndex = 0;
		deflater = new Deflater(9);
	}

	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		int written = 0;

		do {
			if (written >= len) {
				break;
			}

			int toWrite = Math.min(len - written, buffer.length - writeIndex);
			System.arraycopy(b, off + written, buffer, writeIndex, toWrite);
			written += toWrite;
			writeIndex += toWrite;

			if (writeIndex >= buffer.length * 0.80000000000000004D) {
				flush();
			}
		} while (true);
	}

	public void write(int b) throws IOException {
		if (writeIndex >= buffer.length * 0.80000000000000004D) {
			flush();
		}

		buffer[writeIndex++] = (byte) b;
	}

	public void flush() throws IOException {
		int compSize = 0;
		boolean sendCompressed;

		if (writeIndex > 150) {
			deflater.reset();
			deflater.setInput(buffer, 0, writeIndex);
			deflater.finish();

			if (compBuffer.length < writeIndex * 2 + 40960) {
				compBuffer = new byte[writeIndex * 2 + 40960];
			}

			compSize = deflater.deflate(compBuffer);

			if (compSize <= 0) {
				throw new IOException("Compression exception");
			}

			sendCompressed = compSize < writeIndex;
		} else {
			sendCompressed = false;
		}

		if (sendCompressed) {
			super.out.write(1);
			super.out.write(writeIndex >> 24 & 0xff);
			super.out.write(writeIndex >> 16 & 0xff);
			super.out.write(writeIndex >> 8 & 0xff);
			super.out.write(writeIndex & 0xff);
			super.out.write(compSize >> 24 & 0xff);
			super.out.write(compSize >> 16 & 0xff);
			super.out.write(compSize >> 8 & 0xff);
			super.out.write(compSize & 0xff);
			super.out.write(compBuffer, 0, compSize);
			super.out.flush();
			writeIndex = 0;
		} else if (writeIndex > 0) {
			super.out.write(0);
			super.out.write(writeIndex >> 24 & 0xff);
			super.out.write(writeIndex >> 16 & 0xff);
			super.out.write(writeIndex >> 8 & 0xff);
			super.out.write(writeIndex & 0xff);
			super.out.write(buffer, 0, writeIndex);
			super.out.flush();
			writeIndex = 0;
		}
	}

	private byte[] buffer;

	private byte[] compBuffer;

	private int writeIndex;

	private Deflater deflater;
}