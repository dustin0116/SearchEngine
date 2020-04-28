import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @author Yen Dah Hsiang
 * @version Fall 2019
 */
public class SimpleJsonWriter {

	/** Formatter for score. */
	public static final DecimalFormat FORMATTER = new DecimalFormat("0.00000000");

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException
	 */
	public static void asArray(Collection<Integer> elements, Writer writer, int level) throws IOException {

		writer.write("[");

		var iterator = elements.iterator();

		if (iterator.hasNext()) {
			writer.write("\n");
			indent(iterator.next(), writer, level + 1);
		}

		while (iterator.hasNext()) {
			writer.write(",");
			writer.write("\n");
			indent(iterator.next(), writer, level + 1);
		}

		writer.write("\n");
		indent("]", writer, level - 1);
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException
	 *
	 * @see #asArray(Collection, Writer, int)
	 */
	public static void asArray(Collection<Integer> elements, Path path) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asArray(Collection, Writer, int)
	 */
	public static String asArray(Collection<Integer> elements) {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try {
			StringWriter writer = new StringWriter();
			asArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException
	 */
	public static void asObject(Map<String, Integer> elements, Writer writer, int level) throws IOException {

		writer.write("{");

		var iterator = elements.entrySet().iterator();
		level++;

		if (iterator.hasNext()) {

			writer.write("\n");
			writeEntry(iterator.next(), writer, level);

		}

		while (iterator.hasNext()) {

			writer.write(",");
			writer.write("\n");
			writeEntry(iterator.next(), writer, level);

		}

		writer.write("\n");

		indent("}", writer, level - 1);
	}

	/**
	 * Writes the entries for JSON object.
	 *
	 * @param elements the entry to write
	 * @param writer   the writer to use
	 * @param level    the indent level
	 * @throws IOException
	 */
	private static void writeEntry(Entry<String, Integer> elements, Writer writer, int level) throws IOException {
		quote(elements.getKey(), writer, level);
		writer.write(": ");
		writer.write(elements.getValue().toString());
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException
	 *
	 * @see #asObject(Map, Writer, int)
	 */
	public static void asObject(Map<String, Integer> elements, Path path) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asObject(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asObject(Map, Writer, int)
	 */
	public static String asObject(Map<String, Integer> elements) {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try {
			StringWriter writer = new StringWriter();
			asObject(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a nested pretty JSON object. The generic notation used
	 * allows this method to be used for any type of map with any type of nested
	 * collection of integer objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException
	 */
	public static void asNestedObject(Map<String, ? extends Collection<Integer>> elements, Writer writer, int level)
			throws IOException {

		writer.write("{");

		var outerIterator = elements.entrySet().iterator();
		level++;

		if (outerIterator.hasNext()) {
			writer.write("\n");
			writeNestedEntry(outerIterator.next(), writer, level);
		}

		while (outerIterator.hasNext()) {
			writer.write(",");
			writer.write("\n");
			writeNestedEntry(outerIterator.next(), writer, level);
		}

		writer.write("\n");
		indent("}", writer, level - 1);
	}

	/**
	 * Writes the nested entries for JSON nested object.
	 *
	 * @param elements the entry to write
	 * @param writer   the writer to use
	 * @param level    the indent level
	 * @throws IOException
	 */
	public static void writeNestedEntry(Entry<String, ? extends Collection<Integer>> elements, Writer writer, int level)
			throws IOException {
		quote(elements.getKey().toString(), writer, level);
		writer.write(": ");

		var innerIterator = elements.getValue().iterator();
		level++;

		writer.write("[");

		if (innerIterator.hasNext()) {
			writer.write("\n");
			indent(innerIterator.next().toString(), writer, level);
		}

		while (innerIterator.hasNext()) {
			writer.write(",");
			writer.write("\n");
			indent(innerIterator.next().toString(), writer, level);
		}

		writer.write("\n");
		indent("]", writer, level - 1);
	}

	/**
	 * Writes the elements as a nested pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException
	 *
	 * @see #asNestedObject(Map, Writer, int)
	 */
	public static void asNestedObject(Map<String, ? extends Collection<Integer>> elements, Path path)
			throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asNestedObject(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a nested pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asNestedObject(Map, Writer, int)
	 */
	public static String asNestedObject(Map<String, ? extends Collection<Integer>> elements) {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try {
			StringWriter writer = new StringWriter();
			asNestedObject(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException
	 */
	public static void invertedIndexJSON(TreeMap<String, TreeMap<String, TreeSet<Integer>>> elements, Writer writer,
			int level) throws IOException {

		writer.write("{");

		var wordIterator = elements.entrySet().iterator();
		level++;

		if (wordIterator.hasNext()) {

			writer.write("\n");
			writeInvertedIndexEntry(wordIterator.next(), writer, level);

		}
		while (wordIterator.hasNext()) {

			writer.write(",");
			writer.write("\n");
			writeInvertedIndexEntry(wordIterator.next(), writer, level);

		}

		writer.write("\n");
		indent("}", writer, level - 2);
	}

	/**
	 * Writes the entries of the inverted index for the inverted index JSON.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException
	 */
	public static void writeInvertedIndexEntry(Entry<String, TreeMap<String, TreeSet<Integer>>> elements, Writer writer,
			int level) throws IOException {

		quote(elements.getKey(), writer, level);
		writer.write(": ");

		var innerMapEntrySet = elements.getValue().entrySet();
		var pathIterator = innerMapEntrySet.iterator();

		writer.write("{");
		level++;

		if (pathIterator.hasNext()) {

			writer.write("\n");
			writeNestedEntry(pathIterator.next(), writer, level);

		}

		while (pathIterator.hasNext()) {

			writer.write(",");
			writer.write("\n");
			writeNestedEntry(pathIterator.next(), writer, level);

		}

		writer.write("\n");
		indent("}", writer, level - 1);
	}

	/**
	 * Writes the elements as a nested pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException
	 */
	public static void invertedIndexJSON(TreeMap<String, TreeMap<String, TreeSet<Integer>>> elements, Path path)
			throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			invertedIndexJSON(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a nested pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 */
	public static String invertedIndexJSON(TreeMap<String, TreeMap<String, TreeSet<Integer>>> elements) {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try {
			StringWriter writer = new StringWriter();
			invertedIndexJSON(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException
	 */
	public static void searchJSON(TreeMap<String, ArrayList<InvertedIndex.Result>> elements, Writer writer, int level)
			throws IOException {
		writer.write("{\n");

		var iterator = elements.entrySet().iterator();
		level++;

		if (iterator.hasNext()) {
			writeQuery(iterator.next(), writer, level);
			writer.write("\n");
			indent("]", writer, level);
		}

		while (iterator.hasNext()) {
			writer.write(",\n");
			writeQuery(iterator.next(), writer, level);
			writer.write("\n");
			indent("]", writer, level);
		}

		writer.write("\n");
		indent("}", writer, level - 1);
	}

	/**
	 * @param entry
	 * @param writer
	 * @param level
	 * @throws IOException
	 */
	public static void writeQuery(Entry<String, ArrayList<InvertedIndex.Result>> entry, Writer writer, int level)
			throws IOException {
		quote(entry.getKey(), writer, level);
		writer.write(": ");
		writer.write("[");
		level++;

		if (!entry.getValue().isEmpty()) {
			var searchResultIterator = entry.getValue().iterator();

			if (searchResultIterator.hasNext()) {

				writer.write("\n");
				indent("{\n", writer, level);
				level++;
				writeResult(searchResultIterator.next(), writer, level);
				indent("}", writer, level - 1);

			}

			while (searchResultIterator.hasNext()) {

				writer.write(",\n");
				indent("{\n", writer, level - 1);
				writeResult(searchResultIterator.next(), writer, level);
				indent("}", writer, level - 1);
			}

		}

	}

	/**
	 * Writes the location, word count, and score for each search result.
	 *
	 * @param searchResult the search result to write
	 * @param writer       the writer to use
	 * @param level
	 * @throws IOException
	 */
	public static void writeResult(InvertedIndex.Result searchResult, Writer writer, int level) throws IOException {
		quote("where", writer, level);
		writer.write(": ");
		quote(searchResult.getWhere(), writer);
		writer.write(",\n");

		quote("count", writer, level);
		writer.write(": ");
		writer.write(searchResult.getCount() + ",\n");

		quote("score", writer, level);
		writer.write(": ");
		writer.write(FORMATTER.format(searchResult.getScore()) + "\n");
	}

	/**
	 * Writes the elements as a nested pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException
	 */
	public static void searchJSON(TreeMap<String, ArrayList<InvertedIndex.Result>> elements, Path path)
			throws IOException {

		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			searchJSON(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a nested pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 */
	public static String searchJSON(TreeMap<String, ArrayList<InvertedIndex.Result>> elements) {

		try {
			StringWriter writer = new StringWriter();
			searchJSON(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the {@code \t} tab symbol by the number of times specified.
	 *
	 * @param writer the writer to use
	 * @param times  the number of times to write a tab symbol
	 * @throws IOException
	 */
	public static void indent(Writer writer, int times) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		for (int i = 0; i < times; i++) {
			writer.write('\t');
		}
	}

	/**
	 * Indents and then writes the element.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param times   the number of times to indent
	 * @throws IOException
	 *
	 * @see #indent(String, Writer, int)
	 * @see #indent(Writer, int)
	 */
	public static void indent(Integer element, Writer writer, int times) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		indent(element.toString(), writer, times);
	}

	/**
	 * Indents and then writes the element.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param times   the number of times to indent
	 * @throws IOException
	 *
	 * @see #indent(Writer, int)
	 */
	public static void indent(String element, Writer writer, int times) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		indent(writer, times);
		writer.write(element);
	}

	/**
	 * Writes the element surrounded by {@code " "} quotation marks.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @throws IOException
	 */
	public static void quote(String element, Writer writer) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * Indents and then writes the element surrounded by {@code " "} quotation
	 * marks.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param times   the number of times to indent
	 * @throws IOException
	 *
	 * @see #indent(Writer, int)
	 * @see #quote(String, Writer)
	 */
	public static void quote(String element, Writer writer, int times) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		indent(writer, times);
		quote(element, writer);
	}
}
