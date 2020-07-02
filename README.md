# simpleframework

##simple-context

    @Bean扫描，并放入容器操作
    
##simple-http

    @RestClient 类注解，http请求的interface，标记请求的组，并会正常javabean的代理类
        参数 url 链接 如：http://localhost:8080/abc;
            ps: 
            @RestClient(url="http://localhost:8080")
            public interface Demo {
                ...
            }
        则发送的请求为http://localhost:8080
        参数 filter 过滤器 RestFilter,提供before和after，分别拦截请求之前和请求之后的操作;
    @RestMapping 方法注解，注解在接口方法之上
        参数 url 同@RestClient.url
            ps: @RestClient(url="http://localhost:8080")
                public interface Demo {
                    @RestMapping(url="/abc")
                    String test();
                }
            则发送的请求为http://localhost:8080/abc
            ps: @RestClient(url="http://localhost:8080")
                public interface Demo {
                    @RestMapping(url="http://localhost:8081/abc")
                    String test();
                }
            则发送的请求为http://localhost:8081/abc
        参数 method 请求方式，默认 RestMethod.GET; 
                   可配置GET, POST, DELETE, PUT, PATCH, HEAD, TRACE, STREAM(STREAM为下载文件或者其他流方式的时候使用GET的请求方式)
        参数 filter 过滤器配置 默认 AbstractRestFilter.class;
        参数 ignoreFilter 是否忽略过滤器，默认false不忽略，参数的意义，当类注解配置了统一过滤器时，某方法不需要过滤，则配置true 
    @RestParam 参数注解
        参数 value 做为http请求的参数key， 
            基本数据类型或者String类型，则必须配置value值，
            其他对象类型时，可不配置，默认以属性名为key
            ps: @RestClient(url="http://localhost:8080")
                public interface Demo {
                    @RestMapping(url="http://localhost:8081/abc")
                    String test(@RestParam("key") String key); //key=3
                }
            则发送的请求为http://localhost:8081/abc?key=3, 默认请求方式为GET
        参数 require 是否为空值，默认false，配置true时，如果传参为空会抛异常RestParameterNullValueException
    @RestBody body传参
        配置这个参数，头部信息会自动加入Content-Type=application/json; charset=UTF-8
        如果传参为空，则会抛异常RestParameterNullValueException
    @RestHead 自定义头部信息
        参数 value 作为http请求的头部信息key
        支持基本数据类型和String以及Map类型， Map类型时，把所有的k-v加入头部信息里面，而其他类型则需要value值必填，为头部信息的key
    @RestURL 动态链接配置
        如果参数中有配置url，则优先级最高
        注解此注解时，参数必填，否则会抛异常RestParameterNullValueException
        参数类型必须是String，否则会抛异常RestParameterTypeSuchException
    
##simple-http-spring  结合spring使用的时候，则需要引用这个包
    
    提供@EnableRestClients注解，注解时才会默认扫描注解类下对应的所有包，开启@RestClient
    application.properties配置文件中，提供simple.http.retry配置，类型为int，
        表示http请求，如果结果请求失败，且结果为502，503，504时，重试请求，默认次数3次
    
        
     