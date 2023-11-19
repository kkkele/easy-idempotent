# easy-idempotent

## 简介

easyIdempotent 一款配置简单，只需要打上注解就可轻松实现幂等性的组件。该组件提供多种存储（支持分布式 和 单机）方式，多种自定义幂等标识的方式，多种场景下的适配使用。打造一个简单而功能丰富的幂等框架。

## 特性

1. 配置简单，即插即用
2. 可基于spel表达式组成幂等标识，可基于spel标识自行决定在方法成功返回后是否删除幂等标识,适应多种复杂场景
3. 支持redis，如果只想写个简单demo，不想启动redis，也可使用Local存储方式存储幂等标识


## 安装教程

```xml
            <dependency>
                <groupId>io.github.kkkele</groupId>
                <artifactId>easy-idempotent-spring-boot3-starter</artifactId>
                <version>${easy-idempotent.version}</version>  <!--选择版本 (目前最新为1.0.1) (1.0.0的yml配置有bug)-->
            </dependency>
```

## 使用说明

1. application.yml配置 (默认为redis存储)

   ```yml
   --- #配置idempotent 
   idempotent:
     prefix: kkkele  #幂等标识前缀
     using-type: redis #幂等标识存储方式
     enable-log: true #是否开启日志打印
     mq:    #mq场景下
       interval: 600s #默认存储幂等标识的时间,即 如果mq消费成功,则600s内对于同样的幂等标识会采取跳过策略
       message: '消息重复消费' #重复消费默认消息
     rest-api: #使用接口请求场景下
       interval: 5s #默认存储时间,即如果设置了幂等标识自动清理,则在该时间内，只能处理一个请求
       message: '请求正在处理，请勿重复提交' #重复提交默认消息
   ```

2. 在需要幂等标识的地方打上注解@Idempotent

   ```java
   	//默认使用方法参数进行幂等标识的组成    
   	@GetMapping("/demo")
       @Idempotent 
       public String test(TestDemo testDemo) {
           // .... 处理方法
           return "success";
       }
   ```

3. 如果需要用户token来组成我们的幂等标识，需要先实现RepeatToken接口，并交给spring容器进行管理

   ```java
   public class RepeatTokenImpl implements RepeatToken {
       @Override
       public String getToken() {
       //...自定义获取token
           return TokenUtil.getToken();
       }
   }
   @Configuration
   public class IdempotentConfig {
   
       @Bean
       public RepeatToken repeatToken() {
           return new RepeatTokenImpl();
       }
   }
   
   //------------------或者
   @Compotent
   public class RepeatTokenImpl implements RepeatToken {
       @Override
       public String getToken() {
      		 //...自定义获取token
           return TokenUtil.getToken();
       }
   }
   ```

 4. 完全使用示例

    ```java
        //完全配置
        @GetMapping("/demo2")
        @Idempotent(type = {IdempotentType.PARAM,IdempotentType.SPEL,IdempotentType.TOKEN},
                scene = IdempotentScene.RESTAPI,
                spelKey = "#testDemo.id",
                interval = 5,
                timeUnit = TimeUnit.MINUTES,
                clean = "#result != null")
        public TestDemo test2(TestDemo testDemo){
            // .... 处理方法
            return testDemo;
        }
    ```

## 高级使用

1. 自定义清理策略

   ```java
       @GetMapping("/demo2")
       @Idempotent(type = IdempotentType.PARAM,
               interval = 500,
               timeUnit = TimeUnit.MINUTES,
               clean = "#result.id != null")
       public TestDemo test2(TestDemo testDemo){
           // .... 处理方法
           return testDemo;
       }
   ```

   demo演示

   ```http
   GET http://localhost:8080/idempotent/demo2
   ```

   第一次返回json

   ```json
   {
     "id": null,
     "title": null,
     "content": null,
     "loginUser": null
   }
   ```

   5分钟内返回json

   ```json
   {
     "code": 500,
     "msg": "请求正在处理，请勿重复提交",
     "data": null
   }
   ```

   更改url

   ```http
   GET http://localhost:8080/idempotent/demo2?id=1
   ```

   第一次返回json

   ```json
   {
     "id": 1,
     "title": null,
     "content": null,
     "loginUser": null
   }
   ```

   5分钟内返回json

   ```json
   {
     "id": 1,
     "title": null,
     "content": null,
     "loginUser": null
   }
   ```

​		日志打印信息

![image-20231119135158216](C:\Users\16220\AppData\Roaming\Typora\typora-user-images\image-20231119135158216.png)

2.使用spel表达式自定义幂等标识的组成

```java
    @GetMapping("/demo3")
    @Idempotent(type = IdempotentType.SPEL,
            spelKey = "#testDemo.id + '_' + #testDemo.title",
            interval = 600,
            timeUnit = TimeUnit.MINUTES,
            clean = "false")
    public TestDemo test3(TestDemo testDemo){
        return testDemo;
    }
```

demo演示

```http
GET http://localhost:8080/idempotent/demo3?id=2&title=testdemo3
```

多次请求日志打印情况

![image-20231119135650547](C:\Users\16220\AppData\Roaming\Typora\typora-user-images\image-20231119135650547.png)

3.混合使用幂等标识type，自定义幂等标识

```java
//token实现类
public class RepeatTokenImpl implements RepeatToken {
    @Override
    public String getToken() {
        return "123456";
    }
}

//测试接口
    @GetMapping("/demo4")
    @Idempotent(type = {IdempotentType.PARAM,IdempotentType.TOKEN},
            interval = 600,
            timeUnit = TimeUnit.MINUTES,
            clean = "false")
    public TestDemo test4(TestDemo testDemo){
        return testDemo;
    }
```

日志打印情况

![image-20231119140132038](C:\Users\16220\AppData\Roaming\Typora\typora-user-images\image-20231119140132038.png)

4. mq场景下使用 (清理策略不适用于Mq场景)

   ```java
   @Slf4j
   @Component
   @RequiredArgsConstructor
   @RocketMQMessageListener(
           topic = "test_test-demo_topic",
           consumerGroup = "test_test-demo_cg"
   )
   public class TestDemoConsumer implements RocketMQListener<MessageWrapper<TestDemo>> {
       @Override
       @Idempotent(scene = IdempotentScene.MQ,type = IdempotentType.SPEL,spelKey = "#wrapper.uuid + #wrapper.keys")
       public void onMessage(MessageWrapper<TestDemo> wrapper) {
           if (new Random().nextBoolean()){
               log.error("消费失败");
               throw new RuntimeException();
           }
           System.out.printf("消费成功:[%s]\n",wrapper);
       }
   }
   
   
   //发送消息
       @PostMapping("/produce")
       public void produce() {
           produce.sendResult(new TestDemo(1l, "test-title", null, null));
       }
   
   ```

   模拟发送

   ```http
   POST http://localhost:8080/idempotent/produce
   ```

   日志信息

   ![image-20231119143557118](C:\Users\16220\AppData\Roaming\Typora\typora-user-images\image-20231119143557118.png)![image-20231119143618298](C:\Users\16220\AppData\Roaming\Typora\typora-user-images\image-20231119143618298.png)

​		 可以看到，在消息消费失败后，仍然可以再次消费，以保证mq能够正常工作

​		本组件在消息消费成功后，会将mq场景下的幂等标识对应的值改为 1

![image-20231119143748522](C:\Users\16220\AppData\Roaming\Typora\typora-user-images\image-20231119143748522.png)

​		这样，如果发生了极端情况的重复消费，本组件会跳过之后的消费来保证mq场景下的幂等性
