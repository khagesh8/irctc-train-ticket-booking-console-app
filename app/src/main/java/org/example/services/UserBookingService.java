package org.example.services;

//package ticket.booking.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import org.example.entities.Ticket;
import org.example.entities.Train;
import org.example.entities.User;
import org.example.util.DBConnection;
import org.example.util.UserServiceUtil;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

//import static org.example.util.UserServiceUtil.readUsersFromFile;

public class UserBookingService{

//    private ObjectMapper objectMapper = new ObjectMapper();

    private List<User> userList;

    private User user;

//    private final static String USER_FILE_PATH = "app/src/main/java/org/example/localDb/users.json";
//
//    private static final ObjectMapper objectMapper = new ObjectMapper();
//    private static final Gson gson = new Gson();
    public UserBookingService(User user) throws IOException {
        this.user = user;
//        loadUserListFromFile();
    }

//    public UserBookingService() throws IOException {
//        loadUserListFromFile();
//    }

//    public static List<Train> readTrains() {
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            return mapper.readValue(
//                    new File(USER_FILE_PATH),
//                    new TypeReference<List<Train>>() {}
//            );
//        } catch (Exception e) {
//            return new ArrayList<>();
//        }
//    }

//    public static void writeTrains(List<Train> trains) {
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            mapper.writeValue(new File(USER_FILE_PATH), trains);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    private void loadUserListFromFile() throws IOException {
//        File userFile = new File(USER_FILE_PATH);
//
//        if (!userFile.exists()) {
//            System.err.println("User file not found at: " + userFile.getAbsolutePath());
//            userList = new ArrayList<>();
//            // Create the directory if it doesn't exist
//            userFile.getParentFile().mkdirs();
//            saveUserListToFile();
//        } else {
//            System.out.println("Loading users from: " + userFile.getAbsolutePath());
//            try {
//                userList = objectMapper.readValue(userFile, new TypeReference<List<User>>() {});
//            } catch (Exception e) {
//                System.err.println("Error parsing user file: " + e.getMessage());
//                userList = new ArrayList<>();
//                saveUserListToFile();
//            }
//        }
////        userList = objectMapper.readValue(new File(USER_FILE_PATH), new TypeReference<List<User>>() {});
//    }

    public static void login(String username, String password) {

        String sql = "SELECT user_id, password_hash FROM users WHERE username = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String hash = rs.getString("password_hash");

                if (BCrypt.checkpw(password, hash)) {
//                    User user = new User();
////                    user.setUserId(rs.getObject("user_id").toString());
//                    user.setUserId(rs.getObject("user_id").toString());

                    User user = new User();
                    user.setUserId(rs.getObject("user_id").toString()); // 🔴 REQUIRED
                    user.setName(username);

// DEBUG (temporary)
                    System.out.println("DEBUG: userId after login = " + user.getUserId());
//                    user.setName(username);

                    System.out.println("Logged in userId = " + user.getUserId());
                    UserServiceUtil.setCurrentUser(user);
                    System.out.println("Login successful.");
                } else {
                    System.out.println("Invalid password.");
                }
            } else {
                System.out.println("User not found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void signup(String username, String plainPassword) {

        String sql = "INSERT INTO users (user_id, username, password_hash) VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            UUID userId = UUID.randomUUID();
            String hashed = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

            ps.setObject(1, userId);
            ps.setString(2, username);
            ps.setString(3, hashed);
            ps.executeUpdate();

            System.out.println("Signup successful.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


//    private void saveUserListToFile() throws IOException {
//        File usersFile = new File(USER_FILE_PATH);
//        usersFile.getParentFile().mkdirs(); // Create directories if they don't exist
//        objectMapper.writeValue(usersFile, userList);
//    }

//    public void fetchBookings(){
//
//        System.out.println("fetchBooking");
//        if (user == null) {
//            System.out.println("No user logged in.");
//            return;
//        }
//
//        Optional<User> userFetched = userList.stream().filter(user1 -> {
//            return user1.getName().equals(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
//        }).findFirst();
//        if(userFetched.isPresent()){
//            userFetched.get().printTickets();
//        }
//        else {
//            System.out.println("User not found or invalid credentials.");
//        }
//    }




    public static void fetchBookings(User currentUser) {

        if (currentUser == null) {
            System.out.println("Please login first.");
            return;
        }

        String sql = "SELECT * FROM tickets WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, UUID.fromString(currentUser.getUserId()));
            ResultSet rs = ps.executeQuery();

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println(
                        "Ticket ID: " + rs.getString("ticket_id") +
                                ", Train: " + rs.getString("train_id") +
                                ", From: " + rs.getString("source") +
                                ", To: " + rs.getString("destination")
                );
            }

            if (!found) {
                System.out.println("No bookings found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



//    public void fetchBookings() {
//        List<User> users = UserServiceUtil.readUsersFromFile();
//
//        for (User u : users) {
//            if (u.getUserId().equals(user.getUserId())) {
//                user.setTicketsBooked(u.getTicketsBooked());
//                break;
//            }
//        }
//
//        user.printTickets();
//    }


    //    public Boolean cancelBooking(String ticketId){
//         todo: Complete this function


    // ✅ CASE 6 IMPLEMENTED
//    public static void cancelBooking(User currentUser, String ticketId) {
//        List<User> users = readUsersFromFile();
//
//        for (User u : users) {
//            if (u.getUserId().equals(currentUser.getUserId())) {
//                Iterator<Ticket> it = u.getTicketsBooked().iterator();
//                while (it.hasNext()) {
//                    Ticket t = it.next();
//                    if (t.getTicketId().equals(ticketId)) {
//                        it.remove();
//                        writeUsersToFile(users);
//                        System.out.println("Booking cancelled successfully.");
//                        return;
//                    }
//                }
//            }
//        }
//        System.out.println("Ticket not found.");
//    }

//    private static void writeUsersToFile(List<User> users) {
//        try {
//            File file = new File(USER_FILE_PATH);
//
//            // Create directories if they don't exist
//            file.getParentFile().mkdirs();
//
//            // Write updated users list to JSON
//            objectMapper.writeValue(file, users);
//
//        } catch (Exception e) {
//            System.err.println("Error writing users to file");
//            e.printStackTrace();
//        }
//    }

    // ✅ CALLED AFTER BOOKING
//    public static void saveBooking(User currentUser, Ticket ticket) {
//        List<User> users = readUsersFromFile();
//
//        for (User u : users) {
//            if (u.getUserId().equals(currentUser.getUserId())) {
//                u.getTicketsBooked().add(ticket);
//                break;
//            }
//        }
//        writeUsersToFile(users);
//    }

//public Boolean cancelBooking(String ticketId){
//    if (user == null || user.getTicketsBooked() == null) {
//        System.out.println("No user logged in or no tickets found.");
//        return Boolean.FALSE;
//    }
//
//    Scanner s = new Scanner(System.in);
//    System.out.println("Enter the ticket id to cancel");
//    ticketId = s.next();
//
//    if (ticketId == null || ticketId.isEmpty()) {
//        System.out.println("Ticket ID cannot be null or empty.");
//        return Boolean.FALSE;
//    }
//
//    String finalTicketId = ticketId;
//    boolean removed = user.getTicketsBooked().removeIf(ticket -> ticket.getTicketId().equals(finalTicketId));
//
//    if (removed) {
//        System.out.println("Ticket with ID " + ticketId + " has been canceled.");
//        return Boolean.TRUE;
//    } else {
//        System.out.println("No ticket found with ID " + ticketId);
//        return Boolean.FALSE;
//    }
//}


    public static void bookSeat(User user, String trainId, int row, int seat,
                                String source, String destination) {

//        if (user == null || user.getUserId() == null) {
//            throw new IllegalStateException("User not logged in properly (userId is null)");
//        }


        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            // lock seat
            PreparedStatement check = con.prepareStatement(
                    "SELECT is_booked FROM train_seats WHERE train_id=? AND row_no=? AND seat_no=? FOR UPDATE");
            check.setString(1, trainId);
            check.setInt(2, row);
            check.setInt(3, seat);

            ResultSet rs = check.executeQuery();
            if (!rs.next() || rs.getBoolean("is_booked")) {
                System.out.println("Seat already booked.");
                con.rollback();
                return;
            }

            // mark seat booked
            PreparedStatement updateSeat = con.prepareStatement(
                    "UPDATE train_seats SET is_booked=true WHERE train_id=? AND row_no=? AND seat_no=?");
            updateSeat.setString(1, trainId);
            updateSeat.setInt(2, row);
            updateSeat.setInt(3, seat);
            updateSeat.executeUpdate();

            // insert ticket
            PreparedStatement insertTicket = con.prepareStatement(
                    "INSERT INTO tickets VALUES (?, ?, ?, ?, ?, NOW(), ?, ?)");
            insertTicket.setObject(1, UUID.randomUUID());
            insertTicket.setObject(2, UUID.fromString(user.getUserId()));
            insertTicket.setString(3, trainId);
            insertTicket.setString(4, source);
            insertTicket.setString(5, destination);
            insertTicket.setInt(6, row);
            insertTicket.setInt(7, seat);
            insertTicket.executeUpdate();

            con.commit();
            System.out.println("Seat booked successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cancelBooking(User user, UUID ticketId) {

        if (user == null || user.getUserId() == null) {
            System.out.println("Invalid user. Please login first.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            // Get seat information before deleting
            PreparedStatement seatQuery = con.prepareStatement(
                    "SELECT train_id, seat_row, seat_no FROM tickets WHERE ticket_id=? AND user_id=?");
            seatQuery.setObject(1, ticketId);
            seatQuery.setObject(2, UUID.fromString(user.getUserId()));

            ResultSet rs = seatQuery.executeQuery();
            if (!rs.next()) {
                System.out.println("Ticket not found or does not belong to you.");
                con.rollback();
                return;
            }

            String trainId = rs.getString("train_id");
            int row = rs.getInt("seat_row");
            int seat = rs.getInt("seat_no");

            // Delete the ticket
            PreparedStatement deleteTicket = con.prepareStatement(
                    "DELETE FROM tickets WHERE ticket_id=?");
            deleteTicket.setObject(1, ticketId);
            int deletedRows = deleteTicket.executeUpdate();

            if (deletedRows == 0) {
                System.out.println("Failed to delete ticket.");
                con.rollback();
                return;
            }

            // Free up the seat
            PreparedStatement freeSeat = con.prepareStatement(
                    "UPDATE train_seats SET is_booked=false WHERE train_id=? AND row_no=? AND seat_no=?");
            freeSeat.setString(1, trainId);
            freeSeat.setInt(2, row);
            freeSeat.setInt(3, seat);
            int updatedRows = freeSeat.executeUpdate();

            if (updatedRows == 0) {
                System.out.println("Warning: Seat was not marked as booked in the system.");
            }

            con.commit();
            System.out.println("Booking cancelled successfully.");
            System.out.println("Freed seat: Row " + row + ", Seat " + seat + " on Train " + trainId);

        } catch (Exception e) {
            System.err.println("Error cancelling booking: " + e.getMessage());
            e.printStackTrace();
        }
    }


//    public List<Train> getTrains(String source, String destination){
//            try{
//                TrainService trainService = new TrainService();
//                return trainService.searchTrains(source, destination);
//            }catch(IOException ex){
//                System.err.println("Error getting trains: " + ex.getMessage());
//                ex.printStackTrace();
//                return new ArrayList<>();
//            }
//        }

//        public List<List<Integer>> fetchSeats(Train train){
//            if (train == null || train.getSeats() == null) {
//                return new ArrayList<>();
//            }
//            return train.getSeats();
//        }

//        public Boolean bookTrainSeat(Train train, int row, int seat) {
//            try{
//                TrainService trainService = new TrainService();
//                List<List<Integer>> seats = train.getSeats();
//                if (row >= 0 && row < seats.size() && seat >= 0 && seat < seats.get(row).size()) {
//                    if (seats.get(row).get(seat) == 0) {
//                        seats.get(row).set(seat, 1);
//                        train.setSeats(seats);
//                        trainService.addTrain(train);
//                        return true; // Booking successful
//                    } else {
//                        return false; // Seat is already booked
//                    }
//                } else {
//                    return false; // Invalid row or seat index
//                }
//            }catch (IOException ex){
//                System.err.println("Error booking seat: " + ex.getMessage());
//                ex.printStackTrace();
//                return Boolean.FALSE;
//            }
//        }
    }
