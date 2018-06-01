# Android_Things_Iot_HyperLPR4Android
基于Google Android Things系统的车牌识别项目，开发环境采用Android Studio,项目运行于树莓派3B。 

##
#流程介绍
在树莓派的BCM12引脚连接信号输入线，该引脚的GPIO设置为输入方式，电平触发方式设置为上升沿，在Java代码中注册回掉，处理GPIO口的中断事件。
当检测到上升沿之后，自动打开摄像头(CSI接口)拍照，然后在一个后台线程中执行车牌检测与识别，识别结束之后结果保存为JSONObject,通过TCP发
送至服务器端(事先在一个界面设置IP,端口号)。
##
###细节
#服务器端通信
采用TCP进行客户端(树莓派)与服务器之间通信，需要设置IP Address,Port，在服务器端需要对JSON文件解析。
#相机拍照
采用Android API camera2,需要在代码中添加动态授权。
#车牌识别
采用基于深度学习的车牌检测识别算法，流程大致为：车牌粗定位->车牌细定位->车牌字符分割->车牌字符识别。

##
#依赖
Require:OpenCV3.3.0 sdk for Android


