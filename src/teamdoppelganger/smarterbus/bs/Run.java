package teamdoppelganger.smarterbus.bs;

public abstract class Run<T> implements Runnable {

static public Run NONE = new Run(){
	@Override
	public void run( Object $data ){
	}
};

public void run(){
	run( null );
}

public abstract void run( T $data );
}
