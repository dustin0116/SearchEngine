import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Servlet for web interface of search engine.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @author Yen Dah Hsiang
 * @version Fall 2019
 *
 */
public class SearchServlet extends CookieBaseServlet {

	/** Unused. */
	private static final long serialVersionUID = 1L;

	/** Title of web page. */
	private static final String TITLE = "Search Engine";

	/** The inverted index. */
	private final ThreadSafeInvertedIndex index;

	/** The logger. */
	private static Logger log = Log.getRootLogger();

	/** Store user input. */
	private ConcurrentLinkedQueue<String> inputs;

	/** URL to crawl. **/
	private String url;

	/** Store user input history */
	private ConcurrentLinkedQueue<String> searchHistory;

	/** The work queue. */
	private final WorkQueue queue;

	/** The default stemmer algorithm. */
	public static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;

	/**
	 * Constructor for the search servlet.
	 *
	 * @param index   the inverted index
	 * @param threads the number of threads to use
	 */
	public SearchServlet(ThreadSafeInvertedIndex index, int threads) {

		super();
		this.index = index;
		inputs = new ConcurrentLinkedQueue<String>();
		url = "";
		searchHistory = new ConcurrentLinkedQueue<String>();
		queue = new WorkQueue(threads);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);

		log.info("Result ID " + this.hashCode() + " handling GET request.");

		PrintWriter out = response.getWriter();
		HttpSession session = request.getSession(true);
		Map<String, Cookie> cookies = getCookieMap(request);

		// Redirecting to other locations
		if (request.getParameter("show") != null) {

			if (request.getParameter("show").equals("index")) {

				response.sendRedirect(request.getServletPath() + "show/index");

			} else if (request.getParameter("show").equals("locations")) {

				response.sendRedirect(request.getServletPath() + "show/urls");

			} else if (request.getParameter("show").equals("history")) {

				response.sendRedirect(request.getServletPath() + "show/history");
			}
		}

