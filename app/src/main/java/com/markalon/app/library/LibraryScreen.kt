package com.markalon.app.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.markalon.app.data.Note
import com.markalon.app.ui.components.MarkalonLogo
import com.markalon.app.ui.theme.LocalAccent
import com.markalon.app.ui.theme.LocalMarkalonColors
import com.markalon.app.ui.theme.UiFontFamily
import com.markalon.app.util.relativeTime
import com.markalon.app.util.snippet
import com.markalon.app.util.wordCount

@Composable
fun LibraryScreen(
    notes: List<Note>,
    totalCount: Int,
    search: String,
    onSearch: (String) -> Unit,
    onOpenNote: (String) -> Unit,
    onNewNote: () -> Unit,
    onImport: () -> Unit,
    onSettings: () -> Unit,
) {
    val colors = LocalMarkalonColors.current
    val accent = LocalAccent.current

    Box(Modifier.fillMaxSize().background(colors.bg)) {
        Column(Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MarkalonLogo(size = 32)
                Spacer(Modifier.size(10.dp))
                Text(
                    "Markalon",
                    color = colors.fg1,
                    fontFamily = UiFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    letterSpacing = (-0.2).sp,
                )
                Spacer(Modifier.weight(1f))
                HeaderIconButton(Icons.Filled.FileOpen, "Open file", onImport)
                HeaderIconButton(Icons.Filled.Settings, "Settings", onSettings)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 6.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                item {
                    Column {
                        Text(
                            "Documents",
                            color = colors.fg1,
                            fontFamily = UiFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            letterSpacing = (-0.6).sp,
                        )
                        Text(
                            "$totalCount note${if (totalCount == 1) "" else "s"}",
                            color = colors.fg3,
                            fontFamily = UiFontFamily,
                            fontSize = 13.5f.sp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp),
                        )
                        SearchField(search, onSearch)
                        Spacer(Modifier.size(11.dp))
                    }
                }

                if (notes.isEmpty()) {
                    item { EmptyState(searching = search.isNotBlank()) }
                } else {
                    items(notes, key = { it.id }) { note ->
                        DocCard(note, onClick = { onOpenNote(note.id) })
                    }
                }
            }
        }

        // FAB
        val fabInteraction = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 22.dp)
                .size(58.dp)
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(19.dp),
                    spotColor = accent,
                    ambientColor = accent,
                )
                .clip(RoundedCornerShape(19.dp))
                .background(accent)
                .clickable(interactionSource = fabInteraction, indication = null) { onNewNote() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "New note", tint = Color.White, modifier = Modifier.size(26.dp))
        }
    }
}

@Composable
private fun HeaderIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    val colors = LocalMarkalonColors.current
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(11.dp))
            .clickable(interactionSource = interaction, indication = null) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = colors.fg2, modifier = Modifier.size(21.dp))
    }
}

@Composable
private fun SearchField(value: String, onChange: (String) -> Unit) {
    val colors = LocalMarkalonColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(colors.surface2)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = colors.fg3, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(10.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text("Search notes", color = colors.fg3, fontFamily = UiFontFamily, fontSize = 15.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = TextStyle(color = colors.fg1, fontFamily = UiFontFamily, fontSize = 15.sp),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(LocalAccent.current),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun DocCard(note: Note, onClick: () -> Unit) {
    val colors = LocalMarkalonColors.current
    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (colors.isDark) 0.dp else 6.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0x14282114),
                ambientColor = Color(0x14282114),
            )
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .clickable(interactionSource = interaction, indication = null) { onClick() }
            .padding(horizontal = 16.dp, vertical = 15.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                note.title.ifBlank { "Untitled" },
                color = colors.fg1,
                fontFamily = UiFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.fg3,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            snippet(note.body),
            color = colors.fg2,
            fontFamily = UiFontFamily,
            fontSize = 13.5f.sp,
            lineHeight = 20.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 5.dp),
        )
        Text(
            "${relativeTime(note.updated)}  ·  ${wordCount(note.body)} words",
            color = colors.fg3,
            fontFamily = UiFontFamily,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun EmptyState(searching: Boolean) {
    val colors = LocalMarkalonColors.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Filled.Description,
            contentDescription = null,
            tint = colors.fg3,
            modifier = Modifier.size(42.dp),
        )
        Text(
            if (searching) "No matches" else "No notes yet",
            color = colors.fg1,
            fontFamily = UiFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            modifier = Modifier.padding(top = 14.dp),
        )
        Text(
            if (searching) "Try a different search." else "Tap + to start writing.",
            color = colors.fg3,
            fontFamily = UiFontFamily,
            fontSize = 13.5f.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
