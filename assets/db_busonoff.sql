#buson_add : 승차알람 등록 및 수정, 기존것이 있으면 굳이 del을 호출안하고 이걸로 해도 됨
INSERT OR REPLACE buson(buson_rowid,member_rowid,cityId,routeId1,routeId2,stopArsId,stopApiId,busNum,min,regdate)
SELECT
    (SELECT buson_rowid FROM buson WHERE member_rowid=t0.member_rowid),
    t0.member_rowid,
    @cityId:i@,@routeId1:s@,@routeId2:s@,@stopArsId:s@,@stopApiId:s@,@busNum:s@,@min:i@,
    datetime('now','utc')
FROM member t0
WHERE t0.member_rowid=0

#buson_get : 승차알람 등록정보 가져오기
SELECT buson_rowid,member_rowid,cityId,routeId1,routeId2,stopArsId,stopApiId,busNum,min,regdate FROM buson WHERE member_rowid=0

#buson_del : 승차알람 삭제
DELETE FROM buson WHERE member_rowid=0

#busoff_add : 하차알람 등록 및 수정, 기존것이 있으면 굳이 del을 호출안하고 이걸로 해도 됨
INSERT OR REPLACE busoff(busoff_rowid,member_rowid,cityId,routeId1,routeId2,stopArsId,stopApiId,regdate)
SELECT
    (SELECT busoff_rowid FROM busoff WHERE member_rowid=t0.member_rowid),
    t0.member_rowid,
    @cityId:i@,@routeId1:s@,@routeId2:s@,@stopArsId:s@,@stopApiId:s@,
    datetime('now','utc')
FROM member t0
WHERE t0.member_rowid=0

#busoff_get : 하차알람 등록정보 가져오기
SELECT busoff_rowid,member_rowid,cityId,routeId1,routeId2,stopArsId,stopApiId,regdate FROM busoff WHERE member_rowid=0

#busoff_del : 하차알람 삭제
DELETE FROM busoff WHERE member_rowid=0