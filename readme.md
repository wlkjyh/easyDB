# EasyDB

该项目可以让开发者使用原生sql或者查询构造器与数据库进行交互，同时``easyDB``还支持自动分页器，只需要传入一些参数就可以实现自动分页。



## 安装

将``EasyDB-1.0.2.jar``放到项目的``lib``目录下



配置maven

```xml
<dependency>
			<groupId>mysql</groupId>
			<artifactId>mysql-connector-java</artifactId>
			<version>8.0.33</version>
		</dependency>

		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<version>1.18.30</version>
		</dependency>


		<dependency>
			<groupId>com.zaxxer</groupId>
			<artifactId>HikariCP</artifactId>
			<version>2.7.1</version>
			<scope>compile</scope>
		</dependency>

		<dependency>
			<groupId>com.wlkjyy</groupId>
			<artifactId>EasyDB</artifactId>
			<version>1.0.2</version>
			<scope>system</scope>
			<systemPath>${project.basedir}/lib/EasyDB-1.0.2.jar</systemPath>
		</dependency>
```



```xml
<build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <includeSystemScope>true</includeSystemScope>
                </configuration>
            </plugin>
        </plugins>
    </build>
```





## 创建数据库连接

创建一个``Manager``实例，通过``addConnection``方法添加一个连接，通过``getEloquentInstance``方法获取一个DB实例

```java
import com.wlkjyy.Capsule.Manager;
import com.wlkjyy.Eloquent.DB;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @Author: wlkjyy <wlkjyy@vip.qq.com>
 * @Date: 2024/3/26
 * @Project: Default (Template) Project
 */
public class Main {
    public static void main(String[] args) throws SQLException {

        Manager manager = new Manager();
        manager.addConnection(new HashMap<>(){
            {
                put("host", "localhost");
                put("port", 3306);
                put("username", "root");
                put("password", "123456");
                put("database", "testdatabase");
            }
        });

        DB myDB = manager.getEloquentInstance();

    }
}

```



## springboot中使用

创建一个service

```java

@Service
public class db {

    @Bean
    public DB DB(){
        Manager manager = new Manager();
        manager.addConnection(new HashMap<>(){
            {
                put("host", "localhost");
                put("port", 3306);
                put("username", "root");
                put("password", "123456");
                put("database", "jianzhan");
            }
        });

        return manager.getEloquentInstance();
    }

}
```

使用

```java
@RestController
public class IndexController {

    @Autowired
    DB db;


    @GetMapping("/")
    public ArrayList<HashMap<String, Object>> index() throws SQLException {
        return db.table("users").get();
    }

}

```







## 说明

``easyDB``默认情况下是使用的``Hikari``连接池，相关参数位于``MySqlConnector.java``中



默认情况下，是使用``10``个连接。



## 查询构造器

### 从表中检索所有行

你可以使用 提供的 `table` 方法开始查询。table 方法为指定的表返回一个链式查询构造器实例，允许在查询上链接更多约束，最后使用 `get` 方法检索查询结果：

```java
ArrayList<HashMap<String, Object>> users = MyDB.table("users").get();
```



`get` 方法返回包含查询结果的 `ArrayList<HashMap<String, Object>>`，你可以这样去实现访问每一条数据

```java
ArrayList<HashMap<String, Object>> users = MyDB.table("users").get();

for (HashMap<String, Object> user : users) {
    System.out.println(user.get("username"));
}
```





### 包含条件的查询

``where``方法可以创建一个带有where的sql，通常情况下，``where``方法包含三个参数，分别为``字段``、``操作``、``结果``，当不传入``操作``时，``操作``默认为``=``

例如下方这个示例，可以查询``users``表下所有``active``为1的``所有``数据，返回一个``ArrayList<HashMap<String, Object>>``类型的数据

```java
MyDB.table("users").where("active",1).get();
```

例如下方这个示例，可以查询``users``表下所有``active``大于1的``所有``数据

```java
MyDB.table("users").where("active",">",1).get();
```



### 只获取第一条数据

有时，你只想获取第一条数据，你就可以通过``first``方法，你可以查询出第一条数据，他是基于``limit 1``实现的，返回类型为``HashMap<String, Object>``

```java
MyDB.table("users").where("active",1).first();
```



### 通过主键查询一条数据

你可以通过``find``方法实现使用主键来查询``一条数据``，当不传入主键时，主键默认为``id``

```java
MyDB.table("users").find(1);
```

如果你的表主键非``id``时，你需要指定一个主键

```java
MyDB.table("users").find("uid",1);
```



### 获取构造器生成SQL

你可以使用``getSql``方法实现获取查询构造器生成的``SQL``代码

