package com.focusflow.app
import android.content.Context
import android.os.SystemClock
class FocusState(context:Context) {
 private val prefs=context.getSharedPreferences("focus_policy",Context.MODE_PRIVATE)
 var strict:Boolean
  get()=prefs.getBoolean("strict",false)
  set(value){prefs.edit().putBoolean("strict",value).apply()}
 fun blocked():Set<String> = prefs.getStringSet("blocked",emptySet())?.toSet()?:emptySet()
 fun setBlocked(packages:Set<String>){prefs.edit().putStringSet("blocked",packages.toSet()).apply()}
 fun active():Boolean{
  val boot=prefs.getLong("boot_wall",0)
  if(kotlin.math.abs((System.currentTimeMillis()-SystemClock.elapsedRealtime())-boot)>120_000)return false
  return prefs.getLong("until_elapsed",0)>SystemClock.elapsedRealtime()
 }
 fun remainingSeconds():Long=if(active())((prefs.getLong("until_elapsed",0)-SystemClock.elapsedRealtime()+999)/1000).coerceAtLeast(0) else 0
 fun begin(minutes:Int,subject:String){
  require(minutes in 1..720)
  prefs.edit().putLong("until_elapsed",SystemClock.elapsedRealtime()+minutes*60_000L)
   .putLong("boot_wall",System.currentTimeMillis()-SystemClock.elapsedRealtime())
   .putLong("started_wall",System.currentTimeMillis())
   .putInt("minutes",minutes).putString("subject",subject).apply()
 }
 fun subject()=prefs.getString("subject","General")?:"General"
 fun minutes()=prefs.getInt("minutes",25)
 fun end(force:Boolean=false):Boolean{
  if(active()&&strict&&!force)return false
  prefs.edit().putLong("until_elapsed",0).putLong("started_wall",0).apply();return true
 }
 fun clearExpiredAndLog(context:Context){
  val started=prefs.getLong("started_wall",0)
  if(!active()&&started>0){
   StudyStore(context).save(StudySession(subject(),minutes()*60L,System.currentTimeMillis()))
   prefs.edit().putLong("started_wall",0).apply()
  }
 }
}
