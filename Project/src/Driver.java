import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author CS 212 Software Development
 * @author Yen Dah Hsiang
 * @author University of San Francisco
 * @version Fall 2019
 */
public class Driver {

	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) {
		// store initial start time
		Instant start = Instant.now();
		ArgumentParser inputArgs = new ArgumentParser(args);

		// Declare objects
		InvertedIndex index;
		ThreadSafeInvertedIndex threadSafe = null;
		IndexBuilder indexBuilder;
		QueryParserInterface queryParser;
		WorkQueue queue = null;
		WebCrawler webCrawler = null;

		int threads = 5; // Default number of threads
		URL seed = null; // Default URL

		if (inputArgs.hasFlag("-threads") || inputArgs.hasFlag("-url") || inputArgs.hasFlag("-port")) {
			// Multithreading
			try {

				if (inputArgs.hasValue("-threads") && Integer.parseInt(inputArgs.getString("-threads")) > 0) {
					// Valid number of threads given
					threads = Integer.parseInt(inputArgs.getString("-threads"));
				}

			} catch (Exception e) {

				System.out.println("Threads default to 5, invalid number of threads given.");
			}
			// Initialize multithreading objects
			queue = new WorkQueue(threads);
			threadSafe = new ThreadSafeInvertedIndex();
			index = threadSafe;
			indexBuilder = new MultithreadIndexBuilder(threadSafe, queue);
			queryParser = new MultithreadQueryParser(threadSafe, queue);
			webCrawler = new WebCrawler(threadSafe, queue);

		} else {
			// Initialize single threading objects
			index = new InvertedIndex();
			indexBuilder = new IndexBuilder(index);
			queryParser = new QueryParser(index);
		}

		if (inputArgs.hasFlag("-url")) {

			int limit = 0;

			try {

				seed = new URL(inputArgs.getString("-url"));
				limit = Integer.parseInt(inputArgs.getString("-limit", "50"));

			} catch (Exception e) {

				System.out.println("Cannot web crawl.");
			}

			webCrawler.build(seed, limit);
		}

		if (inputArgs.hasFlag("-port")) {

			try {

				int PORT = Integer.parseInt(inputArgs.getString("-port", "8080"));
				SearchServer.start(threadSafe, PORT, threads);

			} catch (Exception e) {

				System.out.println("Cannot build search engine web interface");
			}
		}

		if (inputArgs.hasFlag("-path")) {
			// Building index from path
			if (inputArgs.hasValue("-path")) {

				Path path = inputArgs.getPath("-path");

				try {

					indexBuilder.create(path);

				} catch (IOException e) {

					System.out.println("Unable to build the inverted index from path: " + path);
				}

			} else {

				System.out.println("Warning: No value provided with the -path flag.");
			}
		}

		if (inputArgs.hasFlag("-counts")) {

			Path path = inputArgs.getPath("-counts", Path.of("counts.json"));

			try {

				if (inputArgs.hasValue("-counts")) {
					// Output path is provided
					index.wordCountsToJson(path);

				} else {
					// Default output path
					index.wordCountsToJson(path);
				}

			} catch (IOException e) {

				System.out.println("Unable to output word count from path: " + path);
			}
		}

		if (inputArgs.hasFlag("-index")) {
			// Output index
			Path path = inputArgs.getPath("-index", Path.of("index.json"));

			try {

				index.indexToJson(path);

			} catch (IOException e) {

				System.out.println("Unable to output inverted index from path: " + path);
			}
		}

		if (inputArgs.hasFlag("-query")) {
			// Search inverted index
			if (inputArgs.hasValue("-query")) {

				Path path = inputArgs.getPath("-query");

				try {
					queryParser.parseFile(path, inputArgs.hasFlag("-exact"));

				} catch (IOException e) {

					System.out.println("Unable to search from path: " + path);
				}
			}
		}

		if (inputArgs.hasFlag("-results")) {
			// Output search results
			Path path = inputArgs.getPath("-results", Path.of("results.json"));

			try {

				queryParser.resultsToJson(path);

			} catch (IOException e) {

				if (inputArgs.hasValue("-results")) {

					System.out.println("Unable to output results to: " + path);

				} else {

					System.out.println("Unable to output results to " + path);
				}
			}
		}

		if (queue != null) {
			// Shuts down queue if there is a queue
			queue.shutdown();
		}

		// calculate time elapsed and output
		Duration elapsed = Duration.between(start, Instant.now());
		double seconds = (double) elapsed.toMillis() / Duration.ofSeconds(1).toMillis();

		System.out.printf("Elapsed: %f seconds%n", seconds);
	}
}
