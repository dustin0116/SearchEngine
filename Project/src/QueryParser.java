import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Data structure to store word queries.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @author Yen Dah Hsiang
 * @version Fall 2019
 *
 */

public class QueryParser implements QueryParserInterface {

	/** The inverted index. */
	private final InvertedIndex index;

	/** The search results. */
	private final TreeMap<String, ArrayList<InvertedIndex.Result>> searchResults;

	/**
	 * Constructor for query parser.
	 *
	 * @param index the inverted index
	 */
	public QueryParser(InvertedIndex index) {

		this.index = index;
		this.searchResults = new TreeMap<>();
	}

	/**
	 * Parses through a line and add matches to search results.
	 *
	 * @param line  the line to parse
	 * @param exact whether the search is exact or partial
	 */
	@Override
	public void parseLine(String line, boolean exact) {

		TreeSet<String> queries = TextFileStemmer.uniqueStems(line);

		if (queries.isEmpty()) {
			// Stop if there is no query
			return;
		}

		String cleanedLine = String.join(" ", TextFileStemmer.uniqueStems(line));

		if (searchResults.containsKey(cleanedLine)) {
			// Stop if search results already contains the query
			return;
		}

		ArrayList<InvertedIndex.Result> results = index.search(queries, exact);
		searchResults.put(cleanedLine, results);
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
}
