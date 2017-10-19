package teamdoppelganger.smarterbus.bs;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

import java.util.ArrayList;

public class bsCursor implements Cursor {

private ArrayList<ArrayList<Object>> _rs = new ArrayList<ArrayList<Object>>();
private ArrayList<Object> _row;
private ArrayList<String> _fields = new ArrayList<String>();
private String[] _fieldsArray;
private ArrayList<Integer> _fieldTypes = new ArrayList<Integer>();
private int _length, _columeCount, _cursor;
private Bundle mExtras = Bundle.EMPTY;
public bsCursor(Cursor c){
	if( c != null ){
		_length = c.getCount();
		if(_length > 0 && c.moveToFirst()){
			int i, j = _columeCount = c.getColumnCount();
			for( i = 0; i < j ; i++ ){
				_fields.add(c.getColumnName(i));
				_fieldTypes.add(c.getType(i));
			}
			_fieldsArray = _fields.toArray(new String[_fields.size()]);
			do{
				ArrayList<Object> _row = new ArrayList<Object>();
				for( i = 0; i < j ; i++ ){
					switch( _fieldTypes.get( i ) ){
					case Cursor.FIELD_TYPE_NULL:
						_row.add( null );
						break;
					case Cursor.FIELD_TYPE_INTEGER:
						_row.add( c.getInt( i ) );
						break;
					case Cursor.FIELD_TYPE_FLOAT:
						_row.add( c.getFloat( i ) );
						break;
					case Cursor.FIELD_TYPE_STRING:
						_row.add( c.getString( i ) );
						break;
					case Cursor.FIELD_TYPE_BLOB:
						_row.add( c.getBlob( i ) );
						break;
					}
				}
				_rs.add( _row );
			}while( c.moveToNext() );
			_cursor = 0;
			_row = _rs.get( 0 );
		}
	}
}



public byte[] getBlob( int columnIndex ){
	return (byte[]) _row.get( columnIndex );
}

public double getDouble( int columnIndex ){
	return (Double) _row.get( columnIndex );
}

public float getFloat( int columnIndex ){
	return (Float) _row.get( columnIndex );
}

public int getInt( int columnIndex ){
	return (Integer) _row.get( columnIndex );
}

public long getLong( int columnIndex ){
	return (Long) _row.get( columnIndex );
}

public short getShort( int columnIndex ){
	return (Short) _row.get( columnIndex );
}

public String getString(int columnIndex ){
	return (String) _row.get( columnIndex );
}

public int getColumnCount(){
	return _columeCount;
}

public int getColumnIndex( String columnName ){
	return _fields.indexOf( columnName );
}

public String getColumnName(int columnIndex ){
	return _fieldsArray[columnIndex];
}

public String[] getColumnNames(){
	return _fieldsArray;
}

public int getCount(){
	return _length;
}

public int getPosition(){
	return _cursor;
}

public int getType( int columnIndex ){
	return _fieldTypes.get( columnIndex );
}

public boolean isAfterLast(){
	return _cursor == _length - 2;
}

public boolean isBeforeFirst(){
	return _cursor == 1;
}

public boolean isFirst(){
	return _cursor == 0;
}

public boolean isLast(){
	return _cursor == _length - 1;
}

public boolean isNull( int columnIndex ){
	return _row.get( columnIndex ) == null;
}

public boolean move( int offset ){
	int i = _cursor + offset;
	if( i < _length ){
		_cursor = i;
		_row = _rs.get( _cursor );
		return true;
	}else return false;
}

public boolean moveToFirst(){
	_cursor = 0;
	_row = _rs.get( _cursor );
	return true;
}

public boolean moveToLast(){
	_cursor = _length - 1;
	_row = _rs.get( _cursor );
	return true;
}

public boolean moveToNext(){
	if( _cursor < _length - 1 ){
		_cursor++;
		_row = _rs.get( _cursor );
		return true;
	}else return false;
}

public boolean moveToPosition( int position ){
	if( position > -1 && position < _length ){
		_cursor = position;
		_row = _rs.get( _cursor );
		return true;
	}else return false;
}

public boolean moveToPrevious(){
	if( _cursor > 0 ){
		_cursor--;
		_row = _rs.get( _cursor );
		return true;
	}else return false;
}

public boolean isClosed(){
	return true;
}

public int getColumnIndexOrThrow( String s ) throws IllegalArgumentException {
	return 0;
}

public boolean getWantsAllOnMoveCalls(){
	return false;
}

public void close(){
}

public void copyStringToBuffer( int columnIndex, CharArrayBuffer buffer ){
}

public void deactivate(){
}

public Uri getNotificationUri(){
	return null;
}

public void setExtras(Bundle extras) {
	mExtras = (extras == null) ? Bundle.EMPTY : extras;
}

public Bundle getExtras(){
	return null;
}

public void registerContentObserver( ContentObserver observer ){
}

public void registerDataSetObserver( DataSetObserver observer ){
}

public boolean requery(){
	return false;
}

public Bundle respond(Bundle extras ){
	return null;
}

public void setNotificationUri(ContentResolver cr, Uri uri ){
}

public void unregisterContentObserver( ContentObserver observer ){
}

public void unregisterDataSetObserver( DataSetObserver observer ){
}

}