```java
DB.table("users").where("active",1).orWhere("username","admin").getSql()
```

输出如下

```sql
select * from `users` where `active` = ?  or `username` = ?
```



### 多条件查询

通常情况下，你的查询并不是只有一个条件，你可以使用多个``where``来实现多条件查询

例如下方这个示例，查询`users`表下`active`为1且``username``为`admin`的第一条数据

```java
MyDB.table("users").where("active",1).where("username","admin").first();
```



### orWhere

``orWhere``可以实现一个``或条件查询``，当然也可以指定多个``orWhere``

例如下方这个示例，可以查询``users``表下``active``为1或者``username``为``admin``的第一条数据

```java
MyDB.table("users").where("active",1).orWhere("username","admin").first();
```

该构造器生成的SQL语句如下：

```sql
select * from `users` where `active` = ?  or `username` = ?
```



### md5Where / orMd5Where

查询``users``表下``username``为``admin``且``password``为md5加密的``123456``的第一条数据

```java
MyDB.table("users").where("username","admin").md5Where("password","123456").first();
```



查询``users``表下``username``为``admin``或者``password``为md5加密的``123456``的第一条数据

```java
MyDB.table("users").where("username","admin").orMd5Where("password","123456").first();
```





### whereBetween / orWhereBetween

whereBetween可以查询某个区间内的值

```java
MyDB.table("users").whereBetween("id", 1, 5).get()
```



orWhereBetween可以查询某个区间内的值

```java
MyDB.table("users").orWhereBetween("id", 1, 5).get()
```



### whereIn / orWhereIn

whereIn可以查询某个字段特定的值

```java
DB.table("users").whereIn("id", new Object[]{1,7}).get()
```

### 

orWhereIn可以查询某个字段特定的值

```java
MyDB.table("users").orWhereIn("id", new Object[]{1,7}).get()
```



### whereNotBetween / orWhereNotBetween

``whereNotBetween`` 方法用于验证字段的值是否不在给定的两个值范围之中：

```java
MyDB.table("users").whereNotBetween ("id", 1, 5).get()
```

```java
MyDB.table("users").orWhereNotBetween ("id", 1, 5).get()
```



### whereNull / whereNotNull

``whereNull``和``whereNotNull``可以验证字段是否为null或不为null

```java
MyDB.table("users").whereNull("verify_email").first()
```

```java
MyDB.table("users").whereNotNull("verify_email").first()
```



### whereColumn / orWhereColumn

``whereColumn``和``orWhereColumn``可以实现对两个``字段``进行比较



例如下面这样

```java
MyDB.table("users").whereColumn("id","uid").first()
```

也可以是这样

```java
MyDB.table("users").whereColumn("id",">","uid").first()
```

也可以连贯操作

```java
DB.table("users").whereColumn("id","=","uid").orWhereColumn("id","<","uid").get()
```







### 删除语句

查询构建器的 `delete` 方法可用于从表中删除记录。``delete``方法会返回受影响的行数。你可以通过在调用 `delete` 方法之前添加 `where` 子句来限制 `delete` 语句：

```java
MyDB.table("users").where("username","test123").delete();
```

如果你希望截断整个表，这将从表中删除所有记录并将自动递增 ID 重置为零，你可以使用 `truncate` 方法：

```java
MyDB.table('users').truncate();
```



### 更新一条数据

通过``update``方法可以实现数据的更新，需要传入一个Hashmap的数据格式，其中``key``为字段，``value``为设置的值



``update``方法会返回受影响的行数，通常情况下为0行代表执行错误。



更新users表下username为``test``的数据，将token更新为``123456``

```java
MyDB.table("users").where("username","test").update(new HashMap<String, Object>(){{
	put("token",123456);
}});
```



### 获取条数

通过``count``方法可以获取总条数，返回一个``Int``类型数据

获取``users``表下``active``为1的条数

```java
MyDB.table("users").where("active",1).count();
```



### 获取最大值

通过``max``方法可以获取最大值，返回一个``Double``类型数据



获取``users``表下``uid``的最大值

```java
MyDB.table("users").max("uid");
```

获取``users``表下``active``为1的uid最大值

```java
MyDB.table("users").where("active",1).max("uid");
```



### 获取最小值

通过``min``方法可以获取最小值，返回一个``Double``类型数据



获取``users``表下``uid``的最小值

```java
MyDB.table("users").min("uid");
```

获取``users``表下``active``为1的uid最小值

```java
MyDB.table("users").where("active",1).min("uid");
```





### 获取平均值

通过``avg``方法可以获取最小值，返回一个``Double``类型数据



获取``users``表下``uid``的平均值

```java
MyDB.table("users").avg("uid");
```

获取``users``表下``active``为1的uid平均值

