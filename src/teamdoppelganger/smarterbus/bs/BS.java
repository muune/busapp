package teamdoppelganger.smarterbus.bs;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

public class BS {
    static public final int NONE = 0;
    static public final int CONTEXT = 1;
    static public final int ACTIVITY = 2;

    static private ExecutorService _workers = Executors.newFixedThreadPool( 5 );

    static{
        StrictMode.setThreadPolicy( new StrictMode.ThreadPolicy.Builder().permitAll().build() );
    }

    static public void worker( Runnable $run ){
        _workers.execute( $run );
    }

    static private boolean _isDebug = false;

    static public void debugMode( boolean $is ){
        _isDebug = $is;
    }

    static public String log( String $msg ){
        if( _isDebug ) Log.i( "BS", $msg );
        return $msg;
    }



    //region String casting

    final static private String _strSharp = "#";

    static public String str( final InputStream $v ){
        StringBuilder sb = new StringBuilder( 200 );
        try{
            byte[] b = new byte[4096];
            for( int n ; ( n = $v.read( b ) ) != -1 ; ) sb.append( new String( b, 0, n ) );
            $v.close();
        }catch( Exception $e ){
            ///throw new Error( log( "BsStr.str:" + $e.toString() ) );
        }
        return trim( sb.toString() );
    }

    static public String str( final int $v ){
        return Integer.toString( $v );
    }

    static public String str( final int[] $v ){
        String t0 = Arrays.toString( $v );
        return t0.substring( 1, t0.length() - 1 );
    }

    static public String str( final float[] $v ){
        String t0 = Arrays.toString( $v );
        return t0.substring( 1, t0.length() - 1 );
    }

    static public String str( final String[] $v ){
        String t0 = Arrays.toString( $v );
        return t0.substring( 1, t0.length() - 1 );
    }

    static public String str( final boolean[] $v ){
        String t0 = Arrays.toString( $v );
        return t0.substring( 1, t0.length() - 1 );
    }

    static public String str( final float $v ){
        return Float.toString( $v );
    }

    static public String str( final boolean $v ){
        return $v ? "true" : "false";
    }

    static public int str2int( final String $v ){
        return Integer.parseInt( $v, 10 );
    }

    static public float str2float( final String $v ){
        return Float.parseFloat( $v );
    }
    static public double str2double( final String $v ){
        return Double.parseDouble($v);
    }

    static public boolean str2bool( final String $v ){
        return $v.equalsIgnoreCase( "true" ) || $v.equals( "1" ) ? Boolean.TRUE : Boolean.FALSE;
    }

    static public int str2color( final String $v ){
        if( $v.charAt( 0 ) == '#' ){
            return Color.parseColor($v);
        }else{
            return Color.parseColor( _strSharp + $v );
        }
    }

//endregion
//region StringUtil

    static public String right( String $v, int $right ){
        return $v.substring( $v.length() - $right );
    }

    static public String trim( String $v ){
        return $v.trim();
    }

    static public boolean isEmpty( String $v ){
        return $v == null || $v.length() == 0;
    }

    static public String[] trim( String[] $v ){
        for( int i = 0, j = $v.length ; i < j ; i++ ) $v[i] = $v[i].trim();
        return $v;
    }

    static public LinkedList<String> trim(LinkedList<String> $v ){
        for( int i = 0, j = $v.size() ; i < j ; i++ ) $v.set( i, $v.get( i ).trim() );
        return $v;
    }

    static public String replace( final String $v, final String $from, final String $to ){
        int i = $v.lastIndexOf( $from );
        if( i < 0 ) return $v;
        {
            StringBuilder sb = new StringBuilder( $v.length() + ( $to.length() - $from.length() ) * 10 ).append( $v );
            int j = $from.length();
            while( i > -1 ){
                sb.replace( i, ( i + j ), $to );
                i = $v.lastIndexOf( $from, i - 1 );
            }
            return sb.toString();
        }
    }

    @SuppressWarnings("rawtypes")
    static public String join( final LinkedList $v ){
        return join( $v, "," );
    }

    @SuppressWarnings("rawtypes")
    static public String join( final LinkedList $v, final String $sep ){
        StringBuilder sb = new StringBuilder( $v.get( 0 ).toString() );
        for( int i = 1, j = $v.size() ; i < j ; i++ )
            sb.append( $sep ).append( $v.get( i ).toString() );
        return sb.toString();
    }
    static public String tmpl( final String $v, String ...$data ){
        StringBuilder sb = new StringBuilder( $v.length() + 100 ).append( $v );
        for( int i = 0, j = $data.length ; i < j ; i += 2 ){
            String k = "@" + $data[i] + "@";
            do{
                int l = sb.indexOf( k );
                if( l == -1 ) break;
                sb.replace( l, l + k.length(), $data[i + 1] );
            }while( true );
        }
        return sb.toString();
    }

