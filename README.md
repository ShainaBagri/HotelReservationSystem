# HotelReservationSystem

Contributors:
Shaina Bagri
Piper Feldman

Compilation/Run Instructions:
Run the following command:
./gradlew run

Functionality:
UI is a menu in terminal with numbered options.
Used PreparedStatements to protect against SQL Insert attacks.
User can succesfully make and update reservations.
Conflicting dates will not be accepted.
Added an extra menu option to display the full reservations database (option 6)


Known Bugs/Deficiencies:
For FR1, the total price is not found when a room is reserved, basePrice is inserted for the rate
Not all error-handling for inputs completed such as a Room needing to be previously stored in the database.
For FR5, the output is given in multiple rows for each rather than one row with 13 columns.