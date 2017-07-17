簡易版 Dashboard

# pre install
* sbt
* scala 2.12.2
* maraidb

# init
### mariadb

use root account to create `demo` account and database
```mysql
create user 'demo'@'localhost';
set password for 'demo'@'localhost' = PASSWORD('');
create database dashboard;
grant all privileges on dashboard.* to 'demo'@'localhost' with GRANT OPTION;
```

use demo account to create table, init category data 
```mysql
CREATE TABLE Category (
  id INT NOT NULL AUTO_INCREMENT,
  name varchar(50) NOT NULL,
  PRIMARY KEY (id)
) DEFAULT CHARSET=utf8;


CREATE TABLE DailyData (
  date datetime NOT NULL,
  visitor INT NOT NULL DEFAULT 0,
  page_view INT NOT NULL DEFAULT 0,
  amount INT NOT NULL DEFAULT 0,
  order_count INT NOT NULL DEFAULT 0,
  customer INT NOT NULL DEFAULT 0,
  category_id INT NOT NULL,
  device ENUM('WEB','APP') NOT NULL,
  PRIMARY KEY (date, category_id, device)
) DEFAULT CHARSET=utf8;


CREATE TABLE HourlyData (
  date datetime NOT NULL,
  hour INT NOT NULL,
  visitor INT NOT NULL DEFAULT 0,
  page_view INT NOT NULL DEFAULT 0,
  amount INT NOT NULL DEFAULT 0,
  order_count INT NOT NULL DEFAULT 0,
  customer INT NOT NULL DEFAULT 0,
  category_id INT NOT NULL,
  device ENUM('WEB','APP') NOT NULL,
  PRIMARY KEY (date, hour, category_id, device)
) DEFAULT CHARSET=utf8;


CREATE TABLE DailyOrder (
  date datetime NOT NULL,
  amount INT NOT NULL DEFAULT 0,
  order_count INT NOT NULL DEFAULT 0,
  customer INT NOT NULL DEFAULT 0,
  category_id INT NOT NULL,
  source ENUM('DIRECT','EDM', 'GOOGLE_AD', 'FACEBOOK_AD') NOT NULL,
  PRIMARY KEY (date, category_id, source)
) DEFAULT CHARSET=utf8;


CREATE TABLE HourlyOrder (
  date datetime NOT NULL,
  hour INT NOT NULL,
  amount INT NOT NULL DEFAULT 0,
  order_count INT NOT NULL DEFAULT 0,
  customer INT NOT NULL DEFAULT 0,
  category_id INT NOT NULL,
  source ENUM('DIRECT','EDM', 'GOOGLE_AD', 'FACEBOOK_AD') NOT NULL,
  PRIMARY KEY (date, hour, category_id, source)
) DEFAULT CHARSET=utf8;



## Category
insert into Category(name) values('全站');
insert into Category(name) values('運動');
insert into Category(name) values('精品');
insert into Category(name) values('電競');
insert into Category(name) values('物聯');
```

switch project directory, add `application.conf`
```
mv application.conf.dev application.conf
```

init data
```
sbt "run-main shine.st.dashboard.generate.DemoData"
```

# run
### dev run (local run)
```
sbt run
```

### prod run 
package
```
sbt clean dist
```

unzip 
```
unzip target/universal/demo-0.0.1-SNAPSHOT.zip -d ${destination folder} 
```

run
```
cd ${destination folder} 
./bin/demo -Dconfig.resource=${prod_conf} -Dhttp.port=80
```

