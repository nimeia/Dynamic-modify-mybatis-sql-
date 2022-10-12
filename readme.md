# Dynamic modify mybatis sql 

this demo show how to modify the insert and select sql .It`s base the mybatis orm framework
for example:
* wheh mybatis exe the sql `insert into city (name,state) values(#{name},#{state})` .
* the plugin will edit the sql and add the `appid`,the modified sql may be like this 
* `insert into city (name,state,appid) values(#{name},#{state},${appid})`