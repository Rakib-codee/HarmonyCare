package com.harmonycare.app.data.database;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.harmonycare.app.data.model.EmergencyContact;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class EmergencyContactDao_Impl implements EmergencyContactDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<EmergencyContact> __insertionAdapterOfEmergencyContact;

  private final EntityDeletionOrUpdateAdapter<EmergencyContact> __deletionAdapterOfEmergencyContact;

  private final EntityDeletionOrUpdateAdapter<EmergencyContact> __updateAdapterOfEmergencyContact;

  private final SharedSQLiteStatement __preparedStmtOfClearPrimaryContacts;

  public EmergencyContactDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfEmergencyContact = new EntityInsertionAdapter<EmergencyContact>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `emergency_contacts` (`id`,`user_id`,`name`,`phone_number`,`relationship`,`is_primary`,`notification_enabled`,`notification_method`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final EmergencyContact entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getUserId());
        if (entity.getName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getName());
        }
        if (entity.getPhoneNumber() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getPhoneNumber());
        }
        if (entity.getRelationship() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getRelationship());
        }
        final int _tmp = entity.isPrimary() ? 1 : 0;
        statement.bindLong(6, _tmp);
        final int _tmp_1 = entity.isNotificationEnabled() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        if (entity.getNotificationMethod() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getNotificationMethod());
        }
      }
    };
    this.__deletionAdapterOfEmergencyContact = new EntityDeletionOrUpdateAdapter<EmergencyContact>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `emergency_contacts` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final EmergencyContact entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfEmergencyContact = new EntityDeletionOrUpdateAdapter<EmergencyContact>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `emergency_contacts` SET `id` = ?,`user_id` = ?,`name` = ?,`phone_number` = ?,`relationship` = ?,`is_primary` = ?,`notification_enabled` = ?,`notification_method` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final EmergencyContact entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getUserId());
        if (entity.getName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getName());
        }
        if (entity.getPhoneNumber() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getPhoneNumber());
        }
        if (entity.getRelationship() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getRelationship());
        }
        final int _tmp = entity.isPrimary() ? 1 : 0;
        statement.bindLong(6, _tmp);
        final int _tmp_1 = entity.isNotificationEnabled() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        if (entity.getNotificationMethod() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getNotificationMethod());
        }
        statement.bindLong(9, entity.getId());
      }
    };
    this.__preparedStmtOfClearPrimaryContacts = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE emergency_contacts SET is_primary = 0 WHERE user_id = ?";
        return _query;
      }
    };
  }

  @Override
  public long insertEmergencyContact(final EmergencyContact contact) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfEmergencyContact.insertAndReturnId(contact);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteEmergencyContact(final EmergencyContact contact) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfEmergencyContact.handle(contact);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void updateEmergencyContact(final EmergencyContact contact) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfEmergencyContact.handle(contact);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void clearPrimaryContacts(final int userId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfClearPrimaryContacts.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, userId);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfClearPrimaryContacts.release(_stmt);
    }
  }

  @Override
  public List<EmergencyContact> getContactsByUser(final int userId) {
    final String _sql = "SELECT * FROM emergency_contacts WHERE user_id = ? ORDER BY is_primary DESC, name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phone_number");
      final int _cursorIndexOfRelationship = CursorUtil.getColumnIndexOrThrow(_cursor, "relationship");
      final int _cursorIndexOfIsPrimary = CursorUtil.getColumnIndexOrThrow(_cursor, "is_primary");
      final int _cursorIndexOfNotificationEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "notification_enabled");
      final int _cursorIndexOfNotificationMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "notification_method");
      final List<EmergencyContact> _result = new ArrayList<EmergencyContact>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final EmergencyContact _item;
        _item = new EmergencyContact();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final int _tmpUserId;
        _tmpUserId = _cursor.getInt(_cursorIndexOfUserId);
        _item.setUserId(_tmpUserId);
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        _item.setName(_tmpName);
        final String _tmpPhoneNumber;
        if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
          _tmpPhoneNumber = null;
        } else {
          _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
        }
        _item.setPhoneNumber(_tmpPhoneNumber);
        final String _tmpRelationship;
        if (_cursor.isNull(_cursorIndexOfRelationship)) {
          _tmpRelationship = null;
        } else {
          _tmpRelationship = _cursor.getString(_cursorIndexOfRelationship);
        }
        _item.setRelationship(_tmpRelationship);
        final boolean _tmpIsPrimary;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsPrimary);
        _tmpIsPrimary = _tmp != 0;
        _item.setPrimary(_tmpIsPrimary);
        final boolean _tmpNotificationEnabled;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfNotificationEnabled);
        _tmpNotificationEnabled = _tmp_1 != 0;
        _item.setNotificationEnabled(_tmpNotificationEnabled);
        final String _tmpNotificationMethod;
        if (_cursor.isNull(_cursorIndexOfNotificationMethod)) {
          _tmpNotificationMethod = null;
        } else {
          _tmpNotificationMethod = _cursor.getString(_cursorIndexOfNotificationMethod);
        }
        _item.setNotificationMethod(_tmpNotificationMethod);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public EmergencyContact getPrimaryContact(final int userId) {
    final String _sql = "SELECT * FROM emergency_contacts WHERE user_id = ? AND is_primary = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phone_number");
      final int _cursorIndexOfRelationship = CursorUtil.getColumnIndexOrThrow(_cursor, "relationship");
      final int _cursorIndexOfIsPrimary = CursorUtil.getColumnIndexOrThrow(_cursor, "is_primary");
      final int _cursorIndexOfNotificationEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "notification_enabled");
      final int _cursorIndexOfNotificationMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "notification_method");
      final EmergencyContact _result;
      if (_cursor.moveToFirst()) {
        _result = new EmergencyContact();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _result.setId(_tmpId);
        final int _tmpUserId;
        _tmpUserId = _cursor.getInt(_cursorIndexOfUserId);
        _result.setUserId(_tmpUserId);
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        _result.setName(_tmpName);
        final String _tmpPhoneNumber;
        if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
          _tmpPhoneNumber = null;
        } else {
          _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
        }
        _result.setPhoneNumber(_tmpPhoneNumber);
        final String _tmpRelationship;
        if (_cursor.isNull(_cursorIndexOfRelationship)) {
          _tmpRelationship = null;
        } else {
          _tmpRelationship = _cursor.getString(_cursorIndexOfRelationship);
        }
        _result.setRelationship(_tmpRelationship);
        final boolean _tmpIsPrimary;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsPrimary);
        _tmpIsPrimary = _tmp != 0;
        _result.setPrimary(_tmpIsPrimary);
        final boolean _tmpNotificationEnabled;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfNotificationEnabled);
        _tmpNotificationEnabled = _tmp_1 != 0;
        _result.setNotificationEnabled(_tmpNotificationEnabled);
        final String _tmpNotificationMethod;
        if (_cursor.isNull(_cursorIndexOfNotificationMethod)) {
          _tmpNotificationMethod = null;
        } else {
          _tmpNotificationMethod = _cursor.getString(_cursorIndexOfNotificationMethod);
        }
        _result.setNotificationMethod(_tmpNotificationMethod);
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
  public EmergencyContact getContactById(final int id) {
    final String _sql = "SELECT * FROM emergency_contacts WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phone_number");
      final int _cursorIndexOfRelationship = CursorUtil.getColumnIndexOrThrow(_cursor, "relationship");
      final int _cursorIndexOfIsPrimary = CursorUtil.getColumnIndexOrThrow(_cursor, "is_primary");
      final int _cursorIndexOfNotificationEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "notification_enabled");
      final int _cursorIndexOfNotificationMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "notification_method");
      final EmergencyContact _result;
      if (_cursor.moveToFirst()) {
        _result = new EmergencyContact();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _result.setId(_tmpId);
        final int _tmpUserId;
        _tmpUserId = _cursor.getInt(_cursorIndexOfUserId);
        _result.setUserId(_tmpUserId);
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        _result.setName(_tmpName);
        final String _tmpPhoneNumber;
        if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
          _tmpPhoneNumber = null;
        } else {
          _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
        }
        _result.setPhoneNumber(_tmpPhoneNumber);
        final String _tmpRelationship;
        if (_cursor.isNull(_cursorIndexOfRelationship)) {
          _tmpRelationship = null;
        } else {
          _tmpRelationship = _cursor.getString(_cursorIndexOfRelationship);
        }
        _result.setRelationship(_tmpRelationship);
        final boolean _tmpIsPrimary;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsPrimary);
        _tmpIsPrimary = _tmp != 0;
        _result.setPrimary(_tmpIsPrimary);
        final boolean _tmpNotificationEnabled;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfNotificationEnabled);
        _tmpNotificationEnabled = _tmp_1 != 0;
        _result.setNotificationEnabled(_tmpNotificationEnabled);
        final String _tmpNotificationMethod;
        if (_cursor.isNull(_cursorIndexOfNotificationMethod)) {
          _tmpNotificationMethod = null;
        } else {
          _tmpNotificationMethod = _cursor.getString(_cursorIndexOfNotificationMethod);
        }
        _result.setNotificationMethod(_tmpNotificationMethod);
      } else {
        _result = null;
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
