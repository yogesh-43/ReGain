package com.focusflow.app
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
data class StudySession(val subject:String,val seconds:Long,val finishedAt:Long)
class StudyStore(context:Context){
 private val prefs=context.getSharedPreferences("study_history",Context.MODE_PRIVATE)
 fun all():List<StudySession>{
  val array=try{JSONArray(prefs.getString("sessions","[]"))}catch(_:Exception){JSONArray()}
  return (0 until array.length()).mapNotNull{
   try{val o=array.getJSONObject(it);StudySession(o.getString("subject"),o.getLong("seconds"),o.getLong("finishedAt"))}catch(_:Exception){null}
  }
 }
 fun save(session:StudySession){
  val array=JSONArray()
  (all().takeLast(499)+session).forEach{array.put(JSONObject().put("subject",it.subject).put("seconds",it.seconds).put("finishedAt",it.finishedAt))}
  prefs.edit().putString("sessions",array.toString()).apply()
 }
 fun todaySeconds():Long{
  val date=SimpleDateFormat("yyyyMMdd",Locale.US)
  val now=date.format(Date())
  return all().filter{date.format(Date(it.finishedAt))==now}.sumOf{it.seconds}
 }
}
fun formatDuration(seconds:Long):String="%02d:%02d".format(Locale.US,seconds/60,seconds%60)
