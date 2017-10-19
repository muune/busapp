package teamdoppelganger.smarterbus.common;

import java.io.File;

public class Constants {

    public static String NAVER_API_KEY = "dd045468348028d10f1201797556d9f7";
    public static String TAGO_URL = "http://tago.go.kr/transportation/publicRoutingSearchResultWeb_return.service";

    public static String LOCAL_URL = "http://115.68.14.18/BusInfor/app/";
    public static String PATH_GET_URL = "getbusRoutelatlong.php";
    public static String PATH_NOTI_DB_VERSION = "downCheck2.php";

    public static String TEMP_CHANGE_URL = "address.php";

    public static String PREF_DB_VERSION = "pref_db_version";
    public static String PREF_DB_NAME = "pref_db_name";

    public static String PREF_DEFAULT_DB_NAME = "smarterbusDB_170920_203.sqlite";


    public static String PREF_LAST_LOCATION_LAT = "pref_last_location_lat";
    public static String PREF_LAST_LOCATION_LON = "pref_last_location_lon";

    public static String PREF_EVENT_NAME = "pref_event_name";
    public static String PREF_EVENT_DEFAULT_NAME = "";

    public static String PACKAGE_NAME = "teamdoppelganger.smarterbus";
    public static String TAG = "Smarter_bus";
    public static boolean DEBUG_MODE = false;

    public static String SERVER_ADDRESS = "http://115.68.14.18/BusInfor";
    public static String SERVER_TIMETABLE = SERVER_ADDRESS + "/timetable/";
    public static String SERVER_DB_DOWN = SERVER_ADDRESS + File.separator + "dbs";
    public static String SERVER_DB_PARSER = SERVER_ADDRESS + File.separator + "parser";

    public static String DOWNLOAD_PATH_ROOT = "/sdcard/Android/data/" + PACKAGE_NAME;
    public static String DOWNLOAD_PATH = "/data/data/teamdoppelganger.smarterbus/databases/";  // "/sdcard/Android/data/" + PACKAGE_NAME +  "/files";
    public static String DOWNLOAD_BUS_DB_PATH = "/sdcard/Android/data/" + PACKAGE_NAME + "/files/smartebusDB.sqlite";

    public static String LOCAL_PATH = "/data/data/teamdoppelganger.smarterbus/databases/";


    public static int DBVERSION = 203;
    public static int RETURN_SUCCESS = 0;
    public static int RETURN_FAIL = 1;
    public static int SEOUL_BUS_VILLAGE = 28;   //서울 마을 버스
    public static int GONGJU_BUS_VILLAGE = 73;   //공주 버스
    public static int BUSAN_BUS_VILLAGE = 100;   //부산 마을 버스
    public static int BOSEONG_BUS_VILLAGE = 137;   //공주 버스

    public static String PRE_SMARTER_DB_VERSION = "preference_smarter_db_version";

    public static String TABLE_VERSION = "VERSION";
    public static String TABLE_LOCATION = "LOCATION";
    public static String TABLE_DETAIL_LOCATION = "DETAILLOCATION";
    public static String TABLE_BUSTYPE = "BUSTYPE";
    public static String TABLE_BUSSTOP = "_BUSSTOP";
    public static String TABLE_BUSROUTE = "_BUSROUTE";
    public static String TABLE_COMPANY = "COMPANY";


    public static final int SERCH_MODE_STOP = 0;
    public static final int SERCH_MODE_BUS = 1;
    public static final int SERCH_MODE_ROUTE = 2;


    // 도시 이름들
    public static final String CITY_A_SAN = "ASAN";
    public static final String CITY_BU_SAN = "BUSAN";
    public static final String CITY_BO_SEONG = "BOSEONG";
    public static final String CITY_CHANG_WON = "CHANGWON";
    public static final String CITY_CHEONN_AN = "CHEONNAN";
    public static final String CITY_CHUN_CHEON = "CHUNCHEON";

    public static final String CITY_CHEONG_JU = "CHEONGJU";
    public static final String CITY_DAE_GU = "DAEGU";
    public static final String CITY_DAE_JEON = "DAEJEON";
    public static final String CITY_GANG_NEUNG = "GANGNEUNG";
    public static final String CITY_GIM_HEA = "GIMHEA";
    public static final String CITY_GU_MI = "GUMI";
    public static final String CITY_GONG_JU = "GONGJU";

