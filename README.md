# ArcFaceDemo
Free SDK demo

>工程如何使用？
 1. 将工程中的192.168.0.105全部替换为你运行WebServer的域名。

> demo如何使用?    

 1. 在客户端提交成功后等待约15~30秒，门禁端就会下载好文件并且生成好数据，轻触“轻触以监测识别”按钮即可识别。
 2、若出现无法识别的情况（其实几乎完全没出现过，极个别个案），打开文件管理器，进入android->data->com.arcsoft.sdk_demo->cache，把文件内容全部删除
 
> demo中人脸数据的保存方式?  

　以注册时人名为关键索引，保存在face.txt中。  
　创建的 name.data 则为实际的数据存储文件，保存了所有特征信息。  
　同一个名字可以注册多个不同状态角度的人脸，在name.data 中连续保存，占用的数据文件长度为:  
　N * {4字节(特征数据长度) + 22020字节(特征数据信息)}
  
> 最低支持的API-LEVEL?  

　14-27    　

---------------
> FAQ
1. Gradle 错误提示 Error:Failed to find target with hash string 'android-24'.......    
 一般Android Studio 窗口会有个链接(Install missing platform(s) and sync project)    
 点击下载更新 android-24 即可解决（其他版本没测试过，建议不要随意更改）。