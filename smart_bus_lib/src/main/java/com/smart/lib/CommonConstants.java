package com.smart.lib;

public class CommonConstants {

    // 공통 변수
    public static final String _ID = "_id";
    public static final String ROW_ID = "rowId";

    // 타입
    public static final String TYPE_UNIQUE = "UNIQUE";
    public static final String TYPE_INTEGER = "INTEGER";
    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_DOUBLE = "DOUBLE";
    public static final String TYPE_PRIMARY = "INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL";

    // 버전정보 테이블
    public static final String TBL_VERSION = "Version";
    public static final String VERSION_VERSION = "version";

    // 버스종류 테이블 parent
    public static final String TBL_BUS_TYPE = "BusType";
    public static final String BUS_TYPE_BUS_TYPE = "busType";
    public static final String BUS_TYPE_OPER_CITY = "operCity";
    public static final String BUS_TYPE_COLOR = "color";

    // 버스 정류장 테이블
    public static final String TBL_BUS_STOP = "Stop";
    public static final String BUS_STOP_ARS_ID = "stopArsId";
    public static final String BUS_STOP_API_ID = "stopApiId";
    public static final String BUS_STOP_NAME = "stopName";
    public static final String BUS_STOP_DESC = "stopDesc";
    public static final String BUS_STOP_INITIAL_NAME = "stopInitalName";
    public static final String BUS_STOP_LOCATION_X = "stopX";
    public static final String BUS_STOP_LOCATION_Y = "stopY";
    public static final String BUS_STOP_RELATED_ROUTES = "stopRelatedRoutes";
    public static final String BUS_STOP_NEXT_STOP_ID = "stopNextStopId";

    // 버스 노선 테이블
    public static final String TBL_BUS_ROUTE = "Route";
    public static final String BUS_ROUTE_ID1 = "routeId1";
    public static final String BUS_ROUTE_ID2 = "routeId2";
    public static final String BUS_ROUTE_O_ID = "routeOId";
    public static final String BUS_ROUTE_M_ID = "routeMId";
    public static final String BUS_ROUTE_NAME = "routeName";
    public static final String BUS_ROUTE_SUB_NAME = "routeSubName";
    public static final String BUS_ROUTE_INITIAL_NAME = "routeInitalName";
    public static final String BUS_ROUTE_FRIST_TIME = "routeFristTime";
    public static final String BUS_ROUTE_LAST_TIME = "routeLastTime";
    public static final String BUS_ROUTE_TERM = "routeTerm";
    public static final String BUS_ROUTE_START_STOP_ID = "routeStartStopId";
    public static final String BUS_ROUTE_END_STOP_ID = "routeEndStopId";
    public static final String BUS_ROUTE_DISTANCE = "routeDistance";
    public static final String BUS_ROUTE_BUS_TYPE = "routeType";
    public static final String BUS_ROUTE_COMPANY_ID = "routeCompanyId";
    public static final String BUS_ROUTE_TIME_TALBE_URL = "routeTimeTableUrl";
    public static final String BUS_ROUTE_RELATED_STOPS = "routeRelatedStops";
    public static final String BUS_ROUTE_TURN_STOP_IDX = "routeTurnStopIdx";

    // 세부지역 테이블
    public static final String TBL_CITY = "City";
    public static final String CITY_NAME = "cityName";
    public static final String CITY_EN_NAME = "cityEnName";
    public static final String CITY_ID = "cityId";
    public static final String CITY_AREA_ID = "areaId";
    public static final String CITY_ROUTE_ID1_LENGTH = "routeId1Length";
    public static final String CITY_ROUTE_ID2_LENGTH = "routeId2Length";
    public static final String CITY_STOP_ARS_ID_LENGTH = "stopArsIdLength";
    public static final String CITY_STOP_API_ID_LENGTH = "stopApiIdLength";
    public static final String CITY_TIME_TABLE_PREFIX = "timeTablePrefix";
    public static final String CITY_TIME_TABLE_PARAM = "timeTableParam";

    // 지역정보 테이블
    public static final String TBL_AREA = "Area";
    public static final String AREA_NAME = "areaName";
    public static final String AREA_EN_NAME = "areaEnName";
    public static final String AREA_ID = "areaId";

    // 종착지 정보 테이블
    public static final String TBL_TERMINUS = "Terminus";
    public static final String TERMINUS_NAME = "terminusName";

