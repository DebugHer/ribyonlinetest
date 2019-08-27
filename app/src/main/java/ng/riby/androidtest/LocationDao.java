package ng.riby.androidtest;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LocationModel locationModel);

    @Query("SELECT * FROM Location")
    LocationModel getLocation();
}
