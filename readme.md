### 使用前提
- 你有自己的代理
### 使用步骤
1. 安装pom下的依赖
2. 请求头替换
```
//代理地址和端口  换成你的代理地址和端口
String proxyHost = "127.0.0.1"; 
String proxyPort = "7890"; 
...
.addHeader("x-user-id", "xxx")// 换成你的id
...
.addHeader("Cookie", "xxx")//  换成你的 Cookie
..
```
3. 运行程序