package com.harmonycare.app.data.database;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.harmonycare.app.data.model.Emergency;
import java.lang.Class;
import java.lang.Integer;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class EmergencyDao_Impl implements EmergencyDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Emergency> __insertionAdapterOfEmergency;

  private final EntityDeletionOrUpdateAdapter<Emergency> __updateAdapterOfEmergency;

  public EmergencyDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfEmergency = new EntityInsertionAdapter<Emergency>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `emergencies` (`id`,`elderly_id`,`elderly_name`,`elderly_contact`,`latitude`,`longitude`,`status`,`volunteer_id`,`volunteer_name`,`volunteer_contact`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Emergency entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getElderlyId());
        if (entity.getElderlyName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getElderlyName());
        }
        if (entity.getElderlyContact() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getElderlyContact());
        }
        statement.bindDouble(5, entity.getLatitude());
        statement.bindDouble(6, entity.getLongitude());
        if (entity.getStatus() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getStatus());
        }
        if (entity.getVolunteerId() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getVolunteerId());
        }
        if (entity.getVolunteerName() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getVolunteerName());
        }
        if (entity.getVolunteerContact() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getVolunteerContact());
        }
        statement.bindLong(11, entity.getTimestamp());
      }
    };
    this.__updateAdapterOfEmergency = new EntityDeletionOrUpdateAdapter<Emergency>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `emergencies` SET `id` = ?,`elderly_id` = ?,`elderly_name` = ?,`elderly_contact` = ?,`latitude` = ?,`longitude` = ?,`status` = ?,`volunteer_id` = ?,`volunteer_name` = ?,`volunteer_contact` = ?,`timestamp` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Emergency entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getElderlyId());
        if (entity.getElderlyName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getElderlyName());
        }
        if (entity.getElderlyContact() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getElderlyContact());
        }
        statement.bindDouble(5, entity.getLatitude());
        statement.bindDouble(6, entity.getLongitude());
        if (entity.getStatus() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getStatus());
        }
        if (entity.getVolunteerId() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getVolunteerId());
        }
        if (entity.getVolunteerName() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getVolunteerName());
        }
        if (entity.getVolunteerContact() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getVolunteerContact());
        }
        statement.bindLong(11, entity.getTimestamp());
        statement.bindLong(12, entity.getId());
      }
    };
  }

  @Override
  public long insertEmergency(final Emergency emergency) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfEmergency.insertAndReturnId(emergency);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void updateEmergency(final Emergency emergency) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfEmergency.handle(emergency);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<Emergency> getEmergenciesByStatus(final String status) {
    final String _sql = "SELECT * FROM emergencies WHERE status = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (status == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, status);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfElderlyId = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_id");
      final int _cursorIndexOfElderlyName = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_name");
      final int _cursorIndexOfElderlyContact = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_contact");
      final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
      final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfVolunteerId = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_id");
      final int _cursorIndexOfVolunteerName = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_name");
      final int _cursorIndexOfVolunteerContact = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_contact");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final List<Emergency> _result = new ArrayList<Emergency>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Emergency _item;
        _item = new Emergency();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final int _tmpElderlyId;
        _tmpElderlyId = _cursor.getInt(_cursorIndexOfElderlyId);
        _item.setElderlyId(_tmpElderlyId);
        final String _tmpElderlyName;
        if (_cursor.isNull(_cursorIndexOfElderlyName)) {
          _tmpElderlyName = null;
        } else {
          _tmpElderlyName = _cursor.getString(_cursorIndexOfElderlyName);
        }
        _item.setElderlyName(_tmpElderlyName);
        final String _tmpElderlyContact;
        if (_cursor.isNull(_cursorIndexOfElderlyContact)) {
          _tmpElderlyContact = null;
        } else {
          _tmpElderlyContact = _cursor.getString(_cursorIndexOfElderlyContact);
        }
        _item.setElderlyContact(_tmpElderlyContact);
        final double _tmpLatitude;
        _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
        _item.setLatitude(_tmpLatitude);
        final double _tmpLongitude;
        _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
        _item.setLongitude(_tmpLongitude);
        final String _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        }
        _item.setStatus(_tmpStatus);
        final Integer _tmpVolunteerId;
        if (_cursor.isNull(_cursorIndexOfVolunteerId)) {
          _tmpVolunteerId = null;
        } else {
          _tmpVolunteerId = _cursor.getInt(_cursorIndexOfVolunteerId);
        }
        _item.setVolunteerId(_tmpVolunteerId);
        final String _tmpVolunteerName;
        if (_cursor.isNull(_cursorIndexOfVolunteerName)) {
          _tmpVolunteerName = null;
        } else {
          _tmpVolunteerName = _cursor.getString(_cursorIndexOfVolunteerName);
        }
        _item.setVolunteerName(_tmpVolunteerName);
        final String _tmpVolunteerContact;
        if (_cursor.isNull(_cursorIndexOfVolunteerContact)) {
          _tmpVolunteerContact = null;
        } else {
          _tmpVolunteerContact = _cursor.getString(_cursorIndexOfVolunteerContact);
        }
        _item.setVolunteerContact(_tmpVolunteerContact);
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
  public List<Emergency> getActiveAndAcceptedEmergencies() {
    final String _sql = "SELECT * FROM emergencies WHERE status IN ('active', 'accepted') ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfElderlyId = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_id");
      final int _cursorIndexOfElderlyName = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_name");
      final int _cursorIndexOfElderlyContact = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_contact");
      final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
      final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfVolunteerId = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_id");
      final int _cursorIndexOfVolunteerName = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_name");
      final int _cursorIndexOfVolunteerContact = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_contact");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final List<Emergency> _result = new ArrayList<Emergency>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Emergency _item;
        _item = new Emergency();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final int _tmpElderlyId;
        _tmpElderlyId = _cursor.getInt(_cursorIndexOfElderlyId);
        _item.setElderlyId(_tmpElderlyId);
        final String _tmpElderlyName;
        if (_cursor.isNull(_cursorIndexOfElderlyName)) {
          _tmpElderlyName = null;
        } else {
          _tmpElderlyName = _cursor.getString(_cursorIndexOfElderlyName);
        }
        _item.setElderlyName(_tmpElderlyName);
        final String _tmpElderlyContact;
        if (_cursor.isNull(_cursorIndexOfElderlyContact)) {
          _tmpElderlyContact = null;
        } else {
          _tmpElderlyContact = _cursor.getString(_cursorIndexOfElderlyContact);
        }
        _item.setElderlyContact(_tmpElderlyContact);
        final double _tmpLatitude;
        _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
        _item.setLatitude(_tmpLatitude);
        final double _tmpLongitude;
        _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
        _item.setLongitude(_tmpLongitude);
        final String _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        }
        _item.setStatus(_tmpStatus);
        final Integer _tmpVolunteerId;
        if (_cursor.isNull(_cursorIndexOfVolunteerId)) {
          _tmpVolunteerId = null;
        } else {
          _tmpVolunteerId = _cursor.getInt(_cursorIndexOfVolunteerId);
        }
        _item.setVolunteerId(_tmpVolunteerId);
        final String _tmpVolunteerName;
        if (_cursor.isNull(_cursorIndexOfVolunteerName)) {
          _tmpVolunteerName = null;
        } else {
          _tmpVolunteerName = _cursor.getString(_cursorIndexOfVolunteerName);
        }
        _item.setVolunteerName(_tmpVolunteerName);
        final String _tmpVolunteerContact;
        if (_cursor.isNull(_cursorIndexOfVolunteerContact)) {
          _tmpVolunteerContact = null;
        } else {
          _tmpVolunteerContact = _cursor.getString(_cursorIndexOfVolunteerContact);
        }
        _item.setVolunteerContact(_tmpVolunteerContact);
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
  public List<Emergency> getEmergenciesByElderly(final int elderlyId) {
    final String _sql = "SELECT * FROM emergencies WHERE elderly_id = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, elderlyId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfElderlyId = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_id");
      final int _cursorIndexOfElderlyName = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_name");
      final int _cursorIndexOfElderlyContact = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_contact");
      final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
      final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfVolunteerId = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_id");
      final int _cursorIndexOfVolunteerName = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_name");
      final int _cursorIndexOfVolunteerContact = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_contact");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final List<Emergency> _result = new ArrayList<Emergency>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Emergency _item;
        _item = new Emergency();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final int _tmpElderlyId;
        _tmpElderlyId = _cursor.getInt(_cursorIndexOfElderlyId);
        _item.setElderlyId(_tmpElderlyId);
        final String _tmpElderlyName;
        if (_cursor.isNull(_cursorIndexOfElderlyName)) {
          _tmpElderlyName = null;
        } else {
          _tmpElderlyName = _cursor.getString(_cursorIndexOfElderlyName);
        }
        _item.setElderlyName(_tmpElderlyName);
        final String _tmpElderlyContact;
        if (_cursor.isNull(_cursorIndexOfElderlyContact)) {
          _tmpElderlyContact = null;
        } else {
          _tmpElderlyContact = _cursor.getString(_cursorIndexOfElderlyContact);
        }
        _item.setElderlyContact(_tmpElderlyContact);
        final double _tmpLatitude;
        _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
        _item.setLatitude(_tmpLatitude);
        final double _tmpLongitude;
        _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
        _item.setLongitude(_tmpLongitude);
        final String _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        }
        _item.setStatus(_tmpStatus);
        final Integer _tmpVolunteerId;
        if (_cursor.isNull(_cursorIndexOfVolunteerId)) {
          _tmpVolunteerId = null;
        } else {
          _tmpVolunteerId = _cursor.getInt(_cursorIndexOfVolunteerId);
        }
        _item.setVolunteerId(_tmpVolunteerId);
        final String _tmpVolunteerName;
        if (_cursor.isNull(_cursorIndexOfVolunteerName)) {
          _tmpVolunteerName = null;
        } else {
          _tmpVolunteerName = _cursor.getString(_cursorIndexOfVolunteerName);
        }
        _item.setVolunteerName(_tmpVolunteerName);
        final String _tmpVolunteerContact;
        if (_cursor.isNull(_cursorIndexOfVolunteerContact)) {
          _tmpVolunteerContact = null;
        } else {
          _tmpVolunteerContact = _cursor.getString(_cursorIndexOfVolunteerContact);
        }
        _item.setVolunteerContact(_tmpVolunteerContact);
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
  public Emergency getActiveEmergencyByElderly(final int elderlyId, final String status) {
    final String _sql = "SELECT * FROM emergencies WHERE elderly_id = ? AND status = ? ORDER BY timestamp DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, elderlyId);
    _argIndex = 2;
    if (status == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, status);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfElderlyId = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_id");
      final int _cursorIndexOfElderlyName = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_name");
      final int _cursorIndexOfElderlyContact = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_contact");
      final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
      final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfVolunteerId = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_id");
      final int _cursorIndexOfVolunteerName = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_name");
      final int _cursorIndexOfVolunteerContact = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_contact");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final Emergency _result;
      if (_cursor.moveToFirst()) {
        _result = new Emergency();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _result.setId(_tmpId);
        final int _tmpElderlyId;
        _tmpElderlyId = _cursor.getInt(_cursorIndexOfElderlyId);
        _result.setElderlyId(_tmpElderlyId);
        final String _tmpElderlyName;
        if (_cursor.isNull(_cursorIndexOfElderlyName)) {
          _tmpElderlyName = null;
        } else {
          _tmpElderlyName = _cursor.getString(_cursorIndexOfElderlyName);
        }
        _result.setElderlyName(_tmpElderlyName);
        final String _tmpElderlyContact;
        if (_cursor.isNull(_cursorIndexOfElderlyContact)) {
          _tmpElderlyContact = null;
        } else {
          _tmpElderlyContact = _cursor.getString(_cursorIndexOfElderlyContact);
        }
        _result.setElderlyContact(_tmpElderlyContact);
        final double _tmpLatitude;
        _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
        _result.setLatitude(_tmpLatitude);
        final double _tmpLongitude;
        _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
        _result.setLongitude(_tmpLongitude);
        final String _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        }
        _result.setStatus(_tmpStatus);
        final Integer _tmpVolunteerId;
        if (_cursor.isNull(_cursorIndexOfVolunteerId)) {
          _tmpVolunteerId = null;
        } else {
          _tmpVolunteerId = _cursor.getInt(_cursorIndexOfVolunteerId);
        }
        _result.setVolunteerId(_tmpVolunteerId);
        final String _tmpVolunteerName;
        if (_cursor.isNull(_cursorIndexOfVolunteerName)) {
          _tmpVolunteerName = null;
        } else {
          _tmpVolunteerName = _cursor.getString(_cursorIndexOfVolunteerName);
        }
        _result.setVolunteerName(_tmpVolunteerName);
        final String _tmpVolunteerContact;
        if (_cursor.isNull(_cursorIndexOfVolunteerContact)) {
          _tmpVolunteerContact = null;
        } else {
          _tmpVolunteerContact = _cursor.getString(_cursorIndexOfVolunteerContact);
        }
        _result.setVolunteerContact(_tmpVolunteerContact);
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
  public List<Emergency> getEmergenciesByVolunteer(final int volunteerId) {
    final String _sql = "SELECT * FROM emergencies WHERE volunteer_id = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, volunteerId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfElderlyId = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_id");
      final int _cursorIndexOfElderlyName = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_name");
      final int _cursorIndexOfElderlyContact = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_contact");
      final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
      final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfVolunteerId = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_id");
      final int _cursorIndexOfVolunteerName = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_name");
      final int _cursorIndexOfVolunteerContact = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_contact");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final List<Emergency> _result = new ArrayList<Emergency>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Emergency _item;
        _item = new Emergency();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final int _tmpElderlyId;
        _tmpElderlyId = _cursor.getInt(_cursorIndexOfElderlyId);
        _item.setElderlyId(_tmpElderlyId);
        final String _tmpElderlyName;
        if (_cursor.isNull(_cursorIndexOfElderlyName)) {
          _tmpElderlyName = null;
        } else {
          _tmpElderlyName = _cursor.getString(_cursorIndexOfElderlyName);
        }
        _item.setElderlyName(_tmpElderlyName);
        final String _tmpElderlyContact;
        if (_cursor.isNull(_cursorIndexOfElderlyContact)) {
          _tmpElderlyContact = null;
        } else {
          _tmpElderlyContact = _cursor.getString(_cursorIndexOfElderlyContact);
        }
        _item.setElderlyContact(_tmpElderlyContact);
        final double _tmpLatitude;
        _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
        _item.setLatitude(_tmpLatitude);
        final double _tmpLongitude;
        _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
        _item.setLongitude(_tmpLongitude);
        final String _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        }
        _item.setStatus(_tmpStatus);
        final Integer _tmpVolunteerId;
        if (_cursor.isNull(_cursorIndexOfVolunteerId)) {
          _tmpVolunteerId = null;
        } else {
          _tmpVolunteerId = _cursor.getInt(_cursorIndexOfVolunteerId);
        }
        _item.setVolunteerId(_tmpVolunteerId);
        final String _tmpVolunteerName;
        if (_cursor.isNull(_cursorIndexOfVolunteerName)) {
          _tmpVolunteerName = null;
        } else {
          _tmpVolunteerName = _cursor.getString(_cursorIndexOfVolunteerName);
        }
        _item.setVolunteerName(_tmpVolunteerName);
        final String _tmpVolunteerContact;
        if (_cursor.isNull(_cursorIndexOfVolunteerContact)) {
          _tmpVolunteerContact = null;
        } else {
          _tmpVolunteerContact = _cursor.getString(_cursorIndexOfVolunteerContact);
        }
        _item.setVolunteerContact(_tmpVolunteerContact);
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
  public List<Emergency> getEmergenciesByVolunteerNotNull(final int volunteerId) {
    final String _sql = "SELECT * FROM emergencies WHERE volunteer_id = ? AND volunteer_id IS NOT NULL ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, volunteerId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfElderlyId = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_id");
      final int _cursorIndexOfElderlyName = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_name");
      final int _cursorIndexOfElderlyContact = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_contact");
      final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
      final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfVolunteerId = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_id");
      final int _cursorIndexOfVolunteerName = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_name");
      final int _cursorIndexOfVolunteerContact = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_contact");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final List<Emergency> _result = new ArrayList<Emergency>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Emergency _item;
        _item = new Emergency();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final int _tmpElderlyId;
        _tmpElderlyId = _cursor.getInt(_cursorIndexOfElderlyId);
        _item.setElderlyId(_tmpElderlyId);
        final String _tmpElderlyName;
        if (_cursor.isNull(_cursorIndexOfElderlyName)) {
          _tmpElderlyName = null;
        } else {
          _tmpElderlyName = _cursor.getString(_cursorIndexOfElderlyName);
        }
        _item.setElderlyName(_tmpElderlyName);
        final String _tmpElderlyContact;
        if (_cursor.isNull(_cursorIndexOfElderlyContact)) {
          _tmpElderlyContact = null;
        } else {
          _tmpElderlyContact = _cursor.getString(_cursorIndexOfElderlyContact);
        }
        _item.setElderlyContact(_tmpElderlyContact);
        final double _tmpLatitude;
        _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
        _item.setLatitude(_tmpLatitude);
        final double _tmpLongitude;
        _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
        _item.setLongitude(_tmpLongitude);
        final String _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        }
        _item.setStatus(_tmpStatus);
        final Integer _tmpVolunteerId;
        if (_cursor.isNull(_cursorIndexOfVolunteerId)) {
          _tmpVolunteerId = null;
        } else {
          _tmpVolunteerId = _cursor.getInt(_cursorIndexOfVolunteerId);
        }
        _item.setVolunteerId(_tmpVolunteerId);
        final String _tmpVolunteerName;
        if (_cursor.isNull(_cursorIndexOfVolunteerName)) {
          _tmpVolunteerName = null;
        } else {
          _tmpVolunteerName = _cursor.getString(_cursorIndexOfVolunteerName);
        }
        _item.setVolunteerName(_tmpVolunteerName);
        final String _tmpVolunteerContact;
        if (_cursor.isNull(_cursorIndexOfVolunteerContact)) {
          _tmpVolunteerContact = null;
        } else {
          _tmpVolunteerContact = _cursor.getString(_cursorIndexOfVolunteerContact);
        }
        _item.setVolunteerContact(_tmpVolunteerContact);
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
  public List<Emergency> getAcceptedAndCompletedEmergenciesByVolunteer(final int volunteerId) {
    final String _sql = "SELECT * FROM emergencies WHERE volunteer_id = ? AND status IN ('accepted', 'completed') ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, volunteerId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfElderlyId = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_id");
      final int _cursorIndexOfElderlyName = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_name");
      final int _cursorIndexOfElderlyContact = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_contact");
      final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
      final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfVolunteerId = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_id");
      final int _cursorIndexOfVolunteerName = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_name");
      final int _cursorIndexOfVolunteerContact = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_contact");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final List<Emergency> _result = new ArrayList<Emergency>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Emergency _item;
        _item = new Emergency();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final int _tmpElderlyId;
        _tmpElderlyId = _cursor.getInt(_cursorIndexOfElderlyId);
        _item.setElderlyId(_tmpElderlyId);
        final String _tmpElderlyName;
        if (_cursor.isNull(_cursorIndexOfElderlyName)) {
          _tmpElderlyName = null;
        } else {
          _tmpElderlyName = _cursor.getString(_cursorIndexOfElderlyName);
        }
        _item.setElderlyName(_tmpElderlyName);
        final String _tmpElderlyContact;
        if (_cursor.isNull(_cursorIndexOfElderlyContact)) {
          _tmpElderlyContact = null;
        } else {
          _tmpElderlyContact = _cursor.getString(_cursorIndexOfElderlyContact);
        }
        _item.setElderlyContact(_tmpElderlyContact);
        final double _tmpLatitude;
        _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
        _item.setLatitude(_tmpLatitude);
        final double _tmpLongitude;
        _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
        _item.setLongitude(_tmpLongitude);
        final String _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        }
        _item.setStatus(_tmpStatus);
        final Integer _tmpVolunteerId;
        if (_cursor.isNull(_cursorIndexOfVolunteerId)) {
          _tmpVolunteerId = null;
        } else {
          _tmpVolunteerId = _cursor.getInt(_cursorIndexOfVolunteerId);
        }
        _item.setVolunteerId(_tmpVolunteerId);
        final String _tmpVolunteerName;
        if (_cursor.isNull(_cursorIndexOfVolunteerName)) {
          _tmpVolunteerName = null;
        } else {
          _tmpVolunteerName = _cursor.getString(_cursorIndexOfVolunteerName);
        }
        _item.setVolunteerName(_tmpVolunteerName);
        final String _tmpVolunteerContact;
        if (_cursor.isNull(_cursorIndexOfVolunteerContact)) {
          _tmpVolunteerContact = null;
        } else {
          _tmpVolunteerContact = _cursor.getString(_cursorIndexOfVolunteerContact);
        }
        _item.setVolunteerContact(_tmpVolunteerContact);
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
  public Emergency getEmergencyById(final int id) {
    final String _sql = "SELECT * FROM emergencies WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfElderlyId = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_id");
      final int _cursorIndexOfElderlyName = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_name");
      final int _cursorIndexOfElderlyContact = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_contact");
      final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
      final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfVolunteerId = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_id");
      final int _cursorIndexOfVolunteerName = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_name");
      final int _cursorIndexOfVolunteerContact = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_contact");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final Emergency _result;
      if (_cursor.moveToFirst()) {
        _result = new Emergency();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _result.setId(_tmpId);
        final int _tmpElderlyId;
        _tmpElderlyId = _cursor.getInt(_cursorIndexOfElderlyId);
        _result.setElderlyId(_tmpElderlyId);
        final String _tmpElderlyName;
        if (_cursor.isNull(_cursorIndexOfElderlyName)) {
          _tmpElderlyName = null;
        } else {
          _tmpElderlyName = _cursor.getString(_cursorIndexOfElderlyName);
        }
        _result.setElderlyName(_tmpElderlyName);
        final String _tmpElderlyContact;
        if (_cursor.isNull(_cursorIndexOfElderlyContact)) {
          _tmpElderlyContact = null;
        } else {
          _tmpElderlyContact = _cursor.getString(_cursorIndexOfElderlyContact);
        }
        _result.setElderlyContact(_tmpElderlyContact);
        final double _tmpLatitude;
        _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
        _result.setLatitude(_tmpLatitude);
        final double _tmpLongitude;
        _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
        _result.setLongitude(_tmpLongitude);
        final String _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        }
        _result.setStatus(_tmpStatus);
        final Integer _tmpVolunteerId;
        if (_cursor.isNull(_cursorIndexOfVolunteerId)) {
          _tmpVolunteerId = null;
        } else {
          _tmpVolunteerId = _cursor.getInt(_cursorIndexOfVolunteerId);
        }
        _result.setVolunteerId(_tmpVolunteerId);
        final String _tmpVolunteerName;
        if (_cursor.isNull(_cursorIndexOfVolunteerName)) {
          _tmpVolunteerName = null;
        } else {
          _tmpVolunteerName = _cursor.getString(_cursorIndexOfVolunteerName);
        }
        _result.setVolunteerName(_tmpVolunteerName);
        final String _tmpVolunteerContact;
        if (_cursor.isNull(_cursorIndexOfVolunteerContact)) {
          _tmpVolunteerContact = null;
        } else {
          _tmpVolunteerContact = _cursor.getString(_cursorIndexOfVolunteerContact);
        }
        _result.setVolunteerContact(_tmpVolunteerContact);
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
  public List<Emergency> getAllEmergencies() {
    final String _sql = "SELECT * FROM emergencies ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfElderlyId = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_id");
      final int _cursorIndexOfElderlyName = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_name");
      final int _cursorIndexOfElderlyContact = CursorUtil.getColumnIndexOrThrow(_cursor, "elderly_contact");
      final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
      final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfVolunteerId = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_id");
      final int _cursorIndexOfVolunteerName = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_name");
      final int _cursorIndexOfVolunteerContact = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_contact");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final List<Emergency> _result = new ArrayList<Emergency>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Emergency _item;
        _item = new Emergency();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final int _tmpElderlyId;
        _tmpElderlyId = _cursor.getInt(_cursorIndexOfElderlyId);
        _item.setElderlyId(_tmpElderlyId);
        final String _tmpElderlyName;
        if (_cursor.isNull(_cursorIndexOfElderlyName)) {
          _tmpElderlyName = null;
        } else {
          _tmpElderlyName = _cursor.getString(_cursorIndexOfElderlyName);
        }
        _item.setElderlyName(_tmpElderlyName);
        final String _tmpElderlyContact;
        if (_cursor.isNull(_cursorIndexOfElderlyContact)) {
          _tmpElderlyContact = null;
        } else {
          _tmpElderlyContact = _cursor.getString(_cursorIndexOfElderlyContact);
        }
        _item.setElderlyContact(_tmpElderlyContact);
        final double _tmpLatitude;
        _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
        _item.setLatitude(_tmpLatitude);
        final double _tmpLongitude;
        _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
        _item.setLongitude(_tmpLongitude);
        final String _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        }
        _item.setStatus(_tmpStatus);
        final Integer _tmpVolunteerId;
        if (_cursor.isNull(_cursorIndexOfVolunteerId)) {
          _tmpVolunteerId = null;
        } else {
          _tmpVolunteerId = _cursor.getInt(_cursorIndexOfVolunteerId);
        }
        _item.setVolunteerId(_tmpVolunteerId);
        final String _tmpVolunteerName;
        if (_cursor.isNull(_cursorIndexOfVolunteerName)) {
          _tmpVolunteerName = null;
        } else {
          _tmpVolunteerName = _cursor.getString(_cursorIndexOfVolunteerName);
        }
        _item.setVolunteerName(_tmpVolunteerName);
        final String _tmpVolunteerContact;
        if (_cursor.isNull(_cursorIndexOfVolunteerContact)) {
          _tmpVolunteerContact = null;
        } else {
          _tmpVolunteerContact = _cursor.getString(_cursorIndexOfVolunteerContact);
        }
        _item.setVolunteerContact(_tmpVolunteerContact);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