```java
MyDB.table("users").where("active",1).avg("uid");
```



### limit

```java
MyDB.table("users").where("active",1).limit(0,100).get()
```

等价于

```java
MyDB.table("users").where("active",1).limit(100).get()
```



### 自增与自减
查询构造器还提供了方便的方法来增加或减少给定列的值。这两种方法都至少接受一个参数：要修改的列。可以提供第二个参数来指定列应该增加或减少的数量：

```java 
// 默认+1
MyDB.table("users").where("active",1).increment("money");
// 指定+5
MyDB.table("users").where("active",1).increment("money",5);
// 默认-1
MyDB.table("users").where("active",1).decrement("money");
// 指定-5
MyDB.table("users").where("active",1).decrement("money",5);
```



此外，你可以使用 `incrementEach` 和 `decrementEach` 方法同时增加或减少多个列:

```java
DB.table("users").where("id",2).incrementEach(new HashMap<String, Object>(){{
    put("money",1.6);
    put("age",1);
}})
```



### 判断记录是否存在

除了通过 `count` 方法可以确定查询条件的结果是否存在之外，还可以使用 `exists` 和 `doesntExist` 方法：

```java
if(MyDB.table("users").where("username","admin").exists()){
    // .....
}
```

```java
if(MyDB.table("users").where("username","admin").doesntExist()){
    // ...
}
```

### 让结果不重复

``distinct``方法可以让结果不重复，例如下面这样

```java
MyDB.table("users").distinct("username").get()
```

``distinct``方法也可以指定多个列

```java
MyDB.table("users").distinct("username","nickname").get()
```



### 在查询中添加count、sum、avg、min、max

通过``addColumnCount``、``addColumnSum``、``addColumnAvg``、``addColumnMin``、``addColumnMax``中可以加入count、sum、avg、min、max等查询。



例如下面这样

```java
MyDB.table("users").addColumnCount("username").get()
```

默认情况下，统计后的名字为``count``，你也可以为统计字段可以设置一个别名``user_count_number``，例如这样

```java
MyDB.table("users").addColumnCount("username","user_count_number").get()
```

可以得到以下结果

```java
[{user_count_number=2}]
```

你也可以添加多个统计，例如这样

```java
DB.table("users").addColumnCount("id","id_count").addColumnSum("id","sum").get()
```



### groupBy分组

通过下面这个示例，可以获取出所有不重复的``用户名``，切记，``groupBy``需要配置``select``使用

```java
MyDB.table("users").select("username").groupBy("username").get()
```

```
[{username=wlkjyy}, {username=qian}]
```

通过下面这个示例，你可以对分组后的``money``进行求和

```java
MyDB.table("users").select("username").addColumnSum("money","all_money").groupBy("username").get()
```

```
[{all_money=5, username=wlkjyy}, {all_money=1, username=qian}]
```





### 排序

#### orderBy

```java
MyDB.table("users").where("active",1).limit(100).orderBy("id","asc").get()
```

等价于

```java
MyDB.table("users").where("active",1).limit(100).orderBy("id").get()
```

多个orderBy排序

```java
MyDB.table("users").where("active",1).limit(100).orderBy("id","asc").orderBy("username").get()
```



#### orderByAsc / orderByDesc

``orderByAsc``和``orderByDesc``可以对某个字段进行排序



例如下面这样

```java
MyDB.table("users").orderByAsc("id").get()
```

他等价于

```java
MyDB.table("users").orderBy("id","asc").get()
```





#### 随机排序

`inRandomOrder` 方法被用来将查询结果随机排序。例如，你可以使用这个方法去获得一个随机用户:

```java
MyDB.table("users").inRandomOrder().first()
```





#### latest 和 oldest 方法

``latest`` 和 ``oldest`` 方法可以方便让你把结果根据日期排序。查询结果默认根据数据表的 ``created_at ``字段进行排序 。或者，你可以传一个你想要排序的列名，通过:

```java
MyDB.table("users").select("uid").latest().get()
```

```java
MyDB.table("users").select("uid").oldest().get()
```



当然也可以传入其他字段进行排序

```java
MyDB.table("users").select("uid").latest("time").get()
```



#### **reorder**

`reorder` 方法可用于移除所有已存在的排序并且应用一个新的排序（可选）：

```java
MyDB.table("users").orderByAsc("id").reorder().first()
```

也可以这样做

```java
MyDB.table("users").orderByAsc("id").reorder("id","desc").first()
```



### select

通过``select``可以设置只获取那些字段出来，如果不进行设置，默认情况下为`*`，即所有字段

```java
MyDB.table("users").select("uid").get()
```

输出为

```
[{uid=1}, {uid=2}, {uid=7}]
```



