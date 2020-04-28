import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Interface for query parsing.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @author Yen Dah Hsiang
 * @version Fall 2019
 *
 */

public interface QueryParserInterface {

	/**
	 * Parses the query file and loops through each line, eventually adding to
	 * search results.
	 *
	 * @param path  the path of the query file
	 * @param exact whether the search is exact or partial
	 * @throws IOException
	 */
	public default void parseFile(Path path, boolean exact) throws IOException {

		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

			String line = null;

			while ((line = reader.readLine()) != null) {

				parseLine(line, exact);

			}
		}
	}

	/**
	 * Parses through the given file and search.
	 *
	 * @param line
	 * @param exact exact or partial search
	 * @throws IOException
	 */
	public void parseLine(String line, boolean exact) throws IOException;

	/**
	 * Writes search results to JSON object.
	 *
	 * @param path path of file
	 * @throws IOException
	 */
	public void resultsToJson(Path path) throws IOException;

}
