import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import java.util.Map;
import java.util.Scanner;
import java.util.LinkedHashMap;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

/*
 * Introductory JDBC examples based loosely on the BAKERY dataset from CSC 365 labs.
 */
public class InnReservations {

	private final String JDBC_URL = "jdbc:h2:~/csc365_lab7";
	private final String JDBC_USER = "";
	private final String JDBC_PASSWORD = "";

	public static void main(String[] args) {
		try {
			InnReservations lab7 = new InnReservations();
			lab7.initDb();
			lab7.getUserInput();
		} catch (SQLException e) {
			System.err.println("SQLException: " + e.getMessage());
		}
	}

	private void getUserInput() {
		Scanner scanner = new Scanner(System.in);
		Boolean quit = false;
		while (!quit) {
			System.out.println("Enter a command...");
			String user = scanner.nextLine();
			if (user.equals("Get Rooms and Reservations")) {
				getRooms();
			} else if (user.equals("Quit")) {
				quit = true;
			}
		}
	}

	// FR1
	private void getRooms() {
		try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
			try (Statement stmt = conn.createStatement()) {
				ResultSet rs = stmt.executeQuery("SELECT * FROM lab7_rooms");
				while (rs.next()) {
					String rc = rs.getString("RoomCode");
					String rn = rs.getString("RoomName");
					int beds = rs.getInt("Beds");
					String bedType = rs.getString("bedType");
					int maxOcc = rs.getInt("maxOcc");
					float basePrice = rs.getFloat("basePrice");
					String decor = rs.getString("decor");
					System.out.print(
							rc + ' ' + rn + ' ' + beds + ' ' + bedType + ' ' + maxOcc + ' ' + basePrice + ' ' + decor);
				}
				System.out.println("----------------------");
			} catch (SQLException e) {
				System.out.println("Error.");
			}
		} catch (SQLException e) {
			System.out.println("Errorrrrr");
		}
	}

	// Demo1 - Establish JDBC connection, execute DDL statement
	private void demo1() throws SQLException {

		// Step 0: Load JDBC Driver
		// No longer required as of JDBC 2.0 / Java 6
		try {
			Class.forName("org.h2.Driver");
			System.out.println("H2 JDBC Driver loaded");
		} catch (ClassNotFoundException ex) {
			System.err.println("Unable to load JDBC Driver");
			System.exit(-1);
		}

		// Step 1: Establish connection to RDBMS
		try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
			// Step 2: Construct SQL statement
			String sql = "ALTER TABLE hp_goods ADD COLUMN AvailUntil DATE";

			// Step 3: (omitted in this example) Start transaction

			try (Statement stmt = conn.createStatement()) {

				// Step 4: Send SQL statement to DBMS
				boolean exRes = stmt.execute(sql);

				// Step 5: Handle results
				System.out.format("Result from ALTER: %b %n", exRes);
			}

			// Step 6: (omitted in this example) Commit or rollback transaction
		}
		// Step 7: Close connection (handled by try-with-resources syntax)
	}

	// Demo2 - Establish JDBC connection, execute SELECT query, read & print result
	private void demo2() throws SQLException {

		// Step 1: Establish connection to RDBMS
		try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
			// Step 2: Construct SQL statement
			String sql = "SELECT * FROM hp_goods";

			// Step 3: (omitted in this example) Start transaction

			// Step 4: Send SQL statement to DBMS
			try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

				// Step 5: Receive results
				while (rs.next()) {
					String flavor = rs.getString("Flavor");
					String food = rs.getString("Food");
					float price = rs.getFloat("Price");
					java.sql.Date availUntil = rs.getDate("AvailUntil");
					System.out.format("%s %s ($%.2f) %s %n", flavor, food, price, availUntil);
				}
			}

			// Step 6: (omitted in this example) Commit or rollback transaction
		}
		// Step 7: Close connection (handled by try-with-resources syntax)
	}

	// Demo3 - Establish JDBC connection, execute DML query (UPDATE)
	// -------------------------------------------
	// Never (ever) write database code like this!
	// -------------------------------------------
	private void demo3() throws SQLException {

		demo2(); // print contents of goods table

		// Step 1: Establish connection to RDBMS
		try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
			// Step 2: Construct SQL statement
			Scanner scanner = new Scanner(System.in);
			System.out.print("\n Enter a flavor: ");
			String flavor = scanner.nextLine();
			System.out.format("\n Until what date will %s be available (YYYY-MM-DD)? ", flavor);
			String availUntilDate = scanner.nextLine();

			// -------------------------------------------
			// Never (ever) write database code like this!
			// -------------------------------------------
			String updateSql = "UPDATE hp_goods SET AvailUntil = '" + availUntilDate + "' " + "WHERE Flavor = '"
					+ flavor + "'";

			// Step 3: (omitted in this example) Start transaction

			try (Statement stmt = conn.createStatement()) {

				// Step 4: Send SQL statement to DBMS
				int rowCount = stmt.executeUpdate(updateSql);

				// Step 5: Handle results
				System.out.format("Updated %d records for %s pastries%n", rowCount, flavor);
			}

			// Step 6: (omitted in this example) Commit or rollback transaction

		}
		// Step 7: Close connection (handled implcitly by try-with-resources syntax)

		demo2(); // print contents of goods table

	}

	// Demo4 - Establish JDBC connection, execute DML query (UPDATE) using
	// PreparedStatement / transaction
	private void demo4() throws SQLException {

		// Step 1: Establish connection to RDBMS
		try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
			// Step 2: Construct SQL statement
			Scanner scanner = new Scanner(System.in);
			System.out.print("Enter a flavor: ");
			String flavor = scanner.nextLine();
			System.out.format("Until what date will %s be available (YYYY-MM-DD)? ", flavor);
			LocalDate availDt = LocalDate.parse(scanner.nextLine());

			String updateSql = "UPDATE hp_goods SET AvailUntil = ? WHERE Flavor = ?";

			// Step 3: Start transaction
			conn.setAutoCommit(false);

			try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

				// Step 4: Send SQL statement to DBMS
				pstmt.setDate(1, java.sql.Date.valueOf(availDt));
				pstmt.setString(2, flavor);
				int rowCount = pstmt.executeUpdate();

				// Step 5: Handle results
				System.out.format("Updated %d records for %s pastries%n", rowCount, flavor);

				// Step 6: Commit or rollback transaction
				conn.commit();
			} catch (SQLException e) {
				conn.rollback();
			}

		}
		// Step 7: Close connection (handled implcitly by try-with-resources syntax)
	}

	// Demo5 - Construct a query using PreparedStatement
	private void demo5() throws SQLException {

		// Step 1: Establish connection to RDBMS
		try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
			Scanner scanner = new Scanner(System.in);
			System.out.print("Find pastries with price <=: ");
			Double price = Double.valueOf(scanner.nextLine());
			System.out.print("Filter by flavor (or 'Any'): ");
			String flavor = scanner.nextLine();

			List<Object> params = new ArrayList<Object>();
			params.add(price);
			StringBuilder sb = new StringBuilder("SELECT * FROM hp_goods WHERE price <= ?");
			if (!"any".equalsIgnoreCase(flavor)) {
				sb.append(" AND Flavor = ?");
				params.add(flavor);
			}

			try (PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {
				int i = 1;
				for (Object p : params) {
					pstmt.setObject(i++, p);
				}

				try (ResultSet rs = pstmt.executeQuery()) {
					System.out.println("Matching Pastries:");
					int matchCount = 0;
					while (rs.next()) {
						System.out.format("%s %s ($%.2f) %n", rs.getString("Flavor"), rs.getString("Food"),
								rs.getDouble("price"));
						matchCount++;
					}
					System.out.format("----------------------%nFound %d match%s %n", matchCount,
							matchCount == 1 ? "" : "es");
				}
			}

		}
	}

	private void initDb() throws SQLException {
		try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
			try (Statement stmt = conn.createStatement()) {
				stmt.execute("DROP TABLE IF EXISTS lab7_reservations");
				stmt.execute("DROP TABLE IF EXISTS lab7_rooms");
				stmt.execute(
						"CREATE TABLE lab7_rooms (RoomCode char(5) PRIMARY KEY, RoomName varchar(30), Beds int(11), bedType varchar(8), maxOcc int(11), basePrice float, decor varchar(20), UNIQUE(RoomName))");
				stmt.execute(
						"CREATE TABLE lab7_reservations (CODE int(11) PRIMARY KEY, Room char(5), CheckIn DATE, Checkout DATE, Rate float, LastName varchar(15), FirstName varchar(15), Adults int(11), Kids int(11), FOREIGN KEY (Room) REFERENCES lab7_rooms (RoomCode))");
				stmt.execute(
						"INSERT INTO lab7_rooms (RoomCode, RoomName, Beds, bedType, maxOcc, basePrice, decor) VALUES ('HBB', 'Hello Barbie Brie', 2, 'Queen', 4, 100.0, 'traditional')");

				stmt.execute(
						"INSERT INTO lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids) VALUES (10105, 'HBB', '2010-10-23', '2010-10-25', 100, 'SELBIG', 'CONRAD', 1, 0)");

				ResultSet rs = stmt.executeQuery("SELECT * FROM lab7_rooms");
				while (rs.next()) {
					String rc = rs.getString("RoomCode");
					String rn = rs.getString("RoomName");
					int beds = rs.getInt("Beds");
					String bedType = rs.getString("bedType");
					int maxOcc = rs.getInt("maxOcc");
					float basePrice = rs.getFloat("basePrice");
					String decor = rs.getString("decor");

					System.out.print(
							rc + ' ' + rn + ' ' + beds + ' ' + bedType + ' ' + maxOcc + ' ' + basePrice + ' ' + decor);
				}
			}
		}
	}

}
