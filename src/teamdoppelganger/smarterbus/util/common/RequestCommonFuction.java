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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.item.BusStopItem;

import android.util.Log;

import com.smart.lib.CommonConstants;

/**
 * 파싱을 할때 반복적ㅇ으로 사용하는 함수를 모와놓기 위해 만든 클래스
 *
 * @author DOPPELSOFT4
 */
public class RequestCommonFuction {

    private static final int CONNECTION_TIME = 3000;

    /**
     * @param url
     * @param isPost        // true:post형식의 데이터 false:get형식의 데이터
     * @param valuePair     //ex:aa=bb&dd=ee
     * @param encodingStyle //ex: euc-kr
     * @return 문자로 파싱한 결과
     */

    public static final String getSource(String urlString, boolean isPost,
                                         String valuePair, String encodingStyle) {

        String result = null;
        boolean isPass = false;


        HttpURLConnection cnx = null;

        try {

            if (!isPost) {
                if (!valuePair.trim().equals("")) {
                    urlString = urlString + "?" + valuePair;
                }
            } else {

            }

            URL url = new URL(urlString);
            cnx = (HttpURLConnection) url.openConnection();
            cnx.setConnectTimeout(6000);
            cnx.setReadTimeout(6000);
            cnx.setRequestProperty("Accept-Language", "ko-kr");

            cnx.setDoInput(true);
            cnx.setUseCaches(false);

            cnx.setRequestProperty("content-type",
                    "application/x-www-form-urlencoded");

            if (isPost) {
                cnx.setRequestMethod("POST");
                cnx.setDoOutput(true);
                OutputStreamWriter outStream = new OutputStreamWriter(
                        cnx.getOutputStream(), encodingStyle);
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

    public static final String getSource(String urlString, boolean isPost,
                                         String valuePair, String encodingStyle, String[] referer) {

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
            cnx.setConnectTimeout(7000);
            cnx.setDoOutput(true);
            cnx.setDoInput(true);
            cnx.setUseCaches(true);
            cnx.setRequestProperty("content-type",
                    "application/x-www-form-urlencoded");

            if (referer.length > 0) {
                String tmpKey = null;
                for (int i = 0; i < referer.length; i++) {
                    if (i % 2 == 0) {
                        tmpKey = referer[i].toString();
                    } else {
                        cnx.setRequestProperty(tmpKey, referer[i]);
                    }
                }
            }

            if (isPost) {
                cnx.setRequestMethod("POST");

                if (cnx.getOutputStream() == null)
                    return null;
                OutputStreamWriter outStream = new OutputStreamWriter(
                        cnx.getOutputStream(), encodingStyle);
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

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (cnx != null) {
                cnx.disconnect();
            }
        }

        return result;

    }

    public static int getApiTpye(int cityId) {

        int apiType = Constants.API_TYPE_1;
        if (cityId == CommonConstants.CITY_GIM_HEA._cityId) {
            apiType = Constants.API_TYPE_3;
        } else if (cityId == CommonConstants.CITY_GU_MI._cityId) {
            apiType = Constants.API_TYPE_3;
        } else if (cityId == CommonConstants.CITY_CHIL_GOK._cityId) {
            apiType = Constants.API_TYPE_3;
        } else if (cityId == CommonConstants.CITY_YANG_SAN._cityId) {
            apiType = Constants.API_TYPE_3;
        } else if (cityId == CommonConstants.CITY_GEO_JE._cityId) {
            apiType = Constants.API_TYPE_3;
        } else if (cityId == CommonConstants.CITY_GEO_JE._cityId) {
            apiType = Constants.API_TYPE_2;
        } else if (cityId == CommonConstants.CITY_CHUN_CHEON._cityId) {
            apiType = Constants.API_TYPE_2;
        } else if (cityId == CommonConstants.CITY_A_SAN._cityId) {
            apiType = Constants.API_TYPE_1;
        } else if (cityId == CommonConstants.CITY_MIR_YANG._cityId) {
            apiType = Constants.API_TYPE_3;
        } else if (cityId == CommonConstants.CITY_CHIL_GOK._cityId) {
            apiType = Constants.API_TYPE_3;
        } else if (cityId == CommonConstants.CITY_GWANG_JU._cityId) {
            apiType = Constants.API_TYPE_2;
        } else if (cityId == CommonConstants.CITY_MOK_PO._cityId) {
            apiType = Constants.API_TYPE_3;
        } else if (cityId == CommonConstants.CITY_UL_SAN._cityId) {
            apiType = Constants.API_TYPE_2;
        } else if (cityId == CommonConstants.CITY_DAE_GU._cityId) {
            //대구 정류장 apiId 가 유일하여 1만 비교하도록 수정
            apiType = Constants.API_TYPE_1;
        } else if (cityId == CommonConstants.CITY_DAE_JEON._cityId) {
            apiType = Constants.API_TYPE_2;
        } else if (cityId == CommonConstants.CITY_GIM_HEA._cityId) {
            apiType = Constants.API_TYPE_3;
        } else if (cityId == CommonConstants.CITY_TONG_YEONG._cityId) {
            apiType = Constants.API_TYPE_2;
        } else if (cityId == CommonConstants.CITY_YANG_SAN._cityId) {
            apiType = Constants.API_TYPE_3;
        } else if (cityId == CommonConstants.CITY_BU_SAN._cityId) {
            apiType = Constants.API_TYPE_3;
        } else if (cityId == CommonConstants.CITY_SE_JONG._cityId) {
            apiType = Constants.API_TYPE_3;
        }

        return apiType;
    }

    public static String getBusQury(BusStopItem busStopItem,
                                    HashMap<Integer, String> hashLocationEng) {

        String sql = null;

        if (busStopItem.apiType == Constants.API_TYPE_3) {

            sql = String.format("SELECT * FROM %s_Stop where %s='%s' ",
                    hashLocationEng.get(Integer
                            .parseInt(busStopItem.localInfoId)),
                    CommonConstants.BUS_STOP_ARS_ID, busStopItem.arsId);
        } else if (busStopItem.apiType == Constants.API_TYPE_1) {
            sql = String.format("SELECT * FROM %s_Stop where %s='%s' ",
                    hashLocationEng.get(Integer
                            .parseInt(busStopItem.localInfoId)),
                    CommonConstants.BUS_STOP_API_ID, busStopItem.apiId);
        } else if (busStopItem.apiType == Constants.API_TYPE_2) {

            // 파싱 데이터에 apiId가 없는 경우가 있다.
            if (busStopItem.arsId == null) {
                sql = String.format("SELECT * FROM %s_Stop where %s='%s' ",
                        hashLocationEng.get(Integer
                                .parseInt(busStopItem.localInfoId)),
                        CommonConstants.BUS_STOP_API_ID, busStopItem.apiId);
            }
            if (busStopItem.apiId == null) {
                sql = String.format("SELECT * FROM %s_Stop where %s='%s' ",
                        hashLocationEng.get(Integer
                                .parseInt(busStopItem.localInfoId)),
                        CommonConstants.BUS_STOP_ARS_ID, busStopItem.arsId);
            } else {
                sql = String.format(
                        "SELECT * FROM %s_Stop where %s='%s' and %s='%s' ",
                        hashLocationEng.get(Integer
                                .parseInt(busStopItem.localInfoId)),
                        CommonConstants.BUS_STOP_API_ID, busStopItem.apiId,
                        CommonConstants.BUS_STOP_ARS_ID, busStopItem.arsId);

            }

        } else if (busStopItem.apiType == Constants.API_TYPE_4) {

            sql = String.format("SELECT * FROM %s_Stop where %s='%s' ",
                    hashLocationEng.get(Integer
                            .parseInt(busStopItem.localInfoId)),
                    CommonConstants.BUS_STOP_DESC, busStopItem.tempId2);
        }

        return sql;
    }

    public static boolean getAlarmAndWigetAble(int cityId) {

        if (cityId == CommonConstants.CITY_GU_MI._cityId) {
            return true;
        } else if (cityId == CommonConstants.CITY_GEO_JE._cityId) {
            return false;
        } else if (cityId == CommonConstants.CITY_CHIL_GOK._cityId) {
            return false;
        } else if (cityId == CommonConstants.CITY_NA_JU._cityId) {
            return true;
        } else if (cityId == CommonConstants.CITY_SUN_CHEON._cityId) {
            return true;
        } else if (cityId == CommonConstants.CITY_GWANG_YANG._cityId) {
            return true;
        } else if (cityId == CommonConstants.CITY_TONG_YEONG._cityId) {
            return false;
        } else if (cityId == CommonConstants.CITY_DAMYANG._cityId) {
            return false;
        } else if (cityId == CommonConstants.CITY_JANGSEONG._cityId) {
            return false;
        }

        return true;

    }

    public static boolean getSpecialInfoGetAble(int cityId) {
        if (cityId == CommonConstants.CITY_GEO_JE._cityId) {
            return false;
        } else if (cityId == CommonConstants.CITY_NA_JU._cityId) {
            return false;
        } else if (cityId == CommonConstants.CITY_GWANG_YANG._cityId) {
            return false;
        } else if (cityId == CommonConstants.CITY_SUN_CHEON._cityId) {
            return false;
        } else if (cityId == CommonConstants.CITY_DAMYANG._cityId) {
            return false;
        } else if (cityId == CommonConstants.CITY_JANGSEONG._cityId) {
            return true;
        }

        return false;
    }

}
