package com.focusflow.app
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Calendar
class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{
  val policy=remember{FocusState(this)}
  var tab by remember{mutableIntStateOf(0)}
  var tick by remember{mutableIntStateOf(0)}
  LaunchedEffect(Unit){while(true){policy.clearExpiredAndLog(this@MainActivity);tick++;delay(1000)}}
  MaterialTheme(colorScheme=darkColorScheme()){
   Scaffold(bottomBar={NavigationBar{
    listOf("Focus","Block","Stats","Settings").forEachIndexed{i,s->
     NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Text(listOf("⏱","⛔","📊","⚙")[i])},label={Text(s)})
    }
   }}){padding->
    when(tab){
     0->FocusScreen(policy,Modifier.padding(padding),tick){
      startForegroundService(Intent(this@MainActivity,FocusNotificationService::class.java))
     }
     1->BlockScreen(policy,Modifier.padding(padding))
     2->StatsScreen(this@MainActivity,Modifier.padding(padding),tick)
     else->SettingsScreen(this@MainActivity,policy,Modifier.padding(padding))
    }
   }
  }
 }}
}
@Composable private fun FocusScreen(policy:FocusState,modifier:Modifier,tick:Int,onStart:()->Unit){
 var minutes by remember{mutableFloatStateOf(25f)}
 var subject by remember{mutableStateOf("General")}
 var strict by remember{mutableStateOf(policy.strict)}
 val active=policy.active()
 Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){
  Text("FocusFlow",style=MaterialTheme.typography.headlineLarge)
  Text(if(active)"Focus session in progress" else "Start a distraction-free study session")
  Text(if(active)formatDuration(policy.remainingSeconds()) else formatDuration(minutes.toLong()*60),style=MaterialTheme.typography.displayLarge)
  if(!active){
   OutlinedTextField(value=subject,onValueChange={subject=it},label={Text("Subject")},singleLine=true)
   Text("Session: ${minutes.toInt()} minutes")
   Slider(value=minutes,onValueChange={minutes=it},valueRange=5f..180f,steps=34)
   Row{Checkbox(checked=strict,onCheckedChange={strict=it;policy.strict=it});Text("Strict mode: no early end in this app")}
   Button(onClick={policy.strict=strict;policy.begin(minutes.toInt(),subject.ifBlank{"General"});onStart()}){Text("Start focus")}
  }else{
   Text("Subject: ${policy.subject()}")
   Text(if(policy.strict)"Strict mode is enabled. Wait until the timer completes." else "You may end your session early.")
   if(!policy.strict)OutlinedButton(onClick={policy.end()}){Text("End early")}
  }
 }
}
@Composable private fun BlockScreen(policy:FocusState,modifier:Modifier){
 val apps=launchableAppsLocalCache()
 var selected by remember{mutableStateOf(policy.blocked())}
 Column(modifier.fillMaxSize().padding(16.dp)){
  Text("Blocked apps",style=MaterialTheme.typography.headlineMedium)
  Text("Select applications that will be redirected while a focus timer runs.")
  LazyColumn{
   items(apps){app->
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
     Text(app.label,Modifier.weight(1f))
     Checkbox(checked=app.packageName in selected,onCheckedChange={checked->
      selected=if(checked)selected+app.packageName else selected-app.packageName
      policy.setBlocked(selected)
     })
    }
    HorizontalDivider()
   }
  }
 }
}
@Composable private fun launchableAppsLocalCache():List<AppEntry>{
 val context=androidx.compose.ui.platform.LocalContext.current
 return remember(context){launchableApps(context)}
}
@Composable private fun StatsScreen(context:Context,modifier:Modifier,tick:Int){
 val store=remember{StudyStore(context)}
 val sessions=store.all()
 Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  Text("Study analytics",style=MaterialTheme.typography.headlineMedium)
  Text("Today: ${store.todaySeconds()/60} minutes")
  Text("Completed sessions: ${sessions.size}")
  HorizontalDivider()
  sessions.takeLast(15).reversed().forEach{Text("${it.subject}: ${it.seconds/60} min")}
  Text("Screen-time statistics require Usage Access.")
  val usage=context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
  val granted=(context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager).checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,Process.myUid(),context.packageName)==AppOpsManager.MODE_ALLOWED
  if(granted){
   val end=System.currentTimeMillis()
   val start=Calendar.getInstance().apply{set(Calendar.HOUR_OF_DAY,0);set(Calendar.MINUTE,0);set(Calendar.SECOND,0)}.timeInMillis
   val total=usage.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,start,end)?.sumOf{it.totalTimeInForeground}?:0L
   Text("Foreground application time today: ${total/60000} min (approx.)")
  }else Button(onClick={context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))}){Text("Enable Usage Access")}
 }
}
@Composable private fun SettingsScreen(context:Context,policy:FocusState,modifier:Modifier){
 val p=remember{context.getSharedPreferences("reminder",Context.MODE_PRIVATE)}
 val goals=remember{context.getSharedPreferences("goals",Context.MODE_PRIVATE)}
 var goal by remember{mutableFloatStateOf(goals.getInt("daily_goal",120).toFloat())}
 var reminder by remember{mutableStateOf(p.getBoolean("enabled",false))}
 var hour by remember{mutableFloatStateOf(p.getInt("hour",19).toFloat())}
 Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(15.dp)){
  Text("Settings",style=MaterialTheme.typography.headlineMedium)
  Text("Daily study goal: ${goal.toInt()} minutes")
  Slider(value=goal,onValueChange={goal=it;goals.edit().putInt("daily_goal",it.toInt()).apply()},valueRange=15f..480f)
  Row{Checkbox(checked=reminder,onCheckedChange={reminder=it;p.edit().putBoolean("enabled",it).apply();if(it)FocusAlarmReceiver.schedule(context) else FocusAlarmReceiver.cancel(context)});Text("Daily reminder")}
  Text("Reminder hour: ${hour.toInt()}:00")
  Slider(value=hour,onValueChange={hour=it;p.edit().putInt("hour",it.toInt()).apply();FocusAlarmReceiver.cancel(context);FocusAlarmReceiver.schedule(context)},valueRange=0f..23f,steps=22)
  Button(onClick={context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))}){Text("Enable app blocking accessibility")}
  Button(onClick={context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))}){Text("Enable usage statistics")}
  Text("All data stays on your device. App blocking is best effort; Android users can disable accessibility or uninstall the app.")
 }
}
