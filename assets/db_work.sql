#alarmpid_add : 알람용 pid 추가
INSERT INTO alarmpid(alarmpid_rowid)values(null)

#workon_default_set : 출근시간 기본값 셋팅. 기존값 있으면 유지
INSERT OR REPLACE INTO workon(workon_rowid,work_rowid,hour,min)
SELECT
    (SELECT workon_rowid FROM workon where work_rowid=t0.work_rowid),
    t0.work_rowid,
    CASE WHEN t1.hour IS NULL THEN 9 ELSE t1.hour END,
    CASE WHEN t1.min IS NULL THEN 0 ELSE t1.min END
FROM work t0
LEFT JOIN workon t1 ON t0.work_rowid=t1.work_rowid
WHERE t0.member_rowid=0

#workonactive_add : 출근시간 활성화
INSERT OR REPLACE INTO workonactive(workonactive_rowid,workon_rowid,alarmpid_rowid)
SELECT
    (SELECT workonactive_rowid FROM workonactive where workon_rowid=t0.workon_rowid),
    t0.workon_rowid,
    t2.alarmpid_rowid
FROM workon t0
INNER JOIN work t1 on t0.work_rowid=t1.work_rowid
INNER JOIN alarmpid t2 on t2.alarmpid_rowid=(SELECT max(alarmpid_rowid) FROM alarmpid)
WHERE t1.member_rowid=0

#workonactive_del : 출근시간 비활성화
DELETE FROM workonactive WHERE workon_rowid=(SELECT workon_rowid FROM workon WHERE work_rowid=(SELECT work_rowid FROM work WHERE member_rowid=0))

#workoff_default_set : 퇴근시간 기본값 셋팅. 기존값 있으면 유지
INSERT OR REPLACE INTO workoff(workoff_rowid,work_rowid,hour,min)
SELECT
    (SELECT workoff_rowid FROM workoff where work_rowid=t0.work_rowid),
    t0.work_rowid,
    CASE WHEN t1.hour IS NULL THEN 18 ELSE t1.hour END,
    CASE WHEN t1.min IS NULL THEN 0 ELSE t1.min END
FROM work t0
LEFT JOIN workoff t1 ON t0.work_rowid=t1.work_rowid
WHERE t0.member_rowid=0

#workoffactive_add : 퇴근시간 활성화
INSERT OR REPLACE INTO workoffactive(workoffactive_rowid,workoff_rowid,alarmpid_rowid)
SELECT
    (SELECT workoffactive_rowid FROM workoffactive where workoff_rowid=t0.workoff_rowid),
    t0.workoff_rowid,
    t2.alarmpid_rowid
FROM workoff t0
INNER JOIN work t1 on t0.work_rowid=t1.work_rowid
INNER JOIN alarmpid t2 on t2.alarmpid_rowid=(SELECT max(alarmpid_rowid) FROM alarmpid)
WHERE t1.member_rowid=0

#workoffactive_del : 퇴근시간 비활성화
DELETE FROM workoffactive WHERE workoff_rowid=(SELECT workoff_rowid FROM workoff WHERE work_rowid=(SELECT work_rowid FROM work WHERE member_rowid=0))

#workon_set : 출근시간 설정
INSERT OR REPLACE INTO workon(workon_rowid,work_rowid,hour,min)
SELECT
    (SELECT workon_rowid FROM workon where work_rowid=t0.work_rowid),
    t0.work_rowid,
    CASE
        WHEN CAST(@hour:i@ as decimal)<0 THEN 0
        WHEN CAST(@hour:i@ as decimal)>23 THEN 23
        ELSE @hour:i@
    END,
    CASE
        WHEN CAST(@min:i@ as decimal)<0 THEN 0
        WHEN CAST(@min:i@ as decimal)>59 THEN 59
        ELSE @min:i@
    END
FROM work t0
LEFT JOIN workon t1 ON t0.work_rowid=t1.work_rowid
WHERE t0.member_rowid=0