    // 버스 회사 정보 테이블
    public static final String TBL_COMPANY = "Company";
    public static final String COMPANY_NAME = "companyName";
    public static final String COMPANY_DETAIL = "companyDetail";
    public static final String COMPANY_ADDRESS = "companyAddress";
    public static final String COMPANY_TEL = "companyTel";

    // 시각표 URL 테이블
    public static final String TBL_TIME_TALBE_URL = "TimeTableUrl";
    public static final String TIME_TALBE_URL = "url";

    public static final String BUS_TYPE_NOT = "없음";
    public static final String BUS_TYPE_1 = "급행";
    public static final String BUS_TYPE_2 = "간선";
    public static final String BUS_TYPE_3 = "지선";
    public static final String BUS_TYPE_4 = "외곽";
    public static final String BUS_TYPE_5 = "마을";
    public static final String BUS_TYPE_6 = "첨단";
    public static final String BUS_TYPE_7 = "순환";
    public static final String BUS_TYPE_8 = "공항";
    public static final String BUS_TYPE_9 = "광역";
    public static final String BUS_TYPE_10 = "인천";
    public static final String BUS_TYPE_11 = "경기";
    public static final String BUS_TYPE_12 = "폐지";
    public static final String BUS_TYPE_13 = "공용";
    public static final String BUS_TYPE_14 = "좌석";
    public static final String BUS_TYPE_15 = "일반";
    public static final String BUS_TYPE_16 = "직행";
    public static final String BUS_TYPE_17 = "심야";
    public static final String BUS_TYPE_18 = "제주시내";
    public static final String BUS_TYPE_19 = "서귀포시내";
    public static final String BUS_TYPE_20 = "시외";
    public static final String BUS_TYPE_21 = "공단";
    public static final String BUS_TYPE_22 = "리무진";
    public static final String BUS_TYPE_23 = "시내";
    public static final String BUS_TYPE_24 = "농촌";

    public static final int MAP_NOT = 0;
    public static final int MAP_TM = 1;
    public static final int MAP_KTM = 2;
    public static final int MAP_UTM = 3;
    public static final int MAP_CONGNAMUL = 4;
    public static final int MAP_WGS84 = 5;
    public static final int MAP_BESSEL = 6;
    public static final int MAP_WTM = 7;
    public static final int MAP_WKTM = 8;
    public static final int MAP_WCONGNAMUL = 10;

    public static final int MAP_GRS80 = 103;
    public static final int MAP_UTMK = 104;

/*	CENTER_ID	AREA_ID	AREA_NAME
	47770100	01	가평
	41270100	02	고양
	42771400	03	과천
	42370200	04	광명
	46472000	05	광주
	47170200	06	구리
	43570100	07	군포
	41572800	08	김포
	47270100	09	남양주
	48370800	10	동두천
	42073600	11	부천
	46170000	12	성남
	44084101	13	수원
	42970100	14	시흥
	42570200	15	안산
	45670100	16	안성
	43172800	17	안양
	48270900	18	양주
	47670300	19	양평
	46970400	20	여주
	48670100	21	연천
	44770100	22	오산
	44970400	23	용인
	43770100	24	의왕
	48070300	25	의정부
	46771700	26	이천
	41371900	27	파주
	45070200	28	평택
	48770100	29	포천
	46570100	30	하남
	44570200	31	화성
	10000000	32	서울
	28000000	33	인천*/


    // 지역들
    public static final AreaInfo AREA_SUDO = new AreaInfo("수도권", "SUDO", 100);
    public static final AreaInfo AREA_GANGWON = new AreaInfo("강원도", "GANGWON", 200);
    public static final AreaInfo AREA_CHUNGCHEONG = new AreaInfo("충청도", "CHUNGCHEONG", 300);
    public static final AreaInfo AREA_GEYONGSANG = new AreaInfo("경상도", "GEYONGSANG 	", 400);
    public static final AreaInfo AREA_COHNRA = new AreaInfo("전라도", "COHNRA", 500);
    public static final AreaInfo AREA_JEJU = new AreaInfo("제주특별자치도", "JEJU", 600);


    public static final CityInfo CITY_SUDO = new CityInfo("수도권", "SUDO", AREA_SUDO, 1, MAP_WGS84, -1, -1, -1, -1, "", new String[]{});

