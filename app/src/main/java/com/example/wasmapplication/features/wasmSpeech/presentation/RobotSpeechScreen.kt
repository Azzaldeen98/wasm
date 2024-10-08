package com.example.wasmapplication.features.wasmSpeech.presentation

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.wasmapplication.core.constant.STORAGE_RECORD_SERVICE_STATE
import com.example.wasmapplication.core.helpers.ManageService
import com.example.wasmapplication.core.local_storage.ExternalStorage
import com.example.wasmapplication.features.components.BasicButton
import com.example.wasmapplication.services.RecordVoiceLifeCycleService
import com.example.wasmapplication.services.RecordVoiceService
import dagger.hilt.android.qualifiers.ApplicationContext


@Composable
fun ShowDialog() {
    var showDialog by remember { mutableStateOf(false) }

    // زر لعرض مربع الحوار
    Button(onClick = { showDialog = true }) {
        Text(text = "Show Dialog")
    }

    // تحقق إذا كان يجب عرض مربع الحوار
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            title = {
                Text(text = "مربع حوار")
            },
            text = {
                Text("هل ترغب في المتابعة؟")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        // هنا يمكن وضع الحدث عند تأكيد المستخدم
                    }
                ) {
                    Text("تأكيد")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        // هنا يمكن وضع الحدث عند إلغاء المستخدم
                    }
                ) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun RobotSpeechScreen(
    navController: NavController,
    @ApplicationContext  context: Context,
    wasmSpeechViewModel: WasmSpeechViewModel = hiltViewModel(),
//    wasmSpeechViewModel: WasmSpeechViewModel  = viewModel()

) {
    var serviceClass: Class<*> = RecordVoiceLifeCycleService::class.java;
    var result by rememberSaveable { mutableStateOf("placeholderResult") }
    val uiState by wasmSpeechViewModel.uiState.collectAsState();
    var text by remember { mutableStateOf(
        if(ManageService.checkForegroundServiceIsRunning(context, serviceClass))"Stop Service" else "Start Service") }
//    val state = viewModel.state.value
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,

        ) {
        Spacer(Modifier.height(0.dp))


            BasicButton(

                onClick = {
                          if(!ManageService.checkForegroundServiceIsRunning(context,serviceClass)){
                               ManageService.startService(context,serviceClass)
                               text="Stop Service"
                          }else{
                              text="Start Service"
                              ManageService.stopService(context,serviceClass)

                            }
                      },
                text = text
            )




//        if (uiState is UiState.Loading) {
//            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
//        } else {
//            var textColor = MaterialTheme.colorScheme.onSurface
//            if (uiState is UiState.Error) {
//                textColor = MaterialTheme.colorScheme.error
//                result = (uiState as UiState.Error).errorMessage
//            } else if (uiState is UiState.Success) {
//                textColor = MaterialTheme.colorScheme.onSurface
//                result = (uiState as UiState.Success).outputText
//            }
//            val scrollState = rememberScrollState()
//            Text(
//                text = result,
//                textAlign = TextAlign.Center,
//                color = textColor,
//                modifier = Modifier
//                    .align(Alignment.CenterHorizontally)
//                    .padding(16.dp)
//                    .fillMaxSize()
//                    .verticalScroll(scrollState)
//            )
//        }
    }
//    Box(modifier = Modifier.fillMaxSize()) {
//
//        val data= "Data" //if(state.data is String) state.data  else "None"
//        LazyColumn(
//            modifier = Modifier.fillMaxSize(),
//            contentPadding = PaddingValues(20.dp)
//        ) {
//            item {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//
//                    if (uiState is UiState.Loading) {
//                        CircularProgressIndicator()
//                    } else {
//                        var textColor = MaterialTheme.colorScheme.onSurface
//                        if (uiState is UiState.Error) {
//                            textColor = MaterialTheme.colorScheme.error
//                            result = (uiState as UiState.Error).errorMessage
//                        } else if (uiState is UiState.Success) {
//                            textColor = MaterialTheme.colorScheme.onSurface
//                            result = (uiState as UiState.Success).outputText
//                        }
//                        val scrollState = rememberScrollState()
//
//                        Text(
//                            text = result,
//                            textAlign = TextAlign.Center,
//                            color = textColor,
//                            modifier = Modifier
//                                .padding(16.dp)
//                                .fillMaxSize()
//                                .verticalScroll(scrollState)
//                        )
//                    }
////                    Text(
////                        text = "$data",
////                        style = MaterialTheme.typography.bodyLarge,
////                        color= Color.Black,
////                        modifier = Modifier.weight(8f)
////                    )
//
//                }
//                Spacer(modifier = Modifier.height(15.dp))
//
//            }
//        }
//    }
}