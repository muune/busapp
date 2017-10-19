package teamdoppelganger.smarterbus.util.common;

/**
 * 파싱을 받아오는 부모 클래스
 * 함수 정의, 콜백 정의
 *
 * @author DOPPELSOFT4
 */

public class RequestInformation {


    /**
     * 각 파싱 작업이 완료 후 호출해주는 리스너들
     *
     * @author DOPPELSOFT4
     */
    public interface ReuqestInformationListner {
        public void getResult(int type, boolean isSuccess);
    }


    //경로 리스트
    public void getRouteList() {
    }

    //정류장 리스트
    public void getBusStopList() {
    }


    //버스 리스트
    public void getBusLineList() {
    }

}
