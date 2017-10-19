#dayofweek_create : 요일 (1:일, 2:월.....)
CREATE TABLE IF NOT EXISTS dayofweek(
    dayofweek_rowid INTEGER PRIMARY KEY AUTOINCREMENT,
    title VARCHAR(255) not null,
    CONSTRAINT unique_dayofweek UNIQUE (dayofweek_rowid)
)

#dayofweek_add : 요일 insert
INSERT OR REPLACE INTO dayofweek(dayofweek_rowid,title)values
(1,'일'),(2,'월'),(3,'화'),(4,'수'),(5,'목'),(6,'금'),(7,'토')

#member_create : 회원(지금은 그냥 임시이며 rowid=0인 1개만 입력됨)
CREATE TABLE IF NOT EXISTS member(
    member_rowid INTEGER PRIMARY KEY AUTOINCREMENT,
    userid VARCHAR(255) not null,
    username VARCHAR(255) not null,
    CONSTRAINT unique_member UNIQUE (userid)
)

#member_default_add : 회원 기본값 insert
INSERT OR REPLACE INTO member(member_rowid,userid,username)values(0,'@none', '@none')

#work_create : 출퇴근
CREATE TABLE IF NOT EXISTS work(
    work_rowid INTEGER PRIMARY KEY AUTOINCREMENT,
    member_rowid INTEGER not null default 0,
    CONSTRAINT unique_work UNIQUE (member_rowid)
)

#work_default_add : 출퇴근 기본값 insert
INSERT OR REPLACE INTO work(work_rowid,member_rowid)values(1, 0)

#workday_create : 근무하는 날
CREATE TABLE IF NOT EXISTS workday(
    workday_rowid INTEGER PRIMARY KEY AUTOINCREMENT,
    work_rowid INTEGER not null,
    dayofweek_rowid INTEGER not null,
    CONSTRAINT unique_workday UNIQUE (work_rowid,dayofweek_rowid)
)

#workon_create : 출근시간
CREATE TABLE IF NOT EXISTS workon(
    workon_rowid INTEGER PRIMARY KEY AUTOINCREMENT,
    work_rowid INTEGER not null,
    hour INTEGER not null,
    min INTEGER not null,
    CONSTRAINT unique_workon UNIQUE (work_rowid)
)

#workoff_create : 퇴근시간
CREATE TABLE IF NOT EXISTS workoff(
    workoff_rowid INTEGER PRIMARY KEY AUTOINCREMENT,
    work_rowid INTEGER not null,
    hour INTEGER not null,
    min INTEGER not null,
    CONSTRAINT unique_workoff UNIQUE (work_rowid)
)

#workonactive_create : 출근시간 활성화
CREATE TABLE IF NOT EXISTS workonactive(
    workonactive_rowid INTEGER PRIMARY KEY AUTOINCREMENT,
    workon_rowid INTEGER not null,
    alarmpid_rowid INTEGER not null,
    CONSTRAINT unique_workonactive UNIQUE (workon_rowid)
)

#workoffactive_create : 퇴근시간 활성화
CREATE TABLE IF NOT EXISTS workoffactive(
    workoffactive_rowid INTEGER PRIMARY KEY AUTOINCREMENT,
    workoff_rowid INTEGER not null,
    alarmpid_rowid INTEGER not null,
    CONSTRAINT unique_workoffactive UNIQUE (workoff_rowid)
)

#workonfavorite_create : 출근길
CREATE TABLE IF NOT EXISTS workonfavorite(
    workonfavorite_rowid INTEGER PRIMARY KEY AUTOINCREMENT,
    work_rowid INTEGER not null,
    favorite_id INTEGER not null,
    regdate DATETIME not null default current_timestamp,
    CONSTRAINT unique_workonfavorite UNIQUE (work_rowid,favorite_id)
)

#workofffavorite_create : 퇴근길
CREATE TABLE IF NOT EXISTS workofffavorite(
    workofffavorite_rowid INTEGER PRIMARY KEY AUTOINCREMENT,
    work_rowid INTEGER not null,
    favorite_id INTEGER not null,
    regdate DATETIME not null default current_timestamp,
    CONSTRAINT unique_workofffavorite UNIQUE (work_rowid,favorite_id)
)

#alarmpid_create : 알람용pid
CREATE TABLE IF NOT EXISTS alarmpid(
    alarmpid_rowid INTEGER PRIMARY KEY AUTOINCREMENT
)

#buson_create : 버스 승차알람
CREATE TABLE IF NOT EXISTS buson(
    buson_rowid INTEGER PRIMARY KEY AUTOINCREMENT,
    member_rowid INTEGER not null,
    cityId INTEGER not null,
    routeId1 VARCHAR(255) not null,
    routeId2 VARCHAR(255) not null,
    stopArsId VARCHAR(255) not null,
    stopApiId VARCHAR(255) not null,
    busNum VARCHAR(255) not null,
    min INTEGER not null,
    regdate DATETIME not null default current_timestamp,
    CONSTRAINT unique_geton UNIQUE (member_rowid)
)

#busoff_create : 하차알람
CREATE TABLE IF NOT EXISTS busoff(
    busoff_rowid INTEGER PRIMARY KEY AUTOINCREMENT,
    member_rowid INTEGER not null,
    cityId INTEGER not null,
    routeId1 VARCHAR(255) not null,
    routeId2 VARCHAR(255) not null,
    stopArsId VARCHAR(255) not null,
    stopApiId VARCHAR(255) not null,
    regdate DATETIME not null default current_timestamp,
    CONSTRAINT unique_getoff UNIQUE (member_rowid)
)
