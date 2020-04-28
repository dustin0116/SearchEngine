import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Traversing through files, directories and building the inverted index.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @author Yen Dah Hsiang
 * @version Fall 2019
 */
public class IndexBuilder {

	/** The default stemmer algorithm. */
	public static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;

	/** The inverted index. */
	private final InvertedIndex index;

	/**
	 * Constructor for the inverted index builder.
	 *
	 * @param index the inverted index
	 */
	public IndexBuilder(InvertedIndex index) {
		this.index = index;
	}

	/**
	 * Builds the inverted index by traversing through the given path and checking
	 * if it's a text file.
	 *
	 * @param path the path from the flag "-path"
	 * @throws IOException
	 */
	public void create(Path path) throws IOException {
		traverseDirectory(path);
	}

	/**
	 * Traverses through directories recursively and adds all words of to index if
	 * the path is a text file.
	 *
	 * @param path the starting path
	 * @throws IOException
	 */
	private void traverseDirectory(Path path) throws IOException {

		if (Files.isDirectory(path)) {
			// If path is a directory
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {

				for (Path current : stream) {
					traverseDirectory(current);
				}
			}

		} else {
			// If path is to a file
			if (isTextFile(path)) {
				// Only process for text files
				addToIndex(path);

			}
		}


	}

	/**
	 * Parses words in a text file and directly adds to the inverted index.
	 *
	 * @param location the text file
	 * @param index    the inverted index
	 * @throws IOException
	 */
	public static void addToIndex(Path location, InvertedIndex index) throws IOException {

		Stemmer stemmer = new SnowballStemmer(DEFAULT);
		int iterator = 0;

		try (BufferedReader reader = Files.newBufferedReader(location, StandardCharsets.UTF_8)) {

			String line = null;
			String fileName = location.toString();

			while ((line = reader.readLine()) != null) {

				for (String string : TextParser.parse(line)) {

					iterator++;
					index.add(stemmer.stem(string).toString(), fileName, iterator);
				}
			}
		}
	}

	/**
	 * Parses words in a text file and directly adds to the inverted index.
	 *
	 * @param location the text file
	 * @throws IOException
	 */
	public void addToIndex(Path location) throws IOException {
		addToIndex(location, index);
	}

	/**
	 * Checks whether the path is a text file or not.
	 *
	 * @param path the path to check
	 * @return True if the given path is a text file
	 */
	public static boolean isTextFile(Path path) {

		String lower = path.toString().toLowerCase();

		return (lower.endsWith(".txt") || lower.endsWith(".text"));

	}
}