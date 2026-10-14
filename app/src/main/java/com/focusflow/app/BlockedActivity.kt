package com.focusflow.app
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
class BlockedActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{
  val policy=remember{FocusState(this)}
  var seconds by remember{mutableLongStateOf(policy.remainingSeconds())}
  LaunchedEffect(Unit){while(true){seconds=policy.remainingSeconds();if(seconds==0L){policy.clearExpiredAndLog(this@BlockedActivity);finish();break};delay(500)}}
  MaterialTheme(colorScheme=darkColorScheme()){
   Column(Modifier.fillMaxSize().padding(30.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
    Text("Focus mode is active",style=MaterialTheme.typography.headlineMedium,textAlign=TextAlign.Center)
    Spacer(Modifier.height(18.dp));Text("This application is restricted until your focus session ends.",textAlign=TextAlign.Center)
    Spacer(Modifier.height(18.dp));Text(formatDuration(seconds),style=MaterialTheme.typography.displayLarge)
    Spacer(Modifier.height(20.dp))
    Button(onClick={startActivity(Intent(this@BlockedActivity,MainActivity::class.java));finish()}){Text("Return to FocusFlow")}
    if(!policy.strict)TextButton(onClick={policy.end();finish()}){Text("End focus early")}
   }
  }
 }}
}