    public static final String CITY_GYEONG_GI = "GYEONGGI";
    public static final String CITY_GUN_SAN = "GUNSAN";
    public static final String CITY_GWANG_YANG = "GWANGYANG";
    public static final String CITY_IN_CHEON = "INCHEON";
    public static final String CITY_JE_JU = "JEJU";

    public static final String CITY_MIR_YANG = "MIRYANG";
    public static final String CITY_PO_HANG = "POHANG";
    public static final String CITY_SEO_UL = "SEOUL";
    public static final String CITY_SUN_CHEON = "SUNCHEON";
    public static final String CITY_TONG_YEONG = "TONGYEONG";
    public static final String CITY_GYEONG_JU = "GYEONGJU";

    public static final String CITY_UL_SAN = "ULSAN";
    public static final String CITY_WON_JU = "WONJU";
    public static final String CITY_YEO_SU = "YEOSU";
    public static final String CITY_YANGSAN = "YANGSAN";
    public static final String CITY_UWANG = "UWANG";

    public static final String CITY_JEON_JU = "JEONJU";


    //DB Filed Name
    // 공통 변수
    public static final String _ID = "_id";
    public static final String ROW_ID = "row_id";

    // 타입
    public static final String TYPE_INTEGER = "INTEGER";
    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_DOUBLE = "DOUBLE";
    public static final String TYPE_PRIMARY = "INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL";

    // 버전정보 테이블
    public static final String TBL_VERSION = "Version";
    public static final String VERSION_VERSION = "version";

    // 지역정보 테이블
    public static final String TBL_LOCATION = "Location";
    public static final String LOCATION_ID = "locationId";
    public static final String LOCATION_NAME = "locationName";
    public static final String LOCATION_EN_NAME = "locationEnName";

    // 버스종류 테이블 parrent
    public static final String TBL_BUS_TYPE = "BusType";
    public static final String BUS_TYPE_BUS_INDEX = "buIndex";
    public static final String BUS_TYPE_BUS_NAME = "busName";
    public static final String BUS_TYPE_SERVICE_OPTION = "serviceOption";

    // 버스 정류장 테이블
    public static final String TBL_BUS_STOP = "BusStop";
    public static final String BUS_STOP_ID = "busStopId";
    public static final String BUS_STOP_ARS_ID = "busStopArsId";
    public static final String BUS_STOP_API_ID = "busStopApiId";
    public static final String BUS_STOP_NAME = "busStopName";
    public static final String BUS_STOP_SUB_NAME = "busStopSubName";
    public static final String BUS_STOP_INITIAL_NAME = "busStopInitalName";
    public static final String BUS_STOP_LOCATION_X = "locationX";
    public static final String BUS_STOP_LOCATION_Y = "locationY";
    public static final String BUS_STOP_RELATED_ROUTES = "realtedRoutes";

    // 버스 노선 테이블
    public static final String TBL_BUS_ROUTE = "BusRoute";
    public static final String BUS_ROUTE_ARS_ID = "busRouteArsId";
    public static final String BUS_ROUTE_API_ID = "busRouteApiId";
    public static final String BUS_ROUTE_API_ID2 = "busRouteApiId2";
    public static final String BUS_ROUTE_NAME = "busRouteName";
    public static final String BUS_ROUTE_SUB_NAME = "busRouteSubName";
    public static final String BUS_ROUTE_INITIAL_NAME = "busRouteInitalName";
    public static final String BUS_ROUTE_FRIST_TIME = "fristTime";
    public static final String BUS_ROUTE_LAST_TIME = "lastTime";
    public static final String BUS_ROUTE_START_STOP_ID = "startStopId";
    public static final String BUS_ROUTE_END_STOP_ID = "endStopId";
    public static final String BUS_ROUTE_BUS_TYPE = "busType";
    public static final String BUS_ROUTE_COMPANY_ID = "companyId";
    public static final String BUS_ROUTE_TIME_TALBE_URL = "timeTableUrl";
    public static final String BUS_ROUTE_RELATED_STOPS = "realtedStops";
    public static final String BUS_ROUTE_BUS_REAL_TIME = "busRealTime";

