package org.flamering.app;

import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.SelectableChannel;

// TODO: Auto-generated Javadoc
/**
 * The Class ConsoleInputPipe.
 */
public class ConsoleInputPipe {
	
	// ---------------------------------------------------

	/**
	 * The Class CopyThread.
	 */
	public static class CopyThread extends Thread {

		/** The flag of keeping running. */
		boolean keepRunning = true;
		
		/** The bytes for the buffer. */
		byte[] bytes = new byte[128];
		
		/** The buffer. */
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		
		/** The input stream. */
		InputStream in;
		
		/** The output channel. */
		WritableByteChannel out;

		/**
		 * Instantiates a new copy thread.
		 *
		 * @param in the input stream
		 * @param out the output channel
		 */
		CopyThread(InputStream in, WritableByteChannel out) {
			this.in = in;
			this.out = out;
			this.setDaemon(true);
		}

		/**
		 * Stop running.
		 */
		public void stopRunning() {
			keepRunning = false;
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
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

	/** The pipe. */
	Pipe pipe;
	
	/** The copy thread. */
	CopyThread copyThread;

	/**
	 * Instantiates a new console input pipe.
	 *
	 * @param in the input stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ConsoleInputPipe(InputStream in) throws IOException {
		pipe = Pipe.open();
		copyThread = new CopyThread(in, pipe.sink());
	}

	/**
	 * Instantiates a new console input pipe.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ConsoleInputPipe() throws IOException {
		this(System.in);
	}

	/**
	 * Start to work
	 */
	public void start() {
		copyThread.start();
	}

	/**
	 * Stop working
	 */
	public void shutdown() {
		copyThread.stopRunning();
		try {
			Thread.sleep(50);
			copyThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the standard input channel.
	 *
	 * @return the standard input channel
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public SelectableChannel getStdinChannel() throws IOException {
		SelectableChannel channel = pipe.source();
		channel.configureBlocking(false);
		return (channel);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() {
		shutdown();
	}
	
}