    // 수도권 100
    public static final CityInfo CITY_SEO_UL = new CityInfo("서울", "SEOUL", AREA_SUDO, 1, MAP_WGS84, 7, -1, 5, 0, "", new String[]{});
    public static final CityInfo CITY_GYEONG_GI = new CityInfo("경기", "GYEONGGI", AREA_SUDO, 2, MAP_WGS84, 9, -1, 5, 9, "", new String[]{});
    public static final CityInfo CITY_IN_CHEON = new CityInfo("인천", "INCHEON", AREA_SUDO, 3, MAP_WTM, 9, -1, 5, 9, "", new String[]{});
    public static final CityInfo CITY_GOYANG = new CityInfo("고양마을버스", "GM_GOYANG", AREA_SUDO, 4, MAP_WGS84, -1, -1, -1, -1, "", new String[]{});
    public static final CityInfo CITY_GUNPO = new CityInfo("군포마을버스", "GM_GUNPO", AREA_SUDO, 5, MAP_WGS84, -1, -1, -1, -1, "", new String[]{});
    public static final CityInfo CITY_BUCHEON = new CityInfo("부천마을버스", "GM_BUCHEON", AREA_SUDO, 6, MAP_WGS84, -1, -1, -1, -1, "", new String[]{});
    public static final CityInfo CITY_ANYANG = new CityInfo("안양마을버스", "GM_ANYANG", AREA_SUDO, 7, MAP_NOT, -1, -1, -1, -1, "", new String[]{});
    public static final CityInfo CITY_YONGIN = new CityInfo("용인마을버스", "GM_YONGIN", AREA_SUDO, 8, MAP_WGS84, -1, -1, -1, -1, "", new String[]{});
    public static final CityInfo CITY_UWANG = new CityInfo("의왕마을버스", "GM_UWANG", AREA_SUDO, 9, MAP_WGS84, -1, -1, -1, -1, "", new String[]{});
    public static final CityInfo CITY_NAMYANGJU = new CityInfo("남양주마을버스", "GM_NAMYANGJU", AREA_SUDO, 10, MAP_WGS84, -1, -1, -1, -1, "", new String[]{});
    public static final CityInfo CITY_GYEONG_GI_VILLAGE = new CityInfo("경기마을버스", "GM_GYEONGGI", AREA_SUDO, 11, MAP_WGS84, -1, -1, -1, -1, "", new String[]{});

    // 강원권 200
    public static final CityInfo CITY_CHUN_CHEON = new CityInfo("춘천-홍천", "CHUNCHEON", AREA_GANGWON, 1, MAP_WGS84, 9, -1, 4, 9, "", new String[]{});
    public static final CityInfo CITY_WON_JU = new CityInfo("원주", "WONJU", AREA_GANGWON, 2, MAP_WGS84, -1, -1, -1, -1, "http://its.wonju.go.kr/busroute/selectCityScheduleView.do?rn=%s", new String[]{BUS_ROUTE_NAME});
    public static final CityInfo CITY_GANG_NEUNG = new CityInfo("강릉", "GANGNEUNG", AREA_GANGWON, 3, MAP_WGS84, 8, -1, 7, 7, "http://bis.gangneung.go.kr/addInfo/timeTable.do", new String[]{BUS_ROUTE_NAME});

