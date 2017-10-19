package teamdoppelganger.smarterbus;

import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.smart.lib.CommonConstants;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.common.SBBaseActivity;
import teamdoppelganger.smarterbus.item.AutoItem;
import teamdoppelganger.smarterbus.util.common.RequestCommonFuction;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SBAutoCompleteActivity extends SBBaseActivity {

    EditText mInputText;
    ListView mListView;
    ImageView mSeachDel;

    ArrayList<AutoCompleteGetData> mCacheList = new ArrayList<AutoCompleteGetData>();
    ArrayList<AutoItem> mAutoItem = new ArrayList<AutoItem>();

    AutoItem mResultAutoItem;

    DetailAdapter mDetailAdapter;


    String mKey = "AIzaSyCROhKddRttTgAr96Zv3CNUm9SG034S7bQ";
    int mIndex = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.autocompletelayout);

        mIndex = getIntent().getExtras().getInt(Constants.AUTO_INDEX);

        initView();

        mDetailAdapter = new DetailAdapter();
        mListView.setAdapter(mDetailAdapter);

        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {

                new AutoCompleteDetailGetData().execute(mAutoItem.get(position));

            }
        });

    }


    private void initView() {
        mInputText = (EditText) findViewById(R.id.inputText);
        mListView = (ListView) findViewById(R.id.list);
        mSeachDel = (ImageView) findViewById(R.id.seachDel);

        mInputText.setHint(Html.fromHtml("<small>" + getString(R.string.hint_auto_search) + "</small>"));

        if (mIndex == 1) {

            mInputText.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_course_start), null, null, null);
            mInputText.setCompoundDrawablePadding(8);

        } else {

            mInputText.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_course_arrival), null, null, null);
            mInputText.setCompoundDrawablePadding(8);

        }


        mInputText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() > 1) {
                    for (int i = 0; i < mCacheList.size(); i++) {
                        mCacheList.get(i).cancel(true);
                    }
                    mCacheList.clear();

                    AutoCompleteGetData getData = new AutoCompleteGetData();
                    mCacheList.add(getData);
                    getData.execute(s.toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });

        mSeachDel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mInputText.setText("");

            }
        });

    }

    class AutoCompleteDetailGetData extends AsyncTask<AutoItem, AutoItem, AutoItem> {
        @Override
        protected AutoItem doInBackground(AutoItem... params) {

            AutoItem autoItem = params[0];

            String url = "https://maps.googleapis.com/maps/api/place/details/json";
            String param = String.format("reference=%s&sensor=true&key=%s", autoItem.refer, mKey);

            String result = RequestCommonFuction.getSource(url, false, param, "utf-8");

            JSONObject object;

            try {
                object = new JSONObject(result);

                JSONObject jsonObject = object.getJSONObject("result").getJSONObject("geometry").getJSONObject("location");

                if (jsonObject != null) {
                    Double latValue = (double) jsonObject.getDouble("lat");
                    Double longVaue = (double) jsonObject.getDouble("lng");

                    autoItem.latitude = latValue;
                    autoItem.longtude = longVaue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            if (autoItem.city1 == null) {
                String otherCityName = "";
                url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
                param = String.format("location=%s&radius=10&key=%s", autoItem.latitude + "," + autoItem.longtude, mKey);
                result = RequestCommonFuction.getSource(url, false, param, "utf-8");

                try {

                    object = new JSONObject(result);
                    JSONArray jsonArray1 = object.getJSONArray("results");
                    JSONObject value = (JSONObject) jsonArray1.get(0);

                    if (value != null) {
                        otherCityName = value.getString("name");
                        url = "https://maps.googleapis.com/maps/api/place/autocomplete/json";
                        param = String.format("sensor=false&key=%s&input=%s", mKey, URLEncoder.encode(otherCityName));

                        result = RequestCommonFuction.getSource(url, false, param, "utf-8");

                        JSONObject object1;
                        try {
                            object1 = new JSONObject(result);

                            JSONArray jsonArray = object1.getJSONArray("predictions");

                            for (int i = 0; i < jsonArray.length(); i++) {

                                AutoItem autoItem1 = new AutoItem();

                                JSONObject value1 = (JSONObject) jsonArray.get(i);
                                String description = value1.getString("description");

                                autoItem.description = description;

                                if (description != null) {

                                    String split[] = description.split(" ");

                                    if (split.length > 1) {
                                        autoItem.city1 = split[1].trim();
                                    }

                                    if (split.length > 2) {
                                        autoItem.city2 = split[2].trim();
                                    }

                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        ;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                ;
            }


            mResultAutoItem = autoItem;
            return null;
        }

        @Override
        protected void onPostExecute(AutoItem result) {
            super.onPostExecute(result);

            if (mResultAutoItem != null) {
                Intent _intent = new Intent();
                _intent.putExtra(Constants.INTENT_AUTOITEM, mResultAutoItem);
                setResult(RESULT_OK, _intent);
                finish();
            }

        }
    }


    class AutoCompleteGetData extends AsyncTask<String, String, String> {


        ArrayList<AutoItem> _autoItem;

        public AutoCompleteGetData() {
            _autoItem = new ArrayList<AutoItem>();

        }

        @Override
        protected String doInBackground(String... params) {

            if (isCancelled()) return null;

            String searchText = params[0];

            String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json";

            String param = String.format("sensor=false&key=%s&input=%s", mKey, URLEncoder.encode(searchText));

            String result = RequestCommonFuction.getSource(url, false, param, "utf-8");


            JSONObject object;
            try {
                object = new JSONObject(result);

                JSONArray jsonArray = object.getJSONArray("predictions");

                for (int i = 0; i < jsonArray.length(); i++) {

                    AutoItem autoItem = new AutoItem();

                    JSONObject value = (JSONObject) jsonArray.get(i);
                    String description = value.getString("description");
                    String refer = value.getString("reference");

                    JSONArray termsAry = value.getJSONArray("terms");
                    if (termsAry.length() > 0) {
                        JSONObject obj = (JSONObject) termsAry.get(0);
                        String name = obj.getString("value");
                        autoItem.name = name;
                    }

                    autoItem.description = description;
                    autoItem.refer = refer;

                    if (description != null) {

                        String split[] = description.split(" ");

                        if (split.length > 1) {
                            autoItem.city1 = split[1].trim();
                        }

                        if (split.length > 2) {
                            autoItem.city2 = split[2].trim();
                        }

                    }

                    _autoItem.add(autoItem);


                }


            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (isCancelled()) return;

            mAutoItem.clear();
            mAutoItem.addAll(_autoItem);

            mDetailAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }


    }


    class DetailAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return mAutoItem.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder viewHolder = null;

            if (view == null) {
                viewHolder = new ViewHolder();
                view = getLayoutInflater().inflate(R.layout.auto_row,
                        parent, false);

                viewHolder.text = (TextView) view.findViewById(R.id.lineName);
                viewHolder.textSub = (TextView) view.findViewById(R.id.lineSubName);


                view.setTag(viewHolder);
            } else {

                viewHolder = (ViewHolder) view.getTag();
            }


            AutoItem autoItem = mAutoItem.get(position);
            String name = autoItem.name;
            String description = autoItem.description;


            SpannableStringBuilder sp = null;
            String highLighStr = mInputText.getText().toString();
            if (highLighStr != null && autoItem.name.indexOf(highLighStr) != -1) {
                sp = new SpannableStringBuilder(name);
                sp.setSpan(new ForegroundColorSpan(Color.RED), autoItem.name.indexOf(highLighStr), autoItem.name.indexOf(highLighStr) + highLighStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            if (sp != null) {
                viewHolder.text.setText(sp);
            } else {
                viewHolder.text.setText(name);
            }

            viewHolder.textSub.setText(description);

            return view;

        }


        class ViewHolder {
            TextView text;
            TextView textSub;
        }

    }


}
