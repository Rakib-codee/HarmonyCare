package com.harmonycare.app.data.database;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.harmonycare.app.data.model.Message;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class MessageDao_Impl implements MessageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Message> __insertionAdapterOfMessage;

  public MessageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMessage = new EntityInsertionAdapter<Message>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `messages` (`id`,`emergency_id`,`sender_id`,`receiver_id`,`message`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Message entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getEmergencyId());
        statement.bindLong(3, entity.getSenderId());
        statement.bindLong(4, entity.getReceiverId());
        if (entity.getMessage() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getMessage());
        }
        statement.bindLong(6, entity.getTimestamp());
      }
    };
  }

  @Override
  public long insertMessage(final Message message) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfMessage.insertAndReturnId(message);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<Message> getMessagesByEmergency(final int emergencyId) {
    final String _sql = "SELECT * FROM messages WHERE emergency_id = ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, emergencyId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfEmergencyId = CursorUtil.getColumnIndexOrThrow(_cursor, "emergency_id");
      final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "sender_id");
      final int _cursorIndexOfReceiverId = CursorUtil.getColumnIndexOrThrow(_cursor, "receiver_id");
      final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final List<Message> _result = new ArrayList<Message>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Message _item;
        _item = new Message();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final int _tmpEmergencyId;
        _tmpEmergencyId = _cursor.getInt(_cursorIndexOfEmergencyId);
        _item.setEmergencyId(_tmpEmergencyId);
        final int _tmpSenderId;
        _tmpSenderId = _cursor.getInt(_cursorIndexOfSenderId);
        _item.setSenderId(_tmpSenderId);
        final int _tmpReceiverId;
        _tmpReceiverId = _cursor.getInt(_cursorIndexOfReceiverId);
        _item.setReceiverId(_tmpReceiverId);
        final String _tmpMessage;
        if (_cursor.isNull(_cursorIndexOfMessage)) {
          _tmpMessage = null;
        } else {
          _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
        }
        _item.setMessage(_tmpMessage);
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
  public List<Message> getMessagesByEmergencyAndUser(final int emergencyId, final int userId) {
    final String _sql = "SELECT * FROM messages WHERE emergency_id = ? AND (sender_id = ? OR receiver_id = ?) ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, emergencyId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, userId);
    _argIndex = 3;
    _statement.bindLong(_argIndex, userId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfEmergencyId = CursorUtil.getColumnIndexOrThrow(_cursor, "emergency_id");
      final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "sender_id");
      final int _cursorIndexOfReceiverId = CursorUtil.getColumnIndexOrThrow(_cursor, "receiver_id");
      final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final List<Message> _result = new ArrayList<Message>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Message _item;
        _item = new Message();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final int _tmpEmergencyId;
        _tmpEmergencyId = _cursor.getInt(_cursorIndexOfEmergencyId);
        _item.setEmergencyId(_tmpEmergencyId);
        final int _tmpSenderId;
        _tmpSenderId = _cursor.getInt(_cursorIndexOfSenderId);
        _item.setSenderId(_tmpSenderId);
        final int _tmpReceiverId;
        _tmpReceiverId = _cursor.getInt(_cursorIndexOfReceiverId);
        _item.setReceiverId(_tmpReceiverId);
        final String _tmpMessage;
        if (_cursor.isNull(_cursorIndexOfMessage)) {
          _tmpMessage = null;
        } else {
          _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
        }
        _item.setMessage(_tmpMessage);
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
  public Message getLatestMessage(final int emergencyId) {
    final String _sql = "SELECT * FROM messages WHERE emergency_id = ? ORDER BY timestamp DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, emergencyId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfEmergencyId = CursorUtil.getColumnIndexOrThrow(_cursor, "emergency_id");
      final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "sender_id");
      final int _cursorIndexOfReceiverId = CursorUtil.getColumnIndexOrThrow(_cursor, "receiver_id");
      final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final Message _result;
      if (_cursor.moveToFirst()) {
        _result = new Message();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _result.setId(_tmpId);
        final int _tmpEmergencyId;
        _tmpEmergencyId = _cursor.getInt(_cursorIndexOfEmergencyId);
        _result.setEmergencyId(_tmpEmergencyId);
        final int _tmpSenderId;
        _tmpSenderId = _cursor.getInt(_cursorIndexOfSenderId);
        _result.setSenderId(_tmpSenderId);
        final int _tmpReceiverId;
        _tmpReceiverId = _cursor.getInt(_cursorIndexOfReceiverId);
        _result.setReceiverId(_tmpReceiverId);
        final String _tmpMessage;
        if (_cursor.isNull(_cursorIndexOfMessage)) {
          _tmpMessage = null;
        } else {
          _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
        }
        _result.setMessage(_tmpMessage);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