    // http://115.68.14.18/BusInfor/timetable/CHEONAN/CHEONAN_20_14_2.html
    // 충청권 300
    public static final CityInfo CITY_A_SAN = new CityInfo("아산", "ASAN", AREA_CHUNGCHEONG, 1, MAP_WGS84, 0, -1, 0, -1, "http://115.68.14.18/BusInfor/timetable/ASAN/ASAN_%s.html", new String[]{BUS_ROUTE_ID1});
    public static final CityInfo CITY_CHEONN_AN = new CityInfo("천안", "CHEONAN", AREA_CHUNGCHEONG, 2, MAP_WGS84, 0, 0, 0, 0, "http://115.68.14.18/BusInfor/timetable/CHEONAN/CHEONAN_%s_%s_%s.html", new String[]{BUS_ROUTE_NAME, BUS_ROUTE_ID1, BUS_ROUTE_ID2});
    public static final CityInfo CITY_CHEONG_JU = new CityInfo("청주", "CHEONGJU", AREA_CHUNGCHEONG, 3, MAP_WGS84, 13, 4, 4, 13, "http://cha3.cjcity.net/file/imageLoading.do?fileIdntfcNo=%s", new String[]{BUS_ROUTE_ID2});
    public static final CityInfo CITY_DAE_JEON = new CityInfo("대전", "DAEJEON", AREA_CHUNGCHEONG, 4, MAP_WGS84, 8, -1, 5, 8, "http://115.68.14.18/BusInfor/timetable/DAEJEON/DAEJEON_%s.html", new String[]{BUS_ROUTE_ID1});
    public static final CityInfo CITY_SE_JONG = new CityInfo("세종", "SEJONG", AREA_CHUNGCHEONG, 5, MAP_WGS84, 0, -1, 0, -1, "", new String[]{});
    public static final CityInfo CITY_CHUNG_JU = new CityInfo("충주", "CHUNGJU", AREA_CHUNGCHEONG, 6, MAP_WGS84, 0, -1, 0, -1, "", new String[]{});
    public static final CityInfo CITY_JE_CHEON = new CityInfo("제천", "JECHEON", AREA_CHUNGCHEONG, 7, MAP_WGS84, 0, -1, 0, -1, "http://its.okjc.net/m.m04?pageNumber=1&no=&searchType=&route_id=%s&searchWeekday=1", new String[]{BUS_ROUTE_ID1});
    public static final CityInfo CITY_GONG_JU = new CityInfo("공주", "GONGJU", AREA_CHUNGCHEONG, 8, MAP_WGS84, 0, -1, 0, -1, "", new String[]{});

