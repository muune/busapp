package teamdoppelganger.smarterbus.util.common;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mocoplex.adlib.AdlibAdViewContainer;
import com.mocoplex.adlib.nativead.layout.AdlibNativeLayout;

import java.util.List;

import teamdoppelganger.smarterbus.R;
import teamdoppelganger.smarterbus.SBSearchActivity;
import teamdoppelganger.smarterbus.common.Constants;

/**
 * Created by muune on 2017-10-19.
 */

public class SearchCommonFunction {


    public static class BusHolder extends RecyclerView.ViewHolder {
        public TextView name, subname, typename;
        public ImageButton delBtn;
        public BusHolder(View itemView) {
            super(itemView);
            name = (TextView)itemView.findViewById(R.id.name);
            subname = (TextView)itemView.findViewById(R.id.subname);
            typename = (TextView)itemView.findViewById(R.id.typename);
            delBtn = (ImageButton)itemView.findViewById(R.id.delBtn);
        }
    }

    public static class StopHolder extends RecyclerView.ViewHolder {
        public TextView name, subname;
        public ImageButton delBtn;
        public StopHolder(View itemView) {
            super(itemView);
            name = (TextView)itemView.findViewById(R.id.name);
            subname = (TextView)itemView.findViewById(R.id.subname);
            delBtn = (ImageButton)itemView.findViewById(R.id.delBtn);
        }
    }

    public static class AdHolder extends RecyclerView.ViewHolder {
        public AdlibNativeLayout view;
        public AdlibAdViewContainer ablib;
        public AdHolder(View itemView) {
            super(itemView);
            view = (AdlibNativeLayout)itemView;
            ablib = (AdlibAdViewContainer)itemView.findViewById(R.id.adlib);
        }
    }


    public static class SearchAdapter extends  RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private final int VIEW_TYPE_BUS = 0;
        private final int VIEW_TYPE_STOP = 1;
        private final int VIEW_TYPE_AD = 2;

        private int type;
        private boolean isSearchMode;
        private SBSearchActivity context;
        private List<Object> _c;

        public SearchAdapter(SBSearchActivity context){
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder retHolder = null;
            if (viewType == VIEW_TYPE_BUS) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_bus_row, parent, false);
                retHolder = new BusHolder(itemView);
            } else if (viewType == VIEW_TYPE_STOP) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_stop_row, parent, false);
                retHolder = new StopHolder(itemView);
            } else {
                View itemView = new AdlibNativeLayout(context, R.layout.admixer);
                retHolder = new AdHolder(itemView);
            }
            return retHolder;
        }

        @Override
        public int getItemViewType(int position) {
            int l = _c.size();
            if(l <= 2 && position == l) return VIEW_TYPE_AD;
            else if(position == 2) return VIEW_TYPE_AD;
            else{
                if(type == Constants.BUS_TYPE) return VIEW_TYPE_BUS;
                else return VIEW_TYPE_STOP;
            }
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof AdHolder) { //뷰가 광고일 때
                ((AdHolder) holder).ablib.setVisibility(View.VISIBLE);
                context.aManager.bindAdsContainer(((AdHolder) holder).ablib);
            } else if (holder instanceof BusHolder) {
                //BusStopItem obj = _c.get(_c.size() > 2 && position > 2 ? position-1 : position);
            } else if (holder instanceof StopHolder) {
                //BusStopItem obj = _c.get(_c.size() > 2 && position > 2 ? position-1 : position);
            }
        }

        @Override
        public int getItemCount() {
            return _c == null ? 1 : _c.size()+1;
        } //광고가 들어가서 +1을 시킴

        public void render(List<Object> c, int $type, boolean $isSearchMode) {
            isSearchMode = $isSearchMode;
            type = $type;
            _c = c;
            notifyDataSetChanged();
        }
    }

}
