package com.smp.restaurantordering;

import java.io.IOException;
import static com.smp.restaurantcommon.Constants.*;
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

		JSONObject result = null;
		String storeID = req.getParameter(DB_STORE_ID);

		try (Connection conn = DriverManager.getConnection(dbUrl))
		{
			ResultSet rs = queryForMenuItems(conn, dbUrl, storeID);
			result = generateJSONMenu(rs);
		}
		catch (SQLException | JSONException e)
		{
			log.severe(e.toString());
			e.printStackTrace();
			return;
		}

		resp.setContentType("application/json");
		PrintWriter writer = resp.getWriter();
		writer.print(result);
		writer.flush();
	}

	private ResultSet queryForMenuItems(Connection conn, String dbUrl, String storeID) throws SQLException
	{
		PreparedStatement statement =
				conn.prepareStatement("SELECT * FROM " + DB_MENU_ITEMS + " WHERE storeID=?;");
		statement.setString(1, storeID);

		return statement.executeQuery();
	}

	private JSONObject generateJSONMenu(ResultSet rs) throws SQLException, JSONException
	{
		JSONObject result = new JSONObject();
		JSONArray menuItems = new JSONArray();
		while (rs.next())
		{
			String entryName = rs.getString(DB_MENU_NAME);
			String desc = rs.getString(DB_MENU_DESCRIPTION);
			String basePrice = rs.getString(DB_MENU_BASE_PRICE);

			JSONObject menuItem = new JSONObject();

			menuItem.put(JSON_MENU_NAME, entryName);
			menuItem.put(JSON_MENU_DESCRIPTION, desc);
			menuItem.put(JSON_MENU_BASE_PRICE, basePrice);
			menuItems.put(menuItem);
		}
		result.put(JSON_MENU_ITEMS, menuItems);
		return result;

	}

	private String determineDbUrl()
	{
		String url = null;
		try
		{
			if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production)
			{
				log.info("in produciton");
				Class.forName("com.mysql.jdbc.GoogleDriver");
				url = "jdbc:google:mysql://direct-obelisk-521:restaurant-database/restaurant?user=root";
			}
			else
			{
				log.info("in non-produciton");
				Class.forName("com.mysql.jdbc.Driver");
				url = "jdbc:mysql://173.194.85.198:3306/restaurant?user=root&password=helpMePlease312";
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
