import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Multithreading version for QueryParser.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @author Yen Dah Hsiang
 * @version Fall 2019
 *
 */
public class MultithreadQueryParser implements QueryParserInterface {

	/** The inverted index. */
	private final ThreadSafeInvertedIndex index;

	/** The work queue. */
	private final WorkQueue queue;

	/** The search results. */
	private final TreeMap<String, ArrayList<InvertedIndex.Result>> searchResults;

	/**
	 * Constructor for multithreading query parser.
	 *
	 * @param index the inverted index
	 * @param queue the work queue
	 */
	public MultithreadQueryParser(ThreadSafeInvertedIndex index, WorkQueue queue) {

		this.index = index;
		this.queue = queue;
		this.searchResults = new TreeMap<>();
	}

	/**
	 * Parses the query file and loops through each line, eventually adding to
	 * search results.
	 *
	 * @param path  the path of the query file
	 * @param exact whether the search is exact or partial
	 * @throws IOException
	 */
	@Override
	public void parseFile(Path path, boolean exact) throws IOException {

		QueryParserInterface.super.parseFile(path, exact);
		queue.finish();
	}

	/**
	 * Parses through a line of the query file.
	 *
	 * @param line  the line to parse
	 * @param exact exact search or partial search
	 */
	@Override
	public void parseLine(String line, boolean exact) {
		queue.execute(new Task(line, exact));
	}

	/**
	 * Writes search results to JSON object.
	 *
	 * @param path path of file
	 * @throws IOException
	 */
	@Override
	public void resultsToJson(Path path) throws IOException {
		SimpleJsonWriter.searchJSON(this.searchResults, path);
	}

	/**
	 * Task for handling an individual query.
	 *
	 */
	private class Task implements Runnable {

		/** The query line. */
		private final String line;

		/** Exact or Partial Search. */
		private final boolean exact;

		/**
		 * Constructor for the task.
		 *
		 * @param line  the query line
		 * @param exact exact or partial search
		 *
		 */
		private Task(String line, boolean exact) {

			this.line = line;
			this.exact = exact;
		}

		@Override
		public void run() {
			// Implemented everything of parseLine in here so that each thread wouldn't need
			// to wait until the previous thread finishes cleaning a line to execute
			TreeSet<String> queries = TextFileStemmer.uniqueStems(line);

			if (queries.isEmpty()) {
				// Stop if there is no query
				return;
			}

			String cleanedLine = String.join(" ", TextFileStemmer.uniqueStems(line));

			synchronized (searchResults) {
				// Synchronize adding to search results
				if (searchResults.containsKey(cleanedLine)) {
					return;
				}
			}
			ArrayList<InvertedIndex.Result> results = index.search(queries, exact);
			synchronized (searchResults) {
				searchResults.put(cleanedLine, results);
			}
		}
	}
}
