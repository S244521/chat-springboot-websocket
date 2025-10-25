# 项目文档

项目线上地址http://134.175.103.50:6677

黑客大佬别黑我，点钱没有，榨不出油水（问就是被搞过，上一次搞个个人博客，结果数据库给我黑了，还加密给我发勒索邮件）



## 项目演示讲解

新登录的用户没有群聊或者聊天对象需要加入群聊或添加私聊对象

<img src=".\img\image-20251024185148480.png" alt="image-20251024185148480"  />



##### 群聊:有实时搜素匹配群聊名称但是这个接口是通过群聊号搜索的

如这个可以在群聊标题下找到43f2b4b766114a51b21aacf87dbe3033
<img src=".\img\image-20251024185617301.png" alt="image-20251024185617301"  />

<img src=".\img\image-20251024185740442.png" alt="image-20251024185740442"  />



##### 私聊:同时匹配用户名称或者账号
<img src=".\img\image-20251024190019377.png" alt="image-20251024190019377"  />

点击任意用户即可添加与其的私聊，不过一次只能建立一个私聊，

建立之后对方会同时进入私聊无需对方同意（本项目的宗旨就是方便快捷的传输和沟通）发的消息会保留3天过期自动销毁



##### 修改个人信息

本项目密码是加密的作者本人也无法获取所以这里修改需要用户同事输入密码(新旧都可以如果忘了就重新注册吧)
<img src=".\img\image-20251024190446315.png" alt="image-20251024190446315"  />



##### 创建群聊

名称是不可重复的，没有做排序聊天较多会难以查找，建议聊天完就删除聊天，即用及删
<img src=".\img\image-20251024190835523.png" alt="image-20251024190835523"  />



##### 管理员权限

删除上传文件用的，作者管理文件用的没有别的效果，他人别来乱试，界面用了别人的俄罗斯方块左右移动，上旋转，下直接沉底
<img src=".\img\image-20251024191209418.png" alt="image-20251024191209418"  />



##### 文件

就上传下载服务器就只有30G左右的别传太大的东西，这里是分块上传，

此模块是所有用户贡献相当于是资源分享平台，上传前最好备注一下文件名让人一看就看的懂
<img src=".\img\image-20251024191550971.png" alt="image-20251024191550971"  />





### 部署

解决刷新nginx404的问题

```
# 核心配置：处理 Vue Router History 模式（添加这部分）
location / {
    # 尝试访问真实文件/目录，不存在则转发到 index.html
    try_files $uri $uri/ /index.html;
}
```

宝塔配置html项目dist需要重新配置代理

```
location /backend {
    proxy_pass http://localhost:9999;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    rewrite ^/backend(.*)$ $1 break; # 移除请求路径中的/backend前缀
    # 支持WebSocket（如果需要）
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
}
```

