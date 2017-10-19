package teamdoppelganger.smarterbus;

import teamdoppelganger.smarterbus.common.SBBaseFragment;
import teamdoppelganger.smarterbus.util.db.LocalDBHelper;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;

/**
 * select된 fragment를 넘겨주는 클래스 
 * @author DOPPELSOFT4
 *
 */
public class SBControlFragment {
	
	public SBControlFragment(){
		
	}
    
	public static  Fragment newInstance(int position, SQLiteDatabase db, LocalDBHelper localDBHelper) {
		
		SBBaseFragment baseFragment = null;
		baseFragment= new SBFavoriteFragment(R.layout.favorite,db,localDBHelper);		
		
		switch(position){
		case 0:
			baseFragment= new SBFavoriteFragment(R.layout.favorite,db,localDBHelper);		
			break;
		case 1:
			baseFragment = new SBRecentSearchFragment(R.layout.recentesearch,db,localDBHelper);
			break;
		case 2:
			baseFragment = new SBSearchFragment(R.layout.search,db,localDBHelper);
			break;			
		case 3:
			baseFragment = new SBNearFragment(R.layout.near,db,localDBHelper);
			break;
		case 4:
			baseFragment = new SBSettingFragment(R.layout.setting,db,localDBHelper);
			break;			
		}
	
    	return baseFragment;

	}

}
