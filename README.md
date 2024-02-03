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
                <version>${easy-idempotent.version}</version>  <!--选择版本 (目前最新为1.0.2) (1.0.0的yml配置有bug)-->
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

### 1.自定义清理策略

#### 首先来解释一下为什么要有清理策略的存在

幂等是为了保证最终一致性，在用户提交表单后，因为表单处理还没响应，导致用户可能有重复点击的行为。对于这一现象，我们应该对其进行限制，对相同的请求只处理其中一条，来保证结果的最终一致性。所以，当正在处理的请求处理完毕后，即返回给用户成功的消息后，允许用户再次投递相同的消息，因为用户已经看到消息被处理成功了，所以这是他主动的重复消费行为。所以，一般情况下，我们在处理完消息后，会删除幂等标识，允许用户再次提交相同表单。

但是，也存在说用户因为网络延迟的关系，未看到消息已被处理的信息，导致消息被一条接着一条处理。对于这种场景，我们提供自定义的清理策略（支持spel表达式，对结果进行解析，或者其他开发者可以想到的自定义方法），来自行决定这个接口在处理完消息后，允不允许用户在规定时间内重复提交表单的行为。

#### 测试

编写测试接口样例，选择使用方法参数组成我们的幂等标识，设置10s内不可重复消费

```java
@RestController
public class TestController {

    static final Map<String, AtomicInteger> NOT_CLEAN_COUNT_MAP = new ConcurrentHashMap<>();
    static final Map<String, AtomicInteger> CLEAN_COUNT_MAP = new ConcurrentHashMap<>();


    @GetMapping("/not-clean")
    @Idempotent(type = IdempotentType.PARAM, interval = 10, timeUnit = TimeUnit.SECONDS, message = "不可以访问这么频繁哦",clean = "false")
    public R notCleanTest(String param) {
        AtomicInteger atomicInteger = NOT_CLEAN_COUNT_MAP.computeIfAbsent(param, key -> new AtomicInteger(0));
        atomicInteger.incrementAndGet();
        return R.success(param + ",notClean");
    }

    @GetMapping("/clean")
    @Idempotent(type = IdempotentType.PARAM, interval = 10, timeUnit = TimeUnit.SECONDS, message = "不可以访问这么频繁哦",clean = "true")
    public R cleanTest(String param) {
        // 确认clean，即3s一过，消息处理完，就应该可以重复消费 
        TimeUnit.SECONDS.sleep(3);
        AtomicInteger atomicInteger = CLEAN_COUNT_MAP.computeIfAbsent(param, key -> new AtomicInteger(0));
        atomicInteger.incrementAndGet();
        return R.success(param + ",clean");
    }

    @GetMapping("/count")
    public R printCount(){
        System.out.println("NOT_CLEAN_COUNT_MAP\n"+NOT_CLEAN_COUNT_MAP);
        System.out.println("CLEAN_COUNT_MAP\n"+CLEAN_COUNT_MAP);
        return R.success();
    }
}
```

#### **对 /notClean 接口进行测试**

总共开了200个用例，不同的参数各一个,因为设置不清除幂等标识策略，所以预期结果是只有2个用例可以通过

![image-20240203130930932](https://img-blog.csdnimg.cn/direct/23b394ef156849c38ef5bd314e917fb0.jpeg)

![image-20240203130943491](https://img-blog.csdnimg.cn/direct/5348012c68e943e19b9a23f355453b6c.jpeg)

![image-20240203130953167](https://img-blog.csdnimg.cn/direct/191257e1520446e1a6be64a524a65ce3.jpeg)

测试结果

![image-20240203141951143](https://img-blog.csdnimg.cn/direct/160558eb3c0c4805ae1ce672990241e3.png)

符合预期结果

#### **对/clean接口进行测试**

设置间隔5s，重复发送20个请求，预期结果应该是只有2个用例可以通过，即5s内的第一个请求的处理完后，释放自己的幂等标识，然后第二轮只有一个能够消费消息

![image-20240203141439279](https://img-blog.csdnimg.cn/direct/c4d07b39f2e94fe5b47ed9eb11c694fd.png)

直接看结果

![image-20240203141746873](https://img-blog.csdnimg.cn/direct/99ffebf066b04c1587bcf5bfabdf4298.png)

符合预期

#### 开了log打印后，控制台的输出

![image-20240203142119579](https://img-blog.csdnimg.cn/direct/030d3e782b6d41e4827f61faf53f546c.png)

### 2.使用spel表达式自定义幂等标识的组成

```java
    @GetMapping("/spel")
    @Idempotent(type = IdempotentType.SPEL,spelKey = "'Hello,it is spelKey' + #spelKey")
    public R spelTest(String spelKey){
        System.out.println(spelKey);
        return R.success();
    }
```

![image-20240203142734079](https://img-blog.csdnimg.cn/direct/dd12a04f5059428f91413d251113866f.png)

查看幂等标识的组成

![image-20240203142658628](https://img-blog.csdnimg.cn/direct/39684abedafe4ffb8dd383397412e902.png)

符合预期

### 3.混合使用幂等标识type，自定义幂等标识

```java
    @Bean
    public RepeatToken repeatToken() {
        return () -> "123456";
    }

    @GetMapping("/spel_param")
    @Idempotent(type = {IdempotentType.TOKEN, IdempotentType.PARAM, IdempotentType.SPEL}, spelKey = "'Hello,it is spelKey' + #spelKey")
    public R spelParamTest(String spelKey) {
        System.out.println(spelKey);
        return R.success();
    }
```

发送请求

![image-20240203143231873](https://img-blog.csdnimg.cn/direct/2d6ca30d6b274d128af834ddefd969e7.png)

控制台打印

![image-20240203143903556](https://img-blog.csdnimg.cn/direct/846768975c7d4e9c8fe98d332669f6fd.png)

### 4.mq场景下使用 (清理策略不适用于Mq场景)

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

![image-20231119143557118](https://img-blog.csdnimg.cn/direct/3d309fae68934050b1499b668d0156dc.png)

----------

![image-20231119143618298](https://img-blog.csdnimg.cn/direct/1e1e2bc2ba8d44a39eb67cc369e1c0b6.png)

​		 可以看到，在消息消费失败后，仍然可以再次消费，以保证mq能够正常工作

​		本组件在消息消费成功后，会将mq场景下的幂等标识对应的值改为 1

![image-20231119143748522](https://img-blog.csdnimg.cn/direct/91755c834b074b28bebb0402b6f3c6b6.png)

​		这样，如果发生了极端情况的重复消费，本组件会跳过之后的消费来保证mq场景下的幂等性
