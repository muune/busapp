package teamdoppelganger.smarterbus.util.common;

import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.common.SBInforApplication;
import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.BusStopItem;
import teamdoppelganger.smarterbus.item.CommonItem;
import teamdoppelganger.smarterbus.item.DepthFavoriteItem;
import teamdoppelganger.smarterbus.item.DepthItem;
import teamdoppelganger.smarterbus.item.DepthRouteItem;
import teamdoppelganger.smarterbus.item.DepthStopItem;
import teamdoppelganger.smarterbus.item.FavoriteAndHistoryItem;

//공공API를 통해 버스정보를 가져오는 GetData 확장판임.
//1. 기존 호스트 코드에서 Data요청을 위해 사용되던 코드를 더욱 추상화 하고 비동기적 loop 처리를 가능하게 함.
//2. 인터페이스를 간결하게 수정함.
//3. BaseActivity에서 GetDataExt.instance()를 lazy하게 생성이 필요할 때 하고 pauseAll()과 stopAll()를 구현해 두도록 약속함
final public class GetDataExt {
    private GetDataExtListener listener;
    private SQLiteDatabase busDb;
    private SBInforApplication application;
    private Map<Integer,Parser> parsers;
    public interface GetDataExtListener {
        void onComplete(int type, DepthItem item);
    }
    public static GetDataExt instance(@NonNull final GetDataExtListener $listener, @NonNull final SQLiteDatabase $busDb, @NonNull final SBInforApplication $application){
        GetDataExt rtn = new GetDataExt();
        rtn.listener = $listener;
        rtn.busDb = $busDb;
        rtn.application = $application;
        rtn.parsers = new HashMap<>();
        return rtn;
    }
    private GetDataExt(){ //직접생성불가처리
    }
    private GetData createData(@NonNull final Parser $parser, final int $runNum){
        return new GetData(new GetData.GetDataListener() {
            @Override
            public void onCompleted(int type, DepthItem item) {
                if(item == null) return;
                if($runNum == $parser.runId) listener.onComplete(type, item);
            }
        }, busDb, application.mHashLocation);
    }

    //정류장에 특정 노선의 버스의 도착정보를 얻어옴
    public void readyRefreshService(@NonNull final List<FavoriteAndHistoryItem> itemList){
        final Parser parser = getParser(Constants.PARSER_REFRSH_TYPE);
        parser.stop();
        HashMap<Integer,String> hashEng = application.mHashLocation;
        for(int i = 0; i < itemList.size(); i++){
            if(itemList.get(i).type ==  Constants.FAVORITE_TYPE_BUS_STOP) {
                GetData getData = createData(parser, parser.runId);
                itemList.get(i).busRouteItem.arriveInfo.clear();
                DepthFavoriteItem favoDepthFavoriteItem = new DepthFavoriteItem();
                favoDepthFavoriteItem.favoriteAndHistoryItems.add(itemList.get(i));
                parser.add(favoDepthFavoriteItem, getData);
            }
        }
    }
    public boolean run(final int $parserType){
        return run($parserType, 2000); //2초 기본
    }
    public boolean run(final int $parserType, final int $delayTime){
        if(!isExistParser($parserType)) return false;
        Parser parser = getParser($parserType);
        if(parser.running){
            parser.stop();
        }
        parser.run(new ParserRun() {
            @Override
            public void run(CommonItem $item, GetData $getData) {
                switch ($parserType){
                    case Constants.PARSER_LINE_TYPE:
                        $getData.startBusRouteParsing((BusStopItem)$item);
                        break;
                    case Constants.PARSER_STOP_TYPE:
                        $getData.startBusStopParsing((BusRouteItem)$item);
                        break;
                    case Constants.PARSER_ALALM_TYPE:
                        $getData.startAlarmService((BusRouteItem)$item);
                        break;
                    case Constants.PARSER_LINE_DETAIL_TYPE:
                        $getData.startBusRouteDetailParsing((BusRouteItem)$item);
                        break;
                    case Constants.PARSER_STOP_DETAIL_TYPE:
                        $getData.startBusStopDetailParsing((BusStopItem)$item);
                        break;
                    case Constants.PARSER_LINE_DEPTH_TYPE:
                        $getData.depthBusRouteParsing((DepthRouteItem)$item);
                        break;
                    case Constants.PARSER_STOP_DEPTH_TYPE:
                        $getData.depthBusStopParsing((DepthStopItem)$item);
                        break;
                    case Constants.PARSER_RELATE_ROUTE_TYPE:
                        $getData.startRelateRoute((BusStopItem)$item);
                        break;
                    case Constants.PARSER_NEXT_STOP:
                        $getData.startNextStop((BusRouteItem)$item);
                        break;
                    case Constants.PARSER_REFRSH_TYPE:
                        $getData.startOneRefrshService((DepthFavoriteItem)$item);
                        break;
                }
            }
        }, $delayTime);
        return true;
    }
    public void pauseAll(){
        pause(Constants.PARSER_LINE_TYPE);
        pause(Constants.PARSER_STOP_TYPE);
        pause(Constants.PARSER_ALALM_TYPE);
        pause(Constants.PARSER_LINE_DETAIL_TYPE);
        pause(Constants.PARSER_STOP_DETAIL_TYPE);
        pause(Constants.PARSER_LINE_DEPTH_TYPE);
        pause(Constants.PARSER_STOP_DEPTH_TYPE);
        pause(Constants.PARSER_RELATE_ROUTE_TYPE);
        pause(Constants.PARSER_NEXT_STOP);
        pause(Constants.PARSER_REFRSH_TYPE);
    }
    public void pause(int $parserType){
        if(!isExistParser($parserType)) return;
        Parser parser = getParser($parserType);
        if(null == parser) return;
        if(!parser.running) return;
        parser.pause();
    }

