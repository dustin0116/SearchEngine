import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Data structure for the Inverted Index.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @author Yen Dah Hsiang
 * @version Fall 2019
 */
public class InvertedIndex {

	/** The data type for the inverted index. */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;

	/** Map for word count output. */
	private final TreeMap<String, Integer> wordCounts;

	/** Default constructor. Creates a new TreeMap for the inverted index. */
	public InvertedIndex() {

		this.index = new TreeMap<>();
		this.wordCounts = new TreeMap<>();
	}

	/**
	 * Adds word into the inverted index.
	 *
	 * @param string   word
	 * @param location file path of the word
	 * @param position position of the word to be added
	 */
	public void add(String string, String location, int position) {

		this.index.putIfAbsent(string, new TreeMap<>());
		this.index.get(string).putIfAbsent(location, new TreeSet<>()); // Add inner Map's path and TreeSet
		this.index.get(string).get(location).add(position); // Add the position of word

		this.wordCounts.putIfAbsent(location, position);

		if (wordCounts.get(location) < position) {

			wordCounts.put(location, position);

		}
	}

	/**
	 * Adds a collection of words into the inverted index.
	 *
	 * @param words    the collection of words to add.
	 * @param location file path of the words
	 * @param start    starting position
	 */
	public void add(Collection<String> words, String location, int start) {

		for (String string : words) {

			add(string, location, start);
			start++;
		}
	}

	/**
	 * Writes inverted index to JSON object.
	 *
	 * @param path path of file
	 * @throws IOException
	 */
	public void indexToJson(Path path) throws IOException {
		SimpleJsonWriter.invertedIndexJSON(this.index, path);
	}

	/**
	 * Writes all files in path and its word count to JSON object.
	 *
	 * @param path path of file
	 * @throws IOException
	 */
	public void wordCountsToJson(Path path) throws IOException {
		SimpleJsonWriter.asObject(this.wordCounts, path);
	}

	// Getters

	/**
	 * Returns all locations.
	 *
	 * @return Set view of keys (locations) in wordCounts
	 */
	public Set<String> getLocations() {
		return Collections.unmodifiableSet(this.wordCounts.keySet());
	}

	/**
	 * Check if there is such location.
	 *
	 * @param location file path
	 *
	 * @return True if such location exists
	 */
	public boolean hasLocation(String location) {
		return this.wordCounts.containsKey(location);
	}

	/**
	 * Get word counts of a location.
	 *
	 * @param location file path
	 *
	 * @return The number of words in that location
	 */
	public int getWordCount(String location) {
		return this.wordCounts.get(location);
	}

	/**
	 * Gets all word counts of all locations.
	 *
	 * @return The number of words in all locations
	 */
	public Map<String, Integer> getAllWordCount() {
		return Collections.unmodifiableMap(this.wordCounts);
	}

	/**
	 * Gets all words in the index.
	 *
	 * @return All words in the index
	 */
	public Set<String> getWords() {
		return Collections.unmodifiableSet(this.index.keySet());
	}

	/**
	 * Checks if given word exists in the index
	 *
	 * @param word the word to check
	 *
	 * @return True if word exists in the index
	 */
	public boolean hasWord(String word) {
		return this.index.containsKey(word);
	}

	/**
	 * Checks if the given word exists in the index and whether the given location
	 * is a key in the inner map for the word.
	 *
	 * @param word     the word to check
	 * @param location the location to check
	 *
	 * @return True if the word exists and the location is a key within the inner
	 *         map
	 */
	public boolean hasLocation(String word, String location) {
		return this.index.containsKey(word) && this.index.get(word).containsKey(location);
	}

	/**
	 * Returns the key set of inner map if word exists, which is the locations for
	 * the word.
	 *
	 * @param word the inner map's word to get
	 * @return Inner map key set of the word in index
	 */

	public Set<String> getLocations(String word) {
		return hasWord(word) ? Collections.unmodifiableSet(this.index.get(word).keySet()) : Collections.emptySet();
	}

	/**
	 * Checks whether the given word, location, and position combination exists in
	 * the index
	 *
	 * @param word     the word to check
	 * @param location the location to check
	 * @param position the position to check
	 *
	 * @return True if the given word, location, and position combination exists in
	 *         the index
	 */
	public boolean hasPosition(String word, String location, int position) {
		return hasLocation(word, location) && this.index.get(word).get(location).contains(position);
	}

	/**
	 * Gets position of word and location if the given word and location exists in
	 * the index.
	 *
	 * @param word     the word to get
	 * @param location the position to get
	 *
	 * @return Set of the positions of a word's location
	 */
	public Set<Integer> getPositions(String word, String location) {
		return hasLocation(word, location) ? Collections.unmodifiableSet(this.index.get(word).get(location))
				: Collections.emptySet();
	}

	/**
	 * Gets the total number of words in the index.
	 *
	 * @return total number of words in the index
	 */
	public int getTotalWords() {
		return Collections.unmodifiableSet(this.index.keySet()).size();
	}

