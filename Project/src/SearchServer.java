import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Server for web interface of search engine.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @author Yen Dah Hsiang
 * @version Fall 2019
 *
 */
public class SearchServer {

	/**
	 * Starts the server.
	 *
	 * @param index   the inverted index
	 * @param PORT    the port to run server
	 * @param threads the number of threads used
	 * @throws Exception
	 */
	public static void start(ThreadSafeInvertedIndex index, int PORT, int threads) throws Exception {

		Server server = new Server(PORT);

		// Setup the handler component
		ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);

		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setResourceBase("img");
		resourceHandler.setDirectoriesListed(true);

		ContextHandler resourceContext = new ContextHandler("/images");

		resourceContext.setHandler(resourceHandler);

		servletContext.setContextPath("/");
		servletContext.addServlet(new ServletHolder(new SearchServlet(index, threads)), "/");
		servletContext.addServlet(new ServletHolder(new IndexBrowserServlet(index)), "/show/index");
		servletContext.addServlet(new ServletHolder(new IndexLocationsServlet(index)), "/show/urls");
		servletContext.addServlet(new ServletHolder(new HistoryServlet()), "/show/history");

		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { resourceContext, servletContext });
		// Configure server to use connector and handler
		server.setHandler(handlers);
		// Start the server (it is a thread) and wait for it to complete
		server.start();
		server.join();

	}

}
