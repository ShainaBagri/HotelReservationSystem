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
			System.err.println("Error in Main");
			System.err.println("SQLException: " + e.getMessage());
		}
	}

	private void getUserInput() {
		Scanner scanner = new Scanner(System.in);
		Boolean quit = false;
		int codeCounter = 15000;
		while (!quit) {
			System.out.println(
					"Enter a number from the following:\n1. Get Rooms and Reservations\n4. Cancel Reservation\n5. Get Revenue Summary\n");
			String input = scanner.nextLine();
			if (input.equals("1")) {
				getRooms();
			} else if (input.equals("2")) {
				makeReservation(codeCounter);
				codeCounter += 1;
			} else if (input.equals("3")) {
				updateReservation();
			} else if (input.equals("4")) {
				cancelReservation();
			} else if (input.equals("5")) {
				getSummary();
			} else if (input.equals("Quit")) {
				quit = true;
			} else if (input.equals("6")) {
				getReservations();
			} else {
				System.out.println("Input was not a valid command.");
			}
		}
	}

	// FR1
	private void getRooms() {
		try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
			try (Statement stmt = conn.createStatement()) {
				String query = "SELECT * FROM lab7_rooms";
				ResultSet rs = stmt.executeQuery(query);
				while (rs.next()) {
					String rc = rs.getString("RoomCode");
					String rn = rs.getString("RoomName");
					int beds = rs.getInt("Beds");
					String bedType = rs.getString("bedType");
					int maxOcc = rs.getInt("maxOcc");
					float basePrice = rs.getFloat("basePrice");
					String decor = rs.getString("decor");
					System.out.println(
							rc + ' ' + rn + ' ' + beds + ' ' + bedType + ' ' + maxOcc + ' ' + basePrice + ' ' + decor);
				}
				System.out.println("----------------------\n");
			} catch (SQLException e) {
				System.err.println("Error.");
			}
		} catch (SQLException e) {
			System.err.println("Errorrrrr");
		}
	}

	// FR2
	private void makeReservation(int codeCounter) {
		try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {

			Scanner scanner = new Scanner(System.in);
			System.out.println("Enter Your First Name: ");
			String first = scanner.nextLine();
			System.out.println("Enter Your Last Name: ");
			String last = scanner.nextLine();
			System.out.println("Enter The Room Code: ");
			String code = scanner.nextLine();
			System.out.println("Enter your arrival date (YYYY-MM-DD): ");
			String arrival = scanner.nextLine();
			System.out.println("Enter your departure date (YYYY-MM-DD): ");
			String departure = scanner.nextLine();
			System.out.println("Enter the number of children: ");
			int kids = scanner.nextInt();
			System.out.println("Enter the number of adults: ");
			int adults = scanner.nextInt();

			List<Object> params1 = new ArrayList<Object>();
			StringBuilder sb1 = new StringBuilder("SELECT * FROM lab7_reservations ");
			sb1.append("WHERE Room = ?");
			params1.add(code);
			sb1.append(" AND 0 >= DATEDIFF(day, CheckIn, ?)");
			params1.add(departure);
			sb1.append(" AND 0 <= DATEDIFF(day, Checkout, ?)");
			params1.add(arrival);

			try (PreparedStatement pstmt = conn.prepareStatement(sb1.toString())) {
				pstmt.setObject(1, code);
				pstmt.setObject(2, departure);
				pstmt.setObject(3, arrival);
				ResultSet rs = pstmt.executeQuery();
			} catch (SQLException e) {
				System.err.println("Error Updating SQL Statement");
				System.err.println("SQLException: " + e.getMessage());
			}

			List<Object> params = new ArrayList<Object>();
			StringBuilder sb = new StringBuilder(
					"INSERT INTO lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids) VALUES (");
			sb.append("?, ?, ?, ?, ?, ?, ?, ?)");
			params.add(codeCounter);
			params.add(first);
			params.add(last);
			params.add(code);
			params.add(arrival);
			params.add(departure);
			params.add(kids);
			params.add(adults);

			try (PreparedStatement pstmt2 = conn.prepareStatement(sb.toString())) {
				pstmt2.setObject(1, codeCounter);
				pstmt2.setObject(2, first);
				pstmt2.setObject(3, last);
				pstmt2.setObject(4, code);
				pstmt2.setObject(5, arrival);
				pstmt2.setObject(6, departure);
				pstmt2.setObject(7, kids);
				pstmt2.setObject(8, adults);
				pstmt2.executeUpdate();
				System.out.println("Reservation Successful");
			} catch (SQLException e) {
				System.err.println("Error Updating SQL Statement");
				System.err.println("SQLException: " + e.getMessage());
			}

			System.out.println("----------------------\n");
		} catch (SQLException e) {
			System.err.println("Error.");
		}
	}

	// FR3
	private void updateReservation() {
		try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
			Scanner scanner = new Scanner(System.in);
			System.out.println("Enter reservation code: ");
			Integer code = Integer.valueOf(scanner.nextLine());
			System.out.println("Enter First Name (or 'No Change'): ");
			String firstname = scanner.nextLine();
			System.out.println("Enter Last Name (or 'No Change'): ");
			String lastname = scanner.nextLine();
			System.out.println("Enter Begin Date (YYYY-MM-DD) (or 'No Change'): ");
			String checkin = scanner.nextLine();
			System.out.println("Enter End Date (YYYY-MM-DD) (or 'No Change'): ");
			String checkout = scanner.nextLine();
			System.out.println("Enter Number of Children (or 'No Change'): ");
			String kids = scanner.nextLine();
			System.out.println("Enter Number of Adults (or 'No Change'): ");
			String adults = scanner.nextLine();

			Boolean foundFirst = false;

			List<Object> params = new ArrayList<Object>();
			StringBuilder sb = new StringBuilder("UPDATE lab7_reservations SET");
			if (!"no change".equalsIgnoreCase(firstname)) {
				sb.append(" FirstName = ?");
				params.add(firstname);
				foundFirst = true;
			}
			if (!"no change".equalsIgnoreCase(lastname)) {
				if (foundFirst) {
					sb.append(",");
				}
				sb.append(" LastName = ?");
				params.add(lastname);
				foundFirst = true;
			}
			if (!"no change".equalsIgnoreCase(checkin)) {
				if (foundFirst) {
					sb.append(",");
				}
				sb.append(" CheckIn = ?");
				params.add(checkin);
				foundFirst = true;
			}
			if (!"no change".equalsIgnoreCase(checkout)) {
				if (foundFirst) {
					sb.append(",");
				}
				sb.append(" Checkout = ?");
				params.add(checkout);
				foundFirst = true;
			}
			if (!"no change".equalsIgnoreCase(kids)) {
				if (foundFirst) {
					sb.append(",");
				}
				sb.append(" Kids = ?");
				params.add(Integer.valueOf(kids));
				foundFirst = true;
			}
			if (!"no change".equalsIgnoreCase(adults)) {
				if (foundFirst) {
					sb.append(",");
				}
				sb.append(" Adults = ?");
				params.add(Integer.valueOf(adults));
			}
			sb.append(" WHERE CODE = ?");
			params.add(code);

			int needCheckDate = 0;
			StringBuilder sbCheckDate = new StringBuilder("");

			if (!"no change".equalsIgnoreCase(checkout) && !"no change".equalsIgnoreCase(checkin)) {
				sbCheckDate.append(
						"SELECT * FROM lab7_reservations WHERE Room = (SELECT Room FROM lab7_reservations WHERE Code = ?)"
								+ " AND NOT (0 >= DATEDIFF(day, CheckIn, ?) OR 0 <= DATEDIFF(day, Checkout, ?))");
				needCheckDate = 1;
			} else if (!"no change".equalsIgnoreCase(checkout)) {
				sbCheckDate.append(
						"SELECT * FROM lab7_reservations WHERE Room = (SELECT Room FROM lab7_reservations WHERE Code = ?)"
								+ " AND NOT (0 >= DATEDIFF(day, CheckIn, ?))");
				needCheckDate = 2;
			} else if (!"no change".equalsIgnoreCase(checkin)) {
				sbCheckDate.append(
						"SELECT * FROM lab7_reservations WHERE Room = (SELECT Room FROM lab7_reservations WHERE Code = ?)"
								+ " AND NOT (0 <= DATEDIFF(day, Checkout, ?))");
				needCheckDate = 3;
			}
			if (needCheckDate != 0) {
				try (PreparedStatement pstmtFind = conn.prepareStatement(sbCheckDate.toString())) {
					pstmtFind.setObject(1, code);
					if (needCheckDate == 1) {
						pstmtFind.setObject(2, checkout);
						pstmtFind.setObject(3, checkin);
					} else if (needCheckDate == 2) {
						pstmtFind.setObject(2, checkout);
					} else {
						pstmtFind.setObject(2, checkin);
					}

					try {
						ResultSet conflicting = pstmtFind.executeQuery();

						if (conflicting.next()) {
							System.out.println("Those dates are unavailable for that room.");
							return;
						}

					} catch (SQLException e) {
						System.err.println("Error Updating SQL Statement");
						System.err.println("SQLException: " + e.getMessage());
					}

				} catch (SQLException e) {
					System.err.println("Error Updating SQL Statement");
					System.err.println("SQLException: " + e.getMessage());
				}
			}

			try (PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {
				int i = 1;
				for (Object p : params) {
					pstmt.setObject(i++, p);
				}

				try {
					pstmt.executeUpdate();
					System.out.println("Change Successful");
				} catch (SQLException e) {
					System.err.println("Error Updating SQL Statement");
					System.err.println("SQLException: " + e.getMessage());
				}
			} catch (SQLException e) {
				System.err.println("Error Creating SQL Statement");
				System.err.println("SQLException: " + e.getMessage());
			}

		} catch (SQLException e) {
			System.err.println("Error Connecting to JDBC Driver");
			System.err.println("SQLException: " + e.getMessage());
		}
	}

	// FR4
	private void cancelReservation() {
		try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
			Scanner scanner = new Scanner(System.in);
			System.out.println("Enter reservation code: ");
			Integer code = Integer.valueOf(scanner.nextLine());
			System.out.println("Are you sure you want to cancel your reservation? Type 0 for No, 1 for Yes: ");
			String confirm = scanner.nextLine();

			if (confirm.equals("0")) {
				System.out.println("Cancellation Aborted");
				return;
			}

			StringBuilder sb = new StringBuilder("DELETE FROM lab7_reservations WHERE CODE = ?");

			try (PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {
				pstmt.setObject(1, code);

				try {
					pstmt.executeUpdate();
					System.out.println("Cancellation Successful");
				} catch (SQLException e) {
					System.err.println("Error Updating SQL Statement");
					System.err.println("SQLException: " + e.getMessage());
				}
			} catch (SQLException e) {
				System.err.println("Error Creating SQL Statement");
				System.err.println("SQLException: " + e.getMessage());
			}
		} catch (SQLException e) {
			System.err.println("Error Connecting to JDBC Driver");
			System.err.println("SQLException: " + e.getMessage());
		}
	}

	// FR5
	private void getSummary() {
		try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
			try (Statement stmt = conn.createStatement()) {

				String monthRev = "SELECT Room, Month, SUM(Price) AS monthRevenue " + "FROM ("
						+ " SELECT CODE, Room, MONTHNAME(CheckOut) AS Month, DATEDIFF(day, CheckIn, Checkout) * Rate AS Price"
						+ " FROM lab7_reservations " + ") AS theTable" + " GROUP BY Room, Month" + " ORDER BY Room";
				String totRev = "SELECT Room, SUM(monthRevenue) AS totalRevenue" + " FROM ("
						+ " SELECT Room, Month, SUM(Price) AS monthRevenue" + " FROM ("
						+ "SELECT CODE, Room, MONTHNAME(CheckOut) AS Month, DATEDIFF(day, CheckIn, Checkout) * Rate AS Price"
						+ " FROM lab7_reservations" + ") AS otherTable" + " GROUP BY Room, Month" + " ORDER BY Room"
						+ ") AS thirdTable GROUP BY Room";
				String query = "SELECT A.Room, Month, monthRevenue, totalRevenue FROM (" + totRev + ") A NATURAL JOIN ("
						+ monthRev + ") B";
				ResultSet rs = stmt.executeQuery(query);

				while (rs.next()) {
					String room = rs.getString("Room");
					String month = rs.getString("Month");
					Float monthRevenue = rs.getFloat("monthRevenue");
					Float totRevenue = rs.getFloat("totRevenue");
					System.out.println(room + ' ' + month + ' ' + monthRevenue);
				}
				System.out.println("----------------------\n");

			} catch (SQLException e) {
				System.err.println("Error Creating SQL Statement");
				System.err.println("SQLException: " + e.getMessage());
			}
		} catch (SQLException e) {
			System.err.println("Error Connecting to JDBC Driver");
			System.err.println("SQLException: " + e.getMessage());
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
						"INSERT INTO lab7_rooms (RoomCode, RoomName, Beds, bedType, maxOcc, basePrice, decor) VALUES ('HBB', 'Hello Barbie Beds', 2, 'Queen', 4, 150.0, 'traditional')");
				stmt.execute(
						"INSERT INTO lab7_rooms (RoomCode, RoomName, Beds, bedType, maxOcc, basePrice, decor) VALUES ('ABC', 'Alphabet Room', 1, 'Full', 2, 115.0, 'modern')");
				stmt.execute(
						"INSERT INTO lab7_rooms (RoomCode, RoomName, Beds, bedType, maxOcc, basePrice, decor) VALUES ('JAZ', 'Jazz Room', 3, 'Full', 5, 175.0, 'modern')");

				stmt.execute(
						"INSERT INTO lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids) VALUES (10105, 'HBB', '2010-10-23', '2010-10-25', 155, 'SELBIG', 'CONRAD', 1, 0)");
				stmt.execute(
						"INSERT INTO lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids) VALUES (14994, 'HBB', '2010-10-28', '2010-10-30', 170, 'GREENBERG', 'TROY', 1, 0)");
				stmt.execute(
						"INSERT INTO lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids) VALUES (14995, 'HBB', '2010-11-01', '2010-11-22', 150, 'KATE', 'MARY', 2, 1)");
				stmt.execute(
						"INSERT INTO lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids) VALUES (14996, 'HBB', '2010-01-02', '2010-01-17', 150, 'SMITH', 'JOY', 1, 2)");
				stmt.execute(
						"INSERT INTO lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids) VALUES (14997, 'HBB', '2010-11-01', '2010-11-22', 150, 'BERN', 'JEREMY', 1, 1)");
				stmt.execute(
						"INSERT INTO lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids) VALUES (14998, 'ABC', '2010-11-01', '2010-11-22', 150, 'BERNER', 'BOB', 1, 0)");
				stmt.execute(
						"INSERT INTO lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids) VALUES (14999, 'ABC', '2010-07-07', '2010-07-23', 125, 'LEE', 'ROY', 1, 0)");

				ResultSet rs = stmt.executeQuery("SELECT * FROM lab7_rooms");
				while (rs.next()) {
					String rc = rs.getString("RoomCode");
					String rn = rs.getString("RoomName");
					int beds = rs.getInt("Beds");
					String bedType = rs.getString("bedType");
					int maxOcc = rs.getInt("maxOcc");
					float basePrice = rs.getFloat("basePrice");
					String decor = rs.getString("decor");

					System.out.println(
							rc + ' ' + rn + ' ' + beds + ' ' + bedType + ' ' + maxOcc + ' ' + basePrice + ' ' + decor);
				}
			}
		}
	}

	private void getReservations() {
		try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
			try (Statement stmt = conn.createStatement()) {
				ResultSet rs = stmt.executeQuery("SELECT * FROM lab7_reservations");
				while (rs.next()) {
					Integer code = rs.getInt("CODE");
					String room = rs.getString("Room");
					String checkin = rs.getString("CheckIn");
					String checkout = rs.getString("Checkout");
					int rate = rs.getInt("Rate");
					String lastname = rs.getString("LastName");
					String firstname = rs.getString("FirstName");
					int adults = rs.getInt("Adults");
					int kids = rs.getInt("Kids");
					System.out.println(code + ' ' + room + ' ' + checkin + ' ' + checkout + ' ' + rate + ' ' + lastname
							+ ' ' + firstname + ' ' + adults + ' ' + kids);
				}
			} catch (SQLException e) {
				System.err.println("Error Creating SQL Statement");
				System.err.println("SQLException: " + e.getMessage());
			}
		} catch (SQLException e) {
			System.err.println("Error Connecting to JDBC Driver");
			System.err.println("SQLException: " + e.getMessage());
		}
	}

}
