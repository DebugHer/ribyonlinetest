package ng.riby.androidtest;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Location")
public class LocationModel {
    @PrimaryKey(autoGenerate = false)
    private int id = 0;
    Double startLongitude;
    Double startLatitude;
    Double stopLongitude;
    Double stopLatitude;

    public LocationModel(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(Double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public Double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(Double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public Double getStopLongitude() {
        return stopLongitude;
    }

    public void setStopLongitude(Double stopLongitude) {
        this.stopLongitude = stopLongitude;
    }

    public Double getStopLatitude() {
        return stopLatitude;
    }

    public void setStopLatitude(Double stopLatitude) {
        this.stopLatitude = stopLatitude;
    }
}
