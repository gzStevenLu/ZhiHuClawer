# ZhiHuClawer

此爬虫仅供学习、研究之用。

首次使用说明：
	1、首次使用请先运行在utils包下的Login程序，登录把cookie序列化下来。
		（注意修改手机登录或邮箱登录验证地址，及验证信息。请手动放置序列化后的文件到根目录/cookies文件夹下）
	2、首次运行请自备HTTPS代理，以每行一条“IP:Port”的格式，保存到根目录下的ip.txt文件中。运行utils包下的ProxyHunter验证有效的IP，程序会自动将有效的代理序列化并保存到根目录proxy.dat中。
	3、根据SQL创建相应数据库，并在DBUtil中配置好数据库参数。
	4、在App.java中，设置静态变量ENTER为目标入口地址，运行App.java即可运行爬虫。