#workoff_set : 퇴근시간 설정
INSERT OR REPLACE INTO workoff(workoff_rowid,work_rowid,hour,min)
SELECT
    (SELECT workoff_rowid FROM workoff where work_rowid=t0.work_rowid),
    t0.work_rowid,
    CASE
        WHEN CAST(@hour:i@ as decimal)<0 THEN 0
        WHEN CAST(@hour:i@ as decimal)>23 THEN 23
        ELSE @hour:i@
    END,
    CASE
        WHEN CAST(@min:i@ as decimal)<0 THEN 0
        WHEN CAST(@min:i@ as decimal)>59 THEN 59
        ELSE @min:i@
    END
FROM work t0
LEFT JOIN workoff t1 ON t0.work_rowid=t1.work_rowid
WHERE t0.member_rowid=0

#workon_get : 출근시간 얻기
SELECT t0.workon_rowid rowid,t0.work_rowid,t0.hour,t0.min,t2.alarmpid_rowid pid
FROM workon t0
INNER JOIN work t1 on t0.work_rowid=t1.work_rowid
INNER JOIN workonactive t2 on t0.workon_rowid=t2.workon_rowid
WHERE t1.member_rowid=0

#workoff_get : 퇴근시간 얻기
SELECT t0.workoff_rowid rowid,t0.work_rowid,t0.hour,t0.min,t2.alarmpid_rowid pid
FROM workoff t0
INNER JOIN work t1 on t0.work_rowid=t1.work_rowid
INNER JOIN workoffactive t2 on t0.workoff_rowid=t2.workoff_rowid
WHERE t1.member_rowid=0

#workon_isactive : 출근시간 활성인가?
SELECT count(*)cnt FROM workon t0
INNER JOIN work t1 on t0.work_rowid=t1.work_rowid
INNER JOIN workonactive t2 on t0.workon_rowid=t2.workon_rowid
WHERE t1.member_rowid=0

#workoff_isactive : 퇴근시간 활성인가?
SELECT count(*)cnt FROM workoff t0
INNER JOIN work t1 on t0.work_rowid=t1.work_rowid
INNER JOIN workoffactive t2 on t0.workoff_rowid=t2.workoff_rowid
WHERE t1.member_rowid=0

#workday_del : 근무요일 삭제, 추가하기 전에 먼저 이것부터 실행하자!
DELETE FROM workday WHERE work_rowid=(SELECT work_rowid FROM work WHERE member_rowid=0) AND dayofweek_rowid NOT IN(@dayofweek_rowids@)

#workday_add : 근무요일 추가. workday_del부터 요청할 것
INSERT OR REPLACE INTO workday(workday_rowid,work_rowid,dayofweek_rowid)
SELECT
    (SELECT workday_rowid FROM workday where work_rowid=t1.work_rowid and dayofweek_rowid=t0.dayofweek_rowid),
    t1.work_rowid,
    t0.dayofweek_rowid
FROM dayofweek t0
INNER JOIN work t1 on t1.work_rowid=(SELECT work_rowid FROM work WHERE member_rowid=0)
WHERE t0.dayofweek_rowid IN(@dayofweek_rowids@)
ORDER BY t0.dayofweek_rowid asc

#workday_list : 근무요일 리스트
SELECT t0.workday_rowid,t0.dayofweek_rowid,t1.title name
FROM workday t0
LEFT JOIN dayofweek t1 on t0.dayofweek_rowid=t1.dayofweek_rowid
WHERE t0.work_rowid=(SELECT work_rowid FROM work WHERE member_rowid=0)
ORDER BY t1.dayofweek_rowid ASC

