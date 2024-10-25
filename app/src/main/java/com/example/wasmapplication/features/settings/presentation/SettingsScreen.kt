package com.example.wasmapplication.features.wasmSpeech.presentation

import android.R.attr.checked
import android.R.attr.color
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.wasmapplication.R
import com.example.wasmapplication.features.settings.presentation.SettingViewModel
import dagger.hilt.android.qualifiers.ApplicationContext


//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun optionButton(onClick: () -> Unit, label: String,icon:Icons){

}
@Composable
fun SettingsScreen(
    navController: NavController,
    @ApplicationContext  context: Context,
    settingsViewModel: SettingViewModel
) {
    val isChecked = remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,

            horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(
                    onClick = {  },
                    modifier = Modifier.border(color =  MaterialTheme.colorScheme.secondary,
                       width = 1.dp, shape = RoundedCornerShape(16.dp)
                    )
                ) {
                    Icon( Icons.Default.ModeNight,
//                        if (!settingsViewModel.isDarkTheme.value) Icons.Default.ModeNight
//                        else Icons.Default.LightMode,
                        contentDescription = "switch theme",
                        tint =  MaterialTheme.colorScheme.secondary,
                    )
                }

                Box(
                    contentAlignment = Alignment.Center) {
                    Text(
                        context.getString(R.string.dark_mode)
//                        if (settingsViewModel.isDarkTheme.value)
//                            context.getString(R.string.dark_mode)
//                        else stringResource(id = R.string.light_mode)
                    )
                }

                Switch(
                    checked = isChecked.value,
                    onCheckedChange = {
                        isChecked.value = it
                        settingsViewModel.toggleTheme()
                    },
                    thumbContent = if (isChecked.value) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    } else {
                        null
                    }
                )
            }



            Spacer(modifier = Modifier.height(5.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.3.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Column {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                   IconButton(
                     onClick ={} ,
                     modifier =   Modifier.border(color = MaterialTheme.colorScheme.secondary,
                           width = 1.dp, shape = RoundedCornerShape(16.dp))
                   ){
                        Icon(
                            Icons.Default.Language,
                            contentDescription = "Switch Language",
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                    }
//                Spacer(Modifier.width(20.dp))
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(context.getString(R.string.change_language))
                    }

                    IconButton(
                        onClick = {
                            isExpanded = !isExpanded
                        },

                    ) {
                        Icon(
                           if(isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Switch Language",
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                    }

                }
                Spacer(modifier = Modifier.height(5.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.3.dp)

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp)
                            .border(width = 0.3.dp, color = MaterialTheme.colorScheme.outline,shape = RoundedCornerShape(0.dp))
                    ) {
                        val languageNames = context.resources.getStringArray(R.array.language_names)
                        for (item in languageNames) {
                            val parts = item.split(":")
                            IconButton(
                                onClick = {
                                    isExpanded=!isExpanded
                                    settingsViewModel.saveLanguage(parts[1]) //if (settingsViewModel.language.value == "ar") "en" else "ar")
                                },
                                modifier = Modifier.fillMaxWidth().background(Color.Transparent)
                            ) {
                            Text(parts[0], modifier = Modifier.padding(8.dp))
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.3.dp)
                        }
                    }
                }
            }
        }


}










