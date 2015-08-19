

Collection of simple aspect classes that you can use in your Spring application:

  * performance logging (using [JaMon library](http://jamonapi.sourceforge.net/))
  * caching bean method invocations results (using [Ehcache](http://ehcache.org/))


## General ##

To use common-spring-aspects with Maven, add to your pom.xml (or if you don't use Maven, just [download the jars](http://common-spring-aspects.googlecode.com/svn/maven2/com/googlecode/commonspringaspects/common-spring-aspects/)):

```
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-aspects</artifactId>
    <version>${springframework-version}</version>
</dependency>
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjrt</artifactId>
    <version>1.6.7</version>
</dependency>
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>1.6.7</version>
</dependency>

<dependency>
    <groupId>com.googlecode.commonspringaspects</groupId>
    <artifactId>common-spring-aspects</artifactId>
    <version>1.0.9</version>
</dependency>

<repositories>
    <repository>
        <id>common-spring-aspects</id>
        <url>http://common-spring-aspects.googlecode.com/svn/maven2</url>
    </repository>
</repositories>
```

## Performance Logging ##

In this example, we will configure Spring JaMon advice to track performance of DAO and service methods execution. We will configure a servlet that outputs the performance tracking results. Also, JaMon will log method executions that take 500ms or more.

Add to your Spring configuration file (applicationContext.xml):

```
    <!-- ========================= Aspects ========================= -->

    <aop:config>
        <aop:aspect ref="jamonAdvice">
            <aop:around method="monitor" pointcut="execution(public * com.example..*.*ServiceImpl.*(..))" />
            <aop:around method="monitor" pointcut="execution(public * com.example..*.*DAO.*(..))" />
            <aop:around method="monitor" pointcut="execution(public * com.example..*.*Dao.*(..))" />
            <aop:around method="monitor" pointcut="execution(public * com.example..*.*DaoImpl.*(..))" />
        </aop:aspect>
    </aop:config>

    <bean id="jamonAdvice" class="com.googlecode.commonspringaspects.aspects.JamonAspect">
        <property name="enabled" value="true" />
        <property name="logThresholdMilliseconds" value="500" />
    </bean>

    <bean id="performance" class="com.googlecode.commonspringaspects.servlets.PerformanceHttpRequestHandler">
        <property name="enabled" value="true" />
    </bean>
```

You will also need to add prefix "aop" in case you don't have one yet:
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:aop="http://www.springframework.org/schema/aop"
    xsi:schemaLocation="
http://www.springframework.org/schema/aop 
...">
...
```

Add to web.xml:

```
    <servlet>
        <servlet-name>performance</servlet-name>
        <servlet-class>org.springframework.web.context.support.HttpRequestHandlerServlet</servlet-class>
    </servlet>

    <servlet-mapping>
        <servlet-name>performance</servlet-name>
        <url-pattern>/performance</url-pattern>
    </servlet-mapping>
```

Now your can run you application and see the JaMon performance report at http://localhost:8080/performance.


## Caching Bean Method Invocation Results ##

Often, you want to improve performance by caching the results of some operation (usually, database calls). Instead of writing custom code for each such case (which will prove buggy later, believe me), you can use the Spring aspect for this task.

In this example, we will configure Spring caching advice to cache results of invocations of DAO and service methods execution for 1 hour.

Add to your Spring configuration file (applicationContext.xml):

```
    <!-- ========================= Aspects ========================= -->

    <aop:config>
        <aop:aspect ref="cachingAspect">
            <aop:around pointcut="execution(public * com.example..*.*ServiceImpl.*(..))"
                method="cachedObject" />
            <aop:around pointcut="execution(public * com.example..*.*DAO.*(..))" method="cachedObject" />
        </aop:aspect>
    </aop:config>

    <bean id="cachingAspect" class="com.googlecode.commonspringaspects.aspects.CachingAspect">
        <property name="enabled" value="true" />
        <property name="cache" ref="cache1hour" />
    </bean>

    <!-- we can define different caches with different caching time (timeToLive) -->
    <bean id="cache1hour" parent="abstractCache">
        <property name="timeToLive" value="3600" />
    </bean>

    <!-- settings that are the same for all caches -->
    <bean id="abstractCache" abstract="true" class="org.springframework.cache.ehcache.EhCacheFactoryBean">
        <property name="cacheManager" ref="cacheManager" />
        <property name="overflowToDisk" value="false" />
        <property name="timeToIdle" value="0" />
    </bean>

    <bean id="cacheManager" class="org.springframework.cache.ehcache.EhCacheManagerFactoryBean">
        <!-- setting the name makes it look better in JMX -->
        <property name="cacheManagerName" value="mainCacheManager" />
    </bean>
```

You will also need to add prefix "aop" in case you don't have one yet:
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:aop="http://www.springframework.org/schema/aop"
    xsi:schemaLocation="
http://www.springframework.org/schema/aop 
...">
...
```

### Managing Caches Using JMX ###

As a bonus, we can now manage the caches via JMX (using either jconsole or jvisualvm with MBeans plugin).
This makes possible to see the cache size, cached keys, etc; and also to empty the cache without restarting the application.

```
    <bean id="mbeanServer" class="org.springframework.jmx.support.MBeanServerFactoryBean">
        <property name="locateExistingServerIfPossible" value="true" />
    </bean>

    <!-- This is the official way to export Ehcache MBeans (see http://ehcache.org/documentation/jmx.html) -->
    <!-- But the MBeans do not show the cached keys. -->
    <bean class="net.sf.ehcache.management.ManagementService" init-method="init">
        <constructor-arg ref="cacheManager" />
        <constructor-arg ref="mbeanServer" />
        <constructor-arg value="true" />
        <constructor-arg value="true" />
        <constructor-arg value="true" />
        <constructor-arg value="true" />
    </bean>

    <!-- Additional MBeans to show the cached keys. -->
    <bean id="exporter" class="org.springframework.jmx.export.MBeanExporter" lazy-init="false">
        <property name="server" ref="mbeanServer"/>
        <property name="beans">
            <map>
                <entry key="org.example.myapp:name=cache1hour" value-ref="cache1hour" />
            </map>
        </property>
    </bean>
```

## Forking Execution ##

Using the ForkAspect, it is possible to define bean methods to be executed in a separate thread. This is useful to avoid waiting on slow methods whose results we do not actually need to proceed (common examples - email sending, statistics event registration, etc).

```
    <!-- ========================= Aspects ========================= -->

    <aop:config>
        <aop:aspect ref="forkingAspect">
            <aop:around pointcut="execution(public * com.example..*.EmailServiceImpl.*(..))"
                method="cachedObject" />
        </aop:aspect>
    </aop:config>

    <bean id="forkingAspect" class="com.googlecode.commonspringaspects.aspects.ForkingAspect">
        <constructor-arg ref="taskExecutor" />
	<property name="enabled" value="true" />
    </bean>

    <bean id="taskExecutor" class="org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor">
        <property name="corePoolSize" value="1" />
        <property name="maxPoolSize" value="10" />
    </bean>
```

As you can see, we use the standard Spring ThreadPoolTaskExecutor here, to limit the maximum number of new threads. Without this, there is a risk that too many threads are opened which overwhelm the system. You can also use other [TaskExecutor subtypes](http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/scheduling.html#scheduling-task-executor-types) if needed.