    public void stopAll(){
        stop(Constants.PARSER_LINE_TYPE);
        stop(Constants.PARSER_STOP_TYPE);
        stop(Constants.PARSER_ALALM_TYPE);
        stop(Constants.PARSER_LINE_DETAIL_TYPE);
        stop(Constants.PARSER_STOP_DETAIL_TYPE);
        stop(Constants.PARSER_LINE_DEPTH_TYPE);
        stop(Constants.PARSER_STOP_DEPTH_TYPE);
        stop(Constants.PARSER_RELATE_ROUTE_TYPE);
        stop(Constants.PARSER_NEXT_STOP);
        stop(Constants.PARSER_REFRSH_TYPE);
    }
    public void stop(int $parserType){
        if(!isExistParser($parserType)) return;
        Parser parser = getParser($parserType);
        if(null == parser) return;
        if(!parser.running) return;
        parser.stop();
    }

    //버스노선에 있는 정류장 리스트
    public void busStopParsing(BusRouteItem item){
        //TODO 위 구조처럼 구현할 것
        GetData getData = new GetData(new GetData.GetDataListener() {
            @Override
            public void onCompleted(int type, DepthItem item) {
                if(item == null) return;
                listener.onComplete(type, item);
            }
        }, busDb, application.mHashLocation);
        getData.startBusStopParsing(item);
    }
    //정류장에 지나가는 버스노선
    public void relateRoute(BusStopItem item){
        //TODO 위 구조처럼 구현할 것
        GetData getData = new GetData(new GetData.GetDataListener() {
            @Override
            public void onCompleted(int type, DepthItem item) {
                if(item == null) return;
                listener.onComplete(type, item);
            }
        }, busDb, application.mHashLocation);
        getData.startRelateRoute(item);
    }

    @Nullable
    private Parser getParser(int $key){
        switch ($key){
            case Constants.PARSER_LINE_TYPE:
            case Constants.PARSER_STOP_TYPE:
            case Constants.PARSER_ALALM_TYPE:
            case Constants.PARSER_LINE_DETAIL_TYPE:
            case Constants.PARSER_STOP_DETAIL_TYPE:
            case Constants.PARSER_LINE_DEPTH_TYPE:
            case Constants.PARSER_STOP_DEPTH_TYPE:
            case Constants.PARSER_RELATE_ROUTE_TYPE:
            case Constants.PARSER_NEXT_STOP:
            case Constants.PARSER_REFRSH_TYPE:
                break;
            default:
                return null;
        }
        synchronized (parsers){
            if(!parsers.containsKey($key)){
                parsers.put($key, new Parser($key));
            }
        }
        return parsers.get($key);
    }
    private boolean isExistParser(int $key){
        synchronized (parsers){
            return parsers.containsKey($key);
        }
    }
    //GetData를 관리하고 구동(루프),중지,정지등을 수행함
    final private class Parser{
        int type; //Constants.PARSER 시리즈
        final int constDelayTime = 1000 * 2; //2초
        Map<CommonItem,GetData> dataMap; //아이템과 GetData쌍
        boolean running = false; //loop를 돌고 있다면 true
        Handler handler; //loop 동작을 위해 사용
        int runId = 0; //clear()할때마다 새로운 ID가 만들어짐.
        Parser(final int $type){
            type = $type;
            dataMap = new HashMap<>();
            handler = new Handler();
        }
        synchronized void add(@NonNull final CommonItem $item, @NonNull final GetData $getData){
            dataMap.put($item, $getData);
        }
        synchronized void run(@NonNull final ParserRun $run, final int delayTime){
            if(running) return; //이미 실행중이면 수행못함
            if(dataMap.size() == 0){ //add부터 수행되어야 함
                running = false;
                return;
            }
            //실행
            Iterator<Map.Entry<CommonItem, GetData>> i = dataMap.entrySet().iterator();
            while(i.hasNext()){
                Map.Entry<CommonItem,GetData> pair = i.next();
                $run.run(pair.getKey(), pair.getValue());
            }
            //loop 실행
            if(delayTime > 0){
                final int dt = delayTime < constDelayTime ? constDelayTime : delayTime; //너무 작은 delay는 안됨
                running = true;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(running){
                            running = false;
                            Parser.this.run($run, dt); //재요청
                        }
                    }
                }, dt);
            }
        }
        synchronized void pause(){
            running = false;
        }
        synchronized void stop(){
            if(dataMap.size() > 0) for (Map.Entry<CommonItem, GetData> pair : dataMap.entrySet()) {
                pair.getValue().clear();
            }
            dataMap.clear();
            runId++;
            running = false;
        }
    }
    interface ParserRun{
        void run(CommonItem $item, GetData $getData);
    }

}
