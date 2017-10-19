package teamdoppelganger.smarterbus.bs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class bsSetting {
    static private Map<String, bsSetting> _pool = Collections.synchronizedMap(new HashMap<String, bsSetting>());
    static public bsSetting pool(@NonNull Context $context, @NonNull final String $settingFile){
        if( !_pool.containsKey($settingFile) )
            _pool.put($settingFile, new bsSetting( $context, $settingFile ) );
        return _pool.get($settingFile);
    }

    private JSONObject setting = null;
    private bsSetting(@NonNull Context $context, @NonNull final String $settingFile){
        if(setting != null) return;
        try {
            BS.log("[Setting] Load 실시. file = " + $settingFile);
            InputStream is = $context.getAssets().open($settingFile);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            setting = new JSONObject(json);
        }catch (Exception e) {
            e.printStackTrace();
            BS.log("[Setting] Load 실패. msg=" + e.getMessage());
        }
    }
    public boolean isLoaded(){
        return setting != null;
    }
    @Nullable
    private Object getRecursive(@NonNull final String $k){
        if(setting == null) return null;
        String list[] = $k.split("\\.");
        Object ret = null;
        try{
            String k;
            if(list.length == 1){
                k = list[0];
                ret = setting.get(k);
            }else{
                JSONObject obj = setting.getJSONObject(list[0]);
                for(int i = 1, j = list.length - 1; i < j; i++){
                    k = list[i];
                    obj = obj.getJSONObject(k);
                }
                ret = obj.get(list[list.length-1]);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return ret;
    }
    public synchronized boolean getBoolean(@NonNull final String $k){
        try{
            Object v = getRecursive($k);
            if(v != null) return (boolean)v;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    public synchronized String getString(@NonNull final String $k){
        try{
            Object v = getRecursive($k);
            if(v != null) return (String)v;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
