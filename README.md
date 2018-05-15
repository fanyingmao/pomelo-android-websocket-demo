# pomelo-android-websocket-demo
* 这是安卓通过websocket连接[pomelo](https://github.com/cynron/pomelo)的demo
* demo是基于 [pomelo-websocket-java-client](https://github.com/jzsues/pomelo-websocket-java-client)实现
* 由于pomelo-websocket-java-client 没有实现接收推送的功能，这里改了些代码实现了，同样把代码提出java也可以用。推送在PomeloClient类中加了个on方法