    static public String tmpl( final String $v, final HashMap<String, String> $data ){
        StringBuilder sb = new StringBuilder( $v.length() + 100 ).append( $v );
        for( Map.Entry<String, String> map : $data.entrySet() ){
            String k = "@" + map.getKey() + "@";
            do{
                int l = sb.indexOf( k );
                if( l == -1 ) break;
                sb.replace( l, l + k.length(), map.getValue() );
            }while( true );
        }
        return sb.toString();
    }

    static private LinkedList<String> split( String $v, String $sep, boolean $isTrim ){
        int i = 0, j = $v.indexOf( $sep );
        if( j > -1 ){
            LinkedList<String> result = new LinkedList<String>();
            for( ; j > -1 ; j = $v.indexOf( $sep, i ) )
                result.add( $isTrim ? $v.substring( i++, j ).trim() : $v.substring( i++, j ) );
            if( i < $v.length() - 1 )
                result.add( $isTrim ? $v.substring( i ).trim() : $v.substring( i ) );
            return result;
        }
        return null;
    }

    static public String strIsNum( final String $str ){
        int k = 0;
        for( int i = 0, j = $str.length() ; i < j ; i++ ){
            switch( $str.charAt( i ) ){
                case '-':
                    if( i > 0 ) return "string";
                    break;
                case '.':
                    if( k > 0 ) return "string";
                    else k++;
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
                default:
                    return "string";
            }
        }
        return k == 0 ? "int" : "float";
    }

    static public boolean strIsUrl( final String $v ){
        return $v.substring( 0, 4 ).equals( "http" );
    }

    //endregion


    //region http

    static public String get( final Run<String> $end, final String $uri, String... arg ){
        ArrayList<String> t0 = httpParam( arg );
        return httpRun( $end, "GET", !t0.get( 0 ).equals( "" ) ? $uri + "?" + t0.get( 0 ).equals( "" ) : $uri, t0 );
    }
    static public String post( final Run<String> $end, final String $uri, String... arg ){
        return httpRun( $end, "POST", $uri, httpParam( arg ) );
    }
    static private ArrayList<String> httpParam( String[] arg ){
        int i = 0, j = arg.length;
        boolean isJson = false;
        String t0 = "";
        ArrayList<String> t1 = new ArrayList<String>();
        t1.add( "" );
        try{
            while( i < j ){
                String k = arg[i++];
                String v = arg[i++];
                if( k.charAt( 0 ) == '@' ){
                    t1.add( k.substring( 1 ) );
                    t1.add( v );
                }else{
                    if(k.equals("json")){
                        isJson = true;
                        t1.add("isj"); t1.add("1");
                        t0 = v;
                    }else{
                        if(!isJson) t0 += "&" + URLEncoder.encode( k, "UTF-8" ) + "=" + URLEncoder.encode( v, "UTF-8" ); //json 키가 있는 경우면 전부 무시됨
                    }
                }
            }
            if(isJson){
                t1.set(0, t0);
            }else{
                t1.set(0, t0.substring(1));
            }

        }catch( Exception e ){
        }
        return t1;
    }

    static private String httpRun( final Run<String> $end, final String $method, final String $uri, final ArrayList<String> $data ){
        final String[] result = {null};
        Runnable run = new Runnable(){
            @Override
            public void run(){
                String r = null;
                try{
                    HttpURLConnection conn = (HttpURLConnection) new URL( $uri ).openConnection();
                    conn.setRequestMethod( $method );
                    conn.setConnectTimeout( 10000 );
                    conn.setReadTimeout( 10000 );
                    conn.setRequestProperty( "Connection", "Keep-Alive" );
                    conn.setUseCaches( false );
                    //conn.setRequestProperty( "Accept-Encoding", "gzip" );
                    int i = 1, j = $data.size();
                    while( i < j ) conn.setRequestProperty( $data.get( i++ ), $data.get( i++ ) );
                    if( !$method.equals( "GET" ) ){
                        conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
                        String body = $data.get( 0 );
                        if( body.length() > 0 ){
                            conn.setDoInput( true );
                            conn.setDoOutput( true );
                            OutputStreamWriter out = new OutputStreamWriter( conn.getOutputStream() );
                            out.write( body );
                            out.flush();
                            out.close();
                        }

                    }
                    conn.connect();
                    r = "";
                    if( conn.getResponseCode() == HttpURLConnection.HTTP_OK ){
                        InputStream is = conn.getInputStream();
                        String gzip = conn.getHeaderField( "gzip" );
                        if( gzip != null && gzip.equalsIgnoreCase( "Accept-Encoding" ) )
                            is = new GZIPInputStream( is );
                        r = str(is);
                    }
                }catch( Exception e ){
                    e.printStackTrace();
                    log( "http Error:" + $uri + ":" + e.toString() );
                }
                if( $end == null ) result[0] = r;
                else $end.run(r);
            }
        };
        if( $end == null ){
            run.run();
            return result[0];
        }else{
            worker( run );
            return null;
        }
    }

    //endregion

}
