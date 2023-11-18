# easy-idempotent

#### 介绍
easyIdempotent,一款配置简单，只需要打上注解就可轻松实现幂等性的框架。支持多种幂等标识的存储方式，
既支持redis实现的分布式解决方案（推荐），也支持仅单机应用的Local模式(如果只是想写简单的demo，
不想配置redis的话)。本框架还设计了两种使用场景，默认为RestApi场景，即用户使用url访问该路径时使用到的
幂等性解决方案，也支持MQ场景，即极端情况下消息消费过却重复投递消息的场景。
本框架可以由开发人员自主自定义幂等标识的构成，支持3种类型(param,token,spel)任意组合。
本框架可以由开发人员自行决定在请求或消息处理结束后是否清除幂等标识(支持spel语法)，足以应付各种复杂场景。


#### 软件架构
- `easy-idempotent-core` 定义接口
- `easy-idempotent-spring-boot-starter` 在springboot应用中实现接口


#### 安装教程

1.  xxxx
2.  xxxx
3.  xxxx

#### 使用说明

1.  xxxx
2.  xxxx
3.  xxxx

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
