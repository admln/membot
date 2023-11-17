# membot ![release](https://img.shields.io/badge/version-0.2-orange.svg)

**A java memory web shell extracting tool**

- What:

  侵入式，目前的目标是能够从内存中筛选出已知内存马植入软件的恶意代码。

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

- Todo:

  spring型

  ysoserial工具注入


- Reference:

  [copagent](https://github.com/LandGrey/copagent)

  [arthas](https://github.com/alibaba/arthas)