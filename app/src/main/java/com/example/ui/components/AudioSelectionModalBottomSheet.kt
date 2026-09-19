package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Language
import com.example.media.BuiltInAudioCatalog
import com.example.media.BuiltInAudioItem
import com.example.media.SoundHelper

@Composable
fun AudioSelectionModalBottomSheet(
    title: String,
    currentSelectedIdOrUri: String?,
    currentDisplayName: String,
    isAzanCatalog: Boolean = false,
    isSalawatCatalog: Boolean = false,
    lang: Language,
    onDismiss: () -> Unit,
    onSelectBuiltIn: (BuiltInAudioItem) -> Unit,
    onChooseFromDevice: () -> Unit
) {
    val context = LocalContext.current
    val items = remember(isAzanCatalog, isSalawatCatalog) {
        when {
            isAzanCatalog -> BuiltInAudioCatalog.getAzanSounds()
            isSalawatCatalog -> BuiltInAudioCatalog.getSalawatSounds()
            else -> BuiltInAudioCatalog.getAlertSounds()
        }
    }

    var previewingId by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            SoundHelper.stopCurrentSound()
        }
    }

    AlertDialog(
        onDismissRequest = {
            SoundHelper.stopCurrentSound()
            onDismiss()
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MosqueGold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                // Button: Choose from phone / device storage
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C382E)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            SoundHelper.stopCurrentSound()
                            onChooseFromDevice()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.FileOpen,
                            contentDescription = null,
                            tint = MosqueGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (lang.isRtl) "📁 اختيار ملف صوتي من الهاتف..." else "📁 Choose Audio File from Device...",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (lang.isRtl) "تصفح ذاكرة الجهاز واختيار أي ملف صوتي" else "Browse device storage for any audio file",
                                color = Color(0xFFA7F3D0),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (lang.isRtl) "أو اختر نغمة مدمجة:" else "Or choose built-in tone:",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(items, key = { it.id }) { audioItem ->
                        val isSelected = currentSelectedIdOrUri == audioItem.id ||
                                (currentDisplayName == audioItem.getTitle(lang))
                        val isPlaying = previewingId == audioItem.id

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF104A3C) else Color(0xFF041E18)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    SoundHelper.stopCurrentSound()
                                    onSelectBuiltIn(audioItem)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Play / Stop preview button
                                IconButton(
                                    onClick = {
                                        if (isPlaying) {
                                            SoundHelper.stopCurrentSound()
                                            previewingId = null
                                        } else {
                                            previewingId = audioItem.id
                                            val defaultPath = if (isAzanCatalog) {
                                                SoundHelper.getDefaultAzanPath(context)
                                            } else {
                                                SoundHelper.getDefaultAlertPath(context)
                                            }
                                            SoundHelper.playSound(
                                                context = context,
                                                uriString = null,
                                                fallbackPath = defaultPath,
                                                isAlarm = isAzanCatalog,
                                                onCompletion = { previewingId = null }
                                            )
                                        }
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = "Preview",
                                        tint = if (isPlaying) Color(0xFFEF4444) else MosqueGold
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = audioItem.getTitle(lang),
                                        color = if (isSelected) MosqueGold else Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MosqueGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    SoundHelper.stopCurrentSound()
                    onDismiss()
                }
            ) {
                Text(if (lang.isRtl) "إغلاق" else "Close", color = MosqueGold)
            }
        }
    )
}