    // 세부지역 테이블
    public static final String TBL_DETAIL_LOCATION = "DetailLocation";
    public static final String DETAIL_LOCATION_EN_NAME = "LocationEnName";
    public static final String DETAIL_LOCATION_NAME = "LocationName";
    public static final String DETAIL_LOCATION_PARENT_ID = "Location_id";


    //type 버스, 정류장, 경로 (즐겨찾기 및 최근검색에 사용할 변수)
    public static final int BUS_TYPE = 0;
    public static final int STOP_TYPE = 1;
    public static final int ROUTE_PATH_TYPE = 2;
    public static final int BUS_ROUTE_TYPE = 3;

    //parserType
    public static final int PARSER_LINE_TYPE = 0;
    public static final int PARSER_STOP_TYPE = 1;
    public static final int PARSER_ALALM_TYPE = 2;
    public static final int PARSER_LINE_DETAIL_TYPE = 3;
    public static final int PARSER_STOP_DETAIL_TYPE = 4;
    public static final int PARSER_LINE_DEPTH_TYPE = 5;
    public static final int PARSER_STOP_DEPTH_TYPE = 6;
    public static final int PARSER_RELATE_ROUTE_TYPE = 7;
    public static final int PARSER_NEXT_STOP = 8;
    public static final int PARSER_REFRSH_TYPE = 9;


    public static final int PARSING_TYPE_NOMAL = 0;
    public static final int PARSING_TYPE_MULTI_COMPLEX = 1;


    //intent value
    public static final String INTENT_BUSSTOPITEM = "stopItem";
    public static final String INTENT_BUSROUTEITEM = "routeItem";
    public static final String INTENT_FAVORITEITEM = "favoriteItem";
    public static final String INTENT_SEND_TYPE = "intent_type";
    public static final String INTENT_FILENAME = "file_name";
    public static final String INTENT_AUTOITEM = "autoItem";
    public static final String INTENT_TAGOITEM = "tagoItem";
    public static final String INTENT_WIGETITEM = "wigetItem";
    public static final String INTENT_STARTITEM = "startItem";
    public static final String INTENT_ENDITEM = "endItem";


    public static final String INTENT_URL = "urlValue";
    public static final String INTENT_URL_PARAM = "urlParam";
    public static final String INTENT_URL_SNED_TYPE = "urlSendType";

    public static final int INTENT_RESULT_CODE_FROM_AUTO_1 = 0;
    public static final int INTENT_RESULT_CODE_FROM_AUTO_2 = 1;

    public static final String TABLE_STOP = "_Stop";
    public static final String TABLE_BUS = "_Route";


    public static final int LINE_START = 0;
    public static final int LINE_NOMAL = 1;
    public static final int LINE_END = 2;

    public static final String DEFAULT_COLOR = "1_1";

    public static final int API_TYPE_1 = 0; //api 아이디가 유일한 타입
    public static final int API_TYPE_2 = 1; //api 아이디 & arsId를 검사해야하는 타입
    public static final int API_TYPE_3 = 2; //arsId를 검사해야하는 타입
    public static final int API_TYPE_4 = 3; //tempId를 검사해야하는 타입

    public static final int FAVORITE_TYPE_BUS = 0;
    public static final int FAVORITE_TYPE_STOP = 1;
    public static final int FAVORITE_TYPE_BUS_STOP = 2;
    public static final int FAVORITE_TYPE_NOTHING = 3;


    public static final int STATE_ING = 0;  //운행중
    public static final int STATE_STOP = 1;  //운행종료
    public static final int STATE_PREPARE = 2;  //출발준비중
    public static final int STATE_NEAR = 3;  //진입중
    public static final int STATE_PREPARE_START = 4;  //기점 출발 예쩡
    public static final int STATE_END = 5;  //운행 종료
    public static final int STATE_PREPARE_NOT = 6;  //도착예정버스 없음


    public static final String ALARM_BUS_ORDER = "bus_order";
    public static final String ALARM_BUS_MIN = "bus_min";
    public static final String ALARM_BUS_COLOR = "bus_color";


    public static final int MAP_MODE_ROUTE_PATH = 0;
    public static final int MAP_MODE_TAGO_PATH = 1;
    public static final int MAP_MODE_STOP_PATH = 2;


    public static final int WIDGET_TYPE_BUS = 1;
    public static final int WIDGET_TYPE_STOP = 1;

