package es.usc.citius.servando.calendula.database;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.PickupInfo;
import es.usc.citius.servando.calendula.persistence.Schedule;

/**
 * Created by joseangel.pineiro
 */
public class MedicineDao extends GenericDao<Medicine, Long> {


    public MedicineDao(DatabaseHelper db) {
        super(db);
    }

    @Override
    public Dao<Medicine, Long> getConcreteDao() {
        try {
            return dbHelper.getMedicinesDao();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating medicines dao", e);
        }
    }

    @Override
    public void fireEvent() {
        CalendulaApp.eventBus().post(PersistenceEvents.MEDICINE_EVENT);
    }

    public void deleteCascade(final Medicine m, boolean fireEvent) {

        DB.transaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                List<Schedule> schedules = Schedule.findByMedicine(m);
                for (Schedule s : schedules) {
                    s.deleteCascade();
                }
                List<PickupInfo> pickups = DB.pickups().findByMedicine(m);
                for (PickupInfo p : pickups) {
                    DB.pickups().remove(p);
                }
                DB.medicines().remove(m);
                return null;
            }
        });

        if (fireEvent) {
            fireEvent();
        }

    }

}
