package org.flamering.app;

import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.SelectableChannel;

public class ConsoleInputPipe {
	
	// ---------------------------------------------------

	public static class CopyThread extends Thread {

		boolean keepRunning = true;
		byte[] bytes = new byte[128];
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		InputStream in;
		WritableByteChannel out;

		CopyThread(InputStream in, WritableByteChannel out) {
			this.in = in;
			this.out = out;
			this.setDaemon(true);
		}

		public void stopRunning() {
			keepRunning = false;
		}

		public void run() {
			// this could be improved
			try {
				while (keepRunning) {
					int count = in.read(bytes);
					if (count < 0) {
						sleep(50);
						break;
					}
					buffer.clear().limit(count);
					out.write(buffer);
					sleep(50);
				}
				out.close();
				sleep(50);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	// --------------------------------------------------------

	Pipe pipe;
	CopyThread copyThread;

	public ConsoleInputPipe(InputStream in) throws IOException {
		pipe = Pipe.open();
		copyThread = new CopyThread(in, pipe.sink());
	}

	public ConsoleInputPipe() throws IOException {
		this(System.in);
	}

	public void start() {
		copyThread.start();
	}

	public void shutdown() {
		copyThread.stopRunning();
		try {
			Thread.sleep(50);
			copyThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public SelectableChannel getStdinChannel() throws IOException {
		SelectableChannel channel = pipe.source();
		channel.configureBlocking(false);
		return (channel);
	}

	protected void finalize() {
		shutdown();
	}
	
}
