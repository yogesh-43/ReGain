package com.focusflow.app
import android.app.*
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.*
class FocusNotificationService:Service(){
 private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Main)
 override fun onCreate(){super.onCreate();getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("focus","Focus session",NotificationManager.IMPORTANCE_LOW))}
 override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int{
  if(!FocusState(this).active()){stopSelf();return START_NOT_STICKY}
  val open=PendingIntent.getActivity(this,0,Intent(this,MainActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  val notification=Notification.Builder(this,"focus").setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
   .setContentTitle("FocusFlow · Focus active").setContentText("Blocked apps remain restricted until the timer ends")
   .setContentIntent(open).setOngoing(true).build()
  startForeground(101,notification)
  scope.launch{while(FocusState(this@FocusNotificationService).active())delay(1000)
   FocusState(this@FocusNotificationService).clearExpiredAndLog(this@FocusNotificationService);stopSelf()}
  return START_STICKY
 }
 override fun onDestroy(){scope.cancel();super.onDestroy()}
 override fun onBind(intent:Intent?):IBinder?=null
}
