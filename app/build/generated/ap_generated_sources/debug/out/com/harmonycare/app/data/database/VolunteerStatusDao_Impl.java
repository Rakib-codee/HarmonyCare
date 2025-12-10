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
import com.harmonycare.app.data.model.VolunteerStatus;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class VolunteerStatusDao_Impl implements VolunteerStatusDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<VolunteerStatus> __insertionAdapterOfVolunteerStatus;

  private final EntityDeletionOrUpdateAdapter<VolunteerStatus> __updateAdapterOfVolunteerStatus;

  public VolunteerStatusDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfVolunteerStatus = new EntityInsertionAdapter<VolunteerStatus>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `volunteer_status` (`volunteer_id`,`is_available`) VALUES (?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final VolunteerStatus entity) {
        statement.bindLong(1, entity.getVolunteerId());
        final int _tmp = entity.isAvailable() ? 1 : 0;
        statement.bindLong(2, _tmp);
      }
    };
    this.__updateAdapterOfVolunteerStatus = new EntityDeletionOrUpdateAdapter<VolunteerStatus>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `volunteer_status` SET `volunteer_id` = ?,`is_available` = ? WHERE `volunteer_id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final VolunteerStatus entity) {
        statement.bindLong(1, entity.getVolunteerId());
        final int _tmp = entity.isAvailable() ? 1 : 0;
        statement.bindLong(2, _tmp);
        statement.bindLong(3, entity.getVolunteerId());
      }
    };
  }

  @Override
  public void insertOrUpdateVolunteerStatus(final VolunteerStatus status) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfVolunteerStatus.insert(status);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void updateVolunteerStatus(final VolunteerStatus status) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfVolunteerStatus.handle(status);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public VolunteerStatus getVolunteerStatus(final int volunteerId) {
    final String _sql = "SELECT * FROM volunteer_status WHERE volunteer_id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, volunteerId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfVolunteerId = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_id");
      final int _cursorIndexOfIsAvailable = CursorUtil.getColumnIndexOrThrow(_cursor, "is_available");
      final VolunteerStatus _result;
      if (_cursor.moveToFirst()) {
        _result = new VolunteerStatus();
        final int _tmpVolunteerId;
        _tmpVolunteerId = _cursor.getInt(_cursorIndexOfVolunteerId);
        _result.setVolunteerId(_tmpVolunteerId);
        final boolean _tmpIsAvailable;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsAvailable);
        _tmpIsAvailable = _tmp != 0;
        _result.setAvailable(_tmpIsAvailable);
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
  public List<VolunteerStatus> getAvailableVolunteers() {
    final String _sql = "SELECT * FROM volunteer_status WHERE is_available = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfVolunteerId = CursorUtil.getColumnIndexOrThrow(_cursor, "volunteer_id");
      final int _cursorIndexOfIsAvailable = CursorUtil.getColumnIndexOrThrow(_cursor, "is_available");
      final List<VolunteerStatus> _result = new ArrayList<VolunteerStatus>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final VolunteerStatus _item;
        _item = new VolunteerStatus();
        final int _tmpVolunteerId;
        _tmpVolunteerId = _cursor.getInt(_cursorIndexOfVolunteerId);
        _item.setVolunteerId(_tmpVolunteerId);
        final boolean _tmpIsAvailable;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsAvailable);
        _tmpIsAvailable = _tmp != 0;
        _item.setAvailable(_tmpIsAvailable);
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
