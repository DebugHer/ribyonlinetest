package ng.riby.androidtest;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {LocationModel.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LocationDao getLocationDAO();
    private static AppDatabase INSTANCE;


    public static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "LocationDb")
                            // allow queries on the main thread.
                            // This is not recommended in production apps
                            //i could have used rxjava/coroutines
                            .allowMainThreadQueries()
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}