### addSelect

通过``addSelect``可以动态的添加要查询的字段

```java
MyDB.table("users").select("uid").addSelect("user").get()
```



### 插入一条数据

 你可以通过``insert``方法向表中插入一条数据

```java
MyDB.table("users").insert(new HashMap<>(){
    {
        put("username","admin");
        put("password","123456");
    }
})
```







## 原生sql

对于查询，返回类型为``ArrayList<HashMap<String, Object>>``，对于删除、更新、插入，返回类型为``int``受影响的行数

### 查询

```java
MyDB.query("select * from `user`")
```

### 带有参数绑定的查询

```java
MyDB.query("select * from `admin` where `uid` = ?", new Object[]{1})
```



### 更新

```java
MyDB.update("update `user` set `cookie`=123123 where uid = 1")
```

### 带有参数绑定的更新

```java
MyDB.update("update `user` set `cookie`=123123 where uid = ?",new Object[]{
    1
})
```



### 删除

```java
MyDB.delete("delete from `users` where `uid` = 1");
```

### 带有参数绑定的删除

```java
MyDB.delete("delete from `users` where `uid` = ?",new Object[]{
    1
});
```





## 自动分页器

``easyDB``允许用户传入一些简单的参数就可以自动生成前端分页代码或者输出``json``格式的数据到前端。



### 创建一个分页器实例

```java
db.table("users").paginate(15,1)
```

如果你希望使用自动分页器，那么你就不需要去使用``get``、``find``、``first``方法去获取数据了，你需要直接使用``paginate``来获取一个分页器实例。

``paginate``方法包含3个参数，分别为``每页的数量``、``当前页``、``分页参数``，其中``分页参数``默认不传入情况下为``page``，则URL路径为``?page=1``

你也可以这样

```java
db.table("users").paginate(15,1,"pages")
```

在Springboot中你可以这样做

```java
db.table("users").paginate(15,Integer.valueOf((String) request.getAttribute("pages")),"pages")
```



### 渲染分页HTML到前端

在springboot中可以这样实现

```
@GetMapping("/test")
public String index(Model model,HttpServletRequest request) {

    Paginate paginate = db.table("users").paginate(15,Integer.valueOf((String) request.getAttribute("pages")),"pages");
    model.addAttribute("paginate", paginate);

    return "index";

}
```

index.html

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>

[[${paginate.links()}]]

</body>
</html>
```



### 渲染数据

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
    
<div th:each="row:${paginate.item()}">
    <span th:text="${row.id}"></span>
    <span th:text="${row.username}"></span>
</div>

[[${paginate.links()}]]

</body>
</html>
```





``easyDB``目前只支持渲染``bootstrap5``的分页代码，你可以在``Paginate.java``中实现自己的分页代码渲染逻辑。

### 添加分页参数

通过``links``方法渲染的HTML分页代码，默认情况下``url``只包含``page``分页参数，你可以使用下面方法添加更多的分页参数

```html
[[${paginate.links("usertype=admin&local=admin")}]]
```

你也可以在创建分页器的时候指定分页参数

```java
db.table("users").paginate(15,1).setParamter("username=admin")
```

### 输出分页输出为json

你可以通过下面这个示例，将分页输出为json

```
@RestController
public class IndexController {

    @Autowired
    DB db;

    @GetMapping("/test")
    public String index(HttpServletRequest request) {

        return db.table("users").paginate(15,1).toHashmap();

    }

}
```



### 分页器实例方法
每一个分页器实例都提供了下列方法来获取分页信息：



| 方法                       | 描述                   |
| -------------------------- | ---------------------- |
| paginate.count()           | 获取分页的总数据数量   |
| paginate.currentPage()     | 获取当前页码           |
| paginate.hasPages()        | 判断是否有分页         |
| paginate.links()           | 获取分页HTML渲染       |
| paginate.setParamter()     | 设置额外的分页参数     |
| paginate.toHashmap()       | 转换为hashmap          |
| paginate.count()           | 获取当前分页的数据数量 |
| paginate.items()           | 获取当前分页的数据     |
| paginate.lastPage()        | 获取最后一页的页码     |
| paginate.lastPageUrl()     | 获取最后一页的URL      |
| paginate.firstPageUrl()    | 获取第一页的URL        |
| paginate.nextPageUrl()     | 获取下一页的URL        |
| paginate.onFirstPage()     | 判断是否是第一页       |
| paginate.perPage()         | 获取每页的数量         |
| paginate.previousPageUrl() | 获取上一页的URL        |
| paginate.total()           | 获取数据总量           |
| paginate.url()             | 获取指定页的URL        |
| paginate.hasMorePages()    | 是否有更多的页         |