    // 경상권 400
    public static final CityInfo CITY_BU_SAN = new CityInfo("부산", "BUSAN", AREA_GEYONGSANG, 1, MAP_WGS84, 10, 10, 5, 9, "http://115.68.14.18/BusInfor/timetable/BUSAN/BUSAN_%s.html", new String[]{BUS_ROUTE_ID1});
    public static final CityInfo CITY_UL_SAN = new CityInfo("울산", "ULSAN", AREA_GEYONGSANG, 2, MAP_WGS84, 0, -1, 5, 9, "", new String[]{});
    public static final CityInfo CITY_DAE_GU = new CityInfo("대구", "DAEGU", AREA_GEYONGSANG, 3, MAP_WGS84, 10, -1, 5, 10, "http://businfo.daegu.go.kr/ba/page/timeschedule.do?act=index&route_no=%s", new String[]{BUS_ROUTE_NAME});
    public static final CityInfo CITY_CHANG_WON = new CityInfo("창원", "CHANGWON", AREA_GEYONGSANG, 4, MAP_WGS84, 9, -1, 6, 9, "http://115.68.14.18/BusInfor/timetable/CHANGWON/CHANGWON_%s.html", new String[]{BUS_ROUTE_ID1});
    public static final CityInfo CITY_GIM_HEA = new CityInfo("김해", "GIMHEA", AREA_GEYONGSANG, 5, MAP_WGS84, 0, -1, 4, -1, "http://bus.gimhae.go.kr/ver4/map/inc/inc_result_time_table.php?lineID=%s", new String[]{BUS_ROUTE_ID1});
    public static final CityInfo CITY_GU_MI = new CityInfo("구미", "GUMI", AREA_GEYONGSANG, 6, MAP_WGS84, 1, 1, 5, -1, "http://bis.gumi.go.kr/city_bus/time_table.do?route_id=%s&brtId=%s&remark=%s", new String[]{BUS_ROUTE_ID1, BUS_ROUTE_NAME, BUS_ROUTE_SUB_NAME});
    public static final CityInfo CITY_MIR_YANG = new CityInfo("밀양", "MIRYANG", AREA_GEYONGSANG, 7, MAP_WGS84, 0, -1, 4, -1, "http://bis.miryang.go.kr/busline_loc/pop_line_move_info.asp?hdLineID=%s", new String[]{BUS_ROUTE_ID1});
    public static final CityInfo CITY_PO_HANG = new CityInfo("포항", "POHANG", AREA_GEYONGSANG, 8, MAP_WGS84, 9, -1, 6, 9, "http://bis.ipohang.org/Content/Guide/file/%s.xls", new String[]{BUS_ROUTE_NAME});
    public static final CityInfo CITY_TONG_YEONG = new CityInfo("통영", "TONGYEONG", AREA_GEYONGSANG, 9, MAP_UTMK, 9, -1, 4, 9, "http://bms.tongyeong.go.kr/mobile/route/TimeTableInfo.do?day=%s&route_id=%s", new String[]{"yyyy-MM-dd", BUS_ROUTE_ID1}); // 다시
    public static final CityInfo CITY_YANG_SAN = new CityInfo("양산", "YANGSAN", AREA_GEYONGSANG, 10, MAP_WGS84, 10, -1, 4, -1, "http://bus.yangsan.go.kr/yangsan_2016/yangsan_mobile/busline/bus_search_time.php?lineID=%s&lineName=%s", new String[]{BUS_ROUTE_ID1, BUS_ROUTE_NAME});
    public static final CityInfo CITY_GEO_JE = new CityInfo("거제", "GEOJE", AREA_GEYONGSANG, 11, MAP_WGS84, 0, -1, 4, 0, "http://bis.geoje.go.kr/main/busServiceInfo.do?action=busSchedule&mode=search&stype=weekday&selectType=line&selectTypes=line&searchLineId=%s", new String[]{BUS_ROUTE_ID1});
    public static final CityInfo CITY_JIN_JU = new CityInfo("진주", "JINJU", AREA_GEYONGSANG, 12, MAP_WGS84, 3, -1, -1, 0, "", new String[]{});
    public static final CityInfo CITY_GYEONG_SAN = new CityInfo("경산", "GYEONGSAN", AREA_GEYONGSANG, 13, MAP_WGS84, 0, -1, -1, 9, "http://bis.gbgs.go.kr/bs/businfo/timetable/%s.htm", new String[]{BUS_ROUTE_NAME});
    public static final CityInfo CITY_CHIL_GOK = new CityInfo("칠곡", "CHILGOK", AREA_GEYONGSANG, 14, MAP_WGS84, 1, 1, 5, -1, "http://bus.chilgok.go.kr/GCBIS/web/page/web002_01_2.do?brtId=%s&brtDirection=%s&brtClass=%s&remark=%s", new String[]{BUS_ROUTE_NAME, BUS_ROUTE_ID1, BUS_ROUTE_ID2, BUS_ROUTE_SUB_NAME});
    public static final CityInfo CITY_GYEONG_JU = new CityInfo("경주", "GYEONGJU", AREA_GEYONGSANG, 15, MAP_WGS84, 9, 9, 5, 9, "http://its.gyeongju.go.kr/bis/main/bustime.do", new String[]{BUS_ROUTE_NAME, BUS_ROUTE_ID1, BUS_ROUTE_ID2, BUS_ROUTE_SUB_NAME});
    public static final CityInfo CITY_GIM_CHEON = new CityInfo("김천", "GIMCHEON", AREA_GEYONGSANG, 16, MAP_WGS84, 8, 0, 5, 7, "", new String[]{});

