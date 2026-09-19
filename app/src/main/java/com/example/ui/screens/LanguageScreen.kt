package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Language
import com.example.ui.components.MosqueEmeraldDark
import com.example.ui.components.MosqueEmeraldPanel
import com.example.ui.components.MosqueGold
import com.example.ui.language.AppStrings
import com.example.ui.viewmodel.PrayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(
    viewModel: PrayerViewModel,
    onBack: () -> Unit
) {
    val currentLang by viewModel.settingsRepo.language.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = AppStrings.tabLanguage(currentLang),
                        color = MosqueGold,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MosqueGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MosqueEmeraldDark)
            )
        },
        containerColor = Color(0xFF021612)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LanguageItemRow(
                title = "العربية",
                subtitle = "Arabic (RTL)",
                isSelected = currentLang == Language.ARABIC,
                onClick = { viewModel.settingsRepo.setLanguage(Language.ARABIC) }
            )

            LanguageItemRow(
                title = "English",
                subtitle = "الإنجليزية (LTR)",
                isSelected = currentLang == Language.ENGLISH,
                onClick = { viewModel.settingsRepo.setLanguage(Language.ENGLISH) }
            )

            LanguageItemRow(
                title = "Français",
                subtitle = "الفرنسية (LTR)",
                isSelected = currentLang == Language.FRENCH,
                onClick = { viewModel.settingsRepo.setLanguage(Language.FRENCH) }
            )
        }
    }
}

@Composable
private fun LanguageItemRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF0C3830) else MosqueEmeraldPanel
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    color = if (isSelected) MosqueGold else Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            }
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = MosqueGold)
            }
        }
    }
}
