package teamdoppelganger.smarterbus.util.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.item.LocalNotiItem;
import teamdoppelganger.smarterbus.item.NotificationItem;
import teamdoppelganger.smarterbus.item.RecentItem;

import android.os.AsyncTask;

public class CommonNotiLib {


    GetDataListener mGetDataListener;


    public interface GetDataListener {
        public void onReceiveRecent(RecentItem item);

        public void onReceiveNoti(NotificationItem item);

    }


    public void setDataListener(GetDataListener l) {
        mGetDataListener = l;
    }


    public static final String getSource(String urlString, boolean isPost, String valuePair, String encodingStyle) {

        String result = null;
        boolean isPass = false;

        HttpURLConnection cnx = null;

        try {

            if (!isPost) {
                if (!valuePair.trim().equals("")) {
                    urlString = urlString + "?" + valuePair;
                }
            }

            URL url = new URL(urlString);
            cnx = (HttpURLConnection) url.openConnection();
            cnx.setConnectTimeout(1000);
            cnx.setReadTimeout(1000);
            cnx.setDoOutput(true);
            cnx.setDoInput(true);
            cnx.setUseCaches(false);
            cnx.setRequestProperty("content-type", "application/x-www-form-urlencoded");

            if (isPost) {
                cnx.setRequestMethod("POST");
                OutputStreamWriter outStream = new OutputStreamWriter(cnx.getOutputStream(), encodingStyle);
                PrintWriter writer = new PrintWriter(outStream);
                writer.write(valuePair);
                writer.flush();
            } else {
                cnx.setRequestMethod("GET");
            }

            byte[] w = new byte[1024];
            int size = 0;
            InputStream inputStream = cnx.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            while (true) {
                size = inputStream.read(w);

                if (size <= 0) {
                    break;
                }

                out.write(w, 0, size);
            }
            out.close();
            inputStream.close();
            result = new String(out.toByteArray(), encodingStyle);

        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (cnx != null) {
                cnx.disconnect();
            }
        }

        return result;

    }


    public void getRecentVersion(int appVersion, String localList) {

        new GetData().execute(String.valueOf(Constants.GET_RECENT), Constants.RECENT_URL, String.valueOf(appVersion), String.valueOf(Constants.MARKET_TYPE), localList);

    }

    public void getNotification(int id, String localCityIds) {

        new GetData().execute(String.valueOf(Constants.GET_NOTI), Constants.NOTI_URL, String.valueOf(id), String.valueOf(Constants.MARKET_TYPE), localCityIds);

    }


    class GetData extends AsyncTask<String, String, String> {

        String _result;
        int _type;

        @Override
        protected String doInBackground(String... params) {

            _type = Integer.parseInt(params[0]);
            String url = params[1];
            String id = params[2];
            String marketType = params[3];
            String location = params[4];

            if (_type == Constants.GET_RECENT) {
                _result = getSource(url, false, "version=" + id + "&marketType=" + marketType + "&location=" + location, "utf-8");
            } else if (_type == Constants.GET_NOTI) {
                _result = getSource(url, false, "id=" + id + "&marketType=" + marketType + "&location=" + location, "utf-8");
            }

            return null;

        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
            setItem(_type, _result);
        }

    }


    public void setItem(int type, String result) {

        if (result == null || result.trim().length() == 0) return;

        if (type == Constants.GET_RECENT) {

            JSONObject object;
            try {
                object = new JSONObject(result);
                JSONArray jsonAry = object.getJSONArray("item");


                ArrayList<LocalNotiItem> arrayLocalNoti = new ArrayList<LocalNotiItem>();

                int version = Integer.parseInt(object.getString("id"));


                RecentItem recentItem = new RecentItem();
                recentItem.id = version;

                if (jsonAry != null) {
                    for (int i = 0; i < jsonAry.length(); i++) {
                        JSONObject tmpObj = (JSONObject) jsonAry.get(i);
                        String cityId = tmpObj.getString("city_id");
                        String cityRelateId = tmpObj.getString("local_id");

                        LocalNotiItem localNotiItem = new LocalNotiItem();
                        localNotiItem.cityId = cityId;
                        localNotiItem.id = cityRelateId;

                        arrayLocalNoti.add(localNotiItem);
                    }
                    recentItem.localNotiItem.addAll(arrayLocalNoti);
                }


                if (mGetDataListener != null) {
                    mGetDataListener.onReceiveRecent(recentItem);
                }

				
            } catch (Exception e) {
                e.printStackTrace();
            }


        } else if (type == Constants.GET_NOTI) {

            JSONObject object;
            try {
                object = new JSONObject(result);
                String id = object.getString("id");
                String title = object.getString("title");
                String contents = object.getString("contents");
                String buttonType = object.getString("buttonType");
                String link = object.getString("link");
                String minVersion = object.getString("minVersion");
                String maxVersion = object.getString("maxVersion");
                String buttonName = object.getString("buttonName");


                JSONArray jsonAry = object.getJSONArray("item");


                NotificationItem notiItem = new NotificationItem();
                if (id != null && id.length() > 0) {
                    notiItem.id = Integer.parseInt(id);
                    notiItem.title = title;
                    notiItem.contents = contents;
                    notiItem.buttonType = Integer.parseInt(buttonType);
                    notiItem.link = link;
                    notiItem.minVersion = Integer.parseInt(minVersion);
                    notiItem.maxVersion = Integer.parseInt(maxVersion);
                    notiItem.buttonName = buttonName;
                } else {
                    //지역 공지만 있는 경우

                }


                if (jsonAry != null) {

                    for (int i = 0; i < jsonAry.length(); i++) {

                        JSONObject localJsonObj = (JSONObject) jsonAry.get(i);

                        LocalNotiItem localNotiItem = new LocalNotiItem();
                        localNotiItem.id = localJsonObj.getString("local_id");
                        localNotiItem.cityId = localJsonObj.getString("city_id");
                        localNotiItem.body = URLDecoder.decode(localJsonObj.getString("contents"));

                        notiItem.localNotiItem.add(localNotiItem);
                    }


                }


                if (mGetDataListener != null) {
                    mGetDataListener.onReceiveNoti(notiItem);
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            ;


        }

    }
}
