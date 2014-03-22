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
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class MenuServlet extends HttpServlet
{
	private static final Logger log = Logger.getLogger(MenuServlet.class.getName());

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		String dbUrl = determineDbUrl();

		if (dbUrl == null)
		{
			return;
		}

		String store = req.getParameter("storeID");

		JSONObject result = null;

		try (Connection conn = DriverManager.getConnection(dbUrl))
		{
			PreparedStatement statement =
					conn.prepareStatement("SELECT * FROM menu_items WHERE storeID=?;");
			statement.setString(1, store);
			ResultSet rs = statement.executeQuery();
			result = generateJSONMenu(rs);
		}
		catch (SQLException | JSONException e)
		{
			log.severe(e.toString());
			e.printStackTrace();
			return;
		}

		resp.setContentType("text/plain");
		PrintWriter writer = resp.getWriter();
		writer.write(result.toString());

	}

	private JSONObject generateJSONMenu(ResultSet rs) throws SQLException, JSONException
	{
		JSONObject result = new JSONObject();
		JSONArray menuItems = new JSONArray();
		while (rs.next())
		{
			String entryName = rs.getString("entryName");
			String desc = rs.getString("description");

			JSONObject menuItem = new JSONObject();

			menuItem.put("name", entryName);
			menuItem.put("description", desc);
			menuItems.put(menuItem);
		}
		result.put("menuItems", menuItems);
		return result;

	}

	private String determineDbUrl()
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
