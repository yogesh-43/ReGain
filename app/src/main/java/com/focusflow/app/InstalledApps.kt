package com.focusflow.app
import android.content.Context
import android.content.Intent
data class AppEntry(val label:String,val packageName:String)
fun launchableApps(context:Context):List<AppEntry>{
 val pm=context.packageManager
 val intent=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
 return pm.queryIntentActivities(intent,0).map{AppEntry(it.loadLabel(pm).toString(),it.activityInfo.packageName)}
 .filter{it.packageName!=context.packageName}.distinctBy{it.packageName}.sortedBy{it.label.lowercase()}
}
