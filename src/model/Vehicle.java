package model;

import java.sql.Timestamp;

public class Vehicle {
    private int id;
    private String vehicleNumber;
    private String vehicleType;
    private int slotNumber;
    private Timestamp entryTime;
    private Timestamp exitTime;
    private double parkingHours;
    private double fee;

    public Vehicle() {}

    public Vehicle(String vehicleNumber, String vehicleType, int slotNumber) {
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.slotNumber = slotNumber;
    }

    // getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public int getSlotNumber() { return slotNumber; }
    public void setSlotNumber(int slotNumber) { this.slotNumber = slotNumber; }
    public Timestamp getEntryTime() { return entryTime; }
    public void setEntryTime(Timestamp entryTime) { this.entryTime = entryTime; }
    public Timestamp getExitTime() { return exitTime; }
    public void setExitTime(Timestamp exitTime) { this.exitTime = exitTime; }
    public double getParkingHours() { return parkingHours; }
    public void setParkingHours(double parkingHours) { this.parkingHours = parkingHours; }
    public double getFee() { return fee; }
    public void setFee(double fee) { this.fee = fee; }

    @Override
    public String toString() {
        return String.format("ID:%d | No:%s | Type:%s | Slot:%d | In:%s | Out:%s | Hours:%.2f | Fee:%.2f",
                id,
                vehicleNumber,
                vehicleType,
                slotNumber,
                entryTime == null ? "N/A" : entryTime.toString(),
                exitTime == null ? "N/A" : exitTime.toString(),
                parkingHours,
                fee);
    }
}
