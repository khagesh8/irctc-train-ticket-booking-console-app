//package org.example.services;
//
//public class TrainService {
//}



package org.example.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entities.Train;
import org.example.util.DBConnection;

import java.io.*;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrainService {

    private List<Train> trainList;
    private ObjectMapper objectMapper = new ObjectMapper();
//    private static final String TRAIN_DB = "localdb/trains.json";
//    private static final Gson gson = new Gson();
    private static final String TRAIN_DB_PATH = "app/src/main/java/org/example/localDb/trains.json";

//    public TrainService() {
//        File trains = new File(TRAIN_DB_PATH);
//        if (!trains.exists()) {
//            System.err.println("Train file not found at: " + trains.getAbsolutePath());
//            trainList = new ArrayList<>();
//            // Create the directory if it doesn't exist
//            trains.getParentFile().mkdirs();
//            saveTrainListToFile();
//        } else {
//            System.out.println("Loading trains from: " + trains.getAbsolutePath());
//            trainList = objectMapper.readValue(trains, new TypeReference<List<Train>>() {});
//        }
////        trainList = objectMapper.readValue(trains, new TypeReference<List<Train>>() {});
//    }


    public static List<Train> readTrains() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(
                    new File(TRAIN_DB_PATH),
                    new TypeReference<List<Train>>() {}
            );
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static void writeTrains(List<Train> trains) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File(TRAIN_DB_PATH), trains);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public List<Train> searchTrains(String source, String destination) {

        String sql = " SELECT DISTINCT t.train_id, t.train_no FROM trains t JOIN train_stations s1 ON t.train_id = s1.train_id  JOIN train_stations s2 ON t.train_id = s2.train_id  WHERE s1.station_name = ?  AND s2.station_name = ?  AND s1.station_order < s2.station_order"
    ;

        List<Train> result = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, source.toLowerCase());
            ps.setString(2, destination.toLowerCase());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Train t = new Train();
                t.setTrainId(rs.getString("train_id"));
                t.setTrainNo(rs.getString("train_no"));
                result.add(t);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


    public void addTrain(Train newTrain) {
        // Check if a train with the same trainId already exists
        Optional<Train> existingTrain = trainList.stream()
                .filter(train -> train.getTrainId().equalsIgnoreCase(newTrain.getTrainId()))
                .findFirst();

        if (existingTrain.isPresent()) {
            // If a train with the same trainId exists, update it instead of adding a new one
            updateTrain(newTrain);
        } else {
            // Otherwise, add the new train to the list
            trainList.add(newTrain);
            saveTrainListToFile();
        }
    }

    public void updateTrain(Train updatedTrain) {
        // Find the index of the train with the same trainId
        OptionalInt index = IntStream.range(0, trainList.size())
                .filter(i -> trainList.get(i).getTrainId().equalsIgnoreCase(updatedTrain.getTrainId()))
                .findFirst();

        if (index.isPresent()) {
            // If found, replace the existing train with the updated one
            trainList.set(index.getAsInt(), updatedTrain);
            saveTrainListToFile();
        } else {
            // If not found, treat it as adding a new train
            addTrain(updatedTrain);
        }
    }

    private void saveTrainListToFile() {
        try {
            File file = new File(TRAIN_DB_PATH);
            file.getParentFile().mkdirs(); // Create directories if they don't exist
            objectMapper.writeValue(file, trainList);
//            objectMapper.writeValue(new File(TRAIN_DB_PATH), trainList);
        } catch (IOException e) {
            System.err.println("Error saving train list: " + e.getMessage());
            e.printStackTrace();
//            e.printStackTrace(); // Handle the exception based on your application's requirements
        }
    }

    private boolean validTrain(Train train, String source, String destination) {
        List<String> stationOrder = train.getStations();

        int sourceIndex = stationOrder.indexOf(source.toLowerCase());
        int destinationIndex = stationOrder.indexOf(destination.toLowerCase());

        return sourceIndex != -1 && destinationIndex != -1 && sourceIndex < destinationIndex;
    }
}
