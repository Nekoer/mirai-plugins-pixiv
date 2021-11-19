# mirai-plugins-pixiv
可查看pixiv排行榜，作者作品，插画图片等等
- pixiv排行榜
- 查看图片
- 查看作者作品
- 搜图
- 搜番
- 搜标签
- 涩图

```
{
  "admins": [
    165485 #管理员
  ],
  "config": {
    "token": {
      "acgmx":null, # https://www.acgmx.com/account申请
      "saucenao": null, #saucenao.com注册账号后能看到api_key
    },
    "proxy": { # http请求代理
      "host": null, 
      "port": null
    },
    "recall": 5000 # 涩图经过多少秒撤回
  },
  "command": {
    "getDetailOfId": "psid-", #根据id查看插画
    "picToSearch": "ptst-", #以图搜图
    "showRank":"rank-", #排行榜 day|week|month|setu
    "findUserWorksById": "user-", #查看作者作品
    "searchInfoByPic": "ptsf-", #以图搜番
    "setu": "setu",
    "tag": "tag-", #搜标签 tag-xxx-页码
    "ascii2d": "ascii2d-" #ascii2d引擎搜索 ascii2d-图片 隐藏命令：更改图片搜索
  },
  "groups": [
    123456,548795 #有涩图权限的群
  ]
}
```