    // 전라권 500
    public static final CityInfo CITY_GUN_SAN = new CityInfo("군산", "GUNSAN", AREA_COHNRA, 1, MAP_WGS84, -1, -1, -1, -1, "http://its.gunsan.go.kr/BusInfo.do?routeNum=%s", new String[]{BUS_ROUTE_NAME});
    public static final CityInfo CITY_GWANG_YANG = new CityInfo("광양", "GWANGYANG", AREA_COHNRA, 2, MAP_WGS84, 9, 9, 7, 9, "http://bis.gwangyang.go.kr:8282/internet/bus/time_view.jsp?route_no=%s", new String[]{BUS_ROUTE_NAME});
    public static final CityInfo CITY_SUN_CHEON = new CityInfo("순천", "SUNCHEON", AREA_COHNRA, 3, MAP_WGS84, 9, 9, 7, 9, "http://bis.sc.go.kr:8282/internet/bus/time_view.jsp?route_no=%s", new String[]{BUS_ROUTE_NAME});
    public static final CityInfo CITY_YEO_SU = new CityInfo("여수", "YEOSU", AREA_COHNRA, 4, MAP_WGS84, 9, -1, -1, 9, "http://mbis.yeosu.go.kr:8286/smart/time_view.htm?route_no=%s", new String[]{BUS_ROUTE_NAME});
    public static final CityInfo CITY_JEON_JU = new CityInfo("전주", "JEONJU", AREA_COHNRA, 5, MAP_WGS84, 9, 0, 5, 9, "http://www.jeonjuits.go.kr/main/bus/bus_bustimepop.jsp?inp_brt_stdid=%s", new String[]{BUS_ROUTE_ID1});
    public static final CityInfo CITY_GWANG_JU = new CityInfo("광주", "GWANGJU", AREA_COHNRA, 6, MAP_WGS84, 0, -1, 4, 0, "http://bus.gjcity.net/busmap/busRunTimeTable?LINE_ID=%s&LINE_NAME=%s", new String[]{BUS_ROUTE_ID1, BUS_ROUTE_NAME});
    public static final CityInfo CITY_NA_JU = new CityInfo("나주", "NAJU", AREA_COHNRA, 7, MAP_WGS84, 0, -1, -1, 0, "", new String[]{});
    public static final CityInfo CITY_MOK_PO = new CityInfo("목포", "MOKPO", AREA_COHNRA, 8, MAP_WGS84, 9, 0, 5, 9, "http://bis.mokpo.go.kr/mp/m/bisRouteInfo.view", new String[]{BUS_ROUTE_ID1});
    public static final CityInfo CITY_HWASUN = new CityInfo("화순", "HWASUN", AREA_COHNRA, 9, MAP_WGS84, -1, -1, -1, -1, "", new String[]{});
    public static final CityInfo CITY_DAMYANG = new CityInfo("담양", "DAMYANG", AREA_COHNRA, 10, MAP_WGS84, 0, -1, -1, 0, "", new String[]{});
    public static final CityInfo CITY_JANGSEONG = new CityInfo("장성", "JANGSEONG", AREA_COHNRA, 11, MAP_WGS84, 0, -1, -1, 0, "", new String[]{});
    public static final CityInfo CITY_YEONGAM = new CityInfo("영암", "YEONGAM", AREA_COHNRA, 12, MAP_WGS84, 9, 0, 5, 9, "http://bis.yeongam.go.kr/ya/m/bisRouteInfo.view", new String[]{BUS_ROUTE_ID1});
    public static final CityInfo CITY_MUAN = new CityInfo("무안", "MUAN", AREA_COHNRA, 13, MAP_WGS84, 9, 0, 5, 9, "http://bis.mokpo.go.kr/ma/m/bisRouteInfo.view", new String[]{BUS_ROUTE_ID1});
    public static final CityInfo CITY_SHINAN = new CityInfo("신안", "SHINAN", AREA_COHNRA, 14, MAP_WGS84, 9, 0, 5, 9, "http://bis.shinan.go.kr/sa/m/bisRouteInfo.view", new String[]{BUS_ROUTE_ID1});
    public static final CityInfo CITY_BO_SEONG = new CityInfo("보성", "BOSEONG", AREA_COHNRA, 15, MAP_WGS84, 9, 0, 5, 9, "", new String[]{});


    // 제주권 600
    public static final CityInfo CITY_JE_JU = new CityInfo("제주-서귀포", "JEJU", AREA_JEJU, 1, MAP_WGS84, 9, 9, 10, 9, "", new String[]{});


    public static class AreaInfo {
        public String _korName;
        public String _engName;
        public int _id;

        public AreaInfo(String korName, String engName, int id) {
            _korName = korName;
            _engName = engName;
            _id = id;
        }
    }


    public static class CityInfo {
        public String _korName;
        public String _engName;
        public AreaInfo _area;
        public int _cityId;
        public int _mapType;
        public int _routeId1Len;
        public int _routeId2Len;
        public int _stopArsIdLen;
        public int _stopApiIdLen;
        public String _timeTableUrlPrefix;
        public String[] _timeTableUrlParam;

        public CityInfo(String korName, String engName, AreaInfo area, int cityId, int mapType, int routeId1Len, int routeId2Len, int stopArsIdLen, int stopApiIdLen, String timeTableUrlPrefix, String[] timeTableUrlParam) {
            _korName = korName;
            _engName = engName;
            _area = area;
            _cityId = area._id + cityId;
            _mapType = mapType;
            _routeId1Len = routeId1Len;
            _routeId2Len = routeId2Len;
            _stopArsIdLen = stopArsIdLen;
            _stopApiIdLen = stopApiIdLen;
            _timeTableUrlPrefix = timeTableUrlPrefix;
            _timeTableUrlParam = timeTableUrlParam;
        }
    }
}

