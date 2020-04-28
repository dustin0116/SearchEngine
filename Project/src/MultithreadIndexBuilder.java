import java.io.IOException;
import java.nio.file.Path;

import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Multithreading version for IndexBuilder.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @author Yen Dah Hsiang
 * @version Fall 2019
 */
public class MultithreadIndexBuilder extends IndexBuilder {
	/** The default stemmer algorithm. */
	public static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;

	/** The inverted index. */
	private final ThreadSafeInvertedIndex index;

	/** The work queue. */
	private final WorkQueue queue;

	/**
	 * Constructor for the multithreading inverted index builder.
	 *
	 * @param index the inverted index
	 * @param queue the work queue
	 */
	public MultithreadIndexBuilder(ThreadSafeInvertedIndex index, WorkQueue queue) {

		super(index);
		this.index = index;
		this.queue = queue;
	}

	@Override
	public void create(Path path) throws IOException {

		super.create(path);
		queue.finish();
	}

	@Override
	public void addToIndex(Path location) throws IOException {
		queue.execute(new Task(location));
	}

	/**
	 * Task for parsing each file.
	 *
	 */
	private class Task implements Runnable {

		/** The location to */
		private final Path location;

		/**
		 * Constructor for the task.
		 *
		 * @param location the text file
		 *
		 */
		private Task(Path location) {
			this.location = location;
		}

		@Override
		public void run() {

			try {

				ThreadSafeInvertedIndex local = new ThreadSafeInvertedIndex();

				addToIndex(this.location, local);
				index.addAll(local);

			} catch (IOException e) {

				System.out.println("Unable to build index with multithreading.");
			}
		}
	}
}