	/**
	 * Search matches of the queries and returns a list of search results.
	 *
	 * @param queries the words to search
	 * @param exact   exact or partial search
	 * @return results a list of results
	 */
	public ArrayList<Result> search(Collection<String> queries, boolean exact) {
		return exact ? exactSearch(queries) : partialSearch(queries);
	}

	/**
	 * Finds exact search results and returns a list of Result objects.
	 *
	 * @param queries the words to query
	 * @return results the list of Result objects.
	 */
	public ArrayList<Result> exactSearch(Collection<String> queries) {

		ArrayList<Result> results = new ArrayList<>();
		Map<String, Result> lookup = new HashMap<>();

		for (String query : queries) {

			if (hasWord(query)) {

				searchHelper(results, lookup, query);
			}
		}

		Collections.sort(results);
		return results;
	}

	/**
	 * Finds partial search results and returns a list of Result objects.
	 *
	 * @param queries the words to query
	 * @return results the list of Result objects.
	 */
	public ArrayList<Result> partialSearch(Collection<String> queries) {

		ArrayList<Result> results = new ArrayList<>();
		Map<String, Result> lookup = new HashMap<>();

		for (String query : queries) {

			for (String word : this.index.tailMap(query).keySet()) {

				if (!word.startsWith(query)) {

					break;
				}

				searchHelper(results, lookup, word);
			}
		}

		Collections.sort(results);
		return results;
	}

	/**
	 * Combines with another built inverted index.
	 *
	 * @param local the other inverted index
	 */
	public void addAll(InvertedIndex local) {

		for (String word : local.index.keySet()) {

			if (this.index.containsKey(word)) {
				// Checks if index already has the word to prevent unnecessary loops
				for (String location : local.index.get(word).keySet()) {

					if (this.index.get(word).containsKey(location)) {
						// Combines the new positions with the already present position set of the
						// specific word-location combo
						this.index.get(word).get(location).addAll(local.index.get(word).get(location));

					} else {
						// Adds the location with the new positions if location not present
						this.index.get(word).put(location, local.index.get(word).get(location));
					}
				}
			} else {
				// Adds directly to index if word isn't present
				this.index.put(word, local.index.get(word));
			}
		}

		// Combining the word counts
		for (var location : local.wordCounts.keySet()) {

			if (this.wordCounts.containsKey(location)) {
				// Updates the total count
				if (local.wordCounts.get(location) > this.wordCounts.get(location)) {

					this.wordCounts.put(location, local.wordCounts.get(location));
				}

			} else {
				// Directly adds the new location and total count if location isn't present
				this.wordCounts.put(location, local.wordCounts.get(location));
			}
		}
	}

	/**
	 * Loops through locations of the matched word and either creates a new Result
	 * object or update its count and finally adding to results.
	 *
	 * @param results the list of Result objects
	 * @param lookup  the map to store results to avoid linear search
	 * @param word    the matched word
	 */
	private void searchHelper(ArrayList<Result> results, Map<String, Result> lookup, String word) {

		for (var location : this.index.get(word).keySet()) {

			if (!lookup.containsKey(location)) {

				Result result = new Result(location);

				results.add(result);
				lookup.put(location, result);
			}

			lookup.get(location).updateCount(word);
		}
	}

	/** Single Search Result. */
	public class Result implements Comparable<Result> {

		/** Location of file. */
		private final String where;

		/** Number of matches. */
		private int count;

		/** Total score. */
		private double score;

		/**
		 * Default constructor
		 *
		 * @param where
		 */
		public Result(String where) {
			this.where = where;
			this.count = 0;
			this.score = 0;
		}

		/**
		 * Returns the location of the result.
		 *
		 * @return The location of the result
		 */
		public String getWhere() {
			return this.where;
		}

		/**
		 * Returns the number of occurrences of the word query.
		 *
		 * @return Total count of the result
		 */
		public int getCount() {
			return count;
		}

		/**
		 * Returns the total score of the result
		 *
		 * @return Total score of the result
		 */
		public double getScore() {
			return this.score;
		}

		/**
		 * Update the count of the result
		 *
		 * @param word word to update count and score
		 */
		private void updateCount(String word) {

			this.count += index.get(word).get(this.where).size();
			this.score = (double) getCount() / wordCounts.get(this.where);
		}

		/**
		 * Determines whether if this result is empty or not.
		 *
		 * @return True or false
		 */
		public boolean isEmpty() {
			return this.getCount() == 0 && this.getScore() == 0 ? true : false;
		}

		@Override
		public int compareTo(Result o) {

			int compareScores = Double.compare(o.getScore(), this.getScore());
			int compareCount = Integer.compare(o.getCount(), this.getCount());
			int compareLocation = this.getWhere().compareToIgnoreCase(o.getWhere());

			if (compareScores == 0) {

				if (compareCount == 0) {

					return compareLocation;

				}

				return compareCount;

			}

			return compareScores;

		}
	}
}
