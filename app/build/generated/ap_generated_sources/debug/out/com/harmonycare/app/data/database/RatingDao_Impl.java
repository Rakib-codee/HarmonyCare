package com.harmonycare.app.data.database;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.harmonycare.app.data.model.Rating;
import java.lang.Class;
import java.lang.Double;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class RatingDao_Impl implements RatingDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Rating> __insertionAdapterOfRating;

  public RatingDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRating = new EntityInsertionAdapter<Rating>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `ratings` (`id`,`emergency_id`,`volunteer_id`,`elderly_id`,`rating`,`feedback`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Rating entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getEmergencyId());
        statement.bindLong(3, entity.getVolunteerId());
        statement.bindLong(4, entity.getElderlyId());
        statement.bindLong(5, entity.getRating());
        if (entity.getFeedback() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getFeedback());
        }
        statement.bindLong(7, entity.getTimestamp());
      }
    };
  }

  @Override
  public long insertRating(final Rating rating) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfRating.insertAndReturnId(rating);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public Rating getRatingByEmergency(final int emergencyId) {
    final String _sql = "SELECT * FROM ratings WHERE emergency_id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, emergencyId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfEmergencyId = CursorUtil.getColumnIndexOrThrow(_cursor, "emergency_id");
      final int _cursorIndexOfVolunteerId = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_id");
      final int _cursorIndexOfElderlyId = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_id");
      final int _cursorIndexOfRating = CursorUtil.getColumnIndexOrThrow(_cursor, "rating");
      final int _cursorIndexOfFeedback = CursorUtil.getColumnIndexOrThrow(_cursor, "feedback");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final Rating _result;
      if (_cursor.moveToFirst()) {
        _result = new Rating();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _result.setId(_tmpId);
        final int _tmpEmergencyId;
        _tmpEmergencyId = _cursor.getInt(_cursorIndexOfEmergencyId);
        _result.setEmergencyId(_tmpEmergencyId);
        final int _tmpVolunteerId;
        _tmpVolunteerId = _cursor.getInt(_cursorIndexOfVolunteerId);
        _result.setVolunteerId(_tmpVolunteerId);
        final int _tmpElderlyId;
        _tmpElderlyId = _cursor.getInt(_cursorIndexOfElderlyId);
        _result.setElderlyId(_tmpElderlyId);
        final int _tmpRating;
        _tmpRating = _cursor.getInt(_cursorIndexOfRating);
        _result.setRating(_tmpRating);
        final String _tmpFeedback;
        if (_cursor.isNull(_cursorIndexOfFeedback)) {
          _tmpFeedback = null;
        } else {
          _tmpFeedback = _cursor.getString(_cursorIndexOfFeedback);
        }
        _result.setFeedback(_tmpFeedback);
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _result.setTimestamp(_tmpTimestamp);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<Rating> getRatingsByVolunteer(final int volunteerId) {
    final String _sql = "SELECT * FROM ratings WHERE volunteer_id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, volunteerId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfEmergencyId = CursorUtil.getColumnIndexOrThrow(_cursor, "emergency_id");
      final int _cursorIndexOfVolunteerId = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_id");
      final int _cursorIndexOfElderlyId = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_id");
      final int _cursorIndexOfRating = CursorUtil.getColumnIndexOrThrow(_cursor, "rating");
      final int _cursorIndexOfFeedback = CursorUtil.getColumnIndexOrThrow(_cursor, "feedback");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final List<Rating> _result = new ArrayList<Rating>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Rating _item;
        _item = new Rating();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final int _tmpEmergencyId;
        _tmpEmergencyId = _cursor.getInt(_cursorIndexOfEmergencyId);
        _item.setEmergencyId(_tmpEmergencyId);
        final int _tmpVolunteerId;
        _tmpVolunteerId = _cursor.getInt(_cursorIndexOfVolunteerId);
        _item.setVolunteerId(_tmpVolunteerId);
        final int _tmpElderlyId;
        _tmpElderlyId = _cursor.getInt(_cursorIndexOfElderlyId);
        _item.setElderlyId(_tmpElderlyId);
        final int _tmpRating;
        _tmpRating = _cursor.getInt(_cursorIndexOfRating);
        _item.setRating(_tmpRating);
        final String _tmpFeedback;
        if (_cursor.isNull(_cursorIndexOfFeedback)) {
          _tmpFeedback = null;
        } else {
          _tmpFeedback = _cursor.getString(_cursorIndexOfFeedback);
        }
        _item.setFeedback(_tmpFeedback);
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _item.setTimestamp(_tmpTimestamp);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public Double getAverageRatingByVolunteer(final int volunteerId) {
    final String _sql = "SELECT AVG(rating) FROM ratings WHERE volunteer_id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, volunteerId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final Double _result;
      if (_cursor.moveToFirst()) {
        final Double _tmp;
        if (_cursor.isNull(0)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getDouble(0);
        }
        _result = _tmp;
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int getRatingCountByVolunteer(final int volunteerId) {
    final String _sql = "SELECT COUNT(*) FROM ratings WHERE volunteer_id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, volunteerId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
