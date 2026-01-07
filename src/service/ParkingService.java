package service;

import java.sql.Timestamp;
import java.util.Calendar;


public class ParkingService {

    private final double CAR_RATE = 50.0;  // per hour
    private final double BIKE_RATE = 20.0; // per hour

    public boolean isValidVehicleNumber(String number) {
        if (number == null) return false;
        String t = number.trim();
        return t.length() >= 3 && t.length() <= 20;
    }

    public boolean isValidVehicleType(String type) {
        if (type == null) return false;
        return type.equalsIgnoreCase("car") || type.equalsIgnoreCase("bike");
    }

    public Timestamp nowTs() {
        return new Timestamp(System.currentTimeMillis());
    }

    public double calculateHours(Timestamp entry, Timestamp exit) {
        if (entry == null || exit == null) return 0;
        long ms = exit.getTime() - entry.getTime();
        return ms / (1000.0 * 60 * 60); // fractional hours
    }

    public double calculateFee(double hours, String vehicleType) {
        double rate = BIKE_RATE;
        if (vehicleType == null) rate = (CAR_RATE + BIKE_RATE) / 2;
        else if (vehicleType.equalsIgnoreCase("car")) rate = CAR_RATE;
        else if (vehicleType.equalsIgnoreCase("bike")) rate = BIKE_RATE;
        if (hours < 0) hours = 0;
        return Math.round(hours * rate * 100.0) / 100.0;
    }

    // get today's date range [start, end) for report
    public Timestamp[] todayRange() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY,0); c.set(Calendar.MINUTE,0); c.set(Calendar.SECOND,0); c.set(Calendar.MILLISECOND,0);
        Timestamp start = new Timestamp(c.getTimeInMillis());
        c.add(Calendar.DAY_OF_MONTH,1);
        Timestamp end = new Timestamp(c.getTimeInMillis());
        return new Timestamp[]{start,end};
    }

    // get this month's date range [start, end) for report
    public Timestamp[] monthRange() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH,1); c.set(Calendar.HOUR_OF_DAY,0); c.set(Calendar.MINUTE,0); c.set(Calendar.SECOND,0); c.set(Calendar.MILLISECOND,0);
        Timestamp start = new Timestamp(c.getTimeInMillis());
        c.add(Calendar.MONTH,1);
        Timestamp end = new Timestamp(c.getTimeInMillis());
        return new Timestamp[]{start,end};
    }

    public void displayWelcomeMessage() {
        System.out.println("=== Vehicle Parking Slot Management System ===");
    }
}


    