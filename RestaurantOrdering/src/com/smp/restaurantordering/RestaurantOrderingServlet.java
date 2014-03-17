package com.smp.restaurantordering;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.servlet.http.*;

import com.google.appengine.api.utils.SystemProperty;

@SuppressWarnings("serial")
public class RestaurantOrderingServlet extends HttpServlet
{
	private static final Logger log = Logger.getLogger(RestaurantOrderingServlet.class.getName());

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		String dbUrl = determineDbUrl();

		if (dbUrl == null)
		{
			log.severe("before return");
			return;
		}

		Connection conn;
		resp.setContentType("text/html");
		PrintWriter writer = resp.getWriter();
		writer.println("<html>");
		writer.println("<body>");

		try
		{
			conn = DriverManager.getConnection(dbUrl);
			ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM menu_items;");
			while (rs.next())
			{
				log.info("in <p>");
				writer.println("<p>");
				String entryName = rs.getString("entryName");
				String desc = rs.getString("description");
				writer.print(entryName + " " + desc);
				writer.println("</p>");
			}

		}
		catch (SQLException e)
		{
			log.severe("An error message.");
			log.severe(e.toString());
			writer.println(e.toString());
			e.printStackTrace();
		}

		writer.println("</body>");
		writer.println("</html>");
	}

	public String determineDbUrl()
	{
		String url = null;
		try
		{
			if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production)
			{
				// Load the class that provides the new "jdbc:google:mysql://"
				// prefix.
				log.info("in produciton");
				Class.forName("com.mysql.jdbc.GoogleDriver");
				url = "jdbc:google:mysql://direct-obelisk-521:restaurant-database/restaurant?user=root";
			}
			else
			{
				// Local MySQL instance to use during development.
				log.info("in non-produciton");
				Class.forName("com.mysql.jdbc.Driver");
				url = "jdbc:mysql://127.0.0.1:3306/guestbook?user=root";

				// Alternatively, connect to a Google Cloud SQL instance using:
				// jdbc:mysql://ip-address-of-google-cloud-sql-instance:3306/guestbook?user=root
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			log.severe(e.getMessage());
			return null;
		}
		return url;
	}
}
