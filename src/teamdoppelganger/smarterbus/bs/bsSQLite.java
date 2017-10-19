package teamdoppelganger.smarterbus.bs;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class bsSQLite extends SQLiteOpenHelper {

static private Map<String, bsSQLite> _pool = Collections.synchronizedMap(new HashMap<String, bsSQLite>());

static public bsSQLite pool(Context $context, String $db, Object... $arg ){
	if( !_pool.containsKey( $db ) )
		_pool.put( $db, new bsSQLite( $context, $db, 1, $arg.length == 1 && $arg[0] instanceof Object[] ? (Object[]) $arg[0] : $arg ) );
	return _pool.get( $db );
}

static public void pool( bsSQLite $v ){
	$v.close();
	P.pool( $v.p );
}


private String d;
private SQLiteDatabase w;
private SQLiteDatabase r;
public P p;
private Map<String, Query> querys = new HashMap<>();
public bsSQLite(Context $context, String $database, int $ver, Object[] $arg ){
	super( $context, $database, null, $ver );
	d = $database;
	w = this.getWritableDatabase();
	r = this.getReadableDatabase();
	p = P.pool();
	if($arg != null){
		int i = 0, j = $arg.length;
		while( i < j ) p.put( $arg[i++], $arg[i++] );
	}
	p.put( BS.CONTEXT, $context );
}

public String database(){
	return d;
}

public void onCreate( SQLiteDatabase db ){
}

public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion ){
}

public synchronized void queryLoad(final String... $sqlFiles) {
	try {
		for(String f : $sqlFiles){
			InputStream is = ((Context)p.get(BS.CONTEXT)).getAssets().open(f);
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			String str = new String(buffer, "UTF-8");
			String lines[] = str.split("\n");
			String k = "";
			StringBuilder q = new StringBuilder();
			for (String line: lines) {
				String l = line.trim();
				if(l.length() > 1 && l.substring(0, 1).equals("#")){
					q.trimToSize();
					if(!k.equals("") && !q.equals("")){
						querys.put(k, new Query(k, q.toString()));
						BS.log("[SQL]" + k + " = " + q);
						q.delete(0, q.length() - 1);
					}
					k = l.substring(1);
					int i = l.indexOf(":");
					k = i == -1 ? k.trim() : k.substring(0, i - 1).trim();
					continue;
				}
				q.append(l);
				q.append(" ");
			}
			q.trimToSize();
			if(!k.equals("") && !q.equals("")){
				querys.put(k, new Query(k, q.toString()));
				BS.log("[SQL]" + k + " = " + q);
			}
		}
	} catch (IOException ex) {
		ex.printStackTrace();
	}
}
final public Query query(@NonNull final String $k){
	return querys.containsKey($k) ? querys.get($k) : null;
}
final public void query(@NonNull final String $k, @Nullable final String $sql){
	String k = $k.trim();
	if(k.length() == 0) return;
	if($sql == null){
		if(querys.containsKey(k)) querys.remove(k);
	}else querys.put(k, new Query(k, $sql));
}
final public Cursor select(@NonNull String $key, String... $arg){
	Query query = query($key);
	if(query == null) return null;
	List<String> q = query.prepare(r, $arg);
	if(q == null) return null;
	String sql = (String)q.get(0);
	List<String> arg = null;
	if(q.size() > 1) arg = q.subList(1, q.size());
	Cursor c = r.rawQuery(sql, arg == null ? new String[0] : arg.toArray(new String[arg.size()]));
	return c != null && c.getCount() > 0 && c.moveToFirst() ? c : null;
}
final public int selectInt(@NonNull String $key, String... $arg){
	Cursor c = select($key, $arg);
	int result = 0;
	if(c == null) return result;
	if( c.getCount() == 0 ) {
		c.close();
	}else{
		bsCursor _c = new bsCursor(c);
		result = _c.getInt(0);
		c.close();
	}
	return result;
}
final public String selectStr(@NonNull String $key, String... $arg){
	Cursor c = select($key, $arg);
	String result = null;
	if(c == null) return result;
	if( c.getCount() == 0 ) {
		c.close();
	}else{
		bsCursor _c = new bsCursor(c);
		result = _c.getString(0);
		c.close();
	}
	return result;
}
final public float selectFloat(@NonNull String $key, String... $arg){
	Cursor c = select($key, $arg);
	float result = 0;
	if(c == null) return result;
	if( c.getCount() == 0 ) {
		c.close();
	}else{
		bsCursor _c = new bsCursor(c);
		result = _c.getFloat(0);
		c.close();
	}
	return result;
}
final public int exec(@NonNull String $key, String... $arg){
	Query query = query($key);
	if(query == null) return 0;
	List<String> q = query.prepare(r, $arg);
	if(q == null) return 0;
	String sql = (String)q.get(0);
	if(q.size() > 1){
		List<String> arg = q.subList(1, q.size());
		w.execSQL(sql, arg.toArray(new String[arg.size()]));
	}else{
		w.execSQL(sql);
	}
	Cursor c = r.rawQuery( "SELECT changes()", null );
	return c != null && c.getCount() > 0 && c.moveToFirst() ? c.getInt( 0 ) : 0;
}
final public int lastId(){
	Cursor c = r.rawQuery(BS.log("select last_insert_rowid()"), null );
	return c != null && c.getCount() > 0 && c.moveToFirst() ? c.getInt( 0 ) : -1;
}