		out.printf("<!DOCTYPE html>%n");
		out.printf("<html>%n");
		out.printf("<head>%n");
		out.printf("	<meta charset=\"utf-8\">%n");
		out.printf("	<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">%n");
		out.printf("	<title>%s</title>%n", TITLE);
		out.printf(
				"	<link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/bulma@0.8.0/css/bulma.min.css\">%n");
		out.printf("	<script defer src=\"https://use.fontawesome.com/releases/v5.3.1/js/all.js\"></script>%n");
		out.printf("</head>%n");
		out.printf("%n");

		out.printf("<body>%n");
		out.printf("	<form method=\"%s\" action=\"%s\">%n", "POST", request.getServletPath());
		out.printf("		<div class=\"container\" style=\"margin-top: 5px;\">%n");
		out.printf(
				"				<button class=\"button\" name=\"show\" value=\"index\" style=\"margin-top: 5px;\"><i class=\"fas fa-stream fa-fw\"></i>Show index</button>%n");
		out.printf(
				"				<button class=\"button\" name=\"show\" value=\"locations\" style=\"margin-top: 5px;\"><i class=\"fas fa-globe-americas fa-fw\"></i>Show locations</button>%n");
		out.printf(
				"				<button class=\"button\" name=\"show\" value=\"history\" style=\"margin-top: 5px;\"><i class=\"fas fa-history fa-fw\"></i> Show history</button>%n");
		out.printf("		</div>");
		out.printf("    </form>%n");
		out.printf("%n");

		out.printf("	<section class=\"section\">%n");
		out.printf("	  <div class=\"container\">%n");
		out.printf("	    <div class=\"column is-centered\">%n");
		out.printf("			<div class=\"column is-half\">%n");
		out.printf("				<figure class=\"image is-256x256\" style=\"left: 280px;\">%n");
		out.printf("					<img src=\"images/logo.png\" alt=\"Elephant logo\">%n");
		out.printf("				</figure>%n");
		out.printf("	    		</div>%n");
		out.printf("	  	</div>%n");
		out.printf("	  </div>%n");
		out.printf("	</section>%n");
		out.printf("%n");

		out.printf("	<section class=\"section\">%n");
		out.printf("		<div class=\"container\">%n");
		out.printf("			<form method=\"%s\" action=\"%s\">%n", "POST", request.getServletPath());
		out.printf("				<div class=\"field\">%n");
		out.printf("					<div class=\"input-group\">%n");

		// Web crawling
		WebCrawler webCrawler = new WebCrawler(index, queue);

		if (url != null && !url.isEmpty()) {

			try {

				URL newCrawl = new URL(url);
				webCrawler.build(newCrawl, 20);

			} catch (Exception e) {

				System.out.println("Invalid URL: " + url);
				out.printf("<span>Please enter a valild URL!</span>");
			}

		}

		out.printf(
				"						<input class=\"input\" type=\"text\" name=\"%s\" placeholder=\"URL to crawl...\"><br><br>%n",
				"urlCrawl");

		if (inputs.isEmpty()) {

			out.printf(
					"						<input class=\"input\" type=\"text\" name=\"%s\" style=\"margin-bottom: 10px;\" placeholder=\"Enter your search...\"><br>%n",
					"query");
		} else {

			out.printf(
					"						<input class=\"input\" type=\"text\" name=\"%s\" style=\"margin-bottom: 10px;\" value =\"%s\"><br>%n",
					"query", inputs.peek());
		}

		out.printf("					<div class=\"field is-horizontal\">%n");
		out.printf("						<div class=\"field body\">%n");
		out.printf("							<div class=\"field\">%n");
		out.printf(
				"									<button class=\"button is-info\" type=\"submit\" name=\"button\" value=\"Search\">Search</button>%n",
				"query");
		out.printf(
				"									<button class=\"button is-info\" name=\"button\" value=\"Lucky\">I'm feeling lucky</button><br>%n");

		Cookie radio = cookies.get("radio");

		if (radio != null) {
			// Radio button status
			if (radio.getValue().equals("partial")) {
				out.printf(
						"								<input type=\"radio\" name=\"search\" value=\"partial\" style=\"margin-top: 10px;\" checked> Partial Search</input>");
			} else {
				out.printf(
						"								<input type=\"radio\" name=\"search\" value=\"partial\" style=\"margin-top: 10px;\"> Partial Search</input>");
			}

			if (radio.getValue().equals("exact")) {
				out.printf(
						"								<input type=\"radio\" name=\"search\" value=\"exact\" style=\"margin-top: 10px;\" checked> Exact Search</input>");
			} else {
				out.printf(
						"								<input type=\"radio\" name=\"search\" value=\"exact\" style=\"margin-top: 10px;\"> Exact Search</input>");
			}

		} else {
			// Default radio buttons
			out.printf(
					"								<input type=\"radio\" name=\"search\" value=\"partial\" style=\"margin-top: 10px;\" checked> Partial Search</input>");
			out.printf(
					"								<input type=\"radio\" name=\"search\" value=\"exact\" style=\"margin-top: 10px;\"> Exact Search</input>");
		}

		out.printf("							</div>%n");
		out.printf("						</div>%n");
		out.printf("					</div>%n");
		out.printf("				</div> %n");
		out.printf("%n");
		out.printf("			</form>%n");
		out.printf("		</div>%n");
		out.printf("	</section>%n");
		out.printf("%n");

		out.printf("	<section class=\"section\">%n");
		out.printf("		<div class=\"container\">%n");
		out.printf("			<h2 class=\"title\">Results</h2>%n");
		out.printf("%n");

		if (inputs.isEmpty()) {
			out.printf("				<p>No results.</p>%n");
		}

		else {

			session.setAttribute("searchHistory", searchHistory);

			for (String input : inputs) {

				TreeSet<String> queries = TextFileStemmer.uniqueStems(input);

				// Search and search duration
				long startTime = System.nanoTime();

				ArrayList<InvertedIndex.Result> results = index.search(queries, radio.getValue().equals("exact"));

				long endTime = System.nanoTime();

				long duration = (endTime - startTime);

				if (results.isEmpty()) {

					out.printf("<span> No results.</span>%n");

				} else {

					if (request.getParameter("lucky") != null && request.getParameter("lucky").equals("true")) {

						response.sendRedirect(results.get(0).getWhere());

					} else {

						out.printf("<i>Time took to search: %s (nanoseconds)</i><br><br>", duration);

						out.printf("<ol style=\"text-align:left; list-style-position:outside;\">");

						for (InvertedIndex.Result result : results) {

							out.printf("<li><a href=\"%s\" target=\"_blank\"> %s</a><br>%n", result.getWhere(),
									result.getWhere());
							out.printf("<span>Score: %s</span><br>%n", result.getScore());
							out.printf("<span>Matches: %s</span><br>%n", result.getCount());
							out.printf("<span>Word count: %s</span>%n", index.getWordCount(result.getWhere()));
							out.printf("</li>");
						}
						out.printf("</ol>");
					}
				}
			}
		}

		out.printf("			</div>%n");
		out.printf("%n");
		out.printf("		</div>%n");
		out.printf("	</section>%n");
		out.printf("%n");

		out.printf("	<footer class=\"footer\">%n");
		out.printf("	  <div class=\"content has-text-centered\">%n");
		out.printf("	    <p>%n");
		out.printf("	      Yen Dah Hsiang | 2019");
		out.printf("	    </p>%n");
		out.printf("	  </div>%n");
		out.printf("	</footer>%n");
		out.printf("</body>%n");
		out.printf("</html>%n");

		response.setStatus(HttpServletResponse.SC_OK);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);

