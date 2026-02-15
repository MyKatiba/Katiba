package com.katiba.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A simple Markdown text renderer for Compose Multiplatform.
 * Supports: **bold**, *italic*, ### headings, * / - bullet lists, numbered lists, and `inline code`.
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    headingColor: Color = color
) {
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }

    Column(modifier = modifier) {
        blocks.forEachIndexed { index, block ->
            when (block) {
                is MarkdownBlock.Heading -> {
                    if (index > 0) Spacer(modifier = Modifier.height(8.dp))
                    val style = when (block.level) {
                        1 -> MaterialTheme.typography.titleLarge
                        2 -> MaterialTheme.typography.titleMedium
                        else -> MaterialTheme.typography.titleSmall
                    }
                    Text(
                        text = parseInlineMarkdown(block.text),
                        style = style,
                        fontWeight = FontWeight.Bold,
                        color = headingColor,
                        lineHeight = style.lineHeight
                    )
                }
                is MarkdownBlock.BulletItem -> {
                    Row(modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)) {
                        Text(
                            text = "•",
                            color = color,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = parseInlineMarkdown(block.text),
                            color = color,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                    }
                }
                is MarkdownBlock.NumberedItem -> {
                    Row(modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)) {
                        Text(
                            text = "${block.number}.",
                            color = color,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = parseInlineMarkdown(block.text),
                            color = color,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                    }
                }
                is MarkdownBlock.Paragraph -> {
                    if (index > 0 && blocks[index - 1] !is MarkdownBlock.BlankLine) {
                        // No extra spacing between adjacent paragraphs — the blank line block handles it
                    }
                    Text(
                        text = parseInlineMarkdown(block.text),
                        color = color,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
                is MarkdownBlock.BlankLine -> {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// ─── Block-level parsing ─────────────────────────────────────────────────────

private sealed class MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock()
    data class BulletItem(val text: String) : MarkdownBlock()
    data class NumberedItem(val number: Int, val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
    data object BlankLine : MarkdownBlock()
}

private fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = markdown.lines()

    var i = 0
    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()

        when {
            // Blank line
            trimmed.isEmpty() -> {
                // Only add one blank line between content blocks
                if (blocks.lastOrNull() !is MarkdownBlock.BlankLine &&
                    blocks.isNotEmpty()) {
                    blocks.add(MarkdownBlock.BlankLine)
                }
            }
            // Heading: ### text
            trimmed.startsWith("#") -> {
                val level = trimmed.takeWhile { it == '#' }.length.coerceAtMost(6)
                val text = trimmed.drop(level).trimStart()
                blocks.add(MarkdownBlock.Heading(level, text))
            }
            // Bullet: * text  or  - text
            trimmed.startsWith("* ") || trimmed.startsWith("- ") -> {
                blocks.add(MarkdownBlock.BulletItem(trimmed.drop(2).trim()))
            }
            // Numbered list: 1. text
            trimmed.matches(Regex("""^\d+\.\s+.*""")) -> {
                val dotIndex = trimmed.indexOf('.')
                val number = trimmed.substring(0, dotIndex).toIntOrNull() ?: 1
                val text = trimmed.substring(dotIndex + 1).trim()
                blocks.add(MarkdownBlock.NumberedItem(number, text))
            }
            // Regular paragraph
            else -> {
                // Accumulate consecutive non-blank, non-structural lines into one paragraph
                val paragraphLines = mutableListOf(trimmed)
                while (i + 1 < lines.size) {
                    val nextLine = lines[i + 1].trim()
                    if (nextLine.isEmpty() ||
                        nextLine.startsWith("#") ||
                        nextLine.startsWith("* ") ||
                        nextLine.startsWith("- ") ||
                        nextLine.matches(Regex("""^\d+\.\s+.*"""))
                    ) break
                    paragraphLines.add(nextLine)
                    i++
                }
                blocks.add(MarkdownBlock.Paragraph(paragraphLines.joinToString(" ")))
            }
        }
        i++
    }

    // Remove trailing blank line
    if (blocks.lastOrNull() is MarkdownBlock.BlankLine) {
        blocks.removeLast()
    }

    return blocks
}

// ─── Inline parsing (**bold**, *italic*, `code`) ─────────────────────────────

private fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                // Bold: **text**
                i + 1 < text.length && text[i] == '*' && text[i + 1] == '*' -> {
                    val endIndex = text.indexOf("**", i + 2)
                    if (endIndex != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(parseInlineMarkdown(text.substring(i + 2, endIndex)))
                        }
                        i = endIndex + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // Italic: *text* (single asterisk, not followed by another)
                text[i] == '*' && (i + 1 >= text.length || text[i + 1] != '*') -> {
                    val endIndex = text.indexOf('*', i + 1)
                    if (endIndex != -1 && endIndex > i + 1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 1, endIndex))
                        }
                        i = endIndex + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // Inline code: `text`
                text[i] == '`' -> {
                    val endIndex = text.indexOf('`', i + 1)
                    if (endIndex != -1) {
                        withStyle(SpanStyle(
                            fontWeight = FontWeight.Medium,
                            background = Color(0x1A000000),
                            letterSpacing = 0.sp
                        )) {
                            append(text.substring(i + 1, endIndex))
                        }
                        i = endIndex + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // Regular character
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}
