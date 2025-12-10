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
import com.harmonycare.app.data.model.PendingOperation;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class PendingOperationDao_Impl implements PendingOperationDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PendingOperation> __insertionAdapterOfPendingOperation;

  private final EntityDeletionOrUpdateAdapter<PendingOperation> __deletionAdapterOfPendingOperation;

  private final SharedSQLiteStatement __preparedStmtOfClearAllPendingOperations;

  public PendingOperationDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPendingOperation = new EntityInsertionAdapter<PendingOperation>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `pending_operations` (`id`,`operation_type`,`data_json`,`timestamp`,`retry_count`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final PendingOperation entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getOperationType() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getOperationType());
        }
        if (entity.getDataJson() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getDataJson());
        }
        statement.bindLong(4, entity.getTimestamp());
        statement.bindLong(5, entity.getRetryCount());
      }
    };
    this.__deletionAdapterOfPendingOperation = new EntityDeletionOrUpdateAdapter<PendingOperation>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `pending_operations` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final PendingOperation entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__preparedStmtOfClearAllPendingOperations = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM pending_operations";
        return _query;
      }
    };
  }

  @Override
  public long insertPendingOperation(final PendingOperation operation) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfPendingOperation.insertAndReturnId(operation);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deletePendingOperation(final PendingOperation operation) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfPendingOperation.handle(operation);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void clearAllPendingOperations() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfClearAllPendingOperations.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfClearAllPendingOperations.release(_stmt);
    }
  }

  @Override
  public List<PendingOperation> getAllPendingOperations() {
    final String _sql = "SELECT * FROM pending_operations ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOperationType = CursorUtil.getColumnIndexOrThrow(_cursor, "operation_type");
      final int _cursorIndexOfDataJson = CursorUtil.getColumnIndexOrThrow(_cursor, "data_json");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfRetryCount = CursorUtil.getColumnIndexOrThrow(_cursor, "retry_count");
      final List<PendingOperation> _result = new ArrayList<PendingOperation>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final PendingOperation _item;
        _item = new PendingOperation();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final String _tmpOperationType;
        if (_cursor.isNull(_cursorIndexOfOperationType)) {
          _tmpOperationType = null;
        } else {
          _tmpOperationType = _cursor.getString(_cursorIndexOfOperationType);
        }
        _item.setOperationType(_tmpOperationType);
        final String _tmpDataJson;
        if (_cursor.isNull(_cursorIndexOfDataJson)) {
          _tmpDataJson = null;
        } else {
          _tmpDataJson = _cursor.getString(_cursorIndexOfDataJson);
        }
        _item.setDataJson(_tmpDataJson);
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _item.setTimestamp(_tmpTimestamp);
        final int _tmpRetryCount;
        _tmpRetryCount = _cursor.getInt(_cursorIndexOfRetryCount);
        _item.setRetryCount(_tmpRetryCount);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<PendingOperation> getPendingOperationsByType(final String operationType) {
    final String _sql = "SELECT * FROM pending_operations WHERE operation_type = ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (operationType == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, operationType);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOperationType = CursorUtil.getColumnIndexOrThrow(_cursor, "operation_type");
      final int _cursorIndexOfDataJson = CursorUtil.getColumnIndexOrThrow(_cursor, "data_json");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfRetryCount = CursorUtil.getColumnIndexOrThrow(_cursor, "retry_count");
      final List<PendingOperation> _result = new ArrayList<PendingOperation>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final PendingOperation _item;
        _item = new PendingOperation();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final String _tmpOperationType;
        if (_cursor.isNull(_cursorIndexOfOperationType)) {
          _tmpOperationType = null;
        } else {
          _tmpOperationType = _cursor.getString(_cursorIndexOfOperationType);
        }
        _item.setOperationType(_tmpOperationType);
        final String _tmpDataJson;
        if (_cursor.isNull(_cursorIndexOfDataJson)) {
          _tmpDataJson = null;
        } else {
          _tmpDataJson = _cursor.getString(_cursorIndexOfDataJson);
        }
        _item.setDataJson(_tmpDataJson);
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _item.setTimestamp(_tmpTimestamp);
        final int _tmpRetryCount;
        _tmpRetryCount = _cursor.getInt(_cursorIndexOfRetryCount);
        _item.setRetryCount(_tmpRetryCount);
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