		log.info("Result ID " + this.hashCode() + " handling POST request.");

		String query = request.getParameter("query"); // Get search query
		url = request.getParameter("urlCrawl"); // Get new URL to crawl
		String traverse = request.getParameter("show"); // Check if redirecting to other locations of the search engine
		String button = request.getParameter("button"); // Get I'm feeling lucky boolean
		String search = request.getParameter("search"); // Get partial or exact search

		query = query == null ? null : query; // Set search query
		url = url == null || url.equals("") ? null : url; // Set new URL
		traverse = traverse == null ? null : traverse;
		button = button == null ? null : button; // Set button
		search = search == null ? null : search; // Check if partial or exact search

		// Avoid XSS attacks using Apache Commons Text
		query = StringEscapeUtils.escapeHtml4(query);
		url = StringEscapeUtils.escapeHtml4(url);
		button = StringEscapeUtils.escapeHtml4(button);
		search = StringEscapeUtils.escapeHtml4(search);

		// Turn query to null if new URL crawl value exists so that after crawling, it
		// won't automatically search with the input
		query = url != null ? null : query;

		String redirect = request.getServletPath();

		if (traverse != null) {

			if (traverse.contentEquals("index")) {
				// Redirect to display inverted index
				redirect += "?show=index";

			} else if (traverse.contentEquals("locations")) {
				// Redirect to display index locations
				redirect += "?show=locations";

			} else if (traverse.contentEquals("history")) {
				// Redirect to user search history
				redirect += "?show=history";
			}
		}

		if (button != null && button.contentEquals("Lucky")) {
			// Redirect to I'm feeling lucky result
			redirect += "?lucky=true";
		}

		if (query != null && !query.isEmpty()) {
			// Add user input to history and query
			searchHistory.add(query);
			inputs.add(query);

		} else {

			inputs.poll();
		}

		if (search != null && !search.isEmpty()) {
			// Preserve radio button status for partial/exact search
			Cookie radio = new Cookie("radio", search);
			response.addCookie(radio);
		}

		if (inputs.size() > 1) {
			inputs.poll();
		}

		response.setStatus(HttpServletResponse.SC_OK);
		response.sendRedirect(redirect);
	}
}
