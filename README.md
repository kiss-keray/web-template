# web-template
web后端系统开发模板
*** 
# 框架
springboot<br>
MyBatis<br>
freemaker<br>
mysql

## common.sql 基础数据库表
member表:基本用户表 可按需求增删<br>
role：角色表
roleInterface：权限接口表
role_roleinterface：角色-接口 多对多中间表
## 基本手册
直接在controller包下开发接口。service继承基础BaseService实现数据库基本的增删改查list操作，
dao直接基础BaseMapper接口。model基础BaseModel基类。mapping的编写可以直接使用freemaker包下的XMLService直接生产基础mapper方法实现
