package teamdoppelganger.smarterbus.bs;

import android.os.Message;

import java.util.HashMap;
import java.util.Stack;

public class P extends HashMap<Object, Object> {

static private Stack<P> _pool = new Stack<P>();

static public P pool(){
	return _pool.size() > 0 ? _pool.pop() : new P();
}

static public void pool( P $v ){
	$v.clear();
	_pool.push( $v );
}

public BS bs;

private P(){
}

public String STRING(Object $key ){
	return (String) get( $key );
}

public int INT( Object $key ){
	return (Integer) get( $key );
}

public float FLOAT( Object $key ){
	return (Float) get( $key );
}

public boolean BOOLEAN( Object $key ){
	return (Boolean) get( $key );
}

public long LONG( Object $key ){
	return (Long) get( $key );
}

public Runnable RUNNABLE(Object $key ){
	return (Runnable) get( $key );
}

public Message MESSAGE(Object $key ){
	return (Message) get( $key );
}

public Run RUN( Object $key ){
	return (Run) get( $key );
}

public Run<P> RUN_P( Object $key ){
	return (Run<P>) get( $key );
}
}