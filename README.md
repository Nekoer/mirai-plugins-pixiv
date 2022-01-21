# mirai-plugins-pixiv
可查看pixiv排行榜，作者作品，插画图片等等
- pixiv排行榜
- 查看图片
- 查看作者作品
- 搜图
- 搜番
- 搜标签
- 涩图

~~请复制的时候去掉#以及后面的字~~ 这就不要我说了吧（
```
admins:  #管理员
  - 243462032
groups:  #有涩图权限的群
  - 960879198
  - 77708393
  - 1475993
command: 
  getDetailOfId: 'psid-' #根据id查看插画
  picToSearch: 'ptst-' #以图搜图
  showRank: 'rank-' #排行榜 day|week|month|setu
  findUserWorksById: 'user-' #查看作者作品
  searchInfoByPic: 'ptsf-' #以图搜番
  setu: setu
  tag: 'tag-' #搜标签 tag-xxx-页码
config: 
  token: 
    acgmx: # https://www.acgmx.com/account申请
    saucenao:  #saucenao.com注册账号后能看到api_key
  proxy:  # http请求代理 下面是clash的例子
    host: 127.0.0.1
    port: 7890
  recall: 5000 # 涩图经过多少秒撤回 设置为0即可不撤回
```