    public static final String SETTING_ALARM = "pref_setting_alarm";
    public static final boolean SETTING_ALARM_MODE_SOUND = true;
    public static final boolean SETTING_ALARM_MODE_VIBE = false;

    public static final String SETTING_VIBRATE = "pref_vibrate";
    public static final boolean SETTING_VIBRATE_ON = true;
    public static final boolean SETTING_VIBRATE_OFF = false;

    public static final String SETTING_SEARCHMODE = "pref_serach_mode";
    public static final int SETTING_SEARCHMODE_STOP = 0;
    public static final int SETTING_SEARCHMODE_BUS = 1;


    public static final String SETTING_LOCATION = "pref_location";
    public static final String SETTING_LOCATION_X = "pref_setting_location_X";
    public static final String SETTING_LOCATION_Y = "pref_setting_location_Y";


    public static final String SETTING_FAVORITE_MODE = "pref_setting_fravorite_mode";
    public static final int SETTING_FAOVIRET_TICKET = 1;
    public static final int SETTING_FAOVIRET_LIST = 0;

    public static final String SETTING_FAOVIRET_LIST_TYPE = "setting_favorite_list_type";
    public static final int SETTING_FAOVIRET_LIST_1 = 0;
    public static final int SETTING_FAOVIRET_LIST_2 = 1;
    public static final int SETTING_FAOVIRET_LIST_3 = 2;
    public static final int SETTING_FAOVIRET_LIST_4 = 3;
    public static final int SETTING_FAOVIRET_LIST_5 = 4;
    public static final String SETTING_FAOVIRET_TICKET_TYPE = "setting_favorite_ticket_type";
    public static final int SETTING_FAOVIRET_TICKET_1 = 0;
    public static final int SETTING_FAOVIRET_TICKET_2 = 1;
    public static final int SETTING_FAOVIRET_TICKET_3 = 2;
    public static final int SETTING_FAOVIRET_TICKET_4 = 3;
    public static final int SETTING_FAOVIRET_TICKET_5 = 4;
    public static final int SETTING_FAOVIRET_TICKET_6 = 5;


    public static final String WIDGET_MODE = "widget_mode";
    public static final int WIDGET_MODE_STOP = 1;
    public static final int WIDGET_MODE_BUS_STOP = 2;
    public static final int WIDGET_MODE_NOTHING = 3;
    public static final String WIDGET_ID = "widget_id";
    public static final String WIDGET_SUCCESS = "widget_success";
    public static final String WIDGET_CREATE = "widget_create";


    public static final String AUTO_INDEX = "auto_INDEX";


    //noti
    public static final String NOTI = "NOTI";
    public static final String NOTI_VERSION = "NOTI_VERSION";

    public static final String URL = "http://115.68.14.18/BusInfor/app/";
    public static final String RECENT_URL = URL + "recentNotiVersion3.php";  //파라미터 version=4
    public static final String NOTI_URL = URL + "getNoti3.php";   //id=2


    public static final int GET_RECENT = 0;
    public static final int GET_NOTI = 1;

    public static final String PREF_RECENTVERSION = "pref_recentVersion";
    public static final String PREF_LOCALNOTIVERSION = "pref_localNotiVersion";
    public static final String PREF_LOCATION_INFO_AGREE = "pref_location_info_agree";
    public static final String PREF_BANNER_INFO_CHECK = "pref_banner_info_check";
    //수도권 통합 요금제 업데이트
    //마켓 구분
    public static final int MARKET_ANDROID = 0;
    public static final int MARKET_TSTORE = 1;
    public static final int MARKET_NAVER = 2;
    public static final int MARKET_TYPE = MARKET_TSTORE;

    public static final String LOGIN_GOOGLE = "login_google";
    public static final String LOGIN_FACEBOOK = "login_facebook";
    public static final String LOGIN_NAVER = "login_naver";
    public static final String LOGIN_EMAIL = "login_email";
    public static final String LOGIN_EMPTY = "login_empty";

    public static final String PREF_LOGIN = "pref_login";

    public static final String ADLIB_MAIN_API_KEY = "5840e6760cf27699524028fb";
    public static final String ADLIB_INFO_API_KEY = "5840e6b70cf27699524028fd";
    public static final String ADLIB_WEBVIEW_API_KEY = "587ef2900cf21979d6e16b0c";
    public static final String ADAM_KEY = "9170Z1YT14613c09307";

}

