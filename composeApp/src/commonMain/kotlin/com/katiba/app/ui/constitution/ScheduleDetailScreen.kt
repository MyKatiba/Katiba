package com.katiba.app.ui.constitution

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.katiba.app.data.repository.ArticleReadManager
import com.katiba.app.data.repository.ConstitutionRepository
import com.katiba.app.ui.theme.KatibaColors
import kotlinx.serialization.json.*

/**
 * Screen displaying the content of a specific schedule
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDetailScreen(
    scheduleNumber: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val schedule = remember { ConstitutionRepository.getSchedule(scheduleNumber) }
    val scrollState = rememberScrollState()
    
    // Track schedule read
    LaunchedEffect(scheduleNumber) {
        ArticleReadManager.recordScheduleRead(scheduleNumber)
    }

    if (schedule == null) {
        // Schedule not found
        Box(modifier = Modifier.fillMaxSize()) {
            Text("Schedule not found", modifier = Modifier.padding(16.dp))
        }
        return
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column(
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "Schedule ${schedule.number}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = schedule.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    windowInsets = WindowInsets(0.dp)
                )
                HorizontalDivider(thickness = 2.dp, color = Color.Gray.copy(alpha = 0.3f))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Beadwork accent line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                KatibaColors.KenyaBlack,
                                KatibaColors.KenyaRed,
                                KatibaColors.KenyaGreen,
                                KatibaColors.KenyaWhite,
                                KatibaColors.KenyaGreen,
                                KatibaColors.KenyaRed,
                                KatibaColors.KenyaBlack
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Reference info if available
                if (schedule.reference.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = KatibaColors.KenyaRed.copy(alpha = 0.05f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Reference",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = KatibaColors.KenyaRed
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = schedule.reference,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Schedule content
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Content",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        HorizontalDivider()

                        // Render schedule content based on its structure
                        schedule.content?.let { content ->
                            RenderScheduleContent(content)
                        } ?: Text(
                            text = "No content available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun RenderScheduleContent(content: JsonElement) {
    when (content) {
        is JsonObject -> {
            // Check for specific keys and render accordingly
            when {
                content.containsKey("counties") -> RenderCounties(content["counties"] as? JsonArray)
                content.containsKey("symbols") -> RenderSymbols(content["symbols"] as? JsonObject)
                content.containsKey("oaths") -> RenderOaths(content["oaths"] as? JsonArray)
                content.containsKey("functions") -> RenderFunctions(content["functions"] as? JsonArray)
                content.containsKey("legislation") -> RenderLegislation(content["legislation"] as? JsonArray)
                content.containsKey("sections") -> RenderSections(content["sections"] as? JsonArray)
                else -> RenderGenericObject(content)
            }
        }
        is JsonArray -> {
            content.forEachIndexed { index, item ->
                Text(
                    text = "${index + 1}. ${item.toString().trim('"')}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
        is JsonPrimitive -> {
            Text(
                text = content.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun RenderCounties(counties: JsonArray?) {
    counties?.forEach { county ->
        if (county is JsonObject) {
            val number = county["number"]?.jsonPrimitive?.int ?: 0
            val name = county["name"]?.jsonPrimitive?.content ?: ""

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "$number.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = KatibaColors.KenyaGreen
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RenderSymbols(symbols: JsonObject?) {
    symbols?.forEach { (key, value) ->
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = key.replace("_", " ").capitalize(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = KatibaColors.KenyaRed
            )
            Text(
                text = value.jsonPrimitive.content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun RenderOaths(oaths: JsonArray?) {
    oaths?.forEachIndexed { index, oath ->
        if (oath is JsonObject) {
            val title = oath["title"]?.jsonPrimitive?.content ?: "Oath ${index + 1}"
            val text = oath["text"]?.jsonPrimitive?.content ?: ""

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderFunctions(functions: JsonArray?) {
    functions?.forEach { function ->
        if (function is JsonObject) {
            val number = function["number"]?.jsonPrimitive?.int ?: 0
            val description = function["description"]?.jsonPrimitive?.content ?: ""
            val level = function["level"]?.jsonPrimitive?.content

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "$number.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = KatibaColors.KenyaGreen
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        level?.let {
                            Text(
                                text = "Level: $it",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun RenderLegislation(legislation: JsonArray?) {
    legislation?.forEach { item ->
        if (item is JsonObject) {
            val number = item["number"]?.jsonPrimitive?.int ?: 0
            val title = item["title"]?.jsonPrimitive?.content ?: ""
            val content = item["content"]?.jsonPrimitive?.content ?: ""

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "$number.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = KatibaColors.KenyaRed
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (content.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderSections(sections: JsonArray?) {
    sections?.forEach { section ->
        if (section is JsonObject) {
            val number = section["number"]?.jsonPrimitive?.int ?: 0
            val title = section["title"]?.jsonPrimitive?.content ?: ""
            val content = section["content"]?.jsonPrimitive?.content ?: ""
            val part = section["part"]?.jsonPrimitive?.content

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(12.dp)
            ) {
                part?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = KatibaColors.KenyaRed,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "$number.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = KatibaColors.KenyaGreen
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (content.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = content,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun RenderGenericObject(obj: JsonObject) {
    obj.forEach { (key, value) ->
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Text(
                text = key.replace("_", " ").capitalize(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value.toString().trim('"'),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

private fun String.capitalize(): String {
    return this.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
