import javax.swing.*;
import java.awt.*;
import java.util.*;

class Vehicle {
    private String id;
    private String color;
    private String type;
    private String licensePlate;
    private Location currentLocation; // only store current location
    @SuppressWarnings("unused")
    private boolean isActive;
    private double totalDistance;

    public Vehicle(String id, String color, String type, String licensePlate) {
        this.id = id;
        this.color = color;
        this.type = type;
        this.licensePlate = licensePlate;
        this.isActive = true;
        this.currentLocation = null; // no location yet
    }

    // Update the vehicle's location and calculate distance from previous location
    public void updateLocation(double latitude, double longitude) {
        Location newLocation = new Location(latitude, longitude);
        if (this.currentLocation != null) {
            totalDistance += this.currentLocation.distanceTo(newLocation);
        }
        this.currentLocation = newLocation;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public String getId() {
        return id;
    }

    public String getColor() {
        return color;
    }

    public String getType() {
        return type;
    }

    public String getLicensePlate() {
        return licensePlate;
    }
}

class Driver {
    private String id;
    private String name;
    private String licenseNumber;

    public Driver(String id, String name, String licenseNumber) {
        this.id = id;
        this.name = name;
        this.licenseNumber = licenseNumber;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }
}

class Location {
    private double latitude;
    private double longitude;

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Calculate distance between this and another location using the Haversine formula
    public double distanceTo(Location other) {
        double latDiff = Math.toRadians(other.latitude - this.latitude);
        double lonDiff = Math.toRadians(other.longitude - this.longitude);
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                   Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude)) *
                   Math.sin(lonDiff / 2) * Math.sin(lonDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371 * c; // Distance in kilometers
    }

    @Override
    public String toString() {
        return String.format("%.4f°N, %.4f°E", latitude, longitude);
    }
}

class FleetManagementSystem {
    private Map<String, Vehicle> vehicles;
    private Map<String, Driver> drivers;
    private static final String[] COLORS = {"Red", "Blue", "Green", "Black", "White", "Yellow", "Silver", "Brown", "Orange", "Purple"};
    private static final String[] TYPES = {"G-Wagon", "Toyota", "Ford", "Pontiac", "Maserati", "BMW", "Mercedes", "Tesla", "Chevrolet", "Audi"};

    public FleetManagementSystem() {
        this.vehicles = new HashMap<>();
        this.drivers = new HashMap<>();
    }

    // Register a vehicle with a randomly selected color, type, and license plate
    public void registerVehicle(String id) {
        Random rand = new Random();
        String color = COLORS[rand.nextInt(COLORS.length)];
        String type = TYPES[rand.nextInt(TYPES.length)];
        String licensePlate = "DOC" + (rand.nextInt(90000) + 10000);
        vehicles.put(id, new Vehicle(id, color, type, licensePlate));
    }

    public void registerDriver(String id, String name, String licenseNumber) {
        drivers.put(id, new Driver(id, name, licenseNumber));
    }

    public void updateVehicleLocation(String vehicleId, double latitude, double longitude) {
        Vehicle vehicle = vehicles.get(vehicleId);
        if (vehicle != null) {
            vehicle.updateLocation(latitude, longitude);
        }
    }

    public String generateVehicleReport(String vehicleId) {
        Vehicle vehicle = vehicles.get(vehicleId);
        if (vehicle != null) {
            String currentLocation = (vehicle.getCurrentLocation() != null)
                    ? vehicle.getCurrentLocation().toString()
                    : "No location recorded yet.";
            return "Vehicle ID: " + vehicle.getId() + "\n" +
                   "Color: " + vehicle.getColor() + "\n" +
                   "Type: " + vehicle.getType() + "\n" +
                   "License Plate: " + vehicle.getLicensePlate() + "\n" +
                   "Total Distance Covered: " + String.format("%.2f", vehicle.getTotalDistance()) + " km\n" +
                   "Current Location: " + currentLocation;
        }
        return "Vehicle not found.";
    }
}

public class FleetTrackingGUI extends JFrame {
    private FleetManagementSystem fleetSystem;
    private JTextArea displayArea;
    private int numVehicles;

    public FleetTrackingGUI(int numVehicles) {
        this.numVehicles = numVehicles;
        fleetSystem = new FleetManagementSystem();
        // Register vehicles with IDs "V1", "V2", ..., "V{numVehicles}"
        for (int i = 1; i <= numVehicles; i++) {
            String vehicleId = "V" + i;
            fleetSystem.registerVehicle(vehicleId);
        }

        setTitle("Fleet Tracking System");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        add(new JScrollPane(displayArea), BorderLayout.CENTER);

        // Create a control panel with interactive buttons
        JPanel buttonPanel = new JPanel();
        JButton trackButton = new JButton("Track Vehicles");
        JButton moveButton = new JButton("Move Vehicles");
        JButton exitButton = new JButton("Exit");

        // Button actions
        trackButton.addActionListener(e -> trackVehicles());
        moveButton.addActionListener(e -> moveVehicles());
        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(trackButton);
        buttonPanel.add(moveButton);
        buttonPanel.add(exitButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void trackVehicles() {
        displayArea.setText("Tracking Vehicles...\n\n");
        for (int i = 1; i <= numVehicles; i++) {
            String vehicleId = "V" + i;
            displayArea.append(fleetSystem.generateVehicleReport(vehicleId) + "\n\n");
        }
    }

    private void moveVehicles() {
        Random rand = new Random();
        for (int i = 1; i <= numVehicles; i++) {
            String vehicleId = "V" + i;
            // Generate random coordinates (simulate motion)
            double latitude = rand.nextDouble() * 10;
            double longitude = rand.nextDouble() * 10;
            fleetSystem.updateVehicleLocation(vehicleId, latitude, longitude);
        }
        displayArea.setText("Vehicles have moved! Track to see updated locations.\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String input = JOptionPane.showInputDialog("Enter number of vehicles to track (1-15):");
            try {
                int numVehicles = Integer.parseInt(input);
                if (numVehicles < 1 || numVehicles > 15) {
                    JOptionPane.showMessageDialog(null, "Please enter a number between 1 and 15.");
                    System.exit(0);
                }
                new FleetTrackingGUI(numVehicles).setVisible(true);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid input. Please enter a number.");
                System.exit(0);
            }
        });
    }
}
