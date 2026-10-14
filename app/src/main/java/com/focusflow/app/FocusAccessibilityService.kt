package com.focusflow.app
import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.content.Intent
import android.os.SystemClock
class FocusAccessibilityService:AccessibilityService(){
 private var lastRedirect=0L
 override fun onAccessibilityEvent(event:AccessibilityEvent?){
  if(event?.eventType!=AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)return
  val pkg=event.packageName?.toString()?:return
  if(pkg==packageName||pkg=="com.android.systemui")return
  val policy=FocusState(this)
  if(!policy.active()||pkg !in policy.blocked())return
  val now=SystemClock.elapsedRealtime()
  if(now-lastRedirect<650)return
  lastRedirect=now;performGlobalAction(GLOBAL_ACTION_HOME)
  val intent=Intent(this,BlockedActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
  try{startActivity(intent)}catch(_:Exception){}
 }
 override fun onInterrupt(){}
}
