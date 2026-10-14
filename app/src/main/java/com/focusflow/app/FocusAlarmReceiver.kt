package com.focusflow.app
import android.app.*
import android.content.*
import java.util.Calendar
class FocusAlarmReceiver:BroadcastReceiver(){
 override fun onReceive(context:Context,intent:Intent?){
  val mgr=context.getSystemService(NotificationManager::class.java)
  mgr.createNotificationChannel(NotificationChannel("reminders","Study reminders",NotificationManager.IMPORTANCE_DEFAULT))
  val open=PendingIntent.getActivity(context,3,Intent(context,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
  mgr.notify(303,Notification.Builder(context,"reminders").setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
   .setContentTitle("Time to focus").setContentText("Your scheduled study session is ready").setContentIntent(open).setAutoCancel(true).build())
  schedule(context)
 }
 companion object{
  fun schedule(context:Context){
   val prefs=context.getSharedPreferences("reminder",Context.MODE_PRIVATE)
   if(!prefs.getBoolean("enabled",false))return
   val calendar=Calendar.getInstance().apply{
    set(Calendar.HOUR_OF_DAY,prefs.getInt("hour",19));set(Calendar.MINUTE,prefs.getInt("minute",0))
    set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0)
    if(timeInMillis<=System.currentTimeMillis())add(Calendar.DAY_OF_YEAR,1)
   }
   val pending=PendingIntent.getBroadcast(context,44,Intent(context,FocusAlarmReceiver::class.java),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
   (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,calendar.timeInMillis,pending)
  }
  fun cancel(context:Context){
   val pending=PendingIntent.getBroadcast(context,44,Intent(context,FocusAlarmReceiver::class.java),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
   (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pending)
  }
 }
}
class FocusBootReceiver:BroadcastReceiver(){
 override fun onReceive(context:Context,intent:Intent?){if(intent?.action==Intent.ACTION_BOOT_COMPLETED)FocusAlarmReceiver.schedule(context)}
}
