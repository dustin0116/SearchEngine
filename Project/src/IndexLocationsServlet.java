import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Servlet for displaying index locations.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @author Yen Dah Hsiang
 * @version Fall 2019
 *
 */
public class IndexLocationsServlet extends HttpServlet {

	/** Unused. */
	private static final long serialVersionUID = 1L;

	/** Title of web page. */
	private static final String TITLE = "Search Engine - Show Index Locations";

	/** The inverted index. */
	private final ThreadSafeInvertedIndex index;

	/** The logger. */
	private static Logger log = Log.getRootLogger();

	/** The default stemmer algorithm. */
	public static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;

	/**
	 * Constructor for display locations servlet.
	 *
	 * @param index the inverted index
	 */
	public IndexLocationsServlet(ThreadSafeInvertedIndex index) {

		super();
		this.index = index;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);

		log.info("Result ID " + this.hashCode() + " handling GET request.");

		PrintWriter out = response.getWriter();

		// Redirecting to other locations
		if (request.getParameter("show") != null) {

			if (request.getParameter("show").equals("history")) {

				response.sendRedirect("/show/history");

			} else if (request.getParameter("show").equals("index")) {

				response.sendRedirect("/show/index");

			} else if (request.getParameter("show").equals("back")) {

				response.sendRedirect("/");
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
				"				<button class=\"button\" name=\"show\" value=\"back\" style=\"margin-top: 5px;\"><i class=\"fas fa-arrow-left\"></i></button>%n");
		out.printf(
				"				<button class=\"button\" name=\"show\" value=\"index\" style=\"margin-top: 5px;\"><i class=\"fas fa-stream fa-fw\"></i>Show index</button>%n");
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
		out.printf("					<img src=\"/images/logo.png\" alt=\"Elephant logo\">%n");
		out.printf("				</figure>%n");
		out.printf("	    		</div>%n");
		out.printf("	  	</div>%n");
		out.printf("	  </div>%n");
		out.printf("	</section>%n");
		out.printf("%n");
		out.printf("	<section class=\"section\">%n");
		out.printf("		<div class=\"container\">%n");
		out.printf("			<h2 class=\"title\">Display Inverted Index Locations</h2>%n");
		out.printf("			<ol>%n");

		if (index.getWords().size() == 0) {
			out.printf("			<span>Index not built yet.</span>%n");
		} else {
			for (var location : index.getLocations()) {
				out.printf("				<li><a href=\"%s\" target=\"_blank\">%s</a>: %s</li>%n", location, location,
						index.getWordCount(location));
			}
		}
		out.printf("			</ol>%n");
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

		String traverse = request.getParameter("show"); // Check if redirecting to other locations of the search engine

		traverse = traverse == null ? null : traverse; // Set traverse location

		// Avoid XSS attacks using Apache Commons Text
		traverse = StringEscapeUtils.escapeHtml4(traverse);

		String redirect = request.getServletPath();

		if (traverse != null) {

			if (traverse.contentEquals("back")) {
				// Redirect to display inverted index
				redirect += "?show=back";

			} else if (traverse.contentEquals("history")) {
				// Redirect to display index locations
				redirect += "?show=history";

			} else if (traverse.contentEquals("index")) {
				// Redirect to user search history
				redirect += "?show=index";
			}
		}

		response.setStatus(HttpServletResponse.SC_OK);
		response.sendRedirect(redirect);

	}
}