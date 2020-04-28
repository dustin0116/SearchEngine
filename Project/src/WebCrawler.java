import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Multithreaded Web Crawler
 *
 * @author CS 212 Software Development
 * @author Yen Dah Hsiang
 * @author University of San Francisco
 * @version Fall 2019
 */
public class WebCrawler {

	/** The default stemmer algorithm. */
	public static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;

	/** The inverted index. */
	private final ThreadSafeInvertedIndex index;

	/** The work queue. */
	private final WorkQueue queue;

	/** Storing each unique parsed URL. */
	private final HashSet<URL> uniqueUrls;

	/**
	 * Default Constructor
	 *
	 * @param index the inverted index
	 * @param queue the work queue
	 */
	public WebCrawler(ThreadSafeInvertedIndex index, WorkQueue queue) {

		this.index = index;
		this.queue = queue;
		this.uniqueUrls = new HashSet<>();
	}

	/**
	 * Builds the inverted index from a seed URL.
	 *
	 * @param seed  the seeded URL to crawl
	 * @param limit total number of URL to crawl
	 */
	public void build(URL seed, int limit) {

		uniqueUrls.add(seed);
		queue.execute(new Task(seed, limit));
		queue.finish();
	}

	/**
	 * Add all cleaned HTML text into index.
	 *
	 * @param seed  the seeded URL to crawl
	 * @param html  the fetched html resource
	 * @param index the inverted index to add to
	 */
	public static void addToIndex(URL seed, String html, InvertedIndex index) {

		Stemmer stemmer = new SnowballStemmer(DEFAULT);
		int iterator = 1;

		for (String cleanedHtml : TextParser.parse(HtmlCleaner.stripHtml(html))) {
			// Loop through each cleaned HTML text and add to index
			index.add(stemmer.stem(cleanedHtml).toString(), seed.toString(), iterator);
			iterator++;
		}
	}

	/** Task for building index with web crawling. */
	private class Task implements Runnable {

		/** The seeded URL to crawl */
		private final URL seed;

		/** URL crawling limit */
		private final int limit;

		/**
		 * Constructor for web crawling and adding to index task.
		 *
		 * @param seed  the seeded URL to crawl
		 * @param limit total number of URL to crawl
		 */
		private Task(URL seed, int limit) {

			this.seed = seed;
			this.limit = limit;
		}

		@Override
		public void run() {

			try {
				// Fetches the resource with a redirect limit of 3 to avoid infinite loop
				String html = HtmlFetcher.fetch(seed, 3);

				// Check if resources are fetched correctly
				ThreadSafeInvertedIndex local = new ThreadSafeInvertedIndex();

				addToIndex(seed, html, local);
				index.addAll(local);

				if (uniqueUrls.size() < limit) {
					// Parse all remaining URLs
					ArrayList<URL> links = LinkParser.listLinks(seed, html);

					for (URL url : links) {

						synchronized (uniqueUrls) {

							if (uniqueUrls.size() == limit) {
								// Stop adding when the number of unique URLS hit the limit
								return;
							}

							if (!uniqueUrls.contains(url)) {
								// Add unique URL to total
								uniqueUrls.add(url);
								queue.execute(new Task(url, limit));
							}
						}
					}
				}

			} catch (Exception e) {

				System.out.println("Could not build inverted index with: " + seed);
			}
		}
	}
}
