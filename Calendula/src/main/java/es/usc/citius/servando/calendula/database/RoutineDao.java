package es.usc.citius.servando.calendula.database;

import android.util.Log;

import com.j256.ormlite.dao.Dao;

import org.joda.time.LocalTime;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;

/**
 * Created by joseangel.pineiro on 3/26/15.
 */
public class RoutineDao extends GenericDao<Routine, Long> {

    public static final String TAG = "RoutineDao";

    public RoutineDao(DatabaseHelper db) {
        super(db);
    }

    @Override
    public Dao<Routine, Long> getConcreteDao() {
        try {
            return dbHelper.getRoutinesDao();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating routines dao", e);
        }
    }

    @Override
    public void fireEvent() {
        CalendulaApp.eventBus().post(PersistenceEvents.ROUTINE_EVENT);
    }

    @Override
    public List<Routine> findAll() {
        try {
            return dao.queryBuilder().orderBy(Routine.COLUMN_TIME,true).query();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding models", e);
        }
    }

    public List<Routine> findInHour(int hour) {
        try {
            LocalTime time = new LocalTime(hour, 0);
            // get one hour interval [h:00, h:59:]
            String start = time.toString("kk:mm");
            String end = time.plusMinutes(59).toString("kk:mm");


            LocalTime endTime = time.plusMinutes(59);

            return queryBuilder().where()
                    .between(Routine.COLUMN_TIME, time, endTime)
                    .query();
        } catch (Exception e) {
            Log.e(TAG, "Error in findInHour", e);
            throw new RuntimeException(e);
        }
    }

    public void deleteCascade(final Routine r, boolean fireEvent) {

        DB.transaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Collection<ScheduleItem> items = r.scheduleItems();
                for (ScheduleItem i : items) {
                    i.deleteCascade();
                }
                DB.routines().remove(r);
                return null;
            }
        });

        if (fireEvent) {
            fireEvent();
        }
    }


}
