# membot ![release](https://img.shields.io/badge/version-0.5-orange.svg)

**A java memory web shell extracting tool**

- What:

  侵入式，从WEB容器内存中筛选出已知的内存马并将其源码反编译后转储到本地。

- Usage:

  ```
  java -jar membot.jar
  ```

- Cover

| 类型              | 类名                                   | 方法名                | 是否覆盖 |
|-----------------|--------------------------------------|--------------------|------|
| servlet型        | javax.servlet.Servlet                | service            | ✔    |
| servlet型        | javax.servlet.http.HttpServlet       | doGet              | ✔    |
| servlet型        | javax.servlet.http.HttpServlet       | doPost             | ✔    |
| servlet型        | javax.servlet.http.HttpServlet       | doHead             | ✔    |
| servlet型        | javax.servlet.http.HttpServlet       | doPut              | ✔    |
| servlet型        | javax.servlet.http.HttpServlet       | doDelete           | ✔    |
| servlet型        | javax.servlet.http.HttpServlet       | doTrace            | ✔    |
| servlet型        | javax.servlet.http.HttpServlet       | doOptions          | ✔    |
| filter型         | javax.servlet.Filter                 | doFilter           | ✔    |
| filter型         | javax.servlet.http.HttpFilter        | doFilter           | ✔    |
| listener型       | javax.servlet.ServletRequestListener | requestDestroyed   | ✔    |
| listener型       | javax.servlet.ServletRequestListener | requestInitialized | ✔    |
| Valve型          | org.apache.catalina.Valve            | invoke             | ✔    |
| 字节码增强型          |                                      |                    | ✔    |
| CmdWs           |                                      |                    | ✔    |
| CmdBase64Ws     |                                      |                    | ✔    |
| JSPJSWs         |                                      |                    | ✔    |
| JSPJSBase64Ws   |                                      |                    | ✔    |
| PystingerFilter |                                      |                    | ✔    |
| SorFilter       |                                      |                    | ✔    |
| Behinder        |                                      |                    | ✔    |
| Godzilla        |                                      |                    | ✔    |
| antsword        |                                      |                    | ✔    |
| websocket       |                                      |                    | ✔    |
| ReGeorg         |                                      |                    | ✔    |
| NeoreGeorg      |                                      |                    | ✔    |
| UpgradeMemshell |                                      |                    | ✔    |
| Suo5            |                                      |                    | ✔    |
| BastionFilter   |                                      |                    | ✔    |
| ReGeorg内存马      |                                      |                    | ✔    |
| NeoreGeorg内存马   |                                      |                    | ✔    |
| CmdWs           |                                      |                    | ✔    |
| CmdBase64Ws     |                                      |                    | ✔    |
| JSPJSWs         |                                      |                    | ✔    |
| JSPJSBase64Ws   |                                      |                    | ✔    |
| PystingerFilter |                                      |                    | ✔    |
| SorFilter       |                                      |                    | ✔    |

- Detect features

[《各类Java内存马-植入&检测》](https://www.yuque.com/daemon-v2sjv/okfr9p/db0xgywk3r1756f0?singleDoc# )

- Web containers

| 容器        | 系统        | 进程名称                                          | 链接                                                                             |
|-----------|-----------|-----------------------------------------------|--------------------------------------------------------------------------------|
| tomcat    | win/linux | org.apache.catalina.startup.Bootstrap         | https://tomcat.apache.org/download-80.cgi                                      |
| weblogic  | win/linux | weblogic.Server                               | https://www.oracle.com/middleware/technologies/fusionmiddleware-downloads.html |
| jboss     | win/linux | org.jboss.modules.Main                        | https://developers.redhat.com/products/eap/download                            |
| wildfly   | linux     | org.wildfly.bootable.launcher.WildFlyLauncher | https://www.wildfly.org/                                                       |
| websphere | win/linux | com.ibm.ws.bootstrap.WSLauncher               | https://www.ibm.com/cloud/blog/websphere-trial-options-and-downloads           |
| jetty     | linux     | org.eclipse.jetty.start.Main                  | https://eclipse.dev/jetty/download.php                                         |
| resin     | win/linux | com.caucho.server.resin.Resin                 | https://caucho.com/products/resin/download/gpl#download                        |

- JAVA Version

| 类型                      | 子类型 | 版本   |
|-------------------------|-----|------|
| Sun/Oracle JDK、Open JDK | JDK | >1.6 |
| Sun/Oracle JDK、Open JDK | JRE | >1.6 |
| IBM JDK                 | JRE | >1.8 |

- Physical memory
  
 <100M

- Todo:

  spring型

  ysoserial工具注入


- Reference:

  [copagent](https://github.com/LandGrey/copagent)

  [arthas](https://github.com/alibaba/arthas)