import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Servlet for search history.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @author Yen Dah Hsiang
 * @version Fall 2019
 *
 */
public class HistoryServlet extends CookieBaseServlet{

	/** Unused. */
	private static final long serialVersionUID = 1L;

	/** Title of web page. */
	private static final String TITLE = "Search Engine - History";

	/** The logger. */
	private static Logger log = Log.getRootLogger();

	/** The default stemmer algorithm. */
	public static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;

	/**
	 * Constructor for servlet, with index.
	 */
	public HistoryServlet() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);

		log.info("Result ID " + this.hashCode() + " handling GET request.");

		PrintWriter out = response.getWriter();

		HttpSession session = request.getSession(true);

		// Redirecting to other locations
		if (request.getParameter("show") != null) {

			if (request.getParameter("show").equals("index")) {

				response.sendRedirect("/show/index");

			} else if (request.getParameter("show").equals("locations")) {

				response.sendRedirect("/show/urls");

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
				"				<button class=\"button\" name=\"show\" value=\"locations\" style=\"margin-top: 5px;\"><i class=\"fas fa-globe-americas fa-fw\"></i>Show locations</button>%n");
		out.printf("		</div>");
		out.printf("    </form>%n");
		out.printf("%n");

		out.printf("	<section class=\"section\">%n");
		out.printf("	  <div class=\"container\">%n");
		out.printf("	    <div class=\"column is-centered\">%n");
		out.printf("			<div class=\"column is-half\">%n");
		out.printf("				<figure class=\"image is-256x256\" style=\"left: 280px;\">%n");
		out.printf(
				"					<img src=\"/images/logo.png\" alt=\"Elephant logo\">%n");
		out.printf("				</figure>%n");
		out.printf("	    		</div>%n");
		out.printf("	  	</div>%n");
		out.printf("	  </div>%n");
		out.printf("	</section>%n");
		out.printf("%n");
		out.printf("	<section class=\"section\">%n");
		out.printf("		<div class=\"container\">%n");
		out.printf("			<h2 class=\"title\">Search History</h2>%n");
		out.printf("		</div>%n");
		out.printf("	</section>%n");
		out.printf("%n");


		out.printf("	<section class=\"section\">%n");
		out.printf("		<div class=\"container\">%n");
		out.printf("			<form method=\"%s\" action=\"%s\">%n", "POST", request.getServletPath());
		out.printf("				<button class=\"button is-fullwidth\" name=\"button\" value=\"clear\">Clear search history</button><br><br>%n");
		out.printf("			</form>%n");

		ConcurrentLinkedQueue<?> history = (ConcurrentLinkedQueue<?>) session.getAttribute("searchHistory");

		if (history != null && request.getParameter("clearHistory") != null && request.getParameter("clearHistory").equals("true")) {
			history.clear();
		}

		if (history == null || history.isEmpty()) {

			out.printf("			<span>No search history.</span>%n");

		} else {

			out.printf("			<ol style=\"text-align:left; list-style-position:outside;\">");

			for (Object word : history) {

				if (!word.toString().isEmpty()) {
					out.printf("				<li>%s</li>%n", word);
				}
			}
			out.printf("			</ol>%n");
		}


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
		out.printf("<script src=\"/Project/src/script.js\"</script>%n");
		out.printf("</body>%n");
		out.printf("</html>%n");

		response.setStatus(HttpServletResponse.SC_OK);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);

		String button = request.getParameter("button"); // Check clear history
		String traverse = request.getParameter("show"); // Check if redirecting to other locations of the search engine

		button = button == null ? null : button; // Set button
		traverse = traverse  == null ? null : traverse; // Set traverse location

		// Avoid XSS attacks using Apache Commons Text
		button = StringEscapeUtils.escapeHtml4(button);
		traverse = StringEscapeUtils.escapeHtml4(traverse);

		String redirect = request.getServletPath();

		if (traverse != null) {

			if (traverse.contentEquals("back")) {
				// Redirect to display inverted index
				redirect += "?show=back";

			} else if (traverse.contentEquals("locations")) {
				// Redirect to display index locations
				redirect += "?show=locations";

			} else if (traverse.contentEquals("index")) {
				// Redirect to user search history
				redirect += "?show=index";
			}
		}

		if (button != null) {
			redirect += "?clearHistory=true";
		}

		response.setStatus(HttpServletResponse.SC_OK);
		response.sendRedirect(redirect);
	}
}