#workonfavorite_select_list : 출근길 선택 대상 목록, favorite목록중 출근길 대상으로 등록되지 않은 것들
SELECT 0 rowid,_id,city,type,typeID,typeID2,typeID3,typeID4,nickname,color,favorite_order,nickname2,temp1,temp2
FROM FAVORITE
WHERE _id not in(
    SELECT favorite_id FROM workonfavorite WHERE work_rowid=(SELECT work_rowid FROM work WHERE member_rowid=0)
)and type=2

#workofffavorite_select_list : 퇴근길 선택 대상 목록, favorite목록중 퇴근길 대상으로 등록되지 않은 것들
SELECT 0 rowid,_id,city,type,typeID,typeID2,typeID3,typeID4,nickname,color,favorite_order,nickname2,temp1,temp2
FROM FAVORITE t0
WHERE _id not in(
    SELECT favorite_id FROM workofffavorite WHERE work_rowid=(SELECT work_rowid FROM work WHERE member_rowid=0)
)and type=2

#workonfavorite_add : 출근길 추가
INSERT OR REPLACE INTO workonfavorite(workonfavorite_rowid,work_rowid,favorite_id,regdate)
SELECT
    (SELECT workonfavorite_rowid FROM workonfavorite where work_rowid=t0.work_rowid and favorite_id=@favorite_id:i@),
    t0.work_rowid,
    @favorite_id:i@,
    datetime('now','utc')
FROM work t0
INNER JOIN FAVORITE t1 on t1._id=@favorite_id:i@ and t1.type=2
WHERE t0.member_rowid=0


#workofffavorite_add : 퇴근길 추가
INSERT OR REPLACE INTO workofffavorite(workofffavorite_rowid,work_rowid,favorite_id,regdate)
SELECT
    (SELECT workofffavorite_rowid FROM workofffavorite where work_rowid=t0.work_rowid and favorite_id=@favorite_id:i@),
    t0.work_rowid,
    @favorite_id:i@,
    datetime('now','utc')
FROM work t0
INNER JOIN FAVORITE t1 on t1._id=@favorite_id:i@ and t1.type=2
WHERE t0.member_rowid=0

#workonfavorite_list : 출근길 리스트
SELECT t0.workonfavorite_rowid rowid,t1._id,t1.city,t1.type,t1.typeID,t1.typeID2,t1.typeID3,t1.typeID4,t1.nickname,t1.color,t1.favorite_order,t1.nickname2,t1.temp1,t1.temp2
FROM workonfavorite t0
INNER JOIN FAVORITE t1 on t0.favorite_id=t1._id and t1.type=2
WHERE t0.favorite_id in(
    SELECT favorite_id FROM workonfavorite WHERE work_rowid=(SELECT work_rowid FROM work WHERE member_rowid=0)
)
ORDER BY t0.regdate desc

#workofffavorite_list : 퇴근길 리스트
SELECT t0.workofffavorite_rowid rowid,t1._id,t1.city,t1.type,t1.typeID,t1.typeID2,t1.typeID3,t1.typeID4,t1.nickname,t1.color,t1.favorite_order,t1.nickname2,t1.temp1,t1.temp2
FROM workofffavorite t0
INNER JOIN FAVORITE t1 on t0.favorite_id=t1._id and t1.type=2
WHERE t0.favorite_id in(
    SELECT favorite_id FROM workofffavorite WHERE work_rowid=(SELECT work_rowid FROM work WHERE member_rowid=0)
)
ORDER BY t0.regdate desc

#workonfavorite_del : 출근길 제거
DELETE FROM workonfavorite WHERE workonfavorite_rowid=@workonfavorite_rowid:i@

#workofffavorite_del : 퇴근길 제거
DELETE FROM workofffavorite WHERE workofffavorite_rowid=@workofffavorite_rowid:i@

#workfavorite_list_from_ids : favorite id 리스트로부터 Favorite 리스트 가져오기
SELECT 0 rowid,_id,city,type,typeID,typeID2,typeID3,typeID4,nickname,color,favorite_order,nickname2,temp1,temp2
FROM FAVORITE
WHERE _id in(@favorite_ids@)