private enum FIELD_TYPE{NONE,INT,FLOAT,STRING}
private static Pattern queryArgPattern = Pattern.compile("@([^@]+)@", Pattern.CASE_INSENSITIVE);
private class Query{
	class Item{
		String key;
		FIELD_TYPE type;
		Item(String $k, FIELD_TYPE $t){
			key = $k;
			type = $t;
		}
	}

	private String sql;
	private String key;
	List<Item> noneItemList = new ArrayList<Item>();
	List<Item> itemList = new ArrayList<>();
	Query(@NonNull final String $key, @NonNull final String $query){
		key = $key;
		Matcher matcher = queryArgPattern.matcher($query);
		StringBuffer q = new StringBuffer();
		while(matcher.find()) {
			String grp = matcher.group();
			int i = grp.indexOf(":");
			if(i == -1){
				String k = grp.substring(1,grp.length()-1);
				matcher.appendReplacement(q, grp);
				noneItemList.add(new Item(k, FIELD_TYPE.NONE));
			}else{
				matcher.appendReplacement(q, "?");
				String k = grp.substring(1, i);
				String t = grp.substring(i + 1, grp.length()-1);
				if(t.equals("s") || t.equals("str") || t.equals("string")){
					itemList.add(new Item(k, FIELD_TYPE.STRING));
				}else if(t.equals("i") || t.equals("int") || t.equals("integer")){
					itemList.add(new Item(k, FIELD_TYPE.INT));
				}else if(t.equals("f") || t.equals("float") || t.equals("double") || t.equals("number")){
					itemList.add(new Item(k, FIELD_TYPE.FLOAT));
				}
			}
		}
		matcher.appendTail(q);
		sql = q.toString();
	}
	List<String> prepare(SQLiteDatabase $db, String...$arg){
		String s = sql;
		Map<String,String> arg = new HashMap<String,String>();
		for(int i = 0; i < $arg.length;) arg.put($arg[i++], $arg[i++]);
		for(int i = 0; i < noneItemList.size(); i++){
			Item item = noneItemList.get(i);
			if(!arg.containsKey(item.key)) return null;
			s = s.replace("@" + item.key + "@", arg.get(item.key));
		}
		List<String> result = new ArrayList<String>();
		result.add(s);
		for(int i = 0; i < itemList.size(); i++){
			Item item = itemList.get(i);
			String k = item.key;
			if(!arg.containsKey(k)) return null;
			String v = arg.get(k);
			if(v == null){
				result.add(null);
			}else if(item.type == FIELD_TYPE.STRING){
				result.add(v);
			}else if(item.type == FIELD_TYPE.INT){
				result.add(BS.str2int(v) + "");
			}else if(item.type == FIELD_TYPE.FLOAT){
				result.add(BS.str2double(v) + "");
			}
		}
		return result;
	}
